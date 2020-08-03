package solver

import org.ejml.simple.SimpleMatrix

abstract class Abstract2DCoreTensor(val modeLength: Int,
                                    val rows: Int,
                                    val cols: Int) {
    abstract fun multFromLeft(i: Int, j: Int, v: SimpleMatrix): SimpleMatrix
    abstract fun multFromRight(i: Int, j: Int, v: SimpleMatrix): SimpleMatrix
    abstract fun toDenseCore(): CoreTensor
}