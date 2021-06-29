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
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder
abstract class FaultTreeNode(val repairable: Boolean) {
    abstract fun getBasicEvents(): Set<AbstractBasicEvent>
    abstract fun failureAsMdd(order: MddVariableOrder): MddHandle
    abstract fun nonFailureAsMdd(order: MddVariableOrder): MddHandle
    abstract fun getOrderingWeight(): Double

    /**
     * Returns the DFT variables of this node and its children as HashMap, so that the properties of a variable can
     * be efficiently accessed using its MDD variable descriptor.
     * The dynamic relations of the returned variables are set to those that are known from the node's subtree
     */
    abstract fun getVariables(): HashMap<MddVariableDescriptor, DFTVar>

    open infix fun or(rhs: FaultTreeNode) = when (rhs) {
        is OrGate -> OrGate(this, *rhs.inputs)
        else -> OrGate(this, rhs)
    }

    open infix fun and(rhs: FaultTreeNode) = when(rhs) {
        is AndGate -> AndGate(this, *rhs.inputs)
        else -> AndGate(this, rhs)
    }
}

