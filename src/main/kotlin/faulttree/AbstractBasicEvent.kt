package faulttree

import org.ejml.simple.SimpleMatrix

abstract class AbstractBasicEvent(val name: String, repairable: Boolean): FaultTreeNode(repairable) {
    abstract fun getSteadyStateVector(): SimpleMatrix
}