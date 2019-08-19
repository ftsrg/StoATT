
import faulttree.galileoParser
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

fun main(args: Array<String>) {

    val galTest =
            """
                toplevel MyTree;
                MyTree or EAndD AAndFAndBOrC TriAnd;

                EventA lambda=.2;
                EventB lambda=.1;
                EventC lambda=0.1;
                EventD lambda=.3;
                EventE lambda=.423;
                EventF lambda=0.5;

                EAndD and EventE EventD;
                /* (((a and f) and (b or c)) or (b and c and e)) */
                AAndFAndBOrC and EventA EventF BOrC;
                BOrC or EventB EventC;
                TriAnd and EventB EventC EventE;
            """.trimIndent()

    val Ft = galileoParser.parse(galTest.byteInputStream())
    val rateMtx = Ft.getTransientGenerator()

    println("GMRES without preconditioner:")
    val stateMaskVector = Ft.getStateMaskVector()
    rateMtx.tt.round(0.0001)
    stateMaskVector.tt.round(0.0001)
    val res = TTGMRES(rateMtx, stateMaskVector, TTVector.zeros(stateMaskVector.modes), 0.0001)
    res.printElements()
    println()
    println()

    println("GMRES with preconditioner:")
    val prec = jacobiPreconditioner(rateMtx, stateMaskVector)
    val precdMtx = prec * rateMtx
    precdMtx.tt.round(0.0001)
    val precdVect = prec*stateMaskVector
    precdVect.tt.round(0.0001)
    val resPrecd = TTGMRES(precdMtx, precdVect, TTVector.zeros(stateMaskVector.modes), 0.0001)
    resPrecd.printElements()
    println()
    println()

    println("TT-Jacobi: ")
    val res2 = TTJacobi(rateMtx, stateMaskVector, 0.001 * stateMaskVector.norm(), 0.001, stateMaskVector)
    res2.printElements()

    println()
    println("MTFF = ${-res2[0]}")
    println()
}

private fun ttTest() {
    val M = TTSquareMatrix.rand(arrayOf(10, 10, 10, 10), arrayOf(10, 10, 10, 10), max = 2.0)
    val originalNorm = M.tt.frobenius()
    println(originalNorm)
    val Mcpy = M.copy()
    M.tt.round(0.01)
    println(M.tt.frobenius())
    println(M.tt.cores.map { it.rows })
    val errorNorm = (M.tt - Mcpy.tt).frobenius()
    println(errorNorm)
    println(errorNorm / originalNorm)
}


fun GMRESTest(size: Int = 6): Double {
    val A = randSquareMtx(size)
    val b = A * ones(size)
    val res = GMRES({x->A*x}, b, size).solution
    return (b - A * res).vecNorm2()
}

fun reGMRESTest(size: Int=6): Double {
    val A = randSquareMtx(size)
    val b = A * ones(size)
    val res = ReGMRES({ x->A*x}, b, 5, threshold = 0.1)
    return (b - A * res).vecNorm2()
}


/*
//TODO: x0 default better
fun TT_GMRES(A: solver.TTSquareMatrix, b: solver.TTVector, m: Int,
          x0: solver.TTVector = solver.TTVector.solver.ones(b.tt.cores.map { it.modeLength }.toTypedArray())): SolverResult {
    val r0 = b - A * x0
    val beta = r0.tt.frobenius() //TODO: verify norm
    var V = SimpleMatrix(b.numElements, m)
    V[0, 0] = r0 * (1.0 / beta)
    var H = SimpleMatrix(m + 1, m)
    for (j in 0 until m) {
        var w = A * V.solver.col(j)
        for (i in 0..j) {
            val vi = V.solver.col(i)
            H[i, j] = w.solver.T() * vi
            w -= H[i, j] * vi
        }
        H[j + 1, j] = w.tt.frobenius()
        if (H[j + 1, j].solver.nearZero()) {
            H = H[0..j + 1, 0..j]
            V = V[0..V.numRows(), 0..j]
            break
        }
        if (j < m - 1)
            V[0, j + 1] = w * (1.0/H[j + 1, j])
    }
    val g = beta * solver.eye(H.numRows()).solver.col(0)
    for (i in 0 until H.numCols()) {
        val rowNext = H[i + 1, i]
        val rowCurr = H[i, i]
        val denom = sqrt(rowCurr * rowCurr + rowNext * rowNext)
        val s = rowNext / denom
        val c = rowCurr / denom
        val r1 = H.solver.row(i)
        val r2 = H.solver.row(i + 1)
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
*/

fun householder(X: SimpleMatrix): SimpleMatrix {
    var X = X
    var P = eye(X.numCols())
    for (k in 0 until X.numCols()) {
        val x = X.col(k)
        x[0 until k] = 0.0
        val alpha = sign(x[k]) * x.vecNorm2()
        x[k] += alpha
        val w = x / x.vecNorm2()
        X -= w * w.T() * X * 2.0
    }
    return X
}

val rand = Random()
fun randSquareMtx(size: Int): SimpleMatrix {
    return SimpleMatrix.random_DDRM(size, size, 0.0, 10.0, rand)
}

fun SimpleMatrix.roundZeros(threshold: Double = 1E-14) {
    for (i in 0 until numRows()) {
        for (j in 0 until numCols()) {
            if (abs(this[i, j]) < threshold) this[i, j] = 0.0
        }
    }
}

fun householderArnoldi(A: SimpleMatrix, v: SimpleMatrix, m: Int) {
    assert(v.numCols() == 1) { "The second argument must be a column vector!" }
    assert(A.numCols() == v.numRows()) { "Matrix and vector dimensions don't matcg!" }
    val V = SimpleMatrix(v.numRows(), m)
    val z = SimpleMatrix(v)
    V[0, 0] = v
    for (j in 0 until m + 1) {
        val w = SimpleMatrix(z)
        w[0 until j] = 0.0
        val alpha = sign(w[j]) * w.vecNorm2()
        w[j] += alpha
        w[0, 0] = w / w.vecNorm2()
        val P = eye(v.numRows()) - 2.0 * w * w.T()
        val h = P * z
        TODO("calc v")
    }
}