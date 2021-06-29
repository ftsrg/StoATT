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

import org.ejml.simple.SimpleMatrix
import kotlin.math.round
import kotlin.math.sqrt

class CoreTensor(val modeLength: Int, var rows: Int, var cols: Int) {
    val data = Array(modeLength) { SimpleMatrix(rows, cols) }

    companion object {
        fun fromVector(modeLength: Int, rows: Int, cols: Int, vector: SimpleMatrix): CoreTensor {
            val res = CoreTensor(modeLength, rows, cols)
            for (i in 0 until modeLength) {
                for (beta_minus in 0 until rows) {
                    for (beta in 0 until cols) {
                        res[i][beta_minus, beta] = vector[i * rows * cols + beta_minus * cols + beta]
                    }
                }
            }
            return res
        }
    }

    fun vectorize(): SimpleMatrix {
        val res = leftUnfolding()
        res.reshape(res.numElements, 1)
        return res
    }

    operator fun get(modeIdx: Int) = data[modeIdx]
    operator fun get(rowModeIdx: Int, colModeIdx: Int): SimpleMatrix {
        val root = sqrt(modeLength.toDouble())
        if(root.toInt() < root) throw IllegalArgumentException("Couldn't index with square matrix assumption")
        return get(rowModeIdx*root.toInt()+colModeIdx)
    }

    operator fun set(j: Int, value: SimpleMatrix) {
        data[j] = value
    }
    operator fun set(rowModeIdx: Int, colModeIdx: Int, value: SimpleMatrix) {
        val root = sqrt(modeLength.toDouble())
        if(root.toInt() < root) throw IllegalArgumentException("Couldn't index with square matrix assumption")
        set(rowModeIdx*root.toInt()+colModeIdx, value)
    }

    operator fun timesAssign(d: Double) {
        for (i in 0 until data.size) {
            data[i] = data[i]*d
        }
    }

    operator fun times(d: Double): CoreTensor {
        val res = CoreTensor(modeLength, rows, cols) //empty at first
        for (i in 0 until res.data.size) {
            res.data[i] = data[i]*d
        }
        return res
    }

    fun copy(): CoreTensor {
        return CoreTensor(modeLength, rows, cols).also {
            for ((i, mat) in this.data.withIndex()) {
                it.set(i, mat.copy())
            }
        }
    }

    fun updateDimensions() {
        rows = data[0].numRows()
        cols = data[0].numCols()
    }

    /**
     * Returns an unfolding matrix of the core where the matrices of the core are stacked below each other
     */
    fun leftUnfolding(): SimpleMatrix {
        val res = SimpleMatrix(modeLength*rows, cols)
        for(i in 0 until modeLength)
            res[i*rows, 0] = data[i]
        return res
    }

    /**
     * Returns an unfolding matrix of the core where the matrices of the core are stacked to the right of each other
     */
    fun rightUnfolding(): SimpleMatrix {
        val res = SimpleMatrix(rows, modeLength*cols)
        for(i in 0 until modeLength)
            res[0, i*cols] = data[i]
        return res
    }

    /**
     * Returns an unfolding of the core where the (n,m)th matrix of the core is the (n,m)th block
     * of the result considered as a block matrix.
     */
    fun matrixModeUnfolding(): SimpleMatrix {
        val N = round(sqrt(modeLength.toDouble())).toInt()
        val res = SimpleMatrix(N*rows, N*cols)
        for(i in 0 until N)
            for(j in 0 until N)
                res[i*rows, j*cols] = this.get(i, j)
        return res
    }

    override fun toString(): String {
        return super.toString()+" modeLength: $modeLength, rows: $rows, cols: $cols"
    }
}