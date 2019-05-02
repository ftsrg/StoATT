package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder
abstract class FaultTreeNode {
    abstract fun getBasicEvents(): Set<BasicEvent>
    abstract fun failureAsMdd(order: MddVariableOrder): MddHandle
    abstract fun nonFailureAsMdd(order: MddVariableOrder): MddHandle

    open infix fun or(rhs: FaultTreeNode) = when (rhs) {
        is OrNode -> OrNode(this, *rhs.inputs)
        else -> OrNode(this, rhs)
    }

    open infix fun and(rhs: FaultTreeNode) = when(rhs) {
        is AndNode -> AndNode(this, *rhs.inputs)
        else -> AndNode(this, rhs)
    }
}

