package solver

import org.ejml.data.DMatrixRMaj
import org.ejml.data.DMatrixSparseCSC
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.simple.SimpleMatrix
import org.ejml.sparse.csc.CommonOps_DSCC

/**
 * Sparse core tensor for efficient TT representation of a square (row and column mode length are equal) matrix.
 * @param modeLength Row/column mode length
 */
class Sparse2DCoreTensor(
        modeLength: Int,
        rows: Int,
        cols: Int,
        private val data: Array<Array<DMatrixSparseCSC>> = Array(modeLength) { Array(modeLength) { DMatrixSparseCSC(rows, cols) } }
): Abstract2DCoreTensor(modeLength, rows, cols) {
    operator fun get(i: Int, j: Int) = data[i][j]

    operator fun set(i: Int, j: Int, value: DMatrixSparseCSC) { data[i][j] = value }

    /**
     * Returns V*This[i,j]
     */
    override fun multFromLeft(i: Int, j: Int, V: SimpleMatrix): SimpleMatrix {
        val res = DMatrixRMaj(cols, V.numRows())
        CommonOps_DSCC.multTransAB(data[i][j], V.getMatrix() as DMatrixRMaj, res)
        CommonOps_DDRM.transpose(res)
        return SimpleMatrix(res)
    }

    /**
     * Returns This[i,j]*V
     */
    override fun multFromRight(i: Int, j: Int, V: SimpleMatrix): SimpleMatrix {
        val res = DMatrixRMaj(rows, V.numCols())
        CommonOps_DSCC.mult(data[i][j], V.getMatrix() as DMatrixRMaj, res)
        return SimpleMatrix(res)
    }

    /**
     * Returns a new sparse 2D core tensor, whose (i,j)th matrix matrix is [This[i,j], Zero; Zero, Other[i,j]].
     * This operation corresponds to the addition of two TT tensors (the cores of the resulting TT are given
     * by the addition of the appropriate cores).
     */
    operator fun plus(otherCore: Sparse2DCoreTensor): Sparse2DCoreTensor {
        val resData = Array(modeLength) { i ->
            Array(modeLength) { j ->
                val res1 = DMatrixSparseCSC(rows, cols+otherCore.cols)
                val d = data[i][j]
                val other = otherCore.data[i][j]
                CommonOps_DSCC.concatColumns(d, DMatrixSparseCSC(rows, otherCore.cols), res1)
                val res2 = DMatrixSparseCSC(otherCore.rows, cols+otherCore.cols)
                CommonOps_DSCC.concatColumns(DMatrixSparseCSC(otherCore.rows, cols), other, res2)
                val res = DMatrixSparseCSC(rows+otherCore.rows, cols+otherCore.cols)
                CommonOps_DSCC.concatRows(res1, res2, res)
                res
            }
        }
        return Sparse2DCoreTensor(modeLength, rows + otherCore.rows, cols + otherCore.cols, resData)
    }

    /**
     * Returns a new sparse 2D core tensor, whose (i,j)th matrix is This[j,i]
     */
    fun transpose(): Sparse2DCoreTensor {
        val resMats = Array(modeLength) {i ->
            Array(modeLength) { j ->
                this.data[j][i].copy()
            }
        }
        return Sparse2DCoreTensor(this.modeLength, this.rows, this.cols, resMats)
    }

    override fun toDenseCore(): CoreTensor {
        val core = CoreTensor(modeLength*modeLength, rows, cols)
        for(i in data.indices) {
            for(j in data[i].indices) {
                val mat = core[i * modeLength + j]
                val iterator = data[i][j].createCoordinateIterator()
                while(iterator.hasNext()) {
                    val curr = iterator.next()
                    mat[curr.row, curr.col] = curr.value
                }
            }
        }
        return core
    }
}