import org.ejml.simple.SimpleMatrix
import kotlin.math.sqrt

/**
 * Inverts the elements of a TT-Vector using the Newton-Schulz iterative algorithm
 * @param V Vector to invert in TensorTrain format
 * @param thresh Threshold of the residual's Frobenius norm used for convergence check
 * @return Element-wise inverse of the input in TensorTrain format
 */
fun NSInvertVect(V: TTVector, thresh: Double, roundingAccuracy: Double): TTVector {
    val ones = TTVector.ones(V.modes)
    var Vinv = ones * (1.0/V.tt.frobenius())
    do {
        val residual = ones - TTVector(V.tt.hadamard(Vinv.tt))
        Vinv = Vinv + TTVector(Vinv.tt.hadamard(residual.tt))
        Vinv.tt.round(roundingAccuracy)
    } while (residual.tt.frobenius() > thresh)
    return Vinv
}

fun TTJacobi(A: TTSquareMatrix, b: TTVector, thresh: Double, roundingAccuracy: Double, log: Boolean = false): TTVector{
    for ((idx, mode) in A.modes.withIndex()) {
        assert(mode == b.modes[idx]) { "The modes of A and b must be identical!" }
    }
    val D = A.diagVect()
    val Dinv = TTSquareMatrix.diag(
            NSInvertVect(D, 0.00001 * D.tt.frobenius(), 0.0001)
    )
    val R = A - A.diag()
    var x = TTVector.zeros(A.modes)
    var residual: TTVector
    do {
        x = Dinv * (b - R*x)
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
        residual = b - A*x
    } while (residual.tt.frobenius() > thresh)
    if (log) {
        println()
        println("Final residual:")
        residual.printElements()
    }
    return x
}

fun TTGMRES(A: TTSquareMatrix, b: TTVector, x0: TTVector, eps: Double): TTVector {
    val R = arrayListOf(A*x0-b)
    val beta = R[0].tt.frobenius()
    val V = arrayListOf(R[0]/beta)
    val m = 100
    val h = hashMapOf<Pair<Int,Int>, Double>() //TODO: vmi értelmesebb ehelyett
    lateinit var y: SimpleMatrix
    var r: Double
    var x = x0
    for (j in 1..m) {
        val delta = eps / R[j-1].tt.frobenius()
        var w = A * V[j-1]
        w.tt.round(delta)
        for(i in 1..j){
            val t = w * V[i]
            h[Pair(i, j)] = t
            w = w - V[i]*t
        }
        w.tt.round(delta)
        h[Pair(j+1, j)]
        val H = SimpleMatrix(j+1, j)
        H.fill { i, k -> h.getOrDefault(Pair(i,k), 0.0) } //TODO: pazarlás

        val solv = solveWithRots(H, beta)
        r = solv.resNorm
        y = solv.solution

        if(r / b.tt.frobenius() < eps) break
    }
    for (i in 0 until y.numElements) {
        x = x+y[i]*V[i]
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
    val residualNorm = H[H.numElements - 1]
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

fun AMEn(A: TTSquareMatrix, b: TTVector): TTVector {
    TODO()
}

fun newtonSchulz(A: TTSquareMatrix): TTSquareMatrix {
    TODO()
}