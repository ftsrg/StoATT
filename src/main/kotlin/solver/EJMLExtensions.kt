package solver

import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.simple.SimpleMatrix
import kotlin.math.sqrt

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
        else -> throw Exception("Matrix should contain only one column or one solver.row")
    })
}

fun SimpleMatrix.fill(valFunc: (Int, Int) -> Double) {
    for (r in 0 until this.numRows()) {
        for (c in 0 until this.numCols()) {
            this[r, c] = valFunc(r, c)
        }
    }
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