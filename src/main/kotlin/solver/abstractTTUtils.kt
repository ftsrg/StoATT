package solver

import org.ejml.data.DMatrixSparseCSC
import org.ejml.ops.ConvertDMatrixStruct
import org.ejml.simple.SimpleMatrix
import org.ejml.sparse.csc.CommonOps_DSCC
import kotlin.math.abs
import kotlin.math.sqrt

//fun computeStructuredPreconditioner(A: Array<Sparse2DCoreTensor>): Array<Sparse2DCoreTensor> {
//    A.map { ACore ->
//        //TODO: this size won't work
//        val rows = ACore.cols
//        val cols = ACore.rows
//    }
//}

/**
 * Computes the 2-norm of Ax-y, where A is given as an array of abstract core tensors
 */
fun computeResidualNorm(A: Array<Abstract2DCoreTensor>, x: TTVector, y: TTVector): Double {
//    return computeResidualNormSparse(A, x, y)

    fun applyResidualCore(k: Int, i: Int, V: SimpleMatrix): SimpleMatrix {
        val crA = A[k]
        val crX = x.tt.cores[k]
        val crY = y.tt.cores[k]
        val res =
                if(k == 0) SimpleMatrix(1, V.numCols())
                else SimpleMatrix(A[k].rows * crX.rows + crY.rows, V.numCols())
        for (c in 0 until V.numCols()) {
            val v = V.col(c)
            val V1 = if(k == A.size-1) v else v.rows(0, crA.cols * crX.cols)
            val V2 = if(k == A.size-1) -v else v.rows(crA.cols * crX.cols, v.numRows())
            V1.reshape(crA.cols, crX.cols)
            var res1 = SimpleMatrix(crA.rows, crX.rows)
            for (j in 0 until crA.modeLength) {
                res1 += crA.multFromRight(i, j, V1*crX[j].T())
            }
            res1.reshape(res1.numElements, 1)
            val res2 = crY[i] * V2
            if(k == 0) res[0, c] = res1[0] + res2[0]
            else res[0, c] = res1.concatRows(res2)
        }
        return res
    }

    fun applyKroneckerCore(k: Int, i: Int, v: SimpleMatrix): SimpleMatrix {
        val V = v.copy()
        if(k != A.size - 1) {
            val numRows = x.tt.cores[k].cols * A[k].cols + y.tt.cores[k].cols
            V.reshape(numRows, V.numElements / numRows)
        }
        val res = applyResidualCore(k, i, applyResidualCore(k, i, V.T()).T())
        res.reshape(res.numElements, 1)
        return res
    }

//    val lastACore = A.last().toDenseCore()
//    val lastXCore = x.tt.cores.last()
//    val lastYCore = y.tt.cores.last()
//    val rows = lastACore.rows * lastXCore.rows + lastYCore.rows
//    var v = SimpleMatrix(rows*rows, 1)
//    for (i in 0 until lastXCore.modeLength) {
//        var term = SimpleMatrix(lastACore.rows * lastXCore.rows, 1)
//        for (j in 0 until lastXCore.modeLength) {
//            term += lastACore[i, j].kron(lastXCore[j])
//        }
//        term = term.concatRows(-lastYCore[i])
//        v += term.kron(term)
//    }
//
//    for (k in A.size - 2 downTo 0) {

    var v = ones(1)
    for (k in A.size - 1 downTo 0) {
        val ACore = A[k]
        val XCore = x.tt.cores[k]
        val YCore = y.tt.cores[k]
        val numRowsBeforeKron = ACore.rows * XCore.rows + YCore.rows
        var nextv =
                if(k==0) SimpleMatrix(1,1)
                else SimpleMatrix(numRowsBeforeKron * numRowsBeforeKron, 1)
        for (i in 0 until ACore.modeLength) {
            nextv += applyKroneckerCore(k, i, v)
        }
        v = nextv
    }
    return sqrt(abs(v[0]))
}

fun computeResidualNormSparse(A: Array<Abstract2DCoreTensor>, x: TTVector, y: TTVector): Double {
    fun applyResidualCore(k: Int, i: Int, V: DMatrixSparseCSC): DMatrixSparseCSC {
        val crA = A[k]
        val crX = x.tt.cores[k]
        val crY = y.tt.cores[k]
        var res =
                if(k == 0) DMatrixSparseCSC(1, V.numCols)
                else DMatrixSparseCSC(A[k].rows * crX.rows + crY.rows, 0)
        for (c in 0 until V.numCols) {
            val v = CommonOps_DSCC.extractColumn(V, c, null)
            val V1 =
                    if(k == A.size-1) v.reshape2(crA.cols, crX.cols)
                    else CommonOps_DSCC.extractRows(v, 0, crA.cols * crX.cols, null).reshape2(crA.cols, crX.cols)
            val V2 =
                    if(k == A.size-1) DMatrixSparseCSC(v.numRows, v.numCols, v.nz_length)
                    else CommonOps_DSCC.extractRows(v, crA.cols * crX.cols, v.numRows, null)
            if(k == A.size-1) CommonOps_DSCC.scale(-1.0, v, V2)
            var res1 = DMatrixSparseCSC(crA.rows, crX.rows)
            for (j in 0 until crA.modeLength) {
                val crXSparse = ConvertDMatrixStruct.convert(crX[j].getMatrix(), null as DMatrixSparseCSC?, 0.0)
                val crXSparseT = DMatrixSparseCSC(crXSparse.numCols, crXSparse.numRows, crXSparse.nz_length)
                CommonOps_DSCC.transpose(crXSparse, crXSparseT, null)
                res1 += crA.multFromRight(i, j, V1*crXSparseT)
            }
            val res1NumElements = res1.numRows * res1.numCols
            val res1Vect = res1.reshape2(res1NumElements, 1)
            val crYSparse = ConvertDMatrixStruct.convert(crY[i].getMatrix(), null as DMatrixSparseCSC?, 0.0)
            val res2 = crYSparse * V2
            if(k == 0) res[0, c] = res1Vect[0,0] + res2[0,0]
            else res = res.concatCols(res1Vect.concatRows(res2))
        }
        return res
    }

    fun applyKroneckerCore(k: Int, i: Int, v: DMatrixSparseCSC): DMatrixSparseCSC {
        val numRows = x.tt.cores[k].cols * A[k].cols + y.tt.cores[k].cols
        val vNumElements = v.numRows*v.numCols
        val V = if(k == A.size - 1) v else v.reshape2(numRows, vNumElements / numRows)
        val res = applyResidualCore(k, i, applyResidualCore(k, i, V.T()).T())
        val resNumElements = res.numRows * res.numCols
        return res.reshape2(resNumElements, 1)
    }

    var v = DMatrixSparseCSC(1, 1, 1)
    v.set(0, 0, 1.0)
    for (k in A.size - 1 downTo 0) {
        val ACore = A[k]
        val XCore = x.tt.cores[k]
        val YCore = y.tt.cores[k]
        val numRowsBeforeKron = ACore.rows * XCore.rows + YCore.rows
        var nextv =
                if(k==0) DMatrixSparseCSC(1,1)
                else DMatrixSparseCSC(numRowsBeforeKron * numRowsBeforeKron, 1)
        for (i in 0 until ACore.modeLength) {
            nextv += applyKroneckerCore(k, i, v)
        }
        v = nextv
    }
    return sqrt(abs(v[0, 0]))
}

