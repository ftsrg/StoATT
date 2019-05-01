package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder

abstract class FaultTreeNode {
    abstract fun getBasicEvents(): Set<BasicEvent>
    abstract fun failureAsMdd(order: MddVariableOrder): MddHandle
    abstract fun nonFailureAsMdd(order: MddVariableOrder): MddHandle

    infix fun or(rhs: FaultTreeNode) = OrNode(this, rhs)
    infix fun and(rhs: FaultTreeNode) = AndNode(this, rhs)
}