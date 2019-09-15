package faulttree

import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder

class FunctionalDependency(val trigger: BasicEvent, vararg val dependentEvents: BasicEvent): FaultTreeNode(false) {
    init {
        if(trigger.repairable || dependentEvents.any { it.repairable } )
            throw UnsupportedOperationException("Dynamic gate with reparable input has undefined semantics!")
    }

    override fun getBasicEvents(): Set<BasicEvent> {
        return setOf(trigger, *dependentEvents)
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        // dummy output
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf()))
        return builder.build(arrayListOf(), false)
    }

    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        // dummy output
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf()))
        return builder.build(arrayListOf(), true)
    }

    override fun getOrderingWeight(): Double {
        // dummy weight, all of the child variables are ordered according to the dynamic orderig
        return 0.0
    }

    override fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
        val variables = trigger.getVariables()
        val triggerVar = variables.values.first()
        for (dependentEvent in dependentEvents) {
            val depVar = dependentEvent.getVariables().values.first()
            depVar.dynamicallyRelatedVals.add(triggerVar)
            triggerVar.dynamicallyRelatedVals.add(depVar)
            variables.put(depVar.variableDescriptor, depVar)
        }
        return variables
    }
}