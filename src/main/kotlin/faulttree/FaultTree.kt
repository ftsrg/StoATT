package faulttree

import hu.bme.mit.delta.java.mdd.impl.DefaultJavaMddFactory
import hu.bme.mit.delta.mdd.LatticeDefinition
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*


class FaultTree(val topNode: FaultTreeNode) {

    val functionalDeps = arrayListOf<FunctionalDependency>()

    private fun getOrderedVariables(): Collection<MddVariableDescriptor> {
        val orderedVars = arrayListOf<MddVariableDescriptor>()
        val basicEvents = topNode.getBasicEvents()
        val basicEventVars =
                basicEvents.map { it.name to MddVariableDescriptor.create(it.name, 2) }.toMap()
        //TODO: PAND state variables
        val allVars = basicEventVars
        val allVarNames = basicEvents.map { it.name }
        val dependencyGraphNeighbors =
                allVarNames.map { it to hashSetOf<String>() }.toMap()
        // Functional dependencies
        for (dep in functionalDeps) {
            val trigName = dep.trigger.name
            for (dependentEvent in dep.dependentEvents) {
                val depName = dependentEvent.name
                dependencyGraphNeighbors[trigName]?.add(depName) ?: throw RuntimeException("Trigger event not found")
                dependencyGraphNeighbors[depName]?.add(trigName) ?: throw RuntimeException("Dependent event not found")
            }
        }

        //TODO: SEQ gates
        //TODO: PAND gates

        val degreeOrdered = allVarNames
                .sortedBy { dependencyGraphNeighbors[it]!!.size  }
                .toMutableList()
        //vars not having any dynamic relationships with other vars will be ordered using a static fault tree ordering scheme
        val staticVars = degreeOrdered.takeWhile { dependencyGraphNeighbors[it]!!.size == 0 }
        degreeOrdered.removeAll(staticVars)
        val orderedVarNames = arrayListOf<String>()
        orderedVarNames.add(degreeOrdered[0])
        degreeOrdered.removeAt(0)
        var i = 0
        while (degreeOrdered.size > 0) {
            val Ai = degreeOrdered.intersect(dependencyGraphNeighbors[orderedVarNames[i]]!!)
            orderedVarNames.addAll(Ai)
            degreeOrdered.removeAll(Ai)
            i++
        }

        TODO("order static events")



        return orderedVarNames.map { allVars[it]!! }
    }

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
                    //newCore.data[2] = mat[r[0.0, 0.0]]
                    newCore.data[3] = mat[r[0.0, 1.0]]
                    cores.add(newCore)
                }
                events.lastIndex -> {
                    val newCore = CoreTensor(4, 2, 1)
                    newCore.data[0] = mat[r[1.0], r[0.0]]
                    newCore.data[1] = mat[r[0.0], r[event.rate]]
                    //newCore.data[2] = mat[r[0.0], r[0.0]]
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
                    //newCore[2] = SimpleMatrix(2,2)
                    newCore[3] = eye(2)
                    cores.add(newCore)
                }
            }
        }
        val M = TTSquareMatrix(TensorTrain(cores), Array(events.size) { 2 })
        val stateMaskVector = getStateMaskVector()
        val maskMatrix = stateMaskVector.outerProduct(stateMaskVector)
        return (M - TTSquareMatrix.diag(M * TTVector.ones(M.modes))).hadamard(maskMatrix) + TTSquareMatrix.diag(TTVector.ones(stateMaskVector.modes) - stateMaskVector)
    }

    private fun applyFunctionalDependency(orig: TTSquareMatrix, functionalDependency: FunctionalDependency): TTSquareMatrix {
        var res = orig
        val orderedEvents = getOrderdEvents()
        val trigger = functionalDependency.trigger
        assert(orderedEvents.contains(trigger)) {
            "The fault tree must contain the trigger event! \n" +
            "Basic event not found in the tree: ${trigger.name}"
        }

        val triggerIdx = orderedEvents.indexOf(trigger)
        for (dependentEvent in functionalDependency.dependentEvents) {
            assert(orderedEvents.contains(dependentEvent)) {
                "The fault tree must contain the dependent event! \n" +
                "Basic event not found in the tree: ${dependentEvent.name}"
            }

            //TODO: index in the ordered event list aren't the same as the core index if the fault tree has stateful gates
            val depIdx = orderedEvents.indexOf(dependentEvent)
            val changeMatrix = TTSquareMatrix.eye(orig.modes)
            val leftCore = CoreTensor(4, 1, 2)
            val rightCore = CoreTensor(4, 2, 1)
            if(triggerIdx < depIdx) {
                leftCore[0] = mat[r[1.0/2.0, -1.0/2.0]] //trigger 0,0
                leftCore[1] = mat[r[0, 0]] //trigger 0,1
                leftCore[2] = mat[r[0, 0]] //trigger 1,0
                leftCore[3] = mat[r[1.0/2.0, 1.0/2.0]] //trigger 1,1
                changeMatrix.tt.setCore(triggerIdx, leftCore)

                rightCore[0] = mat[r[1, -1]].T() //dependent 0,0
                rightCore[1] = mat[r[1, 1]].T() //dependent 0,1
                rightCore[2] = mat[r[0, 0]].T() //dependent 1,0
                rightCore[3] = mat[r[2, 0]].T() //dependent 1,1
                changeMatrix.tt.setCore(depIdx, rightCore)
            } else {
                leftCore[0] = mat[r[1, -1]] //dependent 0,0
                leftCore[1] = mat[r[1, 1]] //dependent 0,1
                leftCore[2] = mat[r[0, 0]] //dependent 1,0
                leftCore[3] = mat[r[2, 0]] //dependent 1,1
                changeMatrix.tt.setCore(depIdx, leftCore)

                rightCore[0] = mat[r[1.0/2.0, -1.0/2.0]].T() //trigger 0,0
                rightCore[1] = mat[r[0, 0]].T() //trigger 0,1
                rightCore[2] = mat[r[0, 0]].T() //trigger 1,0
                rightCore[3] = mat[r[1.0/2.0, 1.0/2.0]].T() //trigger 1,1
                changeMatrix.tt.setCore(triggerIdx, rightCore)
            }

            // Set cores between the trigger and the dependent cores
            for (coreIdx in triggerIdx + 1 until depIdx) {
                val currCore = changeMatrix.tt.cores[coreIdx]
                for (modeIdx in 0 until currCore.modeLength) {
                    if(currCore[modeIdx][0] == 1.0){
                        currCore[modeIdx] = eye(2)
                    } else {
                        currCore[modeIdx] = SimpleMatrix(3,3)
                    }
                }
                currCore.updateDimensions()
            }

            res = res*changeMatrix //TODO: optimize?
        }
        return res
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
}