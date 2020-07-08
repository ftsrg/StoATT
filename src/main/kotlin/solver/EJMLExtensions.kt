package solver

import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.simple.SimpleMatrix
import java.util.*
import kotlin.math.*

operator fun SimpleMatrix.times(other: SimpleMatrix) = this.mult(other)
operator fun SimpleMatrix.times(scalar: Double) = this.scale(scalar)
operator fun Double.times(matrix: SimpleMatrix) = matrix.scale(this)
operator fun SimpleMatrix.div(scalar: Double) = this.divide(scalar)
operator fun SimpleMatrix.get(rows: IntRange, cols: IntRange) = this.extractMatrix(rows.first, rows.last, cols.first, cols.last)
operator fun SimpleMatrix.set(rows: IntRange, cols: IntRange, value: Double) {
    for (r in rows)
        for (c in cols)
            this.set(r, c, value)
}

operator fun SimpleMatrix.set(range: IntRange, value: Double) {
    for (i in range) this.set(i, value)
}

operator fun SimpleMatrix.unaryMinus() = this.negative()
operator fun SimpleMatrix.set(row: Int, col: Int, value: SimpleMatrix) =
        this.insertIntoThis(row, col, value)

fun SimpleMatrix.T() = this.transpose()
fun SimpleMatrix.row(idx: Int) = this.rows(idx, idx + 1)
fun SimpleMatrix.col(idx: Int) = this.cols(idx, idx + 1)

data class QR(val Q: SimpleMatrix, val R: SimpleMatrix)

fun SimpleMatrix.qr(): QR {
    val dec = DecompositionFactory_DDRM.qr()
    dec.decompose(this.ddrm)
    return QR(
            Q = SimpleMatrix(dec.getQ(null, true)),
            R = SimpleMatrix(dec.getR(null, true))
    )
}

fun SimpleMatrix.vecNorm2(): Double {
    return sqrt(when {
        this.numRows() == 1 -> (this * this.T())[0, 0]
        this.numCols() == 1 -> (this.T() * this)[0, 0]
        else -> throw Exception("The matrix should contain only one column or one row")
    })
}

fun SimpleMatrix.fill(valFunc: (Int, Int) -> Double) {
    for (r in 0 until this.numRows()) {
        for (c in 0 until this.numCols()) {
            this[r, c] = valFunc(r, c)
        }
    }
}

fun SimpleMatrix.scalarProduct(other: SimpleMatrix): Double {
    if ((this.numRows() > 1 && this.numCols() > 1) || (other.numRows() > 1 && other.numCols() > 1))
        throw UnsupportedOperationException("Scalar product only available for vectors!"
        )
    return (this.T() * other)[0]
}

object mat {
    operator fun get(vararg rows: DoubleArray) = SimpleMatrix(rows)
    operator fun get(vararg mats: SimpleMatrix) =
            mats.reduce { acc, matrix -> acc.combine(0, acc.numCols(), matrix) }
}

object r {
    operator fun get(vararg elements: Number) = elements.map { it.toDouble() }.toDoubleArray()
}

fun eye(size: Int) = SimpleMatrix.identity(size)
fun diag(vararg elements: Double) = SimpleMatrix.diag(*elements)
fun ones(size: Int) = SimpleMatrix(size, 1).apply { fill(1.0) }
fun ones(rows: Int, cols: Int) = SimpleMatrix(rows, cols).apply { fill(1.0) }

fun exp2by2(A: SimpleMatrix): SimpleMatrix {
    val a = A[0, 0]
    val b = A[0, 1]
    val c = A[1, 0]
    val d = A[1, 1]
    val delta = sqrt((a - d) * (a - d) + 4 * b * c)

    if (delta == 0.0) {
        return exp((a + d) / 2) * mat[
                r[1 + (a - d) / 2, b],
                r[c, 1 - (a - d) / 2]
        ]
    }

    val deltaHalf = delta / 2
    val exp = exp((a + d) / 2)
    val expPlus = exp((a + d) / 2 + deltaHalf)
    val expMinus = exp((a + d) / 2 - deltaHalf)
    val expsinh = (expPlus - expMinus) / 2
    val expcosh = (expPlus + expMinus) / 2
    val sinh = sinh(deltaHalf)
    val cosh = cosh(deltaHalf)
//    val m11 = exp * (delta * cosh + (a - d) * sinh)
    val m11 = delta * expcosh + (a - d) * expsinh
    val m12 = 2 * b * expsinh
    val m21 = 2 * c * expsinh
//    val m22 = exp * (delta * cosh + (d - a) * sinh)
    val m22 = delta * expcosh + (d - a) * expsinh
    return mat[
            r[m11, m12],
            r[m21, m22]
    ] / delta
}

fun SimpleMatrix.kronSum(other: SimpleMatrix): SimpleMatrix {
    return this.kron(eye(other.numRows())) + eye(this.numRows()).kron(other)
}

data class SVD(val U: SimpleMatrix, val S: SimpleMatrix, val V: SimpleMatrix)

fun SimpleMatrix.truncatedSVDByIterativeEigen(threshold: Double): SVD {
    val thresh2 = threshold * threshold

    var U = SimpleMatrix(0, 0)
    var V = SimpleMatrix(0, 0)
    val s = arrayListOf<Double>()

    var Right = this.T() * this
    var sumOfRemaining = Right.trace()

    while (sumOfRemaining > thresh2) {
//        val rightEigen = powerIter(Right, min(thresh2, 1e-15))
        val rightEigen = restartedLanczos(Right, min(thresh2, 1e-15), innerIters = 10)
        V = V.concatColumns(rightEigen.vector)
        val singularValue = sqrt(rightEigen.value)
        s.add(singularValue)
        U = U.concatColumns(this * rightEigen.vector / singularValue)
        // deflation
        Right -= rightEigen.value * rightEigen.vector * rightEigen.vector.T()

        sumOfRemaining -= rightEigen.value
    }

    return SVD(U, SimpleMatrix.diag(*s.toDoubleArray()), V)
}

fun SimpleMatrix.correctedTruncatedSVDByIterativeEigen(threshold: Double, correctionThreshold: Double = 1e-12): SVD {
    val thresh2 = threshold * threshold

    var U = SimpleMatrix(0, 0)
    var V = SimpleMatrix(0, 0)
    val s = arrayListOf<Double>()

    var Right = this.T() * this
    var sumOfRemaining = Right.trace()

    mainLoop@ while (sumOfRemaining > thresh2) {
        val rightEigen = powerIter(Right)
//        val rightEigen = restartedLanczos(Right, min(thresh2, 1e-15), innerIters = 10)
        for(i in 0..V.numCols()) {
            val v = V.col(i)
            val prod = v.scalarProduct(rightEigen.vector)
            if(abs(1-prod) < correctionThreshold) {
                s[i] = s[i]+rightEigen.value
                sumOfRemaining -= rightEigen.value
                Right -= rightEigen.value * v * v.T()
                continue@mainLoop
            }
        }
        V = V.concatColumns(rightEigen.vector)
        val singularValue = sqrt(rightEigen.value)
        s.add(singularValue)
        U = U.concatColumns(this * rightEigen.vector / singularValue)
        // deflation
        Right -= rightEigen.value * rightEigen.vector * rightEigen.vector.T()

        sumOfRemaining -= rightEigen.value
    }

    return SVD(U, SimpleMatrix.diag(*s.toDoubleArray()), V)
}

data class Eigen(val vector: SimpleMatrix, val value: Double)

fun powerIter(A: SimpleMatrix, threshold: Double = 1e-12): Eigen {
    var current = SimpleMatrix.random_DDRM(A.numCols(), 1, 0.0, 1.0, Random())
    current /= current.vecNorm2()
    var prev = current
    var scalarProduct: Double
    do {
        prev = current / current.vecNorm2()
        current = A * prev
        scalarProduct = current.scalarProduct(prev)
//    } while (abs(scalarProduct / (current.vecNorm2())-1) > threshold)
    } while ((A * prev - scalarProduct * prev).vecNorm2() > threshold)
    return Eigen(current / current.vecNorm2(), scalarProduct)
}

fun lanczos(A: SimpleMatrix, threshold: Double = 1e-15, m: Int = A.numCols(), v0: SimpleMatrix? = null): Eigen {

    var V = v0 ?: SimpleMatrix.random_DDRM(A.numCols(), 1, 0.0, 1.0, Random())
    V /= V.vecNorm2()
    var v = V
    var vprev = v.createLike()
    var beta = 0.0
    var H = SimpleMatrix(1, 1)

    var lambda = 0.0
    var eigVect = SimpleMatrix(0,0)

    for (i in 0 until m) {
        var w = A * v - beta * vprev
        val alpha = w.scalarProduct(v)
        w -= alpha * v
        beta = w.vecNorm2()

        H = H.concatColumns(SimpleMatrix(H.numRows(), 1))
        val newRow = SimpleMatrix(1, H.numCols())
        H = H.concatRows(newRow)
        H[i, i] = alpha
        H[i, i + 1] = beta
        H[i + 1, i] = beta
        vprev = v
        v = w / beta

        val ritz = H[0..H.numRows()-1, 0..H.numCols()-1].eig()
        var maxIdx = 0
        lambda = ritz.eigenvalues[0].real
        for (j in 1 until ritz.eigenvalues.size) {
            val curr = ritz.eigenvalues[j].real
            if (curr.absoluteValue > lambda.absoluteValue) {
                maxIdx = j
                lambda = curr
            }
        }
        eigVect = V * ritz.getEigenVector(maxIdx)
        val residual = A * eigVect - lambda * eigVect
        if(residual.vecNorm2() < threshold) break

        V = V.concatColumns(v)
    }

    return Eigen(eigVect, lambda)
}

fun restartedLanczos(A: SimpleMatrix, threshold: Double = 1e-15, innerIters: Int = 5): Eigen {
    var v0 = SimpleMatrix.random_DDRM(A.numCols(), 1, 0.0, 1.0, Random())
    v0 /= v0.vecNorm2()
    while(true) {
        val eig = lanczos(A, threshold, innerIters, v0)
        val residual = eig.value*eig.vector-A*eig.vector
        if(residual.vecNorm2() > threshold) return eig
    }
}