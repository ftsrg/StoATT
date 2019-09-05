package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder

class AndGate(vararg inputs: FaultTreeNode) : StaticGate(*inputs) {

    private var weightCached = -1.0
    override fun getOrderingWeight(): Double {
        if(weightCached == -1.0) weightCached = inputs.fold(1.0) {agg, node-> agg * node.getOrderingWeight()}
        return weightCached
    }

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

    override infix fun and(rhs: FaultTreeNode) = when (rhs) {
        is AndGate -> AndGate(*inputs, *rhs.inputs)
        else -> AndGate(*inputs, rhs)
    }
}