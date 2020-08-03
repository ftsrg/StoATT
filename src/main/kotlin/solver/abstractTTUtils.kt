package solver

import org.ejml.simple.SimpleMatrix

/**
 * Computes the 2-norm of Ax-y, where A is given as an array of abstract core tensors
 */
fun computeResidualNorm(A: Array<Abstract2DCoreTensor>, x: TTVector, y: TTVector): Double {
    // V must be a col vector
    fun applyResidualCore(k: Int, i: Int, v: SimpleMatrix): SimpleMatrix {
        val crA = A[k]
        val crX = x.tt.cores[k]
        val crY = y.tt.cores[k]
        val V1 = v.rows(0, crA.cols * crX.cols)
        val V2 = v.rows(crA.cols * crX.cols, v.numRows())
        V1.reshape(crA.cols, crX.cols)
        val M = V1 * crX[i].T()
        var res1 = SimpleMatrix(crA.rows, M.numCols())
        for(j in 0 until crA.modeLength) {
            res1 += crA.multFromRight(i, j, M)
        }
        res1.reshape(res1.numElements, 1)
        val res2 = -crY[i]*V2
        return res1.concatRows(res2)
    }

    fun applyKroneckerCore(k: Int, i: Int, V: SimpleMatrix): SimpleMatrix =
            applyResidualCore(k, i, applyResidualCore(k, i, V).T()).T()

    val firstACore = A[0].toDenseCore()

    val xFirstCore = x.tt.cores[0]
    var v = firstACore.data.foldIndexed(SimpleMatrix(firstACore.rows * xFirstCore.rows, firstACore.cols*xFirstCore.cols)) {
        idx, acc, matA ->
        acc+matA.kron(xFirstCore[idx])
    }

    for(i in 1 until A.size) {
        val thisCore = A[i]
        val otherCore = x.tt.cores[i]
        val zero = SimpleMatrix(1, thisCore.cols * otherCore.cols)
        v = otherCore.data.foldIndexed(zero) {
            idx, acc, matX ->
            val prod = applyKroneckerCore(i, idx, matX)
            return@foldIndexed acc + prod
        }
    }
    assert(v.numElements == 1)
    return v[0]
}