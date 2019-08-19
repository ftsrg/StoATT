package solver

import org.ejml.data.SingularMatrixException
import org.ejml.simple.SimpleMatrix
import kotlin.math.abs
import kotlin.math.sqrt

//TODO: do something with non-definite matrices
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
        Vinv.tt.round(roundingAccuracy)
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
        println(X.tt.cores.map { "${it.rows}*${it.cols}" })
        X.tt.round(roundingAccuracy)
    }
    return X
}

fun jacobiPreconditioner(A: TTSquareMatrix, zeroMaskVector: TTVector): TTSquareMatrix {
    val diag = A.diagVect()
    val inv = NSInvertVect(diag, 0.001 * diag.norm(), 0.001, zeroMaskVector)
    return TTSquareMatrix.diag(inv)
}

fun TTJacobi(A: TTSquareMatrix, b: TTVector, thresh: Double, roundingAccuracy: Double, zeroMaskVector: TTVector = TTVector.ones(A.modes), log: Boolean = false): TTVector {
    for ((idx, mode) in A.modes.withIndex()) {
        assert(mode == b.modes[idx]) { "The modes of A and b must be identical!" }
    }
    val D = A.diagVect()
    val Dinv = TTSquareMatrix.diag(
            NSInvertVect(D, 0.00001 * D.tt.frobenius(), 0.0001, zeroMaskVector)
    )
    val R = A - A.diag()
    var x = TTVector.zeros(A.modes)
    var residual: TTVector
    do {
        x = Dinv * (b - R * x)
        if (log) {
            println("Exact:")
            x.printElements()
            println()
        }
        x.tt.round(roundingAccuracy)
        if (log) {
            println("Rounded:")
            x.printElements()
            println()
        }
        residual = b - A * x
    } while (residual.tt.frobenius() > thresh)
    if (log) {
        println()
        println("Final residual:")
        residual.printElements()
    }
    return x
}

fun DMRGInvert(A: TTSquareMatrix): TTSquareMatrix {
    TODO()
}

fun TTGMRES(A: TTSquareMatrix, b: TTVector, x0: TTVector, eps: Double, maxIter: Int = 100): TTVector {
    // Reference for the algorithm:
    // S. V. DOLGOV - TT-GMRES: on solution to a linear system in the structured tensor format
    val res0 = b - A * x0
    val R = arrayListOf(res0.norm())
    val beta = R[0]
    val V = arrayListOf(res0 / beta)
    val h = hashMapOf<Pair<Int, Int>, Double>() //TODO: vmi értelmesebb ehelyett
    lateinit var y: SimpleMatrix
    var r: Double
    var x = x0.copy()
    for (j in 1..maxIter) {
        val delta = eps / R[j - 1]
        var w = A * V[j - 1]
        w.tt.round(delta)
        for (i in 0 until j) {
            val t = w * V[i]
            h[Pair(i, j - 1)] = t
            w = w - V[i] * t
        }
        w.tt.round(delta)
        val norm = w.norm()
        h[Pair(j, j - 1)] = norm
        V.add(w / norm)
        val H = SimpleMatrix(j + 1, j)
        H.fill { i, k -> h.getOrDefault(Pair(i, k), 0.0) } //TODO: waste of time, the data should already be written into the H matrix instead of the dictionary

        val solv = solveWithRots(H, beta) //TODO: use solution from prev
        r = solv.resNorm
        y = solv.solution

        R.add(r)
        if (r / b.norm() < eps)
            break
    }
    for (i in 0 until y.numElements) {
        println("Real res: ${(A * x - b).norm()}, computed res: ${R[i]}")
        x = x + y[i] * V[i]
    }
    return x
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
    val residualNorm = Math.abs(g[g.numElements - 1])
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

fun ALSSolve(A: TTSquareMatrix, f: TTVector, x0: TTVector = TTVector.zeros(f.modes), residualThreshold: Double): TTVector {
    // Reference for the algorithm:
    // I. V. OSELEDETS AND S. V. DOLGOV - Solution of Linear Systems and Matrix Inversion in the TT-Format

    val x = x0.copy()
    //TODO: pre-orthogonalization

    //sweep through the cores forward and backward
    val sweepRange = //list of (core index: Int, forward: Bool)
            (0 until x.modes.size-1).toList().map { it to true } +
            (x.modes.size downTo 1).toList().map { it to false }
    for ((k, forward) in sweepRange) {

        val currCore = x.tt.cores[k]
        //TODO: parallel computation of elements
        //TODO: dynamic computation of psi and phi (reuse previous results)

        //region Computation of psi
        val psi = Array(currCore.rows) {
            Array(currCore.rows) { ones(1, currCore.rows) }
        }
        val psiPrev = Array(currCore.rows) {
            Array(currCore.rows) { ones(1, currCore.rows) }
        }
        for (kk in 0 until k - 1) {
            //TODO: optimizations
            for(beta in 0 until psiPrev.size)
                for(gamma in 0 until psiPrev[beta].size)
                    psiPrev[beta][gamma] = psi[beta][gamma]

            for(beta in 0 until psi.size) {
                for(gamma in 0 until psi[beta].size) {
                    psi[beta][gamma].fill(0.0)

                    for(betaPrev in 0 until psiPrev.size) {
                        for(gammaPrev in 0 until psiPrev[beta].size) {
                            val ACore = A.tt.cores[kk]
                            val xCore = x.tt.cores[kk]
                            for(i in 0 until A.modes[kk]) {
                                val xCore_i = xCore[i]
                                for(j in 0 until A.modes[kk]) {
                                    psi[beta][gamma] += psiPrev[betaPrev][gammaPrev] *
                                                        ACore[i*ACore.modeLength+j] *
                                                        xCore_i[betaPrev,beta] *
                                                        xCore[j][gammaPrev,gamma]
                                }
                            }
                        }
                    }
                }
            }
        }
        //endregion

        //region Computation of phi
        val phi = Array(currCore.cols) {
            Array(currCore.cols) { ones(currCore.cols, 1) }
        }
        val phiPrev = Array(currCore.cols) {
            Array(currCore.cols) { ones(currCore.cols, 1) }
        }
        for(kk in k until x.modes.size) {
            //TODO: optimizations
            for(beta in 0 until phiPrev.size) {
                for(gamma in 0 until phiPrev[beta].size) {
                    phiPrev[beta][gamma] = phi[beta][gamma]
                }
            }

            for(beta in 0 until phi.size) {
                for(gamma in 0 until phi[beta].size) {
                    phi[beta][gamma].fill(0.0)

                    for(betaPrev in 0 until phiPrev.size) {
                        for(gammaPrev in 0 until phiPrev[beta].size) {
                            val ACore = A.tt.cores[kk]
                            val xCore = x.tt.cores[kk]
                            for(i in 0 until A.modes[kk]) {
                                val xCore_i = xCore[i]
                                for(j in 0 until A.modes[kk]) {
                                    phi[beta][gamma] += phiPrev[betaPrev][gammaPrev] *
                                                        ACore[i*ACore.modeLength+j] *
                                                        xCore_i[betaPrev,beta] *
                                                        xCore[j][gammaPrev,gamma]
                                }
                            }
                        }
                    }
                }
            }
        }
        //endregion

        //Local solution

        //region Computation of local right-hand side
        //assemble full F=[ Q^T*f^_k(0); Q^T*f^_k(1) ... Q^T*f^_k(n_k) ]  matrix
        var leftPart = eye(1)
        for (l in 0 until k-1) {
            var currFactor = SimpleMatrix(x.tt.cores[l].rows*f.tt.cores[l].rows, x.tt.cores[l].cols*f.tt.cores[l].cols)
            for(m in 0 until x.modes[l])
                currFactor += x.tt.cores[l][m].kron(f.tt.cores[l][m])
            leftPart *= currFactor
        }
        var rightPart = eye(1)
        for(l in x.modes.size-1 downTo k) {
            var currFactor = SimpleMatrix(x.tt.cores[l].rows*f.tt.cores[l].rows, x.tt.cores[l].cols*f.tt.cores[l].cols)
            for(m in 0 until x.modes[l])
                currFactor += x.tt.cores[l][m].kron(f.tt.cores[l][m])
            rightPart = currFactor * rightPart
        }
        val F = SimpleMatrix(currCore.modeLength*currCore.rows*currCore.cols, 1)
        val middleAux = SimpleMatrix(currCore.rows, currCore.cols)
        //TODO: can this be done more efficiently by computing a product leftpart*(something_that_gives_the_whole_result)*rightpart?
        for(i in 0 until currCore.modeLength) {
            for(alphaMinus in 0 until currCore.rows) {
                for(alpha in 0 until currCore.cols) {
                    middleAux[alphaMinus, alpha] = 1.0
                    //TODO: check indexing!!!
                    val elem = leftPart*middleAux.kron(f.tt.cores[k][i]) * rightPart
                    assert(elem.numElements == 1)
                    F[i*currCore.rows*currCore.cols+alphaMinus*currCore.cols+alpha] = elem[0]
                    middleAux[alphaMinus, alpha] = 0.0
                }
            }
        }
        //endregion

        //TODO: refine this criterion; the current threshold is taken from the original article, but maybe an adaptive/adjustable one would be better
        val solveDirectly = currCore.modeLength*currCore.modeLength*currCore.cols*currCore.rows < 1000
        lateinit var w: SimpleMatrix
        if(solveDirectly) {
            val dim = currCore.modeLength * currCore.rows * currCore.cols
            val FullB = SimpleMatrix(dim, dim)
            //TODO: Parallel computation!!!!!
            //TODO: check indexing!!
            for(betaMinus in 0 until currCore.rows){
                for(beta in 0 until currCore.cols) {
                    for(gammaMinus in 0 until currCore.rows) {
                        for(gamma in 0 until currCore.cols) {
                            for(i in 0 until currCore.modeLength) {
                                for(j in 0 until currCore.modeLength) {
                                    FullB[i*currCore.rows*currCore.cols+betaMinus*currCore.cols+beta, j*currCore.rows*currCore.cols+gammaMinus*currCore.cols+gamma] =
                                            psi[betaMinus][gammaMinus]*currCore[i*currCore.modeLength+j]*phi[beta][gamma]
                                }
                            }
                        }
                    }
                }
            }

            //solve Bw=F
            try {
                w = FullB.solve(F)
            } catch (e: SingularMatrixException) {
                w = FullB.pseudoInverse() * F
            }
        } else {
            val w0 = F.createLike()
            //TODO: use current iterate of the core instead of a zero vector as the initial solution
            w = ALSLocalReGMRES(psi, phi, A, w0, F, k, residualThreshold*0.1)
        }
        for (i in 0 until currCore.modeLength) {
            for(beta_minus in 0 until currCore.rows) {
                for (beta in 0 until currCore.cols) {
                    currCore[i][beta_minus, beta] = w[i*currCore.rows*currCore.cols+beta_minus*currCore.cols+beta]
                }
            }
        }

        //TODO: extract the orthogonalizations into methods in either TT or CoreTensor
        if(forward) { //Left orthogonalization
            val leftUnfolding = SimpleMatrix(currCore.modeLength*currCore.rows, currCore.cols)
            for((i, mat) in currCore.data.withIndex()) {
                leftUnfolding[i*currCore.rows, 0] = mat
            }
            val QR = leftUnfolding.qr()
            val Q = QR.Q
            val R = QR.R
            val nextCore = x.tt.cores[k + 1]
            for ((i, mat) in nextCore.data.withIndex()) {
                nextCore.data[i] = R * mat
            }
            nextCore.rows = nextCore.data[0].numRows()
            nextCore.cols = nextCore.data[0].numCols()
            for(i in 0 until currCore.modeLength) {
                currCore.data[i] = Q[i*currCore.rows..(i+1)*currCore.rows, 0..Q.numCols()]
            }
            currCore.rows = currCore.data[0].numRows()
            currCore.cols = currCore.data[0].numCols()
        } else { //Right orthogonalization
            val rightUnfolding = SimpleMatrix(currCore.rows, currCore.modeLength*currCore.cols)
            for((i, mat) in currCore.data.withIndex()) {
                rightUnfolding[0, i*currCore.cols] = mat
            }
            val RQ_T = rightUnfolding.T().qr()
            val R = RQ_T.R.T()
            val Q = RQ_T.R.T()
            val prevCore = x.tt.cores[k-1]
            for ((i, mat) in prevCore.data.withIndex()) {
                prevCore.data[i] *= R
            }
            prevCore.rows = prevCore.data[0].numRows()
            prevCore.cols = prevCore.data[0].numCols()
            for (i in 0 until currCore.modeLength) {
                currCore.data[i] = Q[0..Q.numRows(), i*currCore.cols..(i+1)*currCore.cols]
            }
            currCore.rows = currCore.data[0].numRows()
            currCore.cols = currCore.data[0].numCols()
        }
    }
    TODO()
    return x
}

private fun ALSLocalReGMRES(psi: Array<Array<SimpleMatrix>>, phi: Array<Array<SimpleMatrix>>, A: TTSquareMatrix, w0: SimpleMatrix, F: SimpleMatrix, k: Int, threshold: Double): SimpleMatrix {
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
                //TODO: check index ranges
                YMat[0, i*r_kminus+gamma_minus] = y[i*r_kminus*r_k+gamma_minus*r_k..i*r_kminus*r_k+(gamma_minus+1)*r_k, 0..0]
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

        return res
    }

    return ReGMRES(::computeMatVec, F, 5, w0, threshold)
}

fun AMEn(A: TTSquareMatrix, b: TTVector): TTVector {
    TODO()
}

