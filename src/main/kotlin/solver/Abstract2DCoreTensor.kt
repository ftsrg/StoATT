package solver

import org.ejml.data.DMatrixSparseCSC
import org.ejml.simple.SimpleMatrix

enum class CoreTensorPosition {
    FIRST, LAST, MIDDLE
}

abstract class Abstract2DCoreTensor(val modeLength: Int,
                                    val rows: Int,
                                    val cols: Int) {
    abstract fun multFromLeft(i: Int, j: Int, v: SimpleMatrix): SimpleMatrix
    abstract fun multFromRight(i: Int, j: Int, v: SimpleMatrix): SimpleMatrix

    abstract fun multFromLeft(i: Int, j: Int, v: DMatrixSparseCSC): DMatrixSparseCSC
    abstract fun multFromRight(i: Int, j: Int, v: DMatrixSparseCSC): DMatrixSparseCSC

    abstract fun toDenseCore(): CoreTensor
}