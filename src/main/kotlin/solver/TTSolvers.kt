package solver

import org.ejml.data.SingularMatrixException
import org.ejml.simple.SimpleMatrix
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

//TODO: do something with vectors with elements with different signs
/**
 * Inverts the elements of a TT-Vector using the Newton-Schulz iterative algorithm
 * @param V Vector to invert in solver.TensorTrain format
 * @param thresh Threshold of the residual's Frobenius norm used for convergence check
 * @return Element-wise inverse of the input in solver.TensorTrain format
 */
fun NSInvertVect(V: TTVector, thresh: Double, roundingAccuracy: Double, zeroMaskVector: TTVector = TTVector.ones(V.modes), log: Boolean = false): TTVector {
    var Vinv = zeroMaskVector * (1.0 / V.tt.frobenius()) * (V[0] / abs(V[0]))
    do {
        val residual = zeroMaskVector - V.hadamard(Vinv)
        Vinv = Vinv + Vinv.hadamard(residual)
        Vinv.tt.roundRelative(roundingAccuracy)
        if (log) {
            Vinv.printElements()
            println()
        }
    } while (residual.tt.frobenius() > thresh)
    return Vinv
}

fun NSInvertMat(M: TTSquareMatrix, iters: Int, roundingAccuracy: Double): TTSquareMatrix {
    val I = TTSquareMatrix.eye(M.modes)
    var X = M.transpose()
    X.divAssign((X * M).tt.frobenius())
    repeat(iters) {
        X = X * (2.0 * I - M * X)
        X.tt.roundRelative(roundingAccuracy)
    }
    return X
}

fun jacobiPreconditioner(A: TTSquareMatrix, zeroMaskVector: TTVector): TTSquareMatrix {
    val diag = A.diagVect()
    val inv = NSInvertVect(diag, 0.001 * diag.norm(), 0.001, zeroMaskVector)
    return TTSquareMatrix.diag(inv)
}

fun TTJacobi(A: TTSquareMatrix, b: TTVector, thresh: Double, roundingAccuracy: Double, zeroMaskVector: TTVector = TTVector.ones(A.modes), log: Boolean = false): TTSolution {
    for ((idx, mode) in A.modes.withIndex()) {
        require(mode == b.modes[idx]) { "The modes of A and b must be identical!" }
    }
    val D = A.diagVect()
    val Dinv = TTSquareMatrix.diag(
            NSInvertVect(D,  thresh*D.norm(), thresh/10, zeroMaskVector)
    )
    val R = A - A.diag()
    var x = TTVector.zeros(A.modes)
    var residual: TTVector
    var i = 0
    do {
        x = Dinv * (b - R * x)
        x.tt.roundAbsolute(0.0)
        x.tt.roundRelative(roundingAccuracy)
        residual = b - A * x
        if(log) println("Jacobi iter $i: resnorm=${residual.norm()} maxrank=${x.ttRanks().max()}")
        i++
    } while (residual.norm() > thresh)
    return TTSolution(x, residual.norm())
}

fun TTReGMRES(
        preconditioner: TTSquareMatrix?,
        A: TTSquareMatrix,
        b: TTVector,
        x0: TTVector,
        relativeResThresold: Double,
        maxInnerIter: Int = 5,
        maxOuterIter: Int = 100,
        verbose: Boolean = false,
        approxSpectralRadius: Double = 1.0): TTSolution =
        TTReGMRES(if(preconditioner==null) {v: TTVector -> A*v } else {v: TTVector -> preconditioner*(A*v)},
                b, x0, relativeResThresold, maxInnerIter, maxOuterIter, verbose, approxSpectralRadius)

fun TTReGMRES(
        linearMap: (TTVector)->TTVector,
        b: TTVector,
        x0: TTVector,
        relativeResThresold: Double,
        maxInnerIter: Int = 5,
        maxOuterIter: Int = 100,
        verbose: Boolean = false,
        approxSpectralRadius: Double = 1.0
): TTSolution {
    val residualThreshold = b.norm() * relativeResThresold
    var x = x0.copy()
    for (i in 0 until maxOuterIter) {
        val solution = TTGMRES(linearMap, b, x, relativeResThresold, maxInnerIter, verbose)
        x = solution.solution
        if(solution.resNorm < residualThreshold) break
        x.tt.roundRelative(relativeResThresold/approxSpectralRadius)
//        val realResNorm = (linearMap * x - b).norm()
        if(verbose) println("TTReGMRES iter $i: resnorm=${solution.resNorm} maxrank=${x.ttRanks().max()}")
//        if(solution.resNorm < residualThreshold && (linearMap * x - b).norm() < residualThreshold) break
    }
    x.tt.roundAbsolute(1e-16)
    return TTSolution(x, (linearMap(x) - b).norm())
}

fun TTGMRES(
        preconditioner: TTSquareMatrix,
        A: TTSquareMatrix, b: TTVector, x0: TTVector,
        eps: Double, maxIter: Int = 100
): TTSolution = TTGMRES({preconditioner*(A*it)}, preconditioner*b, x0, eps, maxIter) //TODO: change eps based on preconditioning
fun TTGMRES(
        A: TTSquareMatrix, b: TTVector, x0: TTVector,
        eps: Double, maxIter: Int = 100,
        verbose: Boolean = false
): TTSolution = TTGMRES(A::times, b, x0, eps, maxIter, verbose)
fun TTGMRES(
        linearMap: (TTVector)->TTVector,
        b: TTVector, x0: TTVector,
        eps: Double, maxIter: Int = 100, verbose: Boolean = false): TTSolution {
    // Reference for the algorithm:
    // S. V. DOLGOV - TT-GMRES: on solution to a linear system in the structured tensor format
    val res0 = b - linearMap(x0)
    res0.tt.roundRelative(0.0)
    val R = arrayListOf(res0.norm())
    val beta = R[0]
    val V = arrayListOf(res0 / beta)
    val h = hashMapOf<Pair<Int, Int>, Double>() //TODO: vmi értelmesebb ehelyett
    lateinit var y: SimpleMatrix
    var r = 0.0
    var x = x0.copy()
    for (j in 1..maxIter) {
        val delta = eps / R[j - 1]
        var w = linearMap(V[j - 1])
        w.tt.roundAbsolute(0.0)
        w.tt.roundRelative(delta)
        for (i in 0 until j) {
            val t = w * V[i]
            h[Pair(i, j - 1)] = t
            w = w - V[i] * t
        }
        w.tt.roundAbsolute(0.0)
        w.tt.roundRelative(delta)
        val norm = w.norm()
        h[Pair(j, j - 1)] = norm
        V.add(w / norm)
        val H = SimpleMatrix(j + 1, j)
        H.fill { i, k -> h.getOrDefault(Pair(i, k), 0.0) } //TODO: wastes time, the data should instantly be written into the H matrix instead of the dictionary

        val solv = solveWithRots(H, beta) //TODO: use solution from prev
        r = solv.resNorm
        y = solv.solution

        R.add(r)
        val relResNorm = r / b.norm()
        if (relResNorm < eps) {
            if(verbose) println("approximate relative residual norm: $relResNorm (required: <$eps)")
            break
        }
    }
    for (i in 0 until y.numElements) {
        x = x + y[i] * V[i]
    }
    return TTSolution(x, r)
}

data class TTSolution(val solution: TTVector, val resNorm: Double)
data class MatSolution(val solution: SimpleMatrix, val resNorm: Double)

private fun solveWithRots(H: SimpleMatrix, beta: Double): MatSolution {
    val g = SimpleMatrix(H.numRows(), 1)
    g[0] = beta
    for (i in 0 until H.numCols()) {
        val rowNext = H[i + 1, i]
        val rowCurr = H[i, i]
        val denom = sqrt(rowCurr * rowCurr + rowNext * rowNext)
        val s = rowNext / denom
        val c = rowCurr / denom
        val r1 = H.row(i)
        val r2 = H.row(i + 1)
        H[i, 0] = c * r1 + s * r2
        H[i + 1, 0] = c * r2 - s * r1
        val g1 = g[i]
        val g2 = g[i + 1]
        g[i] = c * g1 + s * g2
        g[i + 1] = c * g2 - s * g1
    }
    val residualNorm = abs(g[g.numElements - 1])
    val H1 = H[0..H.numRows() - 1, 0..H.numCols()]
    val y = g[0..g.numElements - 1, 0..1]
    for (i in y.numElements - 1 downTo 0) {
        for (j in y.numElements - 1 downTo i + 1) {
            y[i] -= H1[i, j] * y[j]
        }
        y[i] /= H1[i, i]
    }
    return MatSolution(y, residualNorm)
}

fun ALSSolve(
        A: TTSquareMatrix,
        f: TTVector,
        ranks: Array<Int>,
        random: Random,
        residualThreshold: Double,
        maxSweeps: Int
): TTSolution = ALSSolve(
        A, f,
        TTVector.rand(A.modes, ranks, random = random),
        residualThreshold, maxSweeps
)

fun ALSSolve(
        A: TTSquareMatrix,
        f: TTVector,
        x0: TTVector = TTVector.ones(f.modes),
        residualThreshold: Double,
        maxSweeps: Int
): TTSolution {
    // Reference for the algorithm:
    // I. V. OSELEDETS AND S. V. DOLGOV - Solution of Linear Systems and Matrix Inversion in the TT-Format

    val x = x0.copy()
    for (i in x.tt.cores.size - 2 downTo 1) {
        x.tt.rightOrthogonalizeCore(i)
    }

    //sweep through the cores forward and backward
    val sweepRange = //list of (core index: Int, forward: Bool)
            (0 until x.modes.size-1).toList().map { it to true } +
            (x.modes.size-1 downTo 1).toList().map { it to false }
    val psiCache = Array<Array<Array<SimpleMatrix>>?>(x.modes.size) {null}
    psiCache[0] = Array(1) { Array(1) { mat[r[1]] } }
    val phiCache = Array<Array<Array<SimpleMatrix>>?>(x.modes.size) {null}
    phiCache[phiCache.size-1] = Array(1) {Array(1) { mat[r[1]]}}
    var resNorm = (A*x-f).norm()
    for (sweep in 0..maxSweeps) {
        for ((k, forward) in sweepRange) {

            applyALSStep(A, x, f, k, psiCache, phiCache, residualThreshold)

            if (forward) { //Left orthogonalization
                x.tt.leftOrthogonalizeCore(k)
            } else { //Right orthogonalization
                x.tt.rightOrthogonalizeCore(k)
            }

        }

        resNorm = (f - A * x).norm()
        if(resNorm <= residualThreshold) break
    }
    return TTSolution(x, resNorm)
}

private fun applyALSStep(
        A: TTSquareMatrix,
        x: TTVector,
        f: TTVector,
        k: Int,
        psiCache: Array<Array<Array<SimpleMatrix>>?>,
        phiCache: Array<Array<Array<SimpleMatrix>>?>,
        residualThreshold: Double,
        maxLocalIters: Int = 200
) {
    val currCore = x.tt.cores[k]
    //TODO: parallel computation of elements

    fun computePsi(idx: Int) {
        if (idx == 0) return
        if (psiCache[idx - 1] == null) computePsi(idx - 1)
        val psiPrev = psiCache[idx - 1]!!
        val xCore = x.tt.cores[idx - 1]
        val ACore = A.tt.cores[idx - 1]
        psiCache[idx] = Array(xCore.cols) { Array(xCore.cols) { SimpleMatrix(1, ACore.cols) } }
        val psiCurr = psiCache[idx]!!
        for (beta in 0 until xCore.cols) {
            for (gamma in 0 until xCore.cols) {
                psiCurr[beta][gamma].fill(0.0)
                for (i in 0 until xCore.modeLength) {
                    for (j in 0 until xCore.modeLength) {
                        for (betaPrev in 0 until psiPrev.size) {
                            for (gammaPrev in 0 until psiPrev[betaPrev].size) {
                                psiCurr[beta][gamma] += psiPrev[betaPrev][gammaPrev] *
                                                        ACore[i * xCore.modeLength + j] *
                                                        xCore[i][betaPrev, beta] *
                                                        xCore[j][gammaPrev, gamma]
                            }
                        }
                    }
                }
            }
        }
    }

    fun computePhi(idx: Int) {
        if (idx == phiCache.size - 1) return
        if (phiCache[idx + 1] == null) computePhi(idx + 1)
        val phiNext = phiCache[idx + 1]!!
        val xCore = x.tt.cores[idx + 1]
        val ACore = A.tt.cores[idx + 1]
        phiCache[idx] = Array(xCore.rows) { Array(xCore.rows) { SimpleMatrix(ACore.rows, 1) } }
        val phiCurr = phiCache[idx]!!
        for (beta in 0 until xCore.rows) {
            for (gamma in 0 until xCore.rows) {
                phiCurr[beta][gamma].fill(0.0)
                for (i in 0 until xCore.modeLength) {
                    for (j in 0 until xCore.modeLength) {
                        for (betaNext in 0 until phiNext.size) {
                            for (gammaNext in 0 until phiNext[betaNext].size) {
                                phiCurr[beta][gamma] += ACore[i * xCore.modeLength + j] *
                                                        phiNext[betaNext][gammaNext] *
                                                        xCore[i][beta, betaNext] *
                                                        xCore[j][gamma, gammaNext]
                            }
                        }
                    }
                }
            }
        }
    }

    computePsi(k)
    val psi = psiCache[k]!!
    computePhi(k)
    val phi = phiCache[k]!!

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

    //TODO: refine this criterion; the current threshold is taken from the original article, but maybe an adaptive/adjustable one would be better
    val solveDirectly = currCore.modeLength * currCore.modeLength * currCore.cols * currCore.rows < 100
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
        w = try {
            FullB.solve(F)
        } catch (e: SingularMatrixException) {
            FullB.pseudoInverse() * F
        }
    } else {
        val w0 = F.createLike()
        //TODO: use current iterate of the core instead of a zero vector as the initial solution
        w = ALSLocalIterSolve(psi, phi, A, w0, F, k, residualThreshold * 0.001, maxLocalIters = maxLocalIters)
    }
    for (i in 0 until currCore.modeLength) {
        for (beta_minus in 0 until currCore.rows) {
            for (beta in 0 until currCore.cols) {
                currCore[i][beta_minus, beta] = w[i * currCore.rows * currCore.cols + beta_minus * currCore.cols + beta]
            }
        }
    }
}

private fun ALSLocalIterSolve(
        psi: Array<Array<SimpleMatrix>>,
        phi: Array<Array<SimpleMatrix>>,
        A: TTSquareMatrix,
        w0: SimpleMatrix,
        F: SimpleMatrix,
        k: Int,
        threshold: Double,
        preconditioner: SimpleMatrix? = null,
        maxLocalIters: Int = 200
): SimpleMatrix {
    val r_k = phi.size
    val r_kminus = psi.size
    //TODO: assemble this matrix while computing phi, instead of storing phi in an array-of-arrays
    val Ak = A.tt.cores[k]
    val phiMat = SimpleMatrix(Ak.cols*phi.size, phi.size)
    val R_k = Ak.cols
    for ((beta, phi_beta) in phi.withIndex()) {
        for((gamma, phi_beta_gamma) in phi_beta.withIndex()) {
            phiMat[beta*R_k, gamma] = phi_beta_gamma
        }
    }
    val R_kminus = Ak.rows
    val psiMat = SimpleMatrix(r_kminus, r_kminus* R_kminus)
    for((beta_minus, psi_beta_minus) in psi.withIndex()) {
        for((gamma_minus, psiCurr) in psi_beta_minus.withIndex()) {
            psiMat[beta_minus, gamma_minus*R_kminus] = psiCurr
        }
    }

    fun computeMatVec(y: SimpleMatrix): SimpleMatrix {
        val res = F.createLike()
        val n_k = A.modes[k]

        //Computation of Y'
        val YMat = SimpleMatrix(r_k,r_kminus* n_k)
        for(i in 0 until n_k) {
            for(gamma_minus in 0 until r_kminus) {
                YMat[0, i*r_kminus+gamma_minus] = y[i*r_kminus*r_k+gamma_minus*r_k..i*r_kminus*r_k+(gamma_minus+1)*r_k, 0..1]
            }
        }
        val YPrime = phiMat*YMat

        //Computation of Y''
        val YPrimeReshaped = SimpleMatrix(n_k *R_k, r_k*r_kminus)
        //TODO: use matrix extraction/insertion if possible instead of element-by-element copy (hopefully it will be more efficient)
        for(beta in 0 until r_k) {
            for(gamma_minus in 0 until r_kminus) {
                for(jk in 0 until n_k) {
                    for(idx in 0 until R_k) {
                        YPrimeReshaped[jk*R_k+idx, beta*r_kminus+gamma_minus] = YPrime[beta*R_k+idx, jk*r_kminus+gamma_minus]
                    }
                }
            }
        }
        val AkUnfolding = SimpleMatrix(n_k * R_kminus, n_k * Ak.cols)
        for (ik in 0 until n_k) {
            for(jk in 0 until n_k) {
                AkUnfolding[ik* R_kminus, jk*Ak.cols] = Ak[ik*n_k+jk]
            }
        }
        val YDoublePrime = AkUnfolding*YPrimeReshaped

        //Computation of the result
        val YDoublePrimeReshaped = SimpleMatrix(r_kminus*R_kminus, n_k*r_k)
        for(gamma_minus in 0 until r_kminus) {
            for(idx in 0 until R_kminus) {
                for(ik in 0 until n_k) {
                    for(beta in 0 until r_k) {
                        YDoublePrimeReshaped[gamma_minus*R_kminus+idx, ik*r_k+beta] =
                                YDoublePrime[ik*R_kminus+idx, beta*r_kminus+gamma_minus]
                    }
                }
            }
        }
        val resTemp = psiMat*YDoublePrimeReshaped
        //res is indexed like F, hint: "F[i*currCore.rows*currCore.cols+alphaMinus*currCore.cols+alpha] = elem[0]"
        for(i in 0 until n_k) {
            for(beta_minus in 0 until r_kminus) {
                for(beta in 0 until r_k) {
                    res[i*r_kminus*r_k+beta_minus*r_k+beta] = resTemp[beta_minus, i*r_k+beta]
                }
            }
        }

        return preconditioner?.mult(res) ?: res
    }

//    return BiCGStabL(2, ::computeMatVec, preconditioner?.mult(F) ?: F, maxLocalIters, w0, threshold).solution
    return biCGStab(::computeMatVec, preconditioner?.mult(F) ?: F, maxLocalIters, w0, threshold)
//    return ReGMRES(::computeMatVec, F, 10, w0, threshold)
}

fun DMRGSolve(
        A: TTSquareMatrix,
        f: TTVector,
        x0: TTVector = TTVector.ones(f.modes),
        absoluteResidualThreshold: Double,
        maxSweeps: Int,
        truncationRelativeThreshold: Double = 0.0,
        verbose: Boolean = false,
        maxLocalIters: Int = 200
): TTSolution {
    // Reference for the algorithm:
    // I. V. OSELEDETS AND S. V. DOLGOV - Solution of Linear Systems and Matrix Inversion in the TT-Format

    val x = x0.copy()
    for (i in x.tt.cores.size - 2 downTo 1) {
        x.tt.rightOrthogonalizeCore(i)
    }
    var resNorm = (A * x - f).norm()
    val sweepRange = //list of (core index: Int, forward: Bool)
            (0 until x.modes.size-1).toList().map { it to true } +
            (x.modes.size-1 downTo 1).toList().map { it to false }
    val psiCache = Array<Array<Array<SimpleMatrix>>?>(x.modes.size-1) {null}
    psiCache[0] = Array(1) { Array(1) { mat[r[1]] } }
    val phiCache = Array<Array<Array<SimpleMatrix>>?>(x.modes.size-1) {null}
    phiCache[phiCache.size-1]  = Array(1) {Array(1) { mat[r[1]]}}

    fun createSupercoredVector(leftCoreIdx: Int, orig: TensorTrain): TTVector {
        val leftCore = orig.cores[leftCoreIdx]
        val rightCore = orig.cores[leftCoreIdx+1]
        val superCore = CoreTensor(
                modeLength = leftCore.modeLength*rightCore.modeLength,
                rows = leftCore.rows, cols = rightCore.cols)
        for(i in 0 until leftCore.modeLength) {
            for(j in 0 until rightCore.modeLength) {
                superCore[i * rightCore.modeLength + j] = leftCore[i] * rightCore[j]
            }
        }
        val resCores = orig.cores.clone() as ArrayList<CoreTensor>
        resCores.removeAt(leftCoreIdx+1)
        resCores.removeAt(leftCoreIdx)
        resCores.add(leftCoreIdx, superCore)
        return TTVector(TensorTrain(resCores))
    }

    fun createSupercoredMatrix(leftCoreIdx: Int, orig: TTSquareMatrix): TTSquareMatrix {
        val leftCore = orig.tt.cores[leftCoreIdx]
        val rightCore = orig.tt.cores[leftCoreIdx+1]
        val superCore = CoreTensor(
                modeLength = leftCore.modeLength*rightCore.modeLength,
                rows = leftCore.rows, cols = rightCore.cols)
        val leftModeLength = orig.modes[leftCoreIdx]
        val rightModeLength = orig.modes[leftCoreIdx + 1]
        for(rowLeft in 0 until leftModeLength) {
            for(rowRight in 0 until rightModeLength) {
                for(colLeft in 0 until leftModeLength) {
                    for(colRight in 0 until rightModeLength) {
                        val rowIdx = rowLeft * rightModeLength+rowRight
                        val colIdx = colLeft * rightModeLength+colRight
                        superCore[rowIdx*leftModeLength*rightModeLength+colIdx] =
                                leftCore[rowLeft*leftModeLength+colLeft] * rightCore[rowRight*rightModeLength+colRight]
                    }
                }
            }
        }
        val resCores = orig.tt.cores.clone() as ArrayList<CoreTensor>
        resCores.removeAt(leftCoreIdx+1)
        resCores.removeAt(leftCoreIdx)
        resCores.add(leftCoreIdx, superCore)
        val resModes = orig.modes.toMutableList()
        resModes.removeAt(leftCoreIdx+1)
        resModes.removeAt(leftCoreIdx)
        resModes.add(leftCoreIdx, leftModeLength*rightModeLength)
        return TTSquareMatrix(TensorTrain(resCores), resModes.toTypedArray())
    }

    for(sweep in 0 until maxSweeps) {
        for ((k, forward) in sweepRange) {

            //TODO: precompute supercores for A and f
            if (forward) {
                val superX = createSupercoredVector(k, x.tt)
                val superF = createSupercoredVector(k, f.tt)
                val superA = createSupercoredMatrix(k, A)
                applyALSStep(superA, superX, superF, k, psiCache, phiCache, absoluteResidualThreshold, maxLocalIters)
                val optimizedCore = superX.tt.cores[k]
                val unfolding = SimpleMatrix(optimizedCore.rows * x.modes[k], optimizedCore.cols * x.modes[k + 1])
                for (i in 0 until x.modes[k]) {
                    for (j in 0 until x.modes[k + 1]) {
                        unfolding[i * optimizedCore.rows, j * optimizedCore.cols] = optimizedCore[i * x.modes[k + 1] + j]
                    }
                }
                val svd = unfolding.svd(true)
                val delta = unfolding.normF() * truncationRelativeThreshold
                val origSize = svd.singularValues.size
                var maxIdx = origSize - 1
                var sigma2Sum = 0.0
                val delta2 = delta * delta
                for (i in origSize-1 downTo 1) {
                    val sigma = svd.singularValues[i]
                    val sigma2 = sigma * sigma
                    if(sigma2Sum + sigma2 < delta2) {
                        maxIdx--
                        sigma2Sum += sigma2
                    } else break
                }
                maxIdx = max(0, maxIdx)
                val W = svd.w[0..maxIdx+1, 0..maxIdx+1]
                val U = svd.u[0..SimpleMatrix.END, 0..maxIdx+1]
                val SV = W * svd.v[0..SimpleMatrix.END, 0..maxIdx+1].T()
                val leftCore = x.tt.cores[k]
                for (i in 0 until leftCore.modeLength) {
                    leftCore[i] = U[i * optimizedCore.rows..(i + 1) * optimizedCore.rows, 0..U.numCols()]
                }
                leftCore.updateDimensions()
                val rightCore = x.tt.cores[k + 1]
                for (i in 0 until rightCore.modeLength) {
                    rightCore[i] = SV[0..SV.numRows(), i * optimizedCore.cols..(i + 1) * optimizedCore.cols]
                }
                rightCore.updateDimensions()
            } else {
                val superX = createSupercoredVector(k - 1, x.tt)
                val superF = createSupercoredVector(k - 1, f.tt)
                val superA = createSupercoredMatrix(k - 1, A)
                applyALSStep(superA, superX, superF, k - 1, psiCache, phiCache, absoluteResidualThreshold, maxLocalIters)
                val optimizedCore = superX.tt.cores[k - 1]
                val unfolding = SimpleMatrix(optimizedCore.rows * x.modes[k-1], optimizedCore.cols * x.modes[k])
                for (i in 0 until x.modes[k-1]) {
                    for (j in 0 until x.modes[k]) {
                        unfolding[i * optimizedCore.rows, j * optimizedCore.cols] = optimizedCore[i * x.modes[k] + j]
                    }
                }
                val svd = unfolding.svd(true)
                //TODO: truncation based on the local residual instead of the core's relative error
                val delta = unfolding.normF() * truncationRelativeThreshold
                val origSize = svd.singularValues.size
                var maxIdx = origSize - 1
                var sigma2Sum = 0.0
                val delta2 = delta * delta
                for (i in origSize-1 downTo 1) {
                    val sigma = svd.singularValues[i]
                    val sigma2 = sigma * sigma
                    if(sigma2Sum + sigma2 < delta2) {
                        maxIdx--
                        sigma2Sum += sigma2
                    } else break
                }
                maxIdx = max(0, maxIdx)
                val W = svd.w[0..maxIdx+1, 0..maxIdx+1]
                val US = svd.u[0..SimpleMatrix.END, 0..maxIdx+1] * W
                val V = svd.v[0..SimpleMatrix.END, 0..maxIdx+1].T()
                val leftCore = x.tt.cores[k - 1]
                for (i in 0 until leftCore.modeLength) {
                    leftCore[i] = US[i * optimizedCore.rows..(i + 1) * optimizedCore.rows, 0..US.numCols()]
                }
                leftCore.updateDimensions()
                val rightCore = x.tt.cores[k]
                for (i in 0 until rightCore.modeLength) {
                    rightCore[i] = V[0..V.numRows(), i * optimizedCore.cols..(i + 1) * optimizedCore.cols]
                }
                rightCore.updateDimensions()
            }
        }

        resNorm = (f - A * x).norm()
        if(verbose) println("DMRG sweep $sweep: resnorm=$resNorm maxrank=${x.ttRanks().max()}")
        if(resNorm <= absoluteResidualThreshold) break
    }
    return TTSolution(x, resNorm)
}

fun DMRGInvert(
        A: TTSquareMatrix,
        maxSweeps: Int,
        initialGuess: TTVector? = null,
        verbose: Boolean = false,
        truncationRelativeThreshold: Double,
        preconditioner: TTSquareMatrix? = null,
        maxLocalIters: Int = 200
): TTSquareMatrix {
    val extendedCores = arrayListOf<CoreTensor>()
    for ((coreIdx, origCore) in A.tt.cores.withIndex()) {
        val extendedCore = CoreTensor(origCore.modeLength * origCore.modeLength, origCore.rows, origCore.cols)
        val m = A.modes[coreIdx]
        for (r in 0 until m) {
            for(c in 0 until m) {
                val matrix = origCore[r, c]
                for(ext in 0 until m) {
                    extendedCore[ext*m+r, ext*m+c] = matrix
                }
            }
        }
        extendedCores.add(extendedCore)
    }
    val AExtended = TTSquareMatrix(TensorTrain(extendedCores), A.modes.map { it*it }.toTypedArray())
    val eye = tteye(A.modes)
    val IVec = TTVector((preconditioner?.times(eye) ?: eye).T().tt)
    val ranks = arrayOf(1, *(Array(IVec.modes.size-1) { 1 }), 1)
    val x0 = initialGuess ?: TTVector.rand(IVec.modes, ranks, random = Random(10))
    if(verbose) println("dmrginvert: initresnorm=${(AExtended * x0 - IVec).norm()}")
    val AInvVec = DMRGSolve(
            A = AExtended,
            f = IVec,
            x0 = x0,
            absoluteResidualThreshold = 1e-16 * IVec.norm(),
            maxSweeps = maxSweeps,
            verbose = verbose,
            truncationRelativeThreshold = truncationRelativeThreshold,
            maxLocalIters = maxLocalIters
    ).solution
    return TTSquareMatrix(AInvVec.tt, A.modes).T()
}

fun AMEn(A: TTSquareMatrix, f: TTVector, x0: TTVector = TTVector.ones(f.modes), residualThreshold: Double, maxSweeps: Int): TTVector {
    val x = x0.copy()
    for (i in x.tt.cores.size - 2 downTo 1) {
        x.tt.rightOrthogonalizeCore(i)
    }

    //sweep through the cores forward and backward
    val sweepRange = //list of (core index: Int, forward: Bool)
            (0 until x.modes.size-1).toList().map { it to true } +
            (x.modes.size-1 downTo 1).toList().map { it to false }
    val psiCache = Array<Array<Array<SimpleMatrix>>?>(x.modes.size) {null}
    psiCache[0] = Array(1) { Array(1) { mat[r[1]] } }
    val phiCache = Array<Array<Array<SimpleMatrix>>?>(x.modes.size) {null}
    phiCache[phiCache.size-1] = Array(1) {Array(1) { mat[r[1]]}}
    for (sweep in 0..maxSweeps) {
        for ((k, forward) in sweepRange) {

            applyALSStep(A, x, f, k, psiCache, phiCache, residualThreshold)

            TODO("Basis enrichment")

            
            
            if (forward) { //Left orthogonalization
                x.tt.leftOrthogonalizeCore(k)
            } else { //Right orthogonalization
                x.tt.rightOrthogonalizeCore(k)
            }
        }
        val resNorm = (f - A * x).norm()
        if(resNorm <= residualThreshold) return x
    }
    return x
}

