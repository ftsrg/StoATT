package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder

class OrNode(vararg val inputs: FaultTreeNode): FaultTreeNode() {
    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        var ret = inputs[0].nonFailureAsMdd(order)
        for (idx in 1 until inputs.size) {
            ret = ret.intersection(inputs[idx].nonFailureAsMdd(order)) //De-morgan
        }
        return ret
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        var ret = inputs[0].failureAsMdd(order)
        for (idx in 1 until inputs.size) {
            ret = ret.union(inputs[idx].failureAsMdd(order))
        }
        return ret
    }

    override fun getBasicEvents(): Set<BasicEvent> {
        var ret = inputs[0].getBasicEvents()
        for (idx in 1 until inputs.size)
            ret = ret.union(inputs[idx].getBasicEvents())
        return ret
    }
}