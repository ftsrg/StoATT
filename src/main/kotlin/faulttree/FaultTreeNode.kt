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

