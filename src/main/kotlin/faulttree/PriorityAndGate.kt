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

class PriorityAndGate(val name: String, vararg val inputs: FaultTreeNode): FaultTreeNode(false) {
    init {
        if(inputs.any {it.repairable}) throw UnsupportedOperationException("Dynamic gate with repairable input has undefined semantics!")
    }

    override fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBasicEvents(): Set<AbstractBasicEvent> {
        var ret = inputs[0].getBasicEvents()
        for (idx in 1 until inputs.size)
            ret = ret.union(inputs[idx].getBasicEvents())
        return ret
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build(arrayOf(1), true)
    }

    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build(arrayOf(0), true)
    }

    private var weightCached = -1.0
    override fun getOrderingWeight(): Double {
        // treated as an AND gate
        // doesn't really matter much, as all the corresponding variables will be processed by the DFT ordering procedure, not the weighted static FT scheme
        if(weightCached == -1.0) weightCached = inputs.fold(1.0) {agg, node-> agg * node.getOrderingWeight()}
        return weightCached
    }
}