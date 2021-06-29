/*
 *
 *   Copyright 2021 Budapest University of Technology and Economics
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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
     * Returns v*This[i,j]
     */
    override fun multFromLeft(i: Int, j: Int, v: SimpleMatrix): SimpleMatrix {
        val res = DMatrixRMaj(cols, v.numRows())
        CommonOps_DSCC.multTransAB(data[i][j], v.getMatrix() as DMatrixRMaj, res)
        CommonOps_DDRM.transpose(res)
        return SimpleMatrix(res)
    }

    /**
     * Returns v*This[i,j]
     */
    override fun multFromLeft(i: Int, j: Int, v: DMatrixSparseCSC): DMatrixSparseCSC {
        val res = DMatrixSparseCSC(cols, v.numRows)
        CommonOps_DSCC.mult(v, data[i][j], res)
        return res
    }

    /**
     * Returns This[i,j]*v
     */
    override fun multFromRight(i: Int, j: Int, v: SimpleMatrix): SimpleMatrix {
        val res = DMatrixRMaj(rows, v.numCols())
        CommonOps_DSCC.mult(data[i][j], v.getMatrix() as DMatrixRMaj, res)
        return SimpleMatrix(res)
    }

    /**
     * Returns This[i,j]*v
     */
    override fun multFromRight(i: Int, j: Int, v: DMatrixSparseCSC): DMatrixSparseCSC {
        val res = DMatrixSparseCSC(rows, v.numCols)
        CommonOps_DSCC.mult(data[i][j], v, res)
        return res
    }

    /**
     * Returns a new sparse 2D core tensor, whose (i,j)th matrix matrix is [This[i,j], Zero; Zero, Other[i,j]].
     * This operation corresponds to the addition of two TT tensors (the cores of the resulting TT are given
     * by the addition of the appropriate cores).
     */
    fun plus(otherCore: Sparse2DCoreTensor, position: CoreTensorPosition): Sparse2DCoreTensor {
        val resData = Array(modeLength) { i ->
            when(position) {
                CoreTensorPosition.FIRST -> {
                    Array(modeLength) {j ->
                        val d = data[i][j]
                        val other = otherCore.data[i][j]
                        val res = DMatrixSparseCSC(1, cols+otherCore.cols)
                        CommonOps_DSCC.concatColumns(d, other, res)
                        res
                    }
                }
                CoreTensorPosition.LAST -> {
                    Array(modeLength) {j ->
                        val d = data[i][j]
                        val other = otherCore.data[i][j]
                        val res = DMatrixSparseCSC(rows+otherCore.rows, 1)
                        CommonOps_DSCC.concatRows(d, other, res)
                        res
                    }
                }
                CoreTensorPosition.MIDDLE -> {
                    Array(modeLength) { j ->
                        val d = data[i][j]
                        val other = otherCore.data[i][j]
                        val res1 = DMatrixSparseCSC(rows, cols+otherCore.cols)
                        CommonOps_DSCC.concatColumns(d, DMatrixSparseCSC(rows, otherCore.cols), res1)
                        val res2 = DMatrixSparseCSC(otherCore.rows, cols+otherCore.cols)
                        CommonOps_DSCC.concatColumns(DMatrixSparseCSC(otherCore.rows, cols), other, res2)
                        val res = DMatrixSparseCSC(rows+otherCore.rows, cols+otherCore.cols)
                        CommonOps_DSCC.concatRows(res1, res2, res)
                        res
                    }
                }
            }
        }
        return Sparse2DCoreTensor(modeLength, resData[0][0].numRows, resData[0][0].numCols, resData)
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

    fun mult(other: Sparse2DCoreTensor): Sparse2DCoreTensor {
        if(this.modeLength != other.modeLength)
            throw IllegalArgumentException("Different mode lengths")
        val resArray = Array(modeLength) {i ->
            Array(modeLength) { j->
                var res = DMatrixSparseCSC(this.rows*other.rows, this.cols*other.cols, 0)
                for(l in 0 until modeLength) {
                    res += this[i, l].kron(other[l, j])
                }
                res
            }
        }
        return Sparse2DCoreTensor(modeLength, this.rows * other.rows, this.cols*other.cols, resArray)
    }

    fun hadamard(other: Sparse2DCoreTensor): Sparse2DCoreTensor {
        if(this.modeLength != other.modeLength)
            throw IllegalArgumentException("Different mode lengths")
        val resArray = Array(modeLength) { i->
            Array(modeLength) {j ->
                this[i,j].kron(other[i,j])
            }
        }
        return Sparse2DCoreTensor(modeLength, rows*other.rows, cols*other.cols, resArray)
    }
}