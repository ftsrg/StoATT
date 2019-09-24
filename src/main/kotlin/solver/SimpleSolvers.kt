package solver

import org.ejml.simple.SimpleMatrix
import kotlin.math.sqrt

data class SolverResult(val solution: SimpleMatrix, val residualNorm: Double)

fun ReGMRES(linearMap: (SimpleMatrix) -> SimpleMatrix, b: SimpleMatrix, m: Int,
            x0: SimpleMatrix = SimpleMatrix(b.numRows(), 1), threshold: Double): SimpleMatrix {
    var res = x0
    do {
        val iterResult = GMRES(linearMap, b, m, res)
        res = iterResult.solution
        val realResidualNorm = (linearMap(res)-b).normF()
    } while (iterResult.residualNorm > threshold || (linearMap(res)-b).normF() > threshold)
    return res
}

fun biCGStab(linearMap: (SimpleMatrix) -> SimpleMatrix, b: SimpleMatrix, m: Int,
             x0: SimpleMatrix = ones(b.numElements), threshold: Double): SimpleMatrix {
    var result = x0.copy()
    var r = b - linearMap(x0);
    val rstar0 = ones(r.numElements)
    var p = r
    var u = r
    for (j in 0 until m) {
        val alpha = r.scalarProduct(rstar0) / linearMap(p).scalarProduct(rstar0)
        val q = u - alpha * linearMap(p)
        val update = alpha * (u + q)
        result += update
        val temp = r.scalarProduct(rstar0) // TODO: keep from prev iter
        r -= linearMap(update)
        val beta = r.scalarProduct(rstar0) / temp
        u = r + beta * q
        p = u + beta * (q+beta*p)
        if(r.normF() < threshold)
            return result
    }
    return result
}

fun GMRES(linearMap: (SimpleMatrix)->SimpleMatrix, b: SimpleMatrix, m: Int,
          x0: SimpleMatrix = SimpleMatrix(b.numRows(), 1)): SolverResult {
    val r0 = b - linearMap(x0)
    val beta = r0.vecNorm2()
    var V = SimpleMatrix(b.numRows(), m)
    V[0, 0] = r0 / beta
    var H = SimpleMatrix(m + 1, m)
    for (j in 0 until m) {
        var w = linearMap(V.col(j))
        for (i in 0..j) {
            val vi = V.col(i)
            H[i, j] = w.T() * vi
            w -= H[i, j] * vi
        }
        H[j + 1, j] = w.vecNorm2()
        if (H[j + 1, j].nearZero(10E-14)) {
            H = H[0..j + 1, 0..j]
            V = V[0..V.numRows(), 0..j]
            break
        }
        if (j < m - 1)
            V[0, j + 1] = w / H[j + 1, j]
    }
    val g = beta * eye(H.numRows()).col(0)
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
    val residualNorm = H[H.numElements-1]
    H = H[0..H.numRows() - 1, 0..H.numCols()]
    val y = g[0..g.numElements - 1, 0..1]
    for (i in y.numElements - 1 downTo 0) {
        for (j in y.numElements - 1 downTo i + 1) {
            y[i] -= H[i, j] * y[j]
        }
        y[i] /= H[i, i]
    }
    return SolverResult(x0 + V * y, residualNorm)
}
