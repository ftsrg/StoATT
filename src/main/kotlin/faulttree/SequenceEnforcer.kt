package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder

class SequenceEnforcer(vararg val sequencedEventName: String): FaultTreeNode(false) {
    override fun getBasicEvents(): Set<BasicEvent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOrderingWeight(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init{ TODO() }
}