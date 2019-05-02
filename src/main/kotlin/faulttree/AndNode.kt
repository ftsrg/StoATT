package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder

class AndNode(vararg val inputs: FaultTreeNode) : FaultTreeNode() {
    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        var ret = inputs[0].nonFailureAsMdd(order)
        for (idx in 1 until inputs.size) {
            ret = ret.union(inputs[idx].nonFailureAsMdd(order)) // De-Morgan
        }
        return ret
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        var ret = inputs[0].failureAsMdd(order)
        for (idx in 1 until inputs.size) {
            ret = ret.intersection(inputs[idx].failureAsMdd(order))
        }
        return ret
    }

    override fun getBasicEvents(): Set<BasicEvent> {
        var ret = inputs[0].getBasicEvents()
        for (idx in 1 until inputs.size)
            ret = ret.union(inputs[idx].getBasicEvents())
        return ret
    }

    override infix fun and(rhs: FaultTreeNode) = when (rhs) {
        is AndNode -> AndNode(*inputs, *rhs.inputs)
        else -> AndNode(*inputs, rhs)
    }
}