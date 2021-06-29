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