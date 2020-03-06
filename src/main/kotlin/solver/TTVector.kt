package solver

import org.ejml.simple.SimpleMatrix
import java.util.*

class TTVector(_tt: TensorTrain) {

    var tt: TensorTrain = _tt
    set(value) {
        field = value
        modes = tt.cores.map { it.modeLength }.toTypedArray()
    }

    companion object {
        fun zeros(modes: Array<Int>): TTVector {
            val cores = ArrayList<CoreTensor>(modes.size)
            for (mode in modes) {
                cores.add(CoreTensor(mode, 1, 1))
            }
            //TODO: wasting a solver.row and solver.col in each core if another TT is added to it and no rounding is performed
            return TTVector(TensorTrain(cores))
        }

        fun ones(modes: Array<Int>): TTVector {
            val cores = ArrayList<CoreTensor>(modes.size)
            for (mode in modes) {
                val newCore = CoreTensor(mode, 1, 1)
                repeat(newCore.modeLength) {i ->
                    newCore[i] = mat[r[1.0]]
                }
                cores.add(newCore)
            }
            return TTVector(TensorTrain(cores))
        }

        fun rand(modes: Array<Int>, ranks: Array<Int>, min: Double = 0.0, max: Double = 10.0, random: Random = Random()): TTVector {
            assert(modes.size == ranks.size-1)
            assert(ranks.first() == 1 && ranks.last() == 1)
            val cores = ArrayList<CoreTensor>(modes.size)
            repeat(modes.size) { k ->
                val newCore = CoreTensor(modes[k], ranks[k], ranks[k + 1])
                repeat(newCore.modeLength) { m ->
                    newCore[m] = SimpleMatrix.random_DDRM(newCore.rows, newCore.cols, min, max, random)
                }
                cores.add(newCore)
            }
            return TTVector(TensorTrain(cores))
        }
    }

    val numElements = tt.cores.map { it.modeLength.toLong() }.reduce(Long::times)
    var modes = tt.cores.map { it.modeLength }.toTypedArray()

    fun ttRanks() = tt.ranks()

    operator fun get(element: Long): Double {
        val indices = arrayListOf<Int>()
        var tempDiv = numElements
        var tempElem = element
        for (core in tt.cores) {
            tempDiv /= core.modeLength
            indices.add((tempElem / tempDiv).toInt())
            tempElem %= tempDiv
        }
        return tt.get(*(indices.toIntArray()))
    }

    operator fun plus(v: TTVector): TTVector {
        //TODO: assert mode size equalities
        return TTVector(this.tt + v.tt)
    }

    operator fun plusAssign(v: TTVector) = tt.plusAssign(v.tt)

    operator fun minus(v: TTVector): TTVector {
        return this+v*(-1.0)
    }

    operator fun times(d: Double): TTVector {
        return TTVector(this.tt * d)
    }

    operator fun times(V: TTVector): Double = tt.scalarProduct(V.tt)

    fun printElements(sep: String = " ", numDecimals: Int = 2) {
        for (i in 0 until numElements) {
            print("${"%.${numDecimals}f".format(get(i))}$sep")
        }
    }

    operator fun div(d: Double): TTVector = this * (1.0/d)

    fun norm() = tt.frobenius()
    fun copy(): TTVector = TTVector(tt.copy()
    )

    //TODO: this should work with non-square matrices when we have them
    fun outerProduct(B: TTVector): TTSquareMatrix {
        assert(this.modes.size == B.modes.size)
        val newCores = arrayListOf<CoreTensor>()
        for ((idx, modeSize) in modes.withIndex()) {
            assert(modeSize == B.modes[idx]) { "Each mode size must be equivalent for the two TT-vectors!" }
            val newCore = CoreTensor(
                    modeSize * modeSize,
                    this.tt.cores[idx].rows*B.tt.cores[idx].rows,
                    this.tt.cores[idx].cols*B.tt.cores[idx].cols
            )
            for(i in 0 until modeSize) {
                for(j in 0 until modeSize) {
                    newCore[i*modeSize+j] = this.tt.cores[idx][i].kron(B.tt.cores[idx][j])
                }
            }
            newCores.add(newCore)
        }
        return TTSquareMatrix(TensorTrain(newCores), modes)
    }

    //TODO: asserts
    fun hadamard(B: TTVector) = TTVector(this.tt.hadamard(B.tt))
}
operator fun Double.times(V: TTVector): TTVector = V * this
