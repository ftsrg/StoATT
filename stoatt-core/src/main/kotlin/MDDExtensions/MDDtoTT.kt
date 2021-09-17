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

package MDDExtensions

import hu.bme.mit.delta.mdd.MddHandle
import org.ejml.data.DMatrixSparseCSC
import org.ejml.data.DMatrixSparseTriplet
import org.ejml.ops.ConvertDMatrixStruct
import solver.CoreTensor
import solver.Sparse2DCoreTensor
import solver.TensorTrain

fun MddHandle.toTensorTrain(): TensorTrain {
    val cores = arrayListOf<CoreTensor>()

    val (levelLists, domainSizes) = getCleanedLevelLists()

    for (l in 0 until levelLists.size-1) {
        val curr = levelLists[l]
        val next = levelLists[l+1]
        val core = CoreTensor( domainSizes[l], curr.size, next.size)
        for(i in 0 until domainSizes[l]) {
            for((idx, n) in curr.withIndex()) {
                val target = next.indexOf(n[i])
                if(target != -1) core[i][idx, target] = 1.0
            }
        }
        cores.add(core)
    }
    return TensorTrain(cores)
}

fun MddHandle.toSparseTTDiagMatrix(): List<Sparse2DCoreTensor> {
    val cores = arrayListOf<Sparse2DCoreTensor>()

    val (levelLists, domainSizes) = getCleanedLevelLists()

    for (l in 0 until levelLists.size-1) {
        val curr = levelLists[l]
        val next = levelLists[l+1]
        val data = Array(domainSizes[l]) { i ->
            Array(domainSizes[l]) { j ->
                if(i != j) return@Array DMatrixSparseCSC(curr.size, next.size)
                val mtx = DMatrixSparseTriplet(curr.size, next.size, curr.size)
                for((idx, n) in curr.withIndex()) {
                    val target = next.indexOf(n[i])
                    if(target != -1) mtx.addItemCheck(idx, target, 1.0)
                }
                return@Array ConvertDMatrixStruct.convert(mtx, null as DMatrixSparseCSC?)
            }
        }
        val core = Sparse2DCoreTensor(domainSizes[l], curr.size, next.size, data)
        cores.add(core)
    }

    return cores
}

private fun MddHandle.getCleanedLevelLists(): Pair<List<List<MddHandle>>, ArrayList<Int>> {
    val levels = arrayListOf<HashSet<MddHandle>>()
    val domainSizes = arrayListOf<Int>()

    var currNodes = hashSetOf(this)
    levels.add(currNodes)
    while (!currNodes.any(MddHandle::isTerminal)) {
        val nexts = hashSetOf<MddHandle>()
        val domainSize = currNodes.first().variableHandle.variable.get().domainSize
        domainSizes.add(domainSize)
        for (n in currNodes) {
            for (i in 0 until domainSize) {
                nexts.add(n[i])
            }
        }
        levels.add(nexts)
        currNodes = nexts
    }
    levels.last().removeIf { it.isTerminalZero }

    for (l in levels.size - 2 downTo 0) {
        levels[l].removeIf { n ->
            (0 until domainSizes[l]).none { levels[l + 1].contains(n[it]) }
        }
    }
    return Pair(levels.map { it.toList() }, domainSizes)
}
