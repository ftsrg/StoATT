import org.ejml.simple.SimpleMatrix
import java.util.*

class TTVector(var tt: TensorTrain) {

    companion object {
        fun zeros(modes: Array<Int>): TTVector {
            val cores = ArrayList<CoreTensor>(modes.size)
            for (mode in modes) {
                cores.add(CoreTensor(mode, 1, 1))
            }
            //TODO: wasting a row and col in each core if another TT is added to it and no rounding is performed
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

    val numElements = tt.cores.map { it.modeLength }.product()
    val modes = tt.cores.map { it.modeLength }.toTypedArray()

    operator fun get(element: Int): Double {
        val indices = arrayListOf<Int>()
        var tempDiv = numElements
        var tempElem = element
        for (core in tt.cores) {
            tempDiv /= core.modeLength
            indices.add(tempElem/tempDiv)
            tempElem %= tempDiv
        }
        return tt.get(*(indices.toIntArray()))
    }

    operator fun plus(v: TTVector): TTVector {
        //TODO: assert mode size equalities
        return TTVector(this.tt+v.tt)
    }

    operator fun plusAssign(v: TTVector) = tt.plusAssign(v.tt)

    operator fun minus(v: TTVector): TTVector {
        return this+v*(-1.0)
    }

    operator fun times(d: Double): TTVector {
        return TTVector(this.tt*d)
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
}
operator fun Double.times(V: TTVector): TTVector = V * this
