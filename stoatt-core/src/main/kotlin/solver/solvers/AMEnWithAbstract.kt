/*
 *
 *   Copyright 2021 Budapest University of Technology and Economics
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package solver.solvers

import org.ejml.data.SingularMatrixException
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

//private typealias TPhi = List<List<SimpleMatrix>>

// Quick non-optimized prototype
// based on the amen_solve2 function of the TT matlab toolbox
fun AMEnALSSolve(
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
    normalizationFactor: Double = 1.0
): TTSolution {
    val rightSideNorm = y.norm()

    val phiA = Array(A.size + 1) { listOf(listOf(ones(1))) }
    val phiy = Array(A.size + 1) { listOf(listOf(ones(1))) }
    val phizA = Array(A.size + 1) { listOf(listOf(ones(1))) }
    val phizy = Array(A.size + 1) { listOf(listOf(ones(1))) }
    val z = z0 ?: TTVector.rand(y.modes, enrichmentRank, 0.0, 1.0)

    var zAt = SimpleMatrix(0, 0)
    var x = x0
    val d = x.modes.size
    for (swp in 0 until maxSweeps) {
        // orthogonalization
        for (i in d - 1 downTo 1) {
            if (swp > 0) {
                val xCoreVect = x.tt.cores[i].leftUnfolding()
                xCoreVect.reshape(xCoreVect.numElements, 1)
                zAt = projectMatVec(phizA[i], A[i], phizA[i + 1], xCoreVect)
                val yCoreVect = y.tt.cores[i].leftUnfolding()
                yCoreVect.reshape(yCoreVect.numElements, 1)
                val zy = projectVector(phizy[i], phizy[i + 1], y.tt.cores[i])
                val znew = zy - zAt
                val rz1 = z.tt.cores[i].rows
                val rz2 = if(i==d-1) 1 else z.tt.cores[i+1].rows
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
            phiA[i] = computePhi(phiA[i + 1], cr, A[i], cr)
            phiy[i] = computePhi(phiy[i + 1], cr, y.tt.cores[i])

            phizA[i] = computePhi(phizA[i + 1], z.tt.cores[i], A[i], x.tt.cores[i])
            phizy[i] = computePhi(phizy[i + 1], z.tt.cores[i], y.tt.cores[i])
        }

        coreUpdate@ for (i in 0 until d) {
            val phi1 = phiA[i]
            val phi2 = phiA[i + 1]
            val A1 = A[i]
            val y1 = y.tt.cores[i]
            var rhs = projectVector(phiy[i], phiy[i + 1], y1)
            if (normalize) rhs = rhs.concatRows(normalizationFactor* ones(1))
            val normalizer = if (normalize) normalizationFactor* computeNormalizer(x, i, reachableStateSpaceIndicator) else null
            applyALSStep(
                    A,
                    x,
                    y,
                    i,
                    phi1,
                    phi2,
                    residualThreshold * residDamp,
                    normalizer = normalizer,
                    normalizationFactor = normalizationFactor,
                    maxLocalIters = 200
            )

            //truncation
            val newCore = x.tt.cores[i]
            val fullSVD = newCore.leftUnfolding().svd(true)
            var newU = fullSVD.u
            var newS = fullSVD.w
            var newV = fullSVD.v
            if (i < d - 1) {
                if (truncateBasedOnResidual) {
                    while (newU.numCols() > 0) {
                        val u = newU[0..SimpleMatrix.END, 0..newU.numCols() - 1]
                        val s = newS[0..newS.numRows() - 1, 0..newS.numCols() - 1]
                        val v = newV[0..SimpleMatrix.END, 0..newV.numCols() - 1]
                        val currSol = u * s * v.T()
                        currSol.reshape(currSol.numElements, 1)
                        var product = projectMatVec(phi1, A[i], phi2, currSol)
                        if (normalize)
                            product = product.concatRows(normalizer!! * currSol)
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
            val crzy = projectVector(phizy[i], phizy[i + 1], y1)
            val crzAt = projectMatVec(phizA[i], A1, phizA[i + 1], truncSol)
            val crznew = crzy - crzAt
            crznew.reshape(newCore.modeLength * z.ttRanks()[i], z.ttRanks()[i + 1])
            val svd = crznew.svd(true)
            val rank = min(enrichmentRank, svd.u.numCols())
            val kickU = svd.u.cols(0, rank)
            val kickSV = svd.w[0..rank, 0..rank] * svd.v.cols(0, rank).T()
            val zCurrCore = z.tt.cores[i]
            if (i < d - 1) {
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

                // enrichment
                val yVect = newU*modifier
                yVect.reshape(yVect.numElements, 1)
                val leftresid = projectMatVec(phiA[i], A1, phizA[i + 1], yVect)
                val lefty = projectVector(phiy[i], phizy[i + 1], y1)
                val uk = lefty - leftresid
                uk.reshape(newU.numRows(), uk.numElements/newU.numRows())

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

                phiA[i + 1] = computePsi(phiA[i], newCore, A[i], newCore)
                phiy[i + 1] = computePsi(phiy[i], newCore, y.tt.cores[i])

                // update z and its projections
                phizA[i + 1] = computePsi(phizA[i], zCurrCore, A[i], newCore)
                phizy[i + 1] = computePsi(phizy[i], zCurrCore, y.tt.cores[i])
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
            val residNorm = computeResidualNorm(A, x, y)

            if (verbose) println("AMEn-ALS sweep ${swp}: resnorm=$residNorm relresnorm=${residNorm/rightSideNorm} threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
            if (residNorm < residualThreshold) return TTSolution(x, residNorm)
        }
    }

    if (useApproxResidualForStopping) {
        val residNorm = z.norm()
        if (verbose) println("AMEn-ALS exit: resnorm~=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
        return TTSolution(x, residNorm)
    } else {
        val residNorm = computeResidualNorm(A, x, y)
        if (verbose) println("AMEn-ALS exit: resnorm=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
        return TTSolution(x, residNorm)
    }
}

private fun getKroneckerEquivalentMatrix(core: CoreTensor, otherRows: Int, otherCols: Int): SimpleMatrix {
    val resVectLength = core.rows*otherRows*core.cols*otherCols
    val K = SimpleMatrix(core.modeLength*resVectLength, otherRows*otherCols)
    for(i in core.data.indices) {
        K[i*resVectLength, 0] = getKroneckerEquivalentMatrix(core[i], otherRows, otherCols)
    }
    return K
}

private fun computePsi(PsiPrev: TPhi, xCore: CoreTensor, AbstractACore: Abstract2DCoreTensor, yCore: CoreTensor): TPhi {
    val res = arrayListOf<ArrayList<SimpleMatrix>>()
    for (beta in 0 until xCore.cols) {
        res.add(arrayListOf<SimpleMatrix>())
        for (gamma in 0 until yCore.cols) {
            var M = SimpleMatrix(1, AbstractACore.cols)
            for (beta_prev in 0 until xCore.rows)
                for (gamma_prev in 0 until yCore.rows)
                    for (i in 0 until xCore.modeLength)
                        for (j in 0 until yCore.modeLength) {
                            M += AbstractACore.multFromLeft(i, j, PsiPrev[beta_prev][gamma_prev]) * (xCore[i][beta_prev, beta] * yCore[j][gamma_prev, gamma])
                        }
            res.last().add(M)
        }
    }
    return res
}

private fun computePhi(PhiPrev: TPhi, xCore: CoreTensor, AbstractACore: Abstract2DCoreTensor, yCore: CoreTensor): TPhi {
    val res = arrayListOf<ArrayList<SimpleMatrix>>()
    for (beta in 0 until xCore.rows) {
        res.add(arrayListOf<SimpleMatrix>())
        for (gamma in 0 until yCore.rows) {
            var M = SimpleMatrix(AbstractACore.rows, 1)
            for (beta_prev in 0 until xCore.cols)
                for (gamma_prev in 0 until yCore.cols)
                    for (i in 0 until xCore.modeLength)
                        for (j in 0 until yCore.modeLength) {
                            M += AbstractACore.multFromRight(i, j, PhiPrev[beta_prev][gamma_prev]) * (xCore[i][beta, beta_prev] * yCore[j][gamma, gamma_prev])
                        }
            res.last().add(M)
        }
    }
    return res
}

private fun computePsi(PsiPrev: TPhi, xCore: CoreTensor, yCore: CoreTensor): TPhi {
    val res = arrayListOf<ArrayList<SimpleMatrix>>()
    for (beta in 0 until xCore.cols) {
        res.add(arrayListOf<SimpleMatrix>())
        for (gamma in 0 until yCore.cols) {
            var M = SimpleMatrix(1, 1)
            for (beta_prev in 0 until xCore.rows)
                for (gamma_prev in 0 until yCore.rows)
                    for (i in 0 until xCore.modeLength)
                        M += PsiPrev[beta_prev][gamma_prev] * (xCore[i][beta_prev, beta] * yCore[i][gamma_prev, gamma])
            res.last().add(M)
        }
    }
    return res
}

private fun computePhi(PhiPrev: TPhi, xCore: CoreTensor, yCore: CoreTensor): TPhi {
    val res = arrayListOf<ArrayList<SimpleMatrix>>()
    for (beta in 0 until xCore.rows) {
        res.add(arrayListOf<SimpleMatrix>())
        for (gamma in 0 until yCore.rows) {
            var M = SimpleMatrix(1, 1)
            for (beta_prev in 0 until xCore.cols)
                for (gamma_prev in 0 until yCore.cols)
                    for (i in 0 until xCore.modeLength)
                        M += PhiPrev[beta_prev][gamma_prev] * (xCore[i][beta, beta_prev] * yCore[i][gamma, gamma_prev])
            res.last().add(M)
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
    val YDoublePrime = SimpleMatrix(n_k*R_kminus, YPrimeReshaped.numCols())//AkUnfolding * YPrimeReshaped
    for (ik in 0 until n_k) {
        var YDoublePrimeI = SimpleMatrix(ACore.rows, YDoublePrime.numCols())
        for (jk in 0 until n_k) {
            // TODO: switch for loops
            val Yj = YPrimeReshaped.rows(jk*ACore.cols, (jk+1)*ACore.cols)
            YDoublePrimeI +=  ACore.multFromRight(ik, jk, Yj)
        }
        YDoublePrime[ik*R_kminus, 0] = YDoublePrimeI
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

//fun projectVecMat(
//        psi: TPhi,
//        ACore: Abstract2DCoreTensor,
//        phi: TPhi,
//        y: SimpleMatrix,
//        normalizerVector: SimpleMatrix? = null,
//        preconditioner: ((SimpleMatrix) -> SimpleMatrix)? = null
//): SimpleMatrix {
//    val res = SimpleMatrix()
//    for(betaMinus in psi.indices) {
//        for(gammaMinus in psi[betaMinus].indices) {
//            for(beta in phi.indices) {
//                for(gamma in phi[beta].indices) {
//                    for(i in 0 until ACore.modeLength) {
//                        for(j in 0 until ACore.modeLength) {
//                            res[i*]
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

private fun applyALSStep(
    A: Array<Abstract2DCoreTensor>,
    x: TTVector,
    f: TTVector,
    k: Int,
    psi: TPhi,
    phi: TPhi,
    residualThreshold: Double,
    maxLocalIters: Int = 200,
    normalizer: SimpleMatrix? = null,
    normalizationFactor: Double = 1.0
) {
    val currCore = x.tt.cores[k]

    //Local solution

    //region Computation of local right-hand side
    //assemble full F=[ Q^T*f^_k(0); Q^T*f^_k(1) ... Q^T*f^_k(n_k) ]  matrix
    var leftPart = eye(1)
    for (l in 0 until k) {
        var currFactor = SimpleMatrix(x.tt.cores[l].rows * f.tt.cores[l].rows, x.tt.cores[l].cols * f.tt.cores[l].cols)
        for (m in 0 until x.modes[l])
            currFactor += x.tt.cores[l][m].kron(f.tt.cores[l][m])
        leftPart *= currFactor
    }
    var rightPart = eye(1)
    for (l in x.modes.size - 1 downTo k + 1) {
        var currFactor = SimpleMatrix(x.tt.cores[l].rows * f.tt.cores[l].rows, x.tt.cores[l].cols * f.tt.cores[l].cols)
        for (m in 0 until x.modes[l])
            currFactor += x.tt.cores[l][m].kron(f.tt.cores[l][m])
        rightPart = currFactor * rightPart
    }
    val F = SimpleMatrix(currCore.modeLength * currCore.rows * currCore.cols, 1)
    val middleAux = SimpleMatrix(currCore.rows, currCore.cols)
    //TODO: can this be done more efficiently by computing a product leftpart*(something_that_gives_the_whole_result)*rightpart?
    for (i in 0 until currCore.modeLength) {
        for (alphaMinus in 0 until currCore.rows) {
            for (alpha in 0 until currCore.cols) {
                middleAux[alphaMinus, alpha] = 1.0
                val elem = leftPart * middleAux.kron(f.tt.cores[k][i]) * rightPart
                assert(elem.numElements == 1)
                F[i * currCore.rows * currCore.cols + alphaMinus * currCore.cols + alpha] = elem[0]
                middleAux[alphaMinus, alpha] = 0.0
            }
        }
    }
    //endregion

//    val solveDirectly =true // currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
    val solveDirectly = currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
    val ACore = A[k]
    lateinit var w: SimpleMatrix
    if (solveDirectly) {
        val dim = currCore.modeLength * currCore.rows * currCore.cols
        var FullB = SimpleMatrix(dim, dim)
        //TODO: Parallel computation
        for (betaMinus in 0 until currCore.rows) {
            for (beta in 0 until currCore.cols) {
                for (gammaMinus in 0 until currCore.rows) {
                    for (gamma in 0 until currCore.cols) {
                        for (i in 0 until currCore.modeLength) {
                            for (j in 0 until currCore.modeLength) {
                                FullB[i * currCore.rows * currCore.cols + betaMinus * currCore.cols + beta, j * currCore.rows * currCore.cols + gammaMinus * currCore.cols + gamma] =
                                         ACore.multFromLeft(i, j, psi[betaMinus][gammaMinus]) * phi[beta][gamma]
                            }
                        }
                    }
                }
            }
        }

        //solve Bw=F
        w = if (normalizer != null) {
            val FullBExtended = FullB.concatRows(normalizer).concatColumns(normalizer.T().concatRows(SimpleMatrix(1, 1)))
            val FExtended = F.concatRows(mat[r[normalizationFactor]])
            val res = try {
                FullBExtended.solve(FExtended)
            } catch (e: SingularMatrixException) {
                FullBExtended.pseudoInverse() * FExtended
            }
            res[0..res.numElements-1, 0..1]
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
            w0 /= (normalizer*w0)[0]
            w = ALSLocalIterSolve(
                    psi, phi, A, w0, F, k,
                    residualThreshold*0.001,
                    maxLocalIters = maxLocalIters,
                    normalizerVector = normalizer,
                    normalizationFactor = normalizationFactor,
                    preconditioner = preconditioner
            )
        } else {
            w = ALSLocalIterSolve(psi, phi, A, w0, F, k, residualThreshold*0.001,
                    maxLocalIters = maxLocalIters,
                    preconditioner = preconditioner
            )
        }
        val res= projectMatVec(psi, A[k], phi, w, null) -F
    }
    for (i in 0 until currCore.modeLength) {
        for (beta_minus in 0 until currCore.rows) {
            for (beta in 0 until currCore.cols) {
                currCore[i][beta_minus, beta] = w[i * currCore.rows * currCore.cols + beta_minus * currCore.cols + beta]
            }
        }
    }
}

private fun computeNormalizer(x: TTVector, k: Int, normalizationSetIndicator: TTVector? = null): SimpleMatrix {
    var normalizerLeft = ones(1)
    // TODO: cache
    repeat(k) {
        val coreTensor = x.tt.cores[it]
        val normCore = normalizationSetIndicator?.tt?.cores?.get(it)
        var sum =
                if(normCore==null) coreTensor[0].createLike()
                else SimpleMatrix(coreTensor.rows*normCore.rows, coreTensor.cols*normCore.cols)
        for ((idx, M) in coreTensor.data.withIndex()) {
            sum += (normCore?.get(idx)?.kron(M) ?: M)
        }
        normalizerLeft = normalizerLeft * sum
    }
    var normalizerRight = ones(1)
    for (i in x.tt.cores.size - 1 downTo k + 1) {
        val coreTensor = x.tt.cores[i]
        val normCore = normalizationSetIndicator?.tt?.cores?.get(i)
        var sum =
                if(normCore==null) coreTensor[0].createLike()
                else SimpleMatrix(coreTensor.rows*normCore.rows, coreTensor.cols*normCore.cols)
        for ((idx, M) in coreTensor.data.withIndex()) {
            sum += (normCore?.get(idx)?.kron(M) ?: M)
        }
//        normalizerRight = sum
        normalizerRight = sum * normalizerRight
    }
    val normalizer = ones(x.modes[k]).T().kron(normalizerLeft).kron(normalizerRight.T())
    if(normalizationSetIndicator == null)
        return normalizer
    else {
        val A = normalizationSetIndicator.tt.cores[k]
        val B = x.tt.cores[k]
        // TODO: sparse?
        val numelementsA = A.rows * A.cols
        val numelementsB = B.rows * B.cols
        val modifier = SimpleMatrix(numelementsA * B.rows * B.cols * A.modeLength, numelementsB * B.modeLength)
        for(i in 0 until A.modeLength) {
            for(j in 0 until A[i].numRows()) {
                val r = A[i].row(j).T()
                modifier[i*numelementsB*numelementsA+j*A.cols*numelementsB, i*numelementsB] = eye(B.rows).kron(r.kron(
                    eye(B.cols)
                ))
            }
        }
        return normalizer*modifier
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
    preconditioner: ((SimpleMatrix)->SimpleMatrix)? = null,
    maxLocalIters: Int = 200,
    normalizerVector: SimpleMatrix? = null,
    normalizationFactor: Double = 1.0
): SimpleMatrix {
    val r_k = phi.size
    val r_kminus = psi.size

    val linearMap: (SimpleMatrix) -> SimpleMatrix = { projectMatVec(psi, A[k], phi, it, normalizerVector, preconditioner) }
//    return BiCGStabL(2, ::computeMatVec, preconditioner?.mult(F) ?: F, maxLocalIters, w0, threshold).solution
    val f = if(normalizerVector != null) F.concatRows(mat[r[normalizationFactor]]) else F
    var result =
            if (normalizerVector != null) {
                val w0extended = w0.concatRows(SimpleMatrix(1,1))
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

