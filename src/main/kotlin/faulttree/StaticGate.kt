package faulttree

import hu.bme.mit.delta.mdd.MddVariableDescriptor

abstract class StaticGate(vararg val inputs: FaultTreeNode) : FaultTreeNode(inputs.any{it.repairable}) {
    override fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
        return inputs.fold(hashMapOf()) { acc, next -> acc.modifiedUnion(next.getVariables()) }
    }

    override fun getBasicEvents(): Set<BasicEvent> {
        var ret = inputs[0].getBasicEvents()
        for (idx in 1 until inputs.size)
            ret = ret.union(inputs[idx].getBasicEvents())
        return ret
    }
}