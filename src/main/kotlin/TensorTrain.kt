
import org.ejml.simple.SimpleMatrix
import org.ejml.simple.SimpleMatrix.END
import org.ejml.simple.SimpleMatrix.random_DDRM
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.sqrt

class CoreTensor(val modeLength: Int, var rows: Int, var cols: Int) {
    val data = Array(modeLength) { SimpleMatrix(rows, cols) }

    operator fun get(modeIdx: Int) = data[modeIdx]

    operator fun timesAssign(d: Double) {
        for (i in 0 until data.size) {
            data[i] = data[i]*d
        }
    }

    operator fun times(d: Double): CoreTensor {
        val res = CoreTensor(modeLength,rows,cols) //empty at first
        for (i in 0 until res.data.size) {
            res.data[i] = data[i]*d
        }
        return res
    }

    operator fun set(j: Int, value: SimpleMatrix) {
        data[j] = value
    }

    fun copy(): CoreTensor {
        return CoreTensor(modeLength, rows, cols).also {
            for ((i, mat) in this.data.withIndex()) {
                it.set(i, mat.copy())
            }
        }
    }
}

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

        fun rand(modes: Array<Int>, ranks: Array<Int>, min: Double = 0.0, max: Double = 10.0): TTVector {
            assert(modes.size == ranks.size-1)
            assert(ranks.first() == 1 && ranks.last() == 1)
            val cores = ArrayList<CoreTensor>(modes.size)
            val random = Random()
            repeat(modes.size) { k ->
                val newCore = CoreTensor(modes[k], ranks[k], ranks[k + 1])
                repeat(newCore.modeLength) { m ->
                    newCore[m] = random_DDRM(newCore.rows, newCore.cols, min, max, random)
                }
                cores.add(newCore)
            }
            return TTVector(TensorTrain(cores))
        }

        fun fromBlackBox(valFun: (Int) -> Double): TTMatrix {
            TODO()
        }

        fun fromFull(original: SimpleMatrix): TTMatrix {
            TODO()
        }
    }

    val numElements = tt.cores.map { it.modeLength }.product()

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

    operator fun minus(v: TTVector): TTVector {
        return this+v*(-1.0)
    }

    operator fun times(d: Double): TTVector {
        return TTVector(this.tt*d)
    }
}

class TTMatrix(var tt: TensorTrain, val rowModes: Array<Int>, val colModes: Array<Int>) {

    companion object {
        fun zeros(rowModes: Array<Int>, colModes: Array<Int>): TTMatrix {
            assert(rowModes.size == colModes.size)
            val cores = ArrayList<CoreTensor>(rowModes.size)
            for(i in 0 until rowModes.size) {
                cores.add(CoreTensor(rowModes[i]*colModes[i], 1, 1))
            }
            return TTMatrix(TensorTrain(cores), rowModes, colModes)
        }


        fun rand(rowModes: Array<Int>, colModes: Array<Int>, ranks: Array<Int>, min: Double = 0.0, max: Double = 10.0): TTMatrix {
            assert(rowModes.size ==  colModes.size)
            assert(rowModes.size == ranks.size-1)
            assert(ranks.first() == 1 && ranks.last() == 1)
            val cores = ArrayList<CoreTensor>(rowModes.size)
            val random = Random()
            repeat(rowModes.size) { k ->
                val newCore = CoreTensor(rowModes[k] * colModes[k], ranks[k], ranks[k + 1])
                repeat(newCore.modeLength) { m ->
                    newCore[m] = random_DDRM(newCore.rows, newCore.cols, min, max, random)
                }
                cores.add(newCore)
            }
            return TTMatrix(TensorTrain(cores), rowModes, colModes)
        }

        /**
         * Returns an identity matrix in TT format with the give mode sizes as both row and column modes
         */
        fun eye(modes: Array<Int>): TTMatrix {
            val res = zeros(modes, modes)
            for ((coreIdx, m) in modes.withIndex()) {
                for (i in 0 until m) {
                    res.tt.cores[coreIdx][i*m+i] = mat[r[1.0]]
                }
            }
            return res
        }
    }

    init { assert(rowModes.size == colModes.size)}

    val numRows = rowModes.product()
    val numCols = colModes.product()

    operator fun get(row: Int, col: Int): Double {
        val rowIndices = arrayListOf<Int>()
        val colIndices = arrayListOf<Int>()
        var tempDiv = numRows
        var tempElem = row
        for (mode in rowModes) {
            tempDiv /= mode
            rowIndices.add(tempElem/tempDiv)
            tempElem %= tempDiv
        }
        tempElem = col
        tempDiv = numCols
        for (mode in colModes) {
            tempDiv /= mode
            colIndices.add(tempElem/tempDiv)
            tempElem %= tempDiv
        }
        val indices = IntArray(rowModes.size) {idx -> rowIndices[idx]*colModes[idx]+colIndices[idx]}
        return tt.get(*indices)
    }

    operator fun times(v: TTVector): TTVector {
        assert(this.tt.cores.size == v.tt.cores.size)
        { "The TT-vector and the TT-matrix must have the same number of cores! " +
                "Matrix: ${this.tt.cores.size}, vector: ${v.tt.cores.size}" }

        val cores = arrayListOf<CoreTensor>()

        for ((k, vectCore) in v.tt.cores.withIndex()) {
            assert(vectCore.modeLength == colModes[k])
            { "The TT-vector must have the same modes as the column modes of the TT-matrix! " +
                    "Index of first non-matching mode: $k " +
                    "Vector mode length: ${vectCore.modeLength} " +
                    "Matrix column mode length: ${colModes[k]}" }
            val matCore = tt.cores[k]
            val newCore = CoreTensor(rowModes[k], matCore.rows*vectCore.rows, matCore.cols*vectCore.cols)

            for(ik in 0 until newCore.modeLength) {
                for(jk in 0 until vectCore.modeLength) {
                    newCore.data[ik] += matCore[ik*colModes[k]+jk].kron(vectCore[jk])
                }
            }
            cores.add(newCore)
        }

        return TTVector(TensorTrain(cores))
    }

    fun copy(): TTMatrix {
        return TTMatrix(tt.copy(), rowModes, colModes)
    }

    fun diag(): TTMatrix {
        val zeroCores = arrayListOf<CoreTensor>()
        for (core in this.tt.cores) {
            zeroCores.add(CoreTensor(core.modeLength, core.rows, core.cols))
        }
        val res = TTMatrix(TensorTrain(zeroCores), rowModes, colModes)
        for ((coreIdx, m) in rowModes.withIndex()) {
            for (i in 0 until m) {
                res.tt.cores[coreIdx][i*m+i] = this.tt.cores[coreIdx][i*m+i].copy()
            }
        }
        return res
    }
}

class TensorTrain(val cores: ArrayList<CoreTensor>) {

    constructor() : this(arrayListOf())

    operator fun get(vararg indices: Int): Double {
        assert(indices.size == cores.size)
        var res = cores[0][indices[0]]
        for (i in 1 until indices.size) {
            res *= cores[i][indices[i]]
        }
        assert(res.numElements == 1)
        return res[0]
    }

    private fun addCore(core: CoreTensor) = cores.add(core)
    fun setCore(idx: Int, core: CoreTensor) {
        cores.set(idx, core)
    }

    operator fun plus(T: TensorTrain): TensorTrain {
        assert(T.cores.size == this.cores.size) { "The operand trains must have the same number of core tensors!" }
        val res = TensorTrain()
        if(cores.size == 0) return res

        //TODO: handle single core case

        //calculate first core
        val firstCoreThis = this.cores[0]
        val firstCoreThat = T.cores[0]
        assert(firstCoreThis.modeLength == firstCoreThat.modeLength)
        { "First core mode lengths don't match! Left: ${firstCoreThis.modeLength}, Right: ${firstCoreThat.modeLength}" }
        res.addCore(CoreTensor(firstCoreThis.modeLength, firstCoreThis.rows, firstCoreThis.cols+firstCoreThat.cols))
        for(i in 0 until firstCoreThis.modeLength) {
            res.cores[0].data[i][0,0] = firstCoreThis.data[i]
            if(firstCoreThat.cols > 0)
                res.cores[0].data[i][0, firstCoreThis.cols] =  firstCoreThat.data[i]
        }

        //calculate middle cores
        for(i in 1 until cores.size-1) {
            val currCoreThis = this.cores[i]
            val currCoreThat = T.cores[i]
            assert(currCoreThis.modeLength == currCoreThat.modeLength)
            { "Cores with index $i don't have matching mode lengths! Left: ${currCoreThis.modeLength}, right: ${currCoreThat.modeLength}" }

            val newCore = CoreTensor(
                    currCoreThis.modeLength,
                    currCoreThis.rows + currCoreThat.rows,
                    currCoreThis.cols + currCoreThat.cols)

            for(j in 0 until currCoreThis.modeLength) {
                newCore.data[i][0,0] = currCoreThis.data[j]
                if(currCoreThat.cols > 0 && currCoreThat.rows > 0)
                    newCore.data[i][currCoreThis.rows, currCoreThis.cols] = currCoreThat.data[j]
            }
            res.addCore(newCore)
        }

        //calculate last core
        val lastCoreThis = this.cores.last()
        val lastCoreThat = T.cores.last()
        assert(lastCoreThis.modeLength == lastCoreThat.modeLength)
        { "Last core mode lengths don't match! Left: ${lastCoreThis.modeLength}, Right: ${lastCoreThat.modeLength}" }
        res.addCore(CoreTensor(lastCoreThis.modeLength, lastCoreThis.rows+lastCoreThat.rows, lastCoreThis.cols))
        for(i in 0 until lastCoreThis.modeLength) {
            res.cores[res.cores.lastIndex].data[i][0,0] = lastCoreThis.data[i]
            if(lastCoreThat.rows > 0)
                res.cores[res.cores.lastIndex].data[i][lastCoreThis.rows,0] = lastCoreThat.data[i]
        }

        return res
    }

    operator fun plusAssign(T: TensorTrain) {
        TODO()
    }

    operator fun minus(T: TensorTrain): TensorTrain {
        return this+(-1.0)*T //TODO: optimize
    }

    operator fun minusAssign(T: TensorTrain) {
        TODO()
    }

    operator fun times(d: Double): TensorTrain {
        return this.copy().apply { timesAssign(d) }
    }

    operator fun timesAssign(d: Double) {
        if(cores.size > 0) cores[0] = cores[0]*d
    }

    fun frobenius(): Double {
        return sqrt(scalarProduct(this)) //TODO: can it be optimized? cache?
    }

    fun round(accuracy: Double) {
        //init
        val delta = accuracy / Math.sqrt((cores.size-1).toDouble()) * frobenius()

        //right-to-left orthogonalization
        for(i in cores.lastIndex downTo 1) {
            val Gk = cores[i]
            val Gkmat = SimpleMatrix(Gk.rows, Gk.modeLength * Gk.cols)
            for ((idx, m) in Gk.data.withIndex()) {
                Gkmat[0, Gk.cols*idx] = m
            }
            //RQ (row QR) decomposition
            val qr = Gkmat.T().qr()
            val R = qr.R.T()
            val Q = qr.Q.T()
            for(j in 0 until Gk.modeLength) {
                Gk[j] = Q[0..Q.numRows(), Gk.cols*j..Gk.cols*(j+1)]
            }

            val Gkprev = cores[i-1]
            for(j in 0 until Gkprev.modeLength) {
                Gkprev[j] *= R
            }
        }

        //compression
        for (k in 0 until cores.size-1) {
            val Gk = cores[k]
            //reshaping into matrix
            val Gkmat = SimpleMatrix(Gk.rows*Gk.modeLength, Gk.cols)
            for ((i, mat) in Gk.data.withIndex()) {
                Gkmat[i*Gk.rows, 0] = mat
            }
            val svd = Gkmat.svd(true)
            val origSize = svd.singularValues.size
            val maxIdx = max(0, svd.singularValues.indexOfFirst(origSize) { it < delta } - 1)
            val GkmatTrunc = svd.u[0..END, 0..maxIdx+1]
            repeat(Gk.modeLength) {
                Gk.data[it] = GkmatTrunc[it*Gk.rows..(it+1)*Gk.rows, 0..GkmatTrunc.numCols()]
            }
            Gk.rows = Gk.data[0].numRows()
            Gk.cols = Gk.data[0].numCols()

            val modifier = svd.w[0..maxIdx+1, 0..maxIdx+1]*svd.v[0..END, 0..maxIdx+1].T()
            val nextMatData = cores[k + 1].data
            for ((i, mat) in nextMatData.withIndex()) {
                nextMatData[i] = modifier*mat
            }
            val mat = cores[k + 1][0]
            cores[k+1].rows = mat.numRows()
            cores[k+1].cols = mat.numCols()
        }
    }

    /**
     * Performs deep copy of the tensor train
     */
    fun copy(): TensorTrain {
        return TensorTrain(ArrayList<CoreTensor>(cores.size).apply {
            for(c in cores) add(c.copy())
        })
    }

    fun scalarProduct(other: TensorTrain): Double {
        val otherFirstCore = other.cores[0]
        val zeroFirst = SimpleMatrix(cores[0].rows * otherFirstCore.rows, cores[0].cols * otherFirstCore.cols)
        var v = cores[0].data.foldIndexed(zeroFirst) { idx, acc, matA ->
            acc+matA.kron(otherFirstCore[idx])
        }
        for(i in 1 until cores.size) {
            val otherCore = other.cores[i]
            val thisCore = cores[i]
            val zero = SimpleMatrix(1, thisCore.cols * otherCore.cols)
            v = thisCore.data.foldIndexed(zero) {
                //TODO: optimize product with kronecker-structured matrix (res[kth B.cols-long part]=sum(A[j,k]*(v[jth B.rows-long part]*B)))
                idx, acc, matA -> acc + v * matA.kron(otherCore[idx])
            }
        }
        assert(v.numElements == 1)
        return v[0]
    }

    fun hadamard(other: TensorTrain): TensorTrain {
        assert(cores.size == other.cores.size)
        val res = this.copy()
        for ((coreIdx, core) in res.cores.withIndex()) {
            val otherCore = other.cores[coreIdx]
            for (matIdx in core.data.indices) {
                core[matIdx] = core[matIdx].kron(otherCore[matIdx])
            }
        }
        return res
    }
}

operator fun Double.times(T: TensorTrain): TensorTrain {
    return T*this
}
