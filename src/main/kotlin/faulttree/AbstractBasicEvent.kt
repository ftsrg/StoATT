package faulttree

import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder
import org.ejml.simple.SimpleMatrix

abstract class AbstractBasicEvent(val name: String, repairable: Boolean): FaultTreeNode(repairable) {
    abstract fun getSteadyStateVector(): SimpleMatrix
    abstract fun getVariable(): DFTVar
    abstract fun getAbsorbingStatesAsMdd(order: MddVariableOrder): MddHandle
}