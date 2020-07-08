package MDDExtensions

import hu.bme.mit.delta.mdd.MddHandle
import solver.CoreTensor
import solver.TensorTrain

fun MddHandle.toTensorTrain(): TensorTrain {
    val cores = arrayListOf<CoreTensor>()

    var nextNodes = Array(this.size()) {this[it]}.toSet().toList()
    val firstCore = CoreTensor(this.variableHandle.variable.get().domainSize, 1, nextNodes.size)
    for (i in 0 until this.size()) {
        firstCore[i][nextNodes.indexOf(this[i])] = 1.0
    }
    cores.add(firstCore)

    while(!nextNodes[0].isTerminal) {
        val currNodes = nextNodes
        nextNodes = currNodes.flatMap { node -> Array(node.size()) { node[it] }.toList() }.toSet().toList()
        if(nextNodes[0].isTerminal) nextNodes = nextNodes.filter { it.data == true }
        val newCore = CoreTensor(currNodes[0].variableHandle.variable.get().domainSize, currNodes.size, nextNodes.size)
        for ((idx, currNode) in currNodes.withIndex()) {
            for (edge in 0 until currNode.size()) {
                val targetIdx = nextNodes.indexOf(currNode[edge])
                if(targetIdx != -1)
                    newCore[edge][idx, targetIdx] = 1.0
            }
        }
        cores.add(newCore)
    }

    val tensorTrain = TensorTrain(cores)
    return tensorTrain
}