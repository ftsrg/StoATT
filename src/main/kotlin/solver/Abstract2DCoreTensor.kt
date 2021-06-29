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