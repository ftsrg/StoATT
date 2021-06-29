package solver.solvers

import hu.bme.mit.delta.mdd.MddHandle
import mapTuples
import org.ejml.data.SingularMatrixException
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


object ConstrainedAMEnSolver {
    // Quick non-optimized prototype
    // based on the amen_solve2 function of the TT matlab toolbox
    fun solve(
            A: Array<Abstract2DCoreTensor>,
            y: TTVector,
            x0: TTVector = TTVector.ones(y.modes),
            residualThreshold: Double,
            maxSweeps: Int,
            enrichmentRank: Int,
            normalize: Boolean = false,
            verbose: Boolean = true,
            residDamp: Double = 1e-3,
            truncateBasedOnResidual: Boolean = true,
            useApproxResidualForStopping: Boolean = false,
            z0: TTVector? = null,
            reachableStateSpaceIndicator: TTVector? = null,
            constraintCores: TTVector,
            AForResidual: Array<Abstract2DCoreTensor> = A,
            statesForEnumeratedResidualComputation: MddHandle? = null
    ): TTSolution {
        val phiA = Array(A.size + 1) { listOf(listOf(ones(1))) }
        val phiy = Array(A.size + 1) { listOf(listOf(ones(1))) }
        val phizA = Array(A.size + 1) { listOf(listOf(ones(1))) }
        val phizy = Array(A.size + 1) { listOf(listOf(ones(1))) }
        val z = z0 ?: TTVector.rand(y.modes, enrichmentRank, 0.0, 1.0, Random(1))
        val noConstraint = TTVector.ones(x0.modes)

        var zAt: SimpleMatrix
        var x = x0.copy()
        val d = x.modes.size
        for (swp in 0 until maxSweeps) {
            // orthogonalization
            for (i in d - 1 downTo 1) {
                if (swp > 0) {
                    val xCore = x.tt.cores[i]
                    val xCoreVect = xCore.leftUnfolding()
                    xCoreVect.reshape(xCoreVect.numElements, 1)
                    val Kx = getKroneckerEquivalentMatrix(constraintCores.tt.cores[i], xCore.rows, xCore.cols)
                    zAt = projectMatVec(phizA[i], A[i], phizA[i + 1], Kx*xCoreVect)
                    val yCoreVect = y.tt.cores[i].leftUnfolding()
                    yCoreVect.reshape(yCoreVect.numElements, 1)
                    val zy = projectVector(phizy[i], phizy[i + 1], y.tt.cores[i])
                    val znew = zy - zAt
                    val rz1 = z.tt.cores[i].rows
                    val rz2 = if (i == d - 1) 1 else z.tt.cores[i + 1].rows
                    val znewReshaped = SimpleMatrix(rz1, z.modes[i] * rz2)
                    for (n in 0 until z.modes[i]) {
                        for (beta1 in 0 until rz1) {
                            for (beta2 in 0 until rz2) {
                                znewReshaped[beta1, n * rz2 + beta2] = znew[n * rz1 * rz2 + beta1 * rz2 + beta2]
                            }
                        }
                    }
                    val V = znewReshaped.svd(true).v.T()
                    val currZCore = z.tt.cores[i]
                    for (n in 0 until currZCore.modeLength) {
                        currZCore[n] = V[0..V.numRows(), 0..rz2]
                    }
                    currZCore.updateDimensions()
                } else {
                    z.tt.rightOrthogonalizeCore(i)
                }

                x.tt.rightOrthogonalizeCore(i)
                val cr = x.tt.cores[i]
                val constrCore = constraintCores.tt.cores[i]
                phiA[i] = computePhi(phiA[i + 1], cr, A[i], cr, constrCore, constrCore)
                phiy[i] = computePhi(phiy[i + 1], cr, y.tt.cores[i], constrCore, noConstraint.tt.cores[i])

                //TODO: check projection targets
                phizA[i] = computePhi(phizA[i + 1], z.tt.cores[i], A[i], x.tt.cores[i], constrCore, constrCore)
                phizy[i] = computePhi(phizy[i + 1], z.tt.cores[i], y.tt.cores[i], constrCore, noConstraint.tt.cores[i])
            }

            coreUpdate@ for (i in 0 until d) {
//                if(verbose) println("Updating core $i")
                val phi1 = phiA[i]
                val phi2 = phiA[i + 1]
                val A1 = A[i]
                val y1 = y.tt.cores[i]
                var rhs = projectVector(phiy[i], phiy[i + 1], y1)
                if (normalize) rhs = rhs.concatRows(ones(1))
                val normalizer = if (normalize) computeNormalizer(x, i, constraintCores, reachableStateSpaceIndicator) else null
                //TODO: use sparse core and sparse K
                val constrCore = constraintCores.tt.cores[i]
                val K = getKroneckerEquivalentMatrix(constrCore, x.tt.cores[i].rows, x.tt.cores[i].cols)
                applyALSStepConstrained(
                        A,
                        x,
                        y,
                        i,
                        phi1,
                        phi2,
                        constraintCores,
                        residualThreshold * residDamp,
                        normalizer = normalizer,
                        maxLocalIters = 200,
                        localRhs = rhs
                )

                //truncation
                val newCore = x.tt.cores[i]
                val fullSVD = newCore.leftUnfolding().svd(true)
                var newU = fullSVD.u
                var newS = fullSVD.w
                var newV = fullSVD.v
                if (i < d - 1) {
                    if (truncateBasedOnResidual) {
                        while (newU.numCols() > 1) {
                            val u = newU[0..SimpleMatrix.END, 0..newU.numCols() - 1]
                            val s = newS[0..newS.numRows() - 1, 0..newS.numCols() - 1]
                            val v = newV[0..SimpleMatrix.END, 0..newV.numCols() - 1]
                            val currSol = u * s * v.T()
                            currSol.reshape(currSol.numElements, 1)
                            var product = projectMatVec(phi1, A[i], phi2, K * currSol)
                            if (normalize)
                                product = product.concatRows(normalizer!! * (K * currSol))
                            val res = rhs - product
                            if (res.vecNorm2() > residualThreshold * residDamp) break
                            newU = u
                            newS = s
                            newV = v
                        }
                    } else {
                        val origSize = fullSVD.singularValues.size
                        var maxIdx = origSize - 1
                        var sigma2Sum = 0.0
                        val delta = residualThreshold * residDamp
                        val delta2 = delta * delta
                        for (j in origSize - 1 downTo 1) {
                            val sigma = fullSVD.singularValues[j]
                            val sigma2 = sigma * sigma
                            if (sigma2Sum + sigma2 < delta2) {
                                maxIdx--
                                sigma2Sum += sigma2
                            } else break
                        }
                        maxIdx = max(0, maxIdx)
                        newU = fullSVD.u[0..SimpleMatrix.END, 0..maxIdx + 1]
                        newS = fullSVD.w[0..maxIdx + 1, 0..maxIdx + 1]
                        newV = fullSVD.v[0..SimpleMatrix.END, 0..maxIdx + 1]
                    }
                }

                val modifier = newS * newV.T()

                val truncSol = newU * newS * newV.T()
                truncSol.reshape(truncSol.numElements, 1)

                // update approximate residual
                // l_z = (K'Z'ZK)^(-1)K'Z'(y-Au) = (K'Z'ZK)^(-1)(K'Z'y-K' Z' A U K l_u))
                val crzy = projectVector(phizy[i], phizy[i + 1], y1)
                val crzAt = projectMatVec(phizA[i], A1, phizA[i + 1], K * truncSol)
                val ZZ = computeKroneckeredXTX(z, z, constraintCores.tt.cores, i)
                val K_z = getKroneckerEquivalentMatrix(constrCore, z.tt.cores[i].rows, z.tt.cores[i].cols)
                val M = K_z.T() * ZZ * K_z
                val r = K_z.T() * (crzy - crzAt)
                val crznew = try {
                    M.solve(r)
                } catch(e: SingularMatrixException) {
                    M.pseudoInverse()*r
                }
                crznew.reshape(newCore.modeLength * z.ttRanks()[i], z.ttRanks()[i + 1])
                val svd = crznew.svd(true)
                val rank = min(enrichmentRank, svd.u.numCols())
                val kickU = svd.u.cols(0, rank)
                val kickSV = svd.w[0..rank, 0..rank] * svd.v.cols(0, rank).T()
                val zCurrCore = z.tt.cores[i]
                if (i < d - 1) {
                    // enrichment
                    val xVect = newU * modifier
                    xVect.reshape(xVect.numElements, 1)
                    val leftresid = projectMatVec(phiA[i], A1, phizA[i + 1], K*xVect)
                    val lefty = projectVector(phiy[i], phizy[i + 1], y1)
                    val K_xz = getKroneckerEquivalentMatrix(constrCore, x.tt.cores[i].rows, z.tt.cores[i].cols)
                    val K_xzT = K_xz.T()
                    val XX = computeKroneckeredXTX(x, z, constraintCores.tt.cores, i)
                    val M = K_xzT * XX * K_xz
                    val r = K_xzT * (lefty - leftresid)
                    val uk =
                            // TODO: param for projection to low rank
                            try {
                                M.solve(r)
                            }
                            catch(e: SingularMatrixException) { //TODO: better solution for possible singularity
                                M.pseudoInverse()*r
                            }
                    uk.reshape(newU.numRows(), uk.numElements / newU.numRows())

                    newU = newU.concatColumns(uk)
                    val qr = newU.qr()
                    newU = qr.Q
                    val newModifier = qr.R * modifier.concatRows(SimpleMatrix(uk.numCols(), modifier.numRows()))
                    val nextMatData = x.tt.cores[i + 1].data
                    for ((j, mat) in nextMatData.withIndex()) {
                        nextMatData[j] = newModifier * mat
                    }
                    x.tt.cores[i + 1].updateDimensions()

                    repeat(newCore.modeLength) {
                        newCore.data[it] = newU[it * newCore.rows..(it + 1) * newCore.rows, 0..newU.numCols()]
                    }
                    newCore.updateDimensions()

                    phiA[i + 1] = computePsi(phiA[i], newCore, A[i], newCore, constrCore, constrCore)
                    phiy[i + 1] = computePsi(phiy[i], newCore, y.tt.cores[i], constrCore, noConstraint.tt.cores[i])

                    // update z and its projections
                    for (j in 0 until zCurrCore.modeLength) {
                        zCurrCore[j] = kickU.rows(j * z.ttRanks()[i], (j + 1) * z.ttRanks()[i])
                    }
                    zCurrCore.updateDimensions()
                    //TODO: this may be redundant, as the next core will be recomputed in the next step
                    val zNextCore = z.tt.cores[i + 1]
                    for (j in 0 until zNextCore.modeLength) {
                        zNextCore[j] = kickSV * zNextCore[j]
                    }
                    zNextCore.updateDimensions()

                    phizA[i + 1] = computePsi(phizA[i], zCurrCore, A[i], newCore, constrCore, constrCore)
                    phizy[i + 1] = computePsi(phizy[i], zCurrCore, y.tt.cores[i], constrCore, noConstraint.tt.cores[i])
                } else {
                    for (j in 0 until zCurrCore.modeLength) {
                        zCurrCore[j] = crznew.rows(j * z.ttRanks()[i], (j + 1) * z.ttRanks()[i])
                    }
                    zCurrCore.updateDimensions()

                    // no need to change solution core here;
                    // already updated by applyALSStep, and no enrichment is needed for the last core
                }
            }

            if (useApproxResidualForStopping) {
                val residNorm = z.norm()
                if (verbose) println("AMEn-ALS sweep ${swp}: resnorm~=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
                if (residNorm < residualThreshold) return TTSolution(x, residNorm)
            } else {
                val residNorm =
                        if (statesForEnumeratedResidualComputation == null) {
                            computeResidualNorm(AForResidual, x, y)
                        } else {
                            var res = 0.0
                            statesForEnumeratedResidualComputation.mapTuples { tuple: List<Int> ->
                                var termX = ones(1)
                                for ((k, ik) in tuple.withIndex().reversed()) {
                                    val crA = AForResidual[k]
                                    val constrCore = constraintCores.tt.cores[k]
                                    val crX = x.tt.cores[k]
                                    termX.reshape(crA.cols, constrCore.cols*crX.cols)
                                    var nextTerm = SimpleMatrix(crA.rows, constrCore.rows*crX.rows)
                                    val V = termX * constrCore[ik].kron(crX[ik]).T()
                                    for(jk in 0 until crA.modeLength) {
                                        nextTerm += crA.multFromRight(ik, jk, V)
                                    }
                                    termX = nextTerm
                                }
                                val termY = y.tt.get(*(tuple.toIntArray()))
                                res += (termX[0,0]-termY) * (termX[0,0]-termY)
                            }
                            sqrt(res)
                        }

                if (verbose) println("AMEn-ALS sweep ${swp}: resnorm=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
                if (verbose) println("AMEn-ALS sweep ${swp}: resnorm~=${constraintCores.hadamard(z).norm()} threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
                if (residNorm < residualThreshold) return TTSolution(x, residNorm)
            }
        }

        if (useApproxResidualForStopping) {
            val residNorm = z.norm()
            if (verbose) println("AMEn-ALS exit: resnorm~=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
            return TTSolution(x, residNorm)
        } else {
            val residNorm = computeResidualNorm(AForResidual, x, y)
            if (verbose) println("AMEn-ALS exit: resnorm=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
            if (verbose) println("AMEn-ALS exit: resnorm~=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
            return TTSolution(x, residNorm)
        }
    }

    private fun computeKroneckeredXTX(
            lowRankRepLeft: TTVector,
            lowRankRepRight: TTVector,
            kroneckerCores: List<CoreTensor>,
            k: Int
    ): SimpleMatrix {
        var left = ones(1, 1)
        for (c in 0 until k) {
            val lrCore = lowRankRepLeft.tt.cores[c]
            val kCore = kroneckerCores[c]
            var term = SimpleMatrix(
                    lrCore.rows*kCore.rows*lrCore.rows*kCore.rows,
                    lrCore.cols*kCore.cols*lrCore.cols*kCore.cols
            )
            for (i in lrCore.data.indices) {
                val core = kCore[i].kron(lrCore[i])
                term += core.kron(core)
            }
            left *= term
        }

        var right = ones(1, 1)
        for (c in (kroneckerCores.size-1) downTo (k + 1)) {
            val lrCore = lowRankRepRight.tt.cores[c]
            val kCore = kroneckerCores[c]
            var term = SimpleMatrix(
                    lrCore.rows*kCore.rows*lrCore.rows*kCore.rows,
                    lrCore.cols*kCore.cols*lrCore.cols*kCore.cols
            )
            for (i in lrCore.data.indices) {
                val core = kCore[i].kron(lrCore[i])
                term += core.kron(core)
            }
            right = term * right
        }

        val dim1 = lowRankRepLeft.tt.cores[k].rows * kroneckerCores[k].rows
        left.reshape(dim1, dim1) 
        val dim2 = lowRankRepRight.tt.cores[k].cols * kroneckerCores[k].cols
        right.reshape(dim2, dim2)
        return eye(lowRankRepLeft.modes[k]).kron(left.kron(right))
    }

    private fun getKroneckerEquivalentMatrix(core: CoreTensor, otherRows: Int, otherCols: Int): SimpleMatrix {
        val resVectLength = core.rows * otherRows * core.cols * otherCols
        val inpVectLength = otherRows * otherCols
        val K = SimpleMatrix(core.modeLength * resVectLength, core.modeLength * inpVectLength)
        for (i in core.data.indices) {
            K[i * resVectLength, i*inpVectLength] = getKroneckerEquivalentMatrix(core[i], otherRows, otherCols)
        }
        return K
    }

    private fun computePsi(
            PsiPrev: TPhi,
            xCore: CoreTensor,
            AbstractACore: Abstract2DCoreTensor,
            yCore: CoreTensor,
            xKroneckerConstraintCore: CoreTensor,
            yKroneckerConstraintCore: CoreTensor
    ): TPhi {
        val res = arrayListOf<ArrayList<SimpleMatrix>>()
        for (beta_1 in 0 until xKroneckerConstraintCore.cols)
            for (beta_2 in 0 until xCore.cols) {
                res.add(arrayListOf<SimpleMatrix>())
                for (gamma_1 in 0 until yKroneckerConstraintCore.cols) {
                    for (gamma_2 in 0 until yCore.cols) {
                        var M = SimpleMatrix(1, AbstractACore.cols)
                        for (i in 0 until xCore.modeLength) {
                            for (beta_prev_1 in 0 until xKroneckerConstraintCore.rows) {
                                if (xKroneckerConstraintCore[i][beta_prev_1, beta_1] == 0.0) continue
                                for (beta_prev_2 in 0 until xCore.rows) {
                                    val beta_prev = beta_prev_1 * xCore.rows + beta_prev_2
//                                    val beta = beta_1 * xCore.cols + beta_2
                                    for (j in 0 until yCore.modeLength) {
                                        for (gamma_prev_1 in 0 until yKroneckerConstraintCore.rows) {
                                            for (gamma_prev_2 in 0 until yCore.rows) {
                                                val gamma_prev = gamma_prev_1 * yCore.rows + gamma_prev_2
                                                M += AbstractACore.multFromLeft(i, j, PsiPrev[beta_prev][gamma_prev]) *
                                                     (xKroneckerConstraintCore[i][beta_prev_1, beta_1] * xCore[i][beta_prev_2, beta_2] *
                                                      yKroneckerConstraintCore[i][gamma_prev_1, gamma_1] * yCore[j][gamma_prev_2, gamma_2])
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        res.last().add(M)
                    }
                }
            }
        return res
    }

    private fun computePhi(
            PhiPrev: TPhi,
            xCore: CoreTensor,
            AbstractACore: Abstract2DCoreTensor,
            yCore: CoreTensor,
            xKroneckerConstraintCore: CoreTensor,
            yKroneckerConstraintCore: CoreTensor
    ): TPhi {
        val res = arrayListOf<ArrayList<SimpleMatrix>>()
        for (beta_1 in 0 until xKroneckerConstraintCore.rows) {
            for (beta_2 in 0 until xCore.rows) {
                res.add(arrayListOf())
                for (gamma_1 in 0 until yKroneckerConstraintCore.rows) {
                    for (gamma_2 in 0 until yCore.rows) {
                        var M = SimpleMatrix(AbstractACore.rows, 1)
                        for (i in 0 until xCore.modeLength) {
                            for (beta_prev_1 in 0 until xKroneckerConstraintCore.cols) {
                                if (xKroneckerConstraintCore[i][beta_1, beta_prev_1] == 0.0) continue
                                for (beta_prev_2 in 0 until xCore.cols) {
                                    val beta_prev = beta_prev_1 * xCore.cols + beta_prev_2
                                    for (gamma_prev_1 in 0 until yKroneckerConstraintCore.cols) {
                                        for (gamma_prev_2 in 0 until yCore.cols) {
                                            for (j in 0 until yCore.modeLength) {
                                                val gamma_prev = gamma_prev_1 * yCore.cols + gamma_prev_2
                                                M += AbstractACore.multFromRight(i, j, PhiPrev[beta_prev][gamma_prev]) *
                                                     (xKroneckerConstraintCore[i][beta_1, beta_prev_1] * xCore[i][beta_2, beta_prev_2] *
                                                      yKroneckerConstraintCore[j][gamma_1, gamma_prev_1] * yCore[j][gamma_2, gamma_prev_2])
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        res.last().add(M)
                    }
                }
            }
        }
        return res
    }

    private fun computePsi(
            PsiPrev: TPhi,
            xCore: CoreTensor,
            yCore: CoreTensor,
            xKroneckerConstraintCore: CoreTensor,
            yKroneckerConstraintCore: CoreTensor
    ): TPhi {
        val res = arrayListOf<ArrayList<SimpleMatrix>>()
        for (beta_1 in 0 until xKroneckerConstraintCore.cols) {
            for (beta_2 in 0 until xCore.cols) {
                res.add(arrayListOf())
                for (gamma_1 in 0 until yKroneckerConstraintCore.cols) {
                    for (gamma_2 in 0 until yCore.cols) {
                        var M = SimpleMatrix(1, 1)
                        for (i in 0 until xCore.modeLength) {
                            for (beta_prev_1 in 0 until xKroneckerConstraintCore.rows) {
                                if (xKroneckerConstraintCore[i][beta_prev_1, beta_1] == 0.0) continue
                                for (beta_prev_2 in 0 until xCore.rows) {
                                    val beta_prev = beta_prev_1 * xCore.rows + beta_prev_2
                                    for (gamma_prev_1 in 0 until yKroneckerConstraintCore.rows) {
                                        for (gamma_prev_2 in 0 until yCore.rows) {
                                            val gamma_prev = gamma_prev_1 * yCore.rows + gamma_prev_2
                                            M += PsiPrev[beta_prev][gamma_prev] *
                                                 (xKroneckerConstraintCore[i][beta_prev_1, beta_1] * xCore[i][beta_prev_2, beta_2] *
                                                  yKroneckerConstraintCore[i][gamma_prev_1, gamma_1] * yCore[i][gamma_prev_2, gamma_2])
                                        }
                                    }
                                }
                            }
                        }
                        res.last().add(M)
                    }
                }
            }
        }
        return res
    }

    private fun computePhi(
            PhiPrev: TPhi,
            xCore: CoreTensor,
            yCore: CoreTensor,
            xKroneckerConstraintCore: CoreTensor,
            yKroneckerConstraintCore: CoreTensor
    ): TPhi {
        val res = arrayListOf<ArrayList<SimpleMatrix>>()
        for (beta_1 in 0 until xKroneckerConstraintCore.rows) {
            for (beta_2 in 0 until xCore.rows) {
                res.add(arrayListOf())
                for (gamma_1 in 0 until yKroneckerConstraintCore.rows) {
                    for (gamma_2 in 0 until yCore.rows) {
                        var M = SimpleMatrix(1, 1)
                        for (beta_prev_1 in 0 until xKroneckerConstraintCore.cols) {
                            for (beta_prev_2 in 0 until xCore.cols) {
                                for (i in 0 until xCore.modeLength) {
                                    if(xKroneckerConstraintCore[i][beta_1, beta_prev_1] == 0.0) continue
                                    val beta_prev = beta_prev_1 * xCore.cols + beta_prev_2
                                    for (gamma_prev_1 in 0 until yKroneckerConstraintCore.cols) {
                                        for (gamma_prev_2 in 0 until yCore.cols) {
                                            val gamma_prev = gamma_prev_1 * yCore.cols + gamma_prev_2
                                            M += PhiPrev[beta_prev][gamma_prev] *
                                                 (xKroneckerConstraintCore[i][beta_1, beta_prev_1] * xCore[i][beta_2, beta_prev_2] *
                                                  yKroneckerConstraintCore[i][gamma_1, gamma_prev_1] * yCore[i][gamma_2, gamma_prev_2])
                                        }
                                    }
                                }
                            }
                        }
                        res.last().add(M)
                    }
                }
            }
        }
        return res
    }

    private fun projectVector(psi: TPhi, phi: TPhi, y: CoreTensor): SimpleMatrix {
        val res = SimpleMatrix(psi.size * phi.size * y.modeLength, 1)
//    val res = SimpleMatrix(psi[0].size * phi[0].size * y.modeLength, 1)
        for (n in 0 until y.modeLength) {
            val currMtxY = y[n]
            for (beta1 in 0 until psi.size) {
                for (beta2 in 0 until phi.size) {
                    var sum = 0.0
                    for (gamma1 in 0 until psi[0].size) {
                        for (gamma2 in 0 until phi[0].size) {
                            sum += (psi[beta1][gamma1] * currMtxY[gamma1, gamma2] * phi[beta2][gamma2])[0] // indexing used to convert 1x1 mtx to double
                        }
                    }
                    res[n * psi.size * phi.size + beta1 * phi.size + beta2] = sum
                }
            }
        }
        return res
    }

    //Phi1[beta][gamma] row vector, Phi2[beta][gamma] col vector
    private fun projectMatVec(
            psi: TPhi,
            ACore: Abstract2DCoreTensor,
            phi: TPhi,
            y: SimpleMatrix,
            normalizerVector: SimpleMatrix? = null,
            preconditioner: ((SimpleMatrix) -> SimpleMatrix)? = null
    ): SimpleMatrix {
        val r_kx = phi.size
        val r_ky = phi[0].size
        val r_kminusx = psi.size
        val r_kminusy = psi[0].size
        val phiMat = SimpleMatrix(ACore.cols * phi.size, phi[0].size)
        val R_k = ACore.cols
        for ((beta, phi_beta) in phi.withIndex()) {
            for ((gamma, phi_beta_gamma) in phi_beta.withIndex()) {
                phiMat[beta * R_k, gamma] = phi_beta_gamma
            }
        }
        val R_kminus = ACore.rows
        val psiMat = SimpleMatrix(r_kminusx, r_kminusy * R_kminus)
        for ((beta_minus, psi_beta_minus) in psi.withIndex()) {
            for ((gamma_minus, psiCurr) in psi_beta_minus.withIndex()) {
                psiMat[beta_minus, gamma_minus * R_kminus] = psiCurr
            }
        }

        val lambda = y[y.numElements - 1] //used only if normalization is applied
        val y = if (normalizerVector != null) y[0..y.numElements - 1, 0..1] else y
        val n_k = ACore.modeLength
        var res = SimpleMatrix(n_k * r_kminusx * r_kx, 1)// y.createLike()

        //Computation of Y'
        val YMat = SimpleMatrix(r_ky, r_kminusy * n_k)
        for (i in 0 until n_k) {
            for (gamma_minus in 0 until r_kminusy) {
                YMat[0, i * r_kminusy + gamma_minus] = y[i * r_kminusy * r_ky + gamma_minus * r_ky..i * r_kminusy * r_ky + (gamma_minus + 1) * r_ky, 0..1]
            }
        }
        val YPrime = phiMat * YMat

        //Computation of Y''
        val YPrimeReshaped = SimpleMatrix(n_k * R_k, r_kx * r_kminusy)
        for (beta in 0 until r_kx) {
            for (gamma_minus in 0 until r_kminusy) {
                for (jk in 0 until n_k) {
                    for (idx in 0 until R_k) {
                        YPrimeReshaped[jk * R_k + idx, beta * r_kminusy + gamma_minus] = YPrime[beta * R_k + idx, jk * r_kminusy + gamma_minus]
                    }
                }
            }
        }
        val YDoublePrime = SimpleMatrix(n_k * R_kminus, YPrimeReshaped.numCols())//AkUnfolding * YPrimeReshaped
        for (ik in 0 until n_k) {
            var YDoublePrimeI = SimpleMatrix(ACore.rows, YDoublePrime.numCols())
            for (jk in 0 until n_k) {
                // TODO: switch for loops
                val Yj = YPrimeReshaped.rows(jk * ACore.cols, (jk + 1) * ACore.cols)
                YDoublePrimeI += ACore.multFromRight(ik, jk, Yj)
            }
            YDoublePrime[ik * R_kminus, 0] = YDoublePrimeI
        }


        //Computation of the result
        val YDoublePrimeReshaped = SimpleMatrix(r_kminusy * R_kminus, n_k * r_kx)
        for (gamma_minus in 0 until r_kminusy) {
            for (idx in 0 until R_kminus) {
                for (ik in 0 until n_k) {
                    for (beta in 0 until r_kx) {
                        YDoublePrimeReshaped[gamma_minus * R_kminus + idx, ik * r_kx + beta] =
                                YDoublePrime[ik * R_kminus + idx, beta * r_kminusy + gamma_minus]
                    }
                }
            }
        }
        val resTemp = psiMat * YDoublePrimeReshaped
        //res is indexed like F, hint: "F[i*currCore.rows*currCore.cols+alphaMinus*currCore.cols+alpha] = elem[0]"
        for (i in 0 until n_k) {
            for (beta_minus in 0 until r_kminusx) {
                for (beta in 0 until r_kx) {
                    res[i * r_kminusx * r_kx + beta_minus * r_kx + beta] = resTemp[beta_minus, i * r_kx + beta]
                }
            }
        }

        if (normalizerVector != null) {
            res += lambda * normalizerVector.T()
            res = res.concatRows(normalizerVector * y)
        }

        return preconditioner?.invoke(res) ?: res
    }

    /**
     * Computes (P(X_1)^T*A*P(X_2))^T*y, where P(X_1)^T*A*P(X_2) is efficiently computed using phi and psi,
     * as described in the ALS/DMRG paper
     */
    private fun projectMatVecTranspose(
            psi: TPhi,
            ACore: Abstract2DCoreTensor,
            phi: TPhi,
            y: SimpleMatrix,
            normalizerVector: SimpleMatrix? = null,
            preconditioner: ((SimpleMatrix) -> SimpleMatrix)? = null
    ): SimpleMatrix {
        val lambda = y[y.numElements-1]
        val y = if(normalizerVector == null) y else y[0..y.numElements-1, 0..1]
        //OPTIMIZE: matrix operations instead of elementwise computation (see projectMatVec)

        if (preconditioner != null)
            throw NotImplementedError("Using a preconditioner in projectMatVecTranspose is not implemented yet")
        val res = y.createLike()
        for (betaMinus in psi.indices) {
            for (gammaMinus in psi[betaMinus].indices) {
                for (beta in phi.indices) {
                    for (gamma in phi[beta].indices) {
                        for (i in 0 until ACore.modeLength) {
                            for (j in 0 until ACore.modeLength) {
                                val term =
                                        y[j * psi.size * phi.size + betaMinus * phi.size + beta, 0] *
                                        psi[betaMinus][gammaMinus] *
                                        ACore.multFromRight(i, j, phi[beta][gamma])
                                res[j * psi[betaMinus].size * phi[beta].size + gammaMinus * phi[beta].size + gamma, 0] += term[0]
                            }
                        }
                    }
                }
            }
        }
        if (normalizerVector == null) return res
        else return res.concatColumns(normalizerVector.T() * y)
    }

    private fun applyALSStepConstrained(
            A: Array<Abstract2DCoreTensor>,
            x: TTVector,
            f: TTVector,
            k: Int,
            psi: TPhi,
            phi: TPhi,
            kroneckerConstraintVector: TTVector,
            residualThreshold: Double,
            maxLocalIters: Int = 200,
            normalizer: SimpleMatrix? = null,
            localRhs: SimpleMatrix
    ) {
        val kroneckerConstraintCores = kroneckerConstraintVector.tt.cores
        val constrCore = kroneckerConstraintCores[k]
        val K = getKroneckerEquivalentMatrix(constrCore, x.tt.cores[k].rows, x.tt.cores[k].cols)
        val KT = K.T()
        val currCore = x.tt.cores[k]

        //Local solution
        val F = KT * projectMatVecTranspose(psi, A[k], phi, localRhs)

//    val solveDirectly =true // currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
        val solveDirectly = currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
        val ACore = A[k]
        lateinit var w: SimpleMatrix
        if (solveDirectly) {
            val dim = currCore.modeLength * constrCore.rows * currCore.rows * constrCore.cols * currCore.cols
            var FullB = SimpleMatrix(dim, dim)
            //OPTIMIZE: Parallel computation?
            val kronRows = currCore.rows * constrCore.rows
            val kronCols = currCore.cols * constrCore.cols
            for (betaMinus2 in 0 until kronRows) {
                for (beta2 in 0 until kronCols) {
                    for (gammaMinus2 in 0 until kronRows) {
                        for (gamma2 in 0 until kronCols) {
                            for (i in 0 until currCore.modeLength) {
                                for (j in 0 until currCore.modeLength) {
                                    FullB[
                                            i * kronRows * kronCols + betaMinus2 * kronCols + beta2,
                                            j * kronRows * kronCols + gammaMinus2 * kronCols + gamma2] =
                                            ACore.multFromLeft(i, j, psi[betaMinus2][gammaMinus2]) * phi[beta2][gamma2]
                                }
                            }
                        }
                    }
                }
            }
            FullB = KT * FullB.T() * FullB * K

            //solve Bw=F
            w = if (normalizer != null) {
                val normalizerK = normalizer * K
                val FullBExtended = FullB.concatRows(normalizerK).concatColumns(normalizerK.T().concatRows(SimpleMatrix(1, 1)))
                val FExtended = F.concatRows(mat[r[1.0]])
                val res = try {
                    FullBExtended.solve(FExtended)
                } catch (e: SingularMatrixException) {
                    FullBExtended.pseudoInverse() * FExtended
                }
                res[0..res.numElements - 1, 0..1]
            } else {
                try {
                    FullB.solve(F)
                } catch (e: SingularMatrixException) {
                    FullB.pseudoInverse() * F
                }
            }
        } else {
            var w0 = ones(F.numRows(), 1)
//        var w0 = currCore.leftUnfolding()
//        w0.reshape(w0.numElements, 1)
//        for (i in 0 until currCore.modeLength) {
//            val M = currCore[i]
//            for (row in 0 until M.numRows()) {
//                for (col in 0 until M.numCols()) {
//                    w0[i * M.numRows() * M.numCols() + row * M.numCols() + col] = M[row, col]
//                }
//            }
//        }

//        val linearMap: (SimpleMatrix) -> SimpleMatrix = { projectMatVec(psi, A[k], phi, it, normalizer, null) }
//        val preconditioner = createJacobiPreconditioner(
//                linearMap, if(normalizer==null) w0.numElements else w0.numElements+1
//        )
            val preconditioner = null

            if (normalizer != null) {
                val normalizerK = normalizer*K
                w0 /= (normalizerK * w0)[0]
                w = ALSLocalIterSolve(
                        psi, phi, A, w0, F, k,
                        residualThreshold * 0.001,
                        maxLocalIters = maxLocalIters,
                        normalizerVector = normalizer,
                        preconditioner = preconditioner,
                        kroneckerConstraintMatrix = K
                )
            } else {
                w = ALSLocalIterSolve(psi, phi, A, w0, F, k, residualThreshold * 0.001,
                        maxLocalIters = maxLocalIters,
                        preconditioner = preconditioner,
                        kroneckerConstraintMatrix = K
                )
            }
//            val res = projectMatVec(psi, A[k], phi, w, null) - F
        }
        for (i in 0 until currCore.modeLength) {
            for (beta_minus in 0 until currCore.rows) {
                for (beta in 0 until currCore.cols) {
                    currCore[i][beta_minus, beta] = w[i * currCore.rows * currCore.cols + beta_minus * currCore.cols + beta]
                }
            }
        }
    }

    private fun computeNormalizer(x: TTVector, k: Int, constraintVector: TTVector, normalizationSetIndicator: TTVector? = null): SimpleMatrix {
        var normalizerLeft = ones(1)
        // TODO: cache
        repeat(k) {
            val coreTensor = x.tt.cores[it]
            val normCore = normalizationSetIndicator?.tt?.cores?.get(it)
            val constrCore = constraintVector.tt.cores[it]
            var sum =
                    if (normCore == null) SimpleMatrix(constrCore.rows * coreTensor.rows, constrCore.cols * coreTensor.cols)
                    else SimpleMatrix(constrCore.rows * coreTensor.rows * normCore.rows, constrCore.rows * coreTensor.cols * normCore.cols)
            for ((idx, M) in coreTensor.data.withIndex()) {
                val KM = constrCore[idx].kron(M)
                sum += (normCore?.get(idx)?.kron(KM) ?: KM)
            }
            normalizerLeft = normalizerLeft * sum
        }
        var normalizerRight = ones(1)
        for (i in x.tt.cores.size - 1 downTo k + 1) {
            val coreTensor = x.tt.cores[i]
            val normCore = normalizationSetIndicator?.tt?.cores?.get(i)
            val constrCore = constraintVector.tt.cores[i]
            var sum =
                    if (normCore == null) SimpleMatrix(constrCore.rows * coreTensor.rows, constrCore.cols * coreTensor.cols)
                    else SimpleMatrix(constrCore.rows * coreTensor.rows * normCore.rows, constrCore.cols * coreTensor.cols * normCore.cols)
            for ((idx, M) in coreTensor.data.withIndex()) {
                val KM = constrCore[idx].kron(M)
                sum += (normCore?.get(idx)?.kron(KM) ?: KM)
            }
            normalizerRight = sum * normalizerRight
        }
        val normalizer = ones(x.modes[k]).T().kron(normalizerLeft).kron(normalizerRight.T())
        if (normalizationSetIndicator == null)
            return normalizer
        else {
            val A = normalizationSetIndicator.tt.cores[k]
            val B = x.tt.cores[k]
            val BConstr = constraintVector.tt.cores[k]
            // TODO: sparse?
            val numelementsA = A.rows * A.cols
            val numelementsB = B.rows * B.cols * BConstr.rows * BConstr.cols
            val modifier = SimpleMatrix(
                    numelementsA * B.rows * BConstr.rows * B.cols * BConstr.cols * A.modeLength,
                    numelementsB * B.modeLength
            )
            for (i in 0 until A.modeLength) {
                for (j in 0 until A[i].numRows()) {
                    val r = A[i].row(j).T()
                    modifier[i * numelementsB * numelementsA + j * A.cols * numelementsB, i * numelementsB] =
                            eye(B.rows * BConstr.rows).kron(r.kron(eye(B.cols * BConstr.cols)))
                }
            }
            return normalizer * modifier
        }
    }

    private fun ALSLocalIterSolve(
            psi: TPhi,
            phi: TPhi,
            A: Array<Abstract2DCoreTensor>,
            w0: SimpleMatrix,
            F: SimpleMatrix,
            k: Int,
            threshold: Double,
            preconditioner: ((SimpleMatrix) -> SimpleMatrix)? = null,
            maxLocalIters: Int = 200,
            normalizerVector: SimpleMatrix? = null,
            kroneckerConstraintMatrix: SimpleMatrix
    ): SimpleMatrix {
        val r_k = phi.size
        val r_kminus = psi.size
        val K =
                if(normalizerVector != null) SimpleMatrix(kroneckerConstraintMatrix.numRows()+1, kroneckerConstraintMatrix.numCols()+1)
                else kroneckerConstraintMatrix
        if(normalizerVector != null) {
            K[0, 0] = kroneckerConstraintMatrix
            K[K.numRows() - 1, K.numCols() - 1] = 1.0
        }
        val KT = K.transpose()

        val linearMap: (SimpleMatrix) -> SimpleMatrix = {
            val AKx = projectMatVec(psi, A[k], phi, K*it, normalizerVector, preconditioner)
            //TODO: fix normalization in projectMatVecTranspose, see projectMatVec
            val ATAKx = projectMatVecTranspose(psi, A[k], phi, AKx, normalizerVector)
            KT*ATAKx

        }
//    return BiCGStabL(2, ::computeMatVec, preconditioner?.mult(F) ?: F, maxLocalIters, w0, threshold).solution
        val f = if (normalizerVector != null) F.concatRows(mat[r[1.0]]) else F
        var result =
                if (normalizerVector != null) {
                    val w0extended = w0.concatRows(SimpleMatrix(1, 1))
                    biCGStab(linearMap, preconditioner?.invoke(f)
                                        ?: f, maxLocalIters, w0extended, threshold)
                } else {
                    biCGStab(linearMap, preconditioner?.invoke(f) ?: f, maxLocalIters, w0, threshold)
                }
//    if((linearMap(result) - f).vecNorm2() > threshold) {
//      var result = if (normalizerVector != null) {
//            val w0extended = w0.concatRows(SimpleMatrix(1, 1))
//            ReGMRES(linearMap, preconditioner?.invoke(f)
//                               ?: f, 5, w0extended, threshold)
//        } else {
//            ReGMRES(linearMap, preconditioner?.invoke(f) ?: f, 5, w0, threshold)
//        }
//    }
        if (normalizerVector != null) {
            return result[0..result.numElements - 1, 0..1]
        }
        return result
//    return ReGMRES(::computeMatVec, F, 10, w0, threshold)
    }

}