package solver
import org.ejml.simple.SimpleMatrix
import org.ejml.simple.SimpleMatrix.END
import kotlin.math.abs
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
        val res = CoreTensor(modeLength, rows, cols) //empty at first
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

    fun updateDimensions() {
        rows = data[0].numRows()
        cols = data[0].numCols()
    }
}

class TensorTrain(val cores: ArrayList<CoreTensor>) {

    companion object {
        fun ttCross() {
            TODO()
        }
    }

    constructor() : this(arrayListOf())

    fun ranks() = cores.map { it.rows } + listOf(1)

    operator fun get(vararg indices: Int): Double {
        assert(indices.size == cores.size)
        var res = cores[0][indices[0]]
        for (i in 1 until indices.size) {
            res *= cores[i][indices[i]]
        }
        assert(res.numElements == 1)
        return res[0]
    }

    fun dataAsString(): String {
        val res = StringBuilder()
        res.append(cores.map { it.modeLength }).append('\n')
        res.append(ranks()).append('\n')
        for (core in cores) {
            for (matrix in core.data) {
                for (i in 0 until matrix.numElements) {
                    res.append(matrix[i])
                    res.append(' ')
                }
                res.deleteCharAt(res.length-1).append('\n')
            }
        }
        return res.toString()
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
        res.addCore(CoreTensor(firstCoreThis.modeLength, firstCoreThis.rows, firstCoreThis.cols + firstCoreThat.cols))
        for(i in 0 until firstCoreThis.modeLength) {
            res.cores[0].data[i][0,0] = firstCoreThis.data[i]
            if(firstCoreThat.cols > 0)
                res.cores[0].data[i][0, firstCoreThis.cols] = firstCoreThat.data[i]
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
                newCore.data[j][0,0] = currCoreThis.data[j]
                if(currCoreThat.cols > 0 && currCoreThat.rows > 0)
                    newCore.data[j][currCoreThis.rows, currCoreThis.cols] = currCoreThat.data[j]
            }
            res.addCore(newCore)
        }

        //calculate last core
        val lastCoreThis = this.cores.last()
        val lastCoreThat = T.cores.last()
        assert(lastCoreThis.modeLength == lastCoreThat.modeLength)
        { "Last core mode lengths don't match! Left: ${lastCoreThis.modeLength}, Right: ${lastCoreThat.modeLength}" }
        res.addCore(CoreTensor(lastCoreThis.modeLength, lastCoreThis.rows + lastCoreThat.rows, lastCoreThis.cols))
        for(i in 0 until lastCoreThis.modeLength) {
            res.cores[res.cores.lastIndex].data[i][0,0] = lastCoreThis.data[i]
            if(lastCoreThat.rows > 0)
                res.cores[res.cores.lastIndex].data[i][lastCoreThis.rows,0] = lastCoreThat.data[i]
        }

        return res
    }

    operator fun plusAssign(T: TensorTrain) {
        assert(T.cores.size == this.cores.size) { "The operand trains must have the same number of core tensors!" }
        if(cores.size == 0) return

        //TODO: handle single core case

        //calculate first core
        val firstCoreThis = this.cores[0]
        val firstCoreThat = T.cores[0]
        assert(firstCoreThis.modeLength == firstCoreThat.modeLength)
        { "First core mode lengths don't match! Left: ${firstCoreThis.modeLength}, Right: ${firstCoreThat.modeLength}" }
        for(i in 0 until firstCoreThis.modeLength) {
            firstCoreThis.data[i] = firstCoreThis.data[i].combine(firstCoreThis.rows, firstCoreThis.cols, firstCoreThat.data[i])
        }
        firstCoreThis.updateDimensions()

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
                currCoreThis.data[j] = currCoreThis.data[j].combine(currCoreThis.rows, currCoreThis.cols, currCoreThat.data[j])
            }
            currCoreThis.updateDimensions()
        }

        //calculate last core
        val lastCoreThis = this.cores.last()
        val lastCoreThat = T.cores.last()
        assert(lastCoreThis.modeLength == lastCoreThat.modeLength)
        { "Last core mode lengths don't match! Left: ${lastCoreThis.modeLength}, Right: ${lastCoreThat.modeLength}" }
        for(i in 0 until lastCoreThis.modeLength) {
            lastCoreThis.data[i] = lastCoreThis.data[i].combine(lastCoreThis.rows, lastCoreThis.cols, lastCoreThat.data[i])
        }
        lastCoreThis.updateDimensions()
    }

    operator fun minus(T: TensorTrain): TensorTrain {
        return this+(-1.0)*T //TODO: optimize
    }

    operator fun minusAssign(T: TensorTrain) {
        this += (-1.0)*T //TODO: optimize
    }

    operator fun times(d: Double): TensorTrain {
        return this.copy().apply { timesAssign(d) }
    }

    operator fun timesAssign(d: Double) {
        if(cores.size > 0) cores[0] = cores[0]*d
    }

    fun frobenius(): Double {
        //TODO: abs used to protect against NaNs coming from sqrt of negative number resulting from floating point precision error; maybe not the best solution
        return sqrt(abs(scalarProduct (this))) //TODO: can it be optimized? cache?
    }

    fun leftOrthogonalizeCore(coreIdx: Int) {
        if(coreIdx == cores.size-1) throw IndexOutOfBoundsException("The last core cannot be left orthogonalized!")
        if(coreIdx < 0) throw IndexOutOfBoundsException("Index cannot be negative!")
        if(coreIdx >= cores.size) throw IndexOutOfBoundsException()

        val core = cores[coreIdx]
        val leftUnfolding = SimpleMatrix(core.modeLength*core.rows, core.cols)
        for((i, mat) in core.data.withIndex()) {
            leftUnfolding[i*core.rows, 0] = mat
        }
        val QR = leftUnfolding.qr()
        val Q = QR.Q
        val R = QR.R
        val nextCore = this.cores[coreIdx + 1]
        for ((i, mat) in nextCore.data.withIndex()) {
            nextCore.data[i] = R * mat
        }
        nextCore.rows = nextCore.data[0].numRows()
        nextCore.cols = nextCore.data[0].numCols()
        for(i in 0 until core.modeLength) {
            core.data[i] = Q[i*core.rows..(i+1)*core.rows, 0..Q.numCols()]
        }
        core.rows = core.data[0].numRows()
        core.cols = core.data[0].numCols()
    }

    fun rightOrthogonalizeCore(coreIdx: Int) {
        if(coreIdx == 0) throw IndexOutOfBoundsException("The first core cannot be right orthogonalized!")
        if(coreIdx < 0) throw IndexOutOfBoundsException("Index cannot be negative!")
        if(coreIdx >= cores.size) throw IndexOutOfBoundsException()

        val core = cores[coreIdx]
        val rightUnfolding = SimpleMatrix(core.rows, core.modeLength*core.cols)
        for((i, mat) in core.data.withIndex()) {
            rightUnfolding[0, i*core.cols] = mat
        }
        val RQ_T = rightUnfolding.T().qr()
        val R = RQ_T.R.T()
        val Q = RQ_T.Q.T()
        val prevCore = this.cores[coreIdx-1]
        for ((i, mat) in prevCore.data.withIndex()) {
            prevCore.data[i] *= R
        }
        prevCore.rows = prevCore.data[0].numRows()
        prevCore.cols = prevCore.data[0].numCols()
        for (i in 0 until core.modeLength) {
            core.data[i] = Q[0..Q.numRows(), i*core.cols..(i+1)*core.cols]
        }
        core.rows = core.data[0].numRows()
        core.cols = core.data[0].numCols()
    }

    /**
     * Performs SVD-based Tensor Train rounding
     * @param tolerance Relative tolerance of the rounding procedure
     */
    fun round(tolerance: Double, budgetMode: BudgetMode = BudgetMode.NONE) {
        //init
        var delta = tolerance / sqrt((cores.size - 1).toDouble()) * frobenius()

        //right-to-left orthogonalization
        for(i in cores.lastIndex downTo 1) {
            val Gk = cores[i]
            val Gkmat = SimpleMatrix(Gk.rows, Gk.modeLength * Gk.cols)
            for ((idx, m) in Gk.data.withIndex()) {
                Gkmat[0, Gk.cols*idx] = m
            }
            //RQ (solver.row solver.QR) decomposition
            val qr = Gkmat.T().qr()
            val R = qr.R.T()
            val Q = qr.Q.T()
            for(j in 0 until Gk.modeLength) {
                Gk[j] = Q[0..Q.numRows(), Gk.cols*j..Gk.cols*(j+1)]
            }
            Gk.rows = Gk.data[0].numRows()
            Gk.cols = Gk.data[0].numCols()

            val Gkprev = cores[i-1]
            for(j in 0 until Gkprev.modeLength) {
                Gkprev[j] *= R
            }
            Gkprev.rows = Gkprev.data[0].numRows()
            Gkprev.cols = Gkprev.data[0].numCols()
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
            for (c in cores) add(c.copy())
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
                //TODO: optimize solver.product with kronecker-structured matrix
                idx, acc, matA ->

                val prod = SimpleMatrix(1, acc.numCols())

                val B = otherCore[idx]
                val Vs = Array(matA.numRows()) {
                    val vn = v.cols(it*B.numRows(), (it+1)*B.numRows())
                    return@Array vn.times(B)
                }

                for (c in 0 until matA.numCols()) {
                    for (r in 0 until matA.numRows()) {
                        prod[0, c*B.numCols()] = prod.cols(c*B.numCols(), (c+1)*B.numCols()) + matA[r,c]*Vs[r]
                    }
                }

                return@foldIndexed acc + prod
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
            core.rows *= otherCore.rows
            core.cols *= otherCore.cols
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
