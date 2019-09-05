package faulttree

import hu.bme.mit.delta.mdd.MddVariableDescriptor
import solver.CoreTensor

abstract class DFTVar(
        val variableDescriptor: MddVariableDescriptor,
        val dynamicallyRelatedVals: MutableSet<DFTVar> = hashSetOf<DFTVar>()) {
    abstract fun getBaseCore(prevRank: Int, isLast: Boolean): CoreTensor
}

fun HashMap<MddVariableDescriptor, DFTVar>.modifiedUnion(other: HashMap<MddVariableDescriptor, DFTVar>): HashMap<MddVariableDescriptor, DFTVar> {
    val res = this.clone() as HashMap<MddVariableDescriptor, DFTVar>
    for ((key, value) in other.entries) {
        if(res.containsKey(key)) res[key]!!.dynamicallyRelatedVals.addAll(value.dynamicallyRelatedVals)
        else res.put(key, value)
    }
    return res
}