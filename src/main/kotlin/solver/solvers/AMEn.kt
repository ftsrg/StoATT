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
import kotlin.math.round
import kotlin.math.sqrt

typealias TPhi = List<List<SimpleMatrix>>
enum class AmenStoppingCriterion {
    STABILIZATION, NORM2, NORM2_APPROXIMATE_RESIDUAL
}


// Quick non-optimized prototype
// based on the amen_solve2 function of the TT matlab toolbox
fun AMEnALSSolve(
        A: TTSquareMatrix,
        y: TTVector,
        x0: TTVector = TTVector.ones(y.modes),
        residualThreshold: Double,
        maxSweeps: Int,
        enrichmentRank: Int,
        normalize: Boolean = false,
        verbose: Boolean = true,
        residDamp: Double = 1e-2,
        truncateBasedOnResidual: Boolean = true,
        useApproxResidualForStopping: Boolean = false,
        z0: TTVector? = null,
        useDirectForSmall: Boolean = false
): TTSolution {
    val phiA = Array(A.modes.size + 1) { listOf(listOf(ones(1))) }
    val phiy = Array(A.modes.size + 1) { listOf(listOf(ones(1))) }
    val phizA = Array(A.modes.size + 1) { listOf(listOf(ones(1))) }
    val phizy = Array(A.modes.size + 1) { listOf(listOf(ones(1))) }
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
                zAt = projectMatVec(phizA[i], A.tt.cores[i], phizA[i + 1], xCoreVect)
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
            phiA[i] = computePhi(phiA[i + 1], cr, A.tt.cores[i], cr)
            phiy[i] = computePhi(phiy[i + 1], cr, null, y.tt.cores[i])

            phizA[i] = computePhi(phizA[i + 1], z.tt.cores[i], A.tt.cores[i], x.tt.cores[i])
            phizy[i] = computePhi(phizy[i + 1], z.tt.cores[i], null, y.tt.cores[i])
        }

        for (i in 0 until d) {
            val phi1 = phiA[i]
            val phi2 = phiA[i + 1]
            val A1 = A.tt.cores[i]
            val y1 = y.tt.cores[i]
            var rhs = projectVector(phiy[i], phiy[i + 1], y1)
            if (normalize) rhs = rhs.concatRows(ones(1))
            val normalizer = if (normalize) computeNormalizer(x, i) else null
            applyALSStep(
                    A,
                    x,
                    y,
                    i,
                    phi1,
                    phi2,
                    residualThreshold * residDamp,
                    normalizer = normalizer,
                    useDirectForSmall = useDirectForSmall
            )

            //truncation
            val newCore = x.tt.cores[i]
            val fullSVD = newCore.leftUnfolding().svd(true)
            var newU = fullSVD.u
            var newS = fullSVD.w
            var newV = fullSVD.v
            if (i < d - 1) {
                if (truncateBasedOnResidual) {
                    while (true) {
                        val u = newU[0..SimpleMatrix.END, 0..newU.numCols() - 1]
                        val s = newS[0..newS.numRows() - 1, 0..newS.numCols() - 1]
                        val v = newV[0..SimpleMatrix.END, 0..newV.numCols() - 1]
                        val currSol = u * s * v.T()
                        currSol.reshape(currSol.numElements, 1)
                        var product = projectMatVec(phi1, A.tt.cores[i], phi2, currSol)
                        if (normalize)
                            product = product.concatRows(normalizer!! * currSol)
                        val res = rhs - product
                        if (res.vecNorm2() > residualThreshold * residDamp || u.numCols()<=1) break
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
            assert(newCore.modeLength * z.ttRanks()[i] * z.ttRanks()[i + 1] == crznew.numElements) //TODO: for debug purposes; remove it once tested
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

                phiA[i + 1] = computePsi(phiA[i], newCore, A.tt.cores[i], newCore)
                phiy[i + 1] = computePsi(phiy[i], newCore, null, y.tt.cores[i])

                // update z and its projections
                phizA[i + 1] = computePsi(phizA[i], zCurrCore, A.tt.cores[i], newCore)
                phizy[i + 1] = computePsi(phizy[i], zCurrCore, null, y.tt.cores[i])
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
            val resid = (A*x-y)
            val residNorm = resid.norm()
            if (verbose) println("AMEn-ALS sweep ${swp}: resnorm=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
            if (residNorm < residualThreshold)
                return TTSolution(x, residNorm)
        }
    }

    if (useApproxResidualForStopping) {
        val residNorm = z.norm()
        if (verbose) println("AMEn-ALS exit: resnorm~=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
        return TTSolution(x, residNorm)
    } else {
        val residNorm = (A * x - y).norm()
        if (verbose) println("AMEn-ALS exit: resnorm=$residNorm threshold=$residualThreshold maxrank=${x.ttRanks().max()}")
        return TTSolution(x, residNorm)
    }
}

private fun computePsi(PsiPrev: TPhi, xCore: CoreTensor, ACore: CoreTensor?, yCore: CoreTensor): TPhi {
    val res = arrayListOf<ArrayList<SimpleMatrix>>()
    for (beta in 0 until xCore.cols) {
        res.add(arrayListOf<SimpleMatrix>())
        for (gamma in 0 until yCore.cols) {
            var M = SimpleMatrix(1, ACore?.cols ?: 1)
            for (beta_prev in 0 until xCore.rows)
                for (gamma_prev in 0 until yCore.rows)
                    if (ACore == null)
                        for (i in 0 until xCore.modeLength)
                            M += PsiPrev[beta_prev][gamma_prev] * (xCore[i][beta_prev, beta] * yCore[i][gamma_prev, gamma])
                    else {
                        for (i in 0 until xCore.modeLength)
                            for (j in 0 until yCore.modeLength) {
                                M += PsiPrev[beta_prev][gamma_prev] * ACore[i, j] * (xCore[i][beta_prev, beta] * yCore[j][gamma_prev, gamma])
                            }
                    }
            res.last().add(M)
        }
    }
    return res
}

private fun computePhi(PhiPrev: TPhi, xCore: CoreTensor, ACore: CoreTensor?, yCore: CoreTensor): TPhi {
    val res = arrayListOf<ArrayList<SimpleMatrix>>()
    for (beta in 0 until xCore.rows) {
        res.add(arrayListOf<SimpleMatrix>())
        for (gamma in 0 until yCore.rows) {
            var M = SimpleMatrix(ACore?.rows ?: 1, 1)
            for (beta_prev in 0 until xCore.cols)
                for (gamma_prev in 0 until yCore.cols)
                    if (ACore == null) {
                        for (i in 0 until xCore.modeLength)
                            M += PhiPrev[beta_prev][gamma_prev] * (xCore[i][beta, beta_prev] * yCore[i][gamma, gamma_prev])
                    } else {
                        for (i in 0 until xCore.modeLength)
                            for (j in 0 until yCore.modeLength) {
                                M += ACore[i, j] * PhiPrev[beta_prev][gamma_prev] * (xCore[i][beta, beta_prev] * yCore[j][gamma, gamma_prev])
                            }
                    }
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
fun projectMatVec(
        psi: TPhi,
        ACore: CoreTensor,
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
    val n_k = round(sqrt(ACore.modeLength.toDouble())).toInt() //TODO: as param? or any other way but not sqrt...
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
    val AkUnfolding = SimpleMatrix(n_k * R_kminus, n_k * ACore.cols)
    for (ik in 0 until n_k) {
        for (jk in 0 until n_k) {
            AkUnfolding[ik * R_kminus, jk * ACore.cols] = ACore[ik * n_k + jk]
        }
    }
    val YDoublePrime = AkUnfolding * YPrimeReshaped

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
        res = res.concatRows(mat[r[(normalizerVector * y)[0]]])
    }

    return preconditioner?.invoke(res) ?: res
}

private fun applyALSStep(
        A: TTSquareMatrix,
        x: TTVector,
        f: TTVector,
        k: Int,
        psi: TPhi,
        phi: TPhi,
        residualThreshold: Double,
        maxLocalIters: Int = 200,
        normalizer: SimpleMatrix? = null,
        useDirectForSmall: Boolean = false
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

    val solveDirectly = useDirectForSmall && currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
//    val solveDirectly = currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
    val ACore = A.tt.cores[k]
    lateinit var w: SimpleMatrix
    if (solveDirectly) {
        val dim = currCore.modeLength * currCore.rows * currCore.cols
        val FullB = SimpleMatrix(dim, dim)
        //TODO: Parallel computation
        for (betaMinus in 0 until currCore.rows) {
            for (beta in 0 until currCore.cols) {
                for (gammaMinus in 0 until currCore.rows) {
                    for (gamma in 0 until currCore.cols) {
                        for (i in 0 until currCore.modeLength) {
                            for (j in 0 until currCore.modeLength) {
                                FullB[i * currCore.rows * currCore.cols + betaMinus * currCore.cols + beta, j * currCore.rows * currCore.cols + gammaMinus * currCore.cols + gamma] =
                                        psi[betaMinus][gammaMinus] * ACore[i * currCore.modeLength + j] * phi[beta][gamma]
                            }
                        }
                    }
                }
            }
        }

        //solve Bw=F
        w = if (normalizer != null) {
            val FullBExtended = FullB.concatRows(normalizer).concatColumns(normalizer.T().concatRows(SimpleMatrix(1, 1)))
            val FExtended = F.concatRows(mat[r[1.0]])
            try {
                FullBExtended.solve(FExtended)
            } catch (e: SingularMatrixException) {
                FullBExtended.pseudoInverse() * FExtended
            }
        } else {
            try {
                FullB.solve(F)
            } catch (e: SingularMatrixException) {
                FullB.pseudoInverse() * F
            }
        }
    } else {
        val w0 = F.createLike()
        for (i in 0 until currCore.modeLength) {
            val M = currCore[i]
            for (row in 0 until M.numRows()) {
                for (col in 0 until M.numCols()) {
                    w0[i * M.numRows() * M.numCols() + row * M.numCols() + col] = M[row, col]
                }
            }
        }

        if (normalizer != null) {
            w = ALSLocalIterSolve(psi, phi, A, w0, F, k, residualThreshold * 0.001, maxLocalIters = maxLocalIters, normalizerVector = normalizer)
        } else {
            w = ALSLocalIterSolve(psi, phi, A, w0, F, k, residualThreshold * 0.001, maxLocalIters = maxLocalIters)
        }
    }
    for (i in 0 until currCore.modeLength) {
        for (beta_minus in 0 until currCore.rows) {
            for (beta in 0 until currCore.cols) {
                currCore[i][beta_minus, beta] = w[i * currCore.rows * currCore.cols + beta_minus * currCore.cols + beta]
            }
        }
    }
}

private fun computeNormalizer(x: TTVector, k: Int): SimpleMatrix {
    var normalizerLeft = ones(1)
    // TODO: cache
    repeat(k) {
        val coreTensor = x.tt.cores[it]
        var sum = coreTensor[0].createLike()
        for (M in coreTensor.data) {
            sum += M
        }
        normalizerLeft *= sum
    }
    var normalizerRight = ones(1)
    for (i in x.tt.cores.size - 1 downTo k + 1) {
        val coreTensor = x.tt.cores[i]
        var sum = coreTensor[0].createLike()
        for (M in coreTensor.data) {
            sum += M
        }
        normalizerRight = sum * normalizerRight
    }
    val normalizer = ones(x.modes[k]).T().kron(normalizerLeft).kron(normalizerRight.T())
    return normalizer
}

private fun ALSLocalIterSolve(
        psi: List<List<SimpleMatrix>>,
        phi: List<List<SimpleMatrix>>,
        A: TTSquareMatrix,
        w0: SimpleMatrix,
        F: SimpleMatrix,
        k: Int,
        threshold: Double,
        preconditioner: SimpleMatrix? = null,
        maxLocalIters: Int = 200,
        normalizerVector: SimpleMatrix? = null
): SimpleMatrix {
    val r_k = phi.size
    val r_kminus = psi.size
    //TODO: assemble this matrix while computing phi, instead of storing phi in an array-of-arrays
    val Ak = A.tt.cores[k]
    val phiMat = SimpleMatrix(Ak.cols * phi.size, phi.size)
    val R_k = Ak.cols
    for ((beta, phi_beta) in phi.withIndex()) {
        for ((gamma, phi_beta_gamma) in phi_beta.withIndex()) {
            phiMat[beta * R_k, gamma] = phi_beta_gamma
        }
    }
    val R_kminus = Ak.rows
    val psiMat = SimpleMatrix(r_kminus, r_kminus * R_kminus)
    for ((beta_minus, psi_beta_minus) in psi.withIndex()) {
        for ((gamma_minus, psiCurr) in psi_beta_minus.withIndex()) {
            psiMat[beta_minus, gamma_minus * R_kminus] = psiCurr
        }
    }

    fun computeMatVec(y: SimpleMatrix): SimpleMatrix {
        val lambda = y[y.numElements - 1] //used only if normalization is applied
        val y = if (normalizerVector != null) y[0..y.numElements - 1, 0..1] else y
        var res = y.createLike()
        val n_k = A.modes[k]

        //Computation of Y'
        val YMat = SimpleMatrix(r_k, r_kminus * n_k)
        for (i in 0 until n_k) {
            for (gamma_minus in 0 until r_kminus) {
                YMat[0, i * r_kminus + gamma_minus] = y[i * r_kminus * r_k + gamma_minus * r_k..i * r_kminus * r_k + (gamma_minus + 1) * r_k, 0..1]
            }
        }
        val YPrime = phiMat * YMat

        //Computation of Y''
        val YPrimeReshaped = SimpleMatrix(n_k * R_k, r_k * r_kminus)
        for (beta in 0 until r_k) {
            for (gamma_minus in 0 until r_kminus) {
                for (jk in 0 until n_k) {
                    for (idx in 0 until R_k) {
                        YPrimeReshaped[jk * R_k + idx, beta * r_kminus + gamma_minus] = YPrime[beta * R_k + idx, jk * r_kminus + gamma_minus]
                    }
                }
            }
        }
        val AkUnfolding = SimpleMatrix(n_k * R_kminus, n_k * Ak.cols)
        for (ik in 0 until n_k) {
            for (jk in 0 until n_k) {
                AkUnfolding[ik * R_kminus, jk * Ak.cols] = Ak[ik * n_k + jk]
            }
        }
        val YDoublePrime = AkUnfolding * YPrimeReshaped

        //Computation of the result
        val YDoublePrimeReshaped = SimpleMatrix(r_kminus * R_kminus, n_k * r_k)
        for (gamma_minus in 0 until r_kminus) {
            for (idx in 0 until R_kminus) {
                for (ik in 0 until n_k) {
                    for (beta in 0 until r_k) {
                        YDoublePrimeReshaped[gamma_minus * R_kminus + idx, ik * r_k + beta] =
                                YDoublePrime[ik * R_kminus + idx, beta * r_kminus + gamma_minus]
                    }
                }
            }
        }
        val resTemp = psiMat * YDoublePrimeReshaped
        //res is indexed like F, hint: "F[i*currCore.rows*currCore.cols+alphaMinus*currCore.cols+alpha] = elem[0]"
        for (i in 0 until n_k) {
            for (beta_minus in 0 until r_kminus) {
                for (beta in 0 until r_k) {
                    res[i * r_kminus * r_k + beta_minus * r_k + beta] = resTemp[beta_minus, i * r_k + beta]
                }
            }
        }

        if (normalizerVector != null) {
            res += lambda * normalizerVector.T()
            res = res.concatRows(mat[r[(normalizerVector * y)[0]]])
        }

        return preconditioner?.mult(res) ?: res
    }

//    return BiCGStabL(2, ::computeMatVec, preconditioner?.mult(F) ?: F, maxLocalIters, w0, threshold).solution
    val result =
            if (normalizerVector != null) {
                val Fextended = F.concatRows(mat[r[1.0]])
                val w0extended = w0.concatRows(mat[r[w0.scalarProduct(ones(w0.numRows(), 1))]])
                biCGStab(::computeMatVec, preconditioner?.mult(Fextended)
                                          ?: Fextended, maxLocalIters, w0extended, threshold)
            } else {
                biCGStab(::computeMatVec, preconditioner?.mult(F) ?: F, maxLocalIters, w0, threshold)
            }
    if (normalizerVector != null) {
        return result[0..result.numElements - 1, 0..1]
    }
    return result
//    return ReGMRES(::computeMatVec, F, 10, w0, threshold)
}

