package faulttree

import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder

class BasicEvent(val rate: Double, val name: String): FaultTreeNode() {
    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build(arrayOf(0), true)
    }

    val mddVariable = MddVariableDescriptor.create(name, 2)

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build(arrayOf(1), true)
    }

    override fun getBasicEvents(): Set<BasicEvent> {
        return hashSetOf(this)
    }
}