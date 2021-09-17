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

package faulttree

import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.TTSquareMatrix
import kotlin.math.max
import kotlin.math.min

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

    fun apply(generator: TTSquareMatrix, orderedVariables: List<DFTVar>) {
        val changeMatrix = TTSquareMatrix.eye(generator.modes)
        val depIndices = ArrayList<Int>(dependentEvents.size)
        var trigIdx: Int = 0
        for ((idx, variable) in orderedVariables.withIndex()) {
            if(variable.variableDescriptor.traceInfo == trigger.name)
                trigIdx = idx
            else if(dependentEvents.any{it.name == variable.variableDescriptor.traceInfo})
                depIndices.add(idx)
        }
        val start = min(trigIdx, depIndices.min() ?: 0)
        val end = max(trigIdx, depIndices.max() ?: orderedVariables.size)
        for (idx in start..end) {
            if(idx == trigIdx) {
                if(idx == start) {

                } else if(idx == end) {

                } else {

                }
            } else if(depIndices.contains(idx)) {
                if(idx == start) {

                } else if(idx == end) {

                } else {

                }
            }
            TODO()
        }
    }
}