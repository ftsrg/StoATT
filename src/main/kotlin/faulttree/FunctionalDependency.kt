package faulttree

import hu.bme.mit.delta.mdd.MddVariableDescriptor

class FunctionalDependency(val trigger: BasicEvent, vararg val dependentEvents: BasicEvent) {
    fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
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