package faulttree

import hu.bme.mit.delta.java.mdd.impl.DefaultJavaMddFactory
import hu.bme.mit.delta.mdd.LatticeDefinition
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import solver.*
import java.util.*


class FaultTree(val topNode: FaultTreeNode) {

    fun nonFailureAsMdd(): MddHandle {
        val basicEvents = getOrderdEvents()
        val factory = DefaultJavaMddFactory()
        val order = factory.createMddVariableOrder(LatticeDefinition.forSets())
        var prev: MddVariable? = null
        for (event in basicEvents) {
            val descriptor = MddVariableDescriptor.create(event.name, 2)
            prev = if (prev == null) order.createOnTop(descriptor) else order.createBelow(prev, descriptor)
        }

        return topNode.nonFailureAsMdd(order)
    }

    fun getOrderdEvents(): List<BasicEvent> {
        //TODO: var ordering
        return ArrayList(topNode.getBasicEvents())
    }

    fun getTransientGenerator(): TTSquareMatrix {
        //TODO: handle single event
        //TODO: don't care values?
        //TODO: think about transpositions
        val cores = arrayListOf<CoreTensor>()
        val events = getOrderdEvents()
        for ((idx, event) in events.withIndex()) {
            when (idx) {
                0 -> {
                    val newCore = CoreTensor(4, 1, 2)
                    newCore.data[0] = mat[r[0.0, 1.0]]
                    newCore.data[1] = mat[r[event.rate, 0.0]]
                    //              newCore.data[2] = mat[r[0.0, 0.0]]
                    newCore.data[3] = mat[r[0.0, 1.0]]
                    cores.add(newCore)
                }
                events.lastIndex -> {
                    val newCore = CoreTensor(4, 2, 1)
                    newCore.data[0] = mat[r[1.0], r[0.0]]
                    newCore.data[1] = mat[r[0.0], r[event.rate]]
                    //              newCore.data[2] = mat[r[0.0], r[0.0]]
                    newCore.data[3] = mat[r[1.0], r[0.0]]
                    cores.add(newCore)
                }
                else -> {
                    val newCore = CoreTensor(4, 2, 2)
                    newCore[0] = eye(2)
                    newCore[1] = mat[
                            r[0.0, 0.0],
                            r[event.rate, 0.0]
                    ]
                    //              newCore[2] = SimpleMatrix(2,2)
                    newCore[3] = eye(2)
                    cores.add(newCore)
                }
            }
        }
        val M = TTSquareMatrix(TensorTrain(cores), Array(events.size) { 2 })
        val stateMaskVector = getStateMaskVector()
        val maskMatrix = stateMaskVector.outerProduct(stateMaskVector)
        return (M - TTSquareMatrix.diag(M * TTVector.ones(M.modes))).hadamard(maskMatrix) + TTSquareMatrix.diag(TTVector.ones(stateMaskVector.modes)-stateMaskVector)
    }

    public fun getStateMaskVector(): TTVector {
        val cores = arrayListOf<CoreTensor>()
        val mdd = nonFailureAsMdd()
        var currHandles = arrayListOf(mdd)
        while (!(currHandles.isEmpty() || currHandles[0].isTerminal)) {
            val nextHandlesSet = hashSetOf<MddHandle>()
            for (handle in currHandles) {
                nextHandlesSet.add(handle[0])
                nextHandlesSet.add(handle[1])
            }
            val nextHandles = ArrayList(nextHandlesSet)
            if (nextHandles[0].isTerminal) {
                cores.add(CoreTensor(2, currHandles.size, 1))
                for ((currIdx, currHandle) in currHandles.withIndex()) {
                    if (!currHandle[0].isTerminalZero)
                        cores.last()[0][currIdx, 0] = 1.0
                    if (!currHandle[1].isTerminalZero)
                        cores.last()[1][currIdx, 0] = 1.0
                }
            } else {
                cores.add(CoreTensor(2, currHandles.size, nextHandles.size))
                for ((currIdx, currHandle) in currHandles.withIndex()) {
                    for ((nextIdx, nextHandle) in nextHandles.withIndex()) {
                        if (currHandle[0] == nextHandle)
                            cores.last()[0][currIdx, nextIdx] = 1.0
                        if (currHandle[1] == nextHandle)
                            cores.last()[1][currIdx, nextIdx] = 1.0
                    }
                }
            }
            currHandles = nextHandles
        }
        return TTVector(TensorTrain(cores))
    }

    public fun getRateMaskMatrix(): TTSquareMatrix {
        val cores = arrayListOf<CoreTensor>()
        val mdd = nonFailureAsMdd()
        var currHandles = arrayListOf(mdd)
        while (!(currHandles.isEmpty() || currHandles[0].isTerminal)) {
            val nextHandlesSet = hashSetOf<MddHandle>()
            for (handle in currHandles) {
                nextHandlesSet.add(handle[0])
                nextHandlesSet.add(handle[1])
            }
            val nextHandles = ArrayList(nextHandlesSet)
            cores.add(CoreTensor(4, currHandles.size, nextHandles.size))
            for ((currIdx, currHandle) in currHandles.withIndex()) {
                for ((nextIdx, nextHandle) in nextHandles.withIndex()) {
                    if (currHandle[0] == nextHandle) {
                        //TODO: bizt n jo igy
                        cores.last()[0][currIdx, nextIdx] = 1.0
                        cores.last()[2][currIdx, nextIdx] = 1.0
                    }
                    if (currHandle[1] == nextHandle) {
                        cores.last()[1][currIdx, nextIdx] = 1.0
                        cores.last()[3][currIdx, nextIdx] = 1.0
                    }
                }
            }
            currHandles = nextHandles
        }
        return TTSquareMatrix(TensorTrain(cores), Array(cores.size) { 2 })
    }
}