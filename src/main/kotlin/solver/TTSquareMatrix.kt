package solver

import org.ejml.simple.SimpleMatrix
import java.util.*

class TTSquareMatrix(var tt: TensorTrain, val modes: Array<Int>) {

    companion object {
        fun zeros(modes: Array<Int>): TTSquareMatrix {
            val cores = ArrayList<CoreTensor>(modes.size)
            for(i in 0 until modes.size) {
                cores.add(CoreTensor(modes[i] * modes[i], 1, 1))
            }
            return TTSquareMatrix(TensorTrain(cores), modes)
        }

        fun rand(modes: Array<Int>, ranks: Array<Int>, min: Double = 0.0, max: Double = 10.0, random: Random = Random()): TTSquareMatrix {
            assert(modes.size == ranks.size-1)
            assert(ranks.first() == 1 && ranks.last() == 1)
            val cores = ArrayList<CoreTensor>(modes.size)
            repeat(modes.size) { k ->
                val newCore = CoreTensor(modes[k] * modes[k], ranks[k], ranks[k + 1])
                repeat(newCore.modeLength) { m ->
                    newCore[m] = SimpleMatrix.random_DDRM(newCore.rows, newCore.cols, min, max, random)
                }
                cores.add(newCore)
            }
            return TTSquareMatrix(TensorTrain(cores), modes)
        }

        /**
         * Returns an identity matrix in TT format with the give mode sizes as both solver.row and column modes
         */
        fun eye(modes: Array<Int>): TTSquareMatrix {
            val res = zeros(modes)
            for ((coreIdx, m) in modes.withIndex()) {
                for (i in 0 until m) {
                    res.tt.cores[coreIdx][i*m+i] = mat[r[1.0]]
                }
            }
            return res
        }

        fun diag(vect: TTVector): TTSquareMatrix {
            val zeroCores = arrayListOf<CoreTensor>()
            for (core in vect.tt.cores) {
                zeroCores.add(CoreTensor(core.modeLength * core.modeLength, core.rows, core.cols))
            }
            val res = TTSquareMatrix(TensorTrain(zeroCores), vect.modes)
            for ((coreIdx, m) in vect.modes.withIndex()) {
                for (i in 0 until m) {
                    res.tt.cores[coreIdx][i*m+i] = vect.tt.cores[coreIdx][i].copy()
                }
            }
            return res
        }
    }

    val numRows = modes.map(Int::toLong).reduce(Long::times)
    val numCols = modes.map(Int::toLong).reduce(Long::times)

    operator fun get(row: Long, col: Long): Double {
        val rowIndices = arrayListOf<Long>()
        val colIndices = arrayListOf<Long>()
        var tempDiv = numRows
        var tempElem = row
        for (mode in modes) {
            tempDiv /= mode
            rowIndices.add(tempElem/tempDiv)
            tempElem %= tempDiv
        }
        tempElem = col
        tempDiv = numCols
        for (mode in modes) {
            tempDiv /= mode
            colIndices.add(tempElem/tempDiv)
            tempElem %= tempDiv
        }
        val indices = IntArray(modes.size) { idx -> (rowIndices[idx]*modes[idx]+colIndices[idx]).toInt()}
        return tt.get(*indices)
    }

    operator fun times(M: TTSquareMatrix): TTSquareMatrix {
        assert(this.tt.cores.size == M.tt.cores.size)
        { "The TT-Matrices must have the same number of cores!" }
        val newCores = arrayListOf<CoreTensor>()
        for (c in 0 until modes.size) {
            assert(modes[c] == M.modes[c])
            val thisCore = this.tt.cores[c]
            val thatCore = M.tt.cores[c]
            val newCore = CoreTensor(modes[c] * modes[c], thisCore.rows * thatCore.rows, thisCore.cols * thatCore.cols)
            // TODO: nincs kevésbé kellemetlen? :(
            for (i in 0 until modes[c]) {
                for (j in 0 until modes[c]) {
                    val idx = i * modes[c] + j
                    for (m in 0 until modes[c]) {
                        newCore.data[i * modes[c] + j] += thisCore.data[i * modes[c] + m].kron(thatCore.data[m * modes[c] + j])
                    }
                }
            }
            newCores.add(newCore)
        }
        return TTSquareMatrix(TensorTrain(newCores), modes)
    }

    operator fun times(v: TTVector): TTVector {
        assert(this.tt.cores.size == v.tt.cores.size)
        { "The TT-vector and the TT-matrix must have the same number of cores! " +
                "Matrix: ${this.tt.cores.size}, vector: ${v.tt.cores.size}" }

        val cores = arrayListOf<CoreTensor>()

        for ((k, vectCore) in v.tt.cores.withIndex()) {
            assert(vectCore.modeLength == modes[k])
            { "The TT-vector must have the same modes as the column modes of the TT-matrix! " +
                    "Index of first non-matching mode: $k " +
                    "Vector mode length: ${vectCore.modeLength} " +
                    "Matrix column mode length: ${modes[k]}" }
            val matCore = tt.cores[k]
            val newCore = CoreTensor(modes[k], matCore.rows * vectCore.rows, matCore.cols * vectCore.cols)

            for(ik in 0 until newCore.modeLength) {
                for(jk in 0 until vectCore.modeLength) {
                    newCore.data[ik] += matCore[ik*modes[k]+jk].kron(vectCore[jk])
                }
            }
            cores.add(newCore)
        }

        return TTVector(TensorTrain(cores))
    }

    operator fun times(d: Double): TTSquareMatrix {
        return TTSquareMatrix(d * tt, modes)
    }

    operator fun timesAssign(d: Double) {
        tt.timesAssign(d)
    }

    operator fun plus(M: TTSquareMatrix): TTSquareMatrix {
        return TTSquareMatrix(tt + M.tt, modes)
    }

    operator fun plusAssign(M: TTSquareMatrix) {
        tt.plusAssign(M.tt)
    }

    operator fun minus(M: TTSquareMatrix): TTSquareMatrix {
        return this + (M * -1.0)
    }

    operator fun unaryMinus(): TTSquareMatrix {
        return this*(-1.0)
    }

    operator fun minusAssign(M: TTSquareMatrix) {
        this += (M * (-1.0))
    }

    fun copy(): TTSquareMatrix {
        return TTSquareMatrix(tt.copy(), modes)
    }

    fun diag(): TTSquareMatrix {
        val zeroCores = arrayListOf<CoreTensor>()
        for (core in this.tt.cores) {
            zeroCores.add(CoreTensor(core.modeLength, core.rows, core.cols))
        }
        val res = TTSquareMatrix(TensorTrain(zeroCores), modes)
        for ((coreIdx, m) in modes.withIndex()) {
            for (i in 0 until m) {
                res.tt.cores[coreIdx][i*m+i] = this.tt.cores[coreIdx][i*m+i].copy()
            }
        }
        return res
    }

    fun diagVect(): TTVector {
        val resCores = arrayListOf<CoreTensor>()
        for ((coreIdx, m) in modes.withIndex()) {
            val core = this.tt.cores[coreIdx]
            val newCore = CoreTensor(m, core.rows, core.cols)
            for (i in 0 until m) {
                newCore[i] = core[i*m+i].copy()
            }
            resCores.add(newCore)
        }
        return TTVector(TensorTrain(resCores))
    }

    fun printElements(colSep: String = " ", rowSep: String = "\n", numDecimals: Int = 2) {
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                print("${"%.${numDecimals}f".format(get(r,c))}$colSep")
            }
            print(rowSep)
        }
    }

    operator fun div(d: Double): TTSquareMatrix = this * (1.0 / d)

    fun transpose(): TTSquareMatrix {
        val transpCores = arrayListOf<CoreTensor>()
        for ((idx, core) in tt.cores.withIndex()) {
            val transpCore = CoreTensor(core.modeLength, core.rows, core.cols)
            val modeLength = modes[idx]
            for (i in 0 until modeLength) {
                for (j in 0 until modeLength) {
                    transpCore.data[i*modeLength+j] = core.data[j*modeLength+i].copy()
                }
            }
            transpCores.add(transpCore)
        }
        return TTSquareMatrix(TensorTrain(transpCores), modes)
    }

    fun divAssign(d: Double) = timesAssign(1.0/d)

    //TODO: assertions
    fun hadamard(B: TTSquareMatrix) = TTSquareMatrix(tt.hadamard(B.tt), modes)
}

operator fun Double.times(M: TTSquareMatrix) = M * this