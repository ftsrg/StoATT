package faulttree

import faulttree.BasicEvent.Companion.BasicEventVar
import hu.bme.mit.delta.java.mdd.impl.DefaultJavaMddFactory
import hu.bme.mit.delta.mdd.LatticeDefinition
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable
import org.ejml.simple.SimpleMatrix
import solver.*


class FaultTree(val topNode: FaultTreeNode) {

    val functionalDeps = arrayListOf<FunctionalDependency>()

    private fun getOrderedVariables(): List<DFTVar> {
        val allVars = topNode.getVariables()

        // Order dynamic variables
        val degreeOrdered = allVars.values
                .sortedBy { it.dynamicallyRelatedVals.size }
                .toMutableList()
        //vars not having any dynamic relationships with other vars will be ordered using a static fault tree ordering scheme
        val staticVars = degreeOrdered.takeWhile { it.dynamicallyRelatedVals.size == 0 }
        degreeOrdered.removeAll(staticVars)
        val orderedVars = arrayListOf<DFTVar>()
        if(degreeOrdered.size > 0) {
            orderedVars.add(degreeOrdered[0])
            degreeOrdered.removeAt(0)
            var i = 0
            while (degreeOrdered.size > 0) {
                val Ai = degreeOrdered.intersect(orderedVars[i].dynamicallyRelatedVals)
                orderedVars.addAll(Ai)
                degreeOrdered.removeAll(Ai)
                i++
            }
        }

        // Order static events
        fun traverse(node: FaultTreeNode) {
            if(node is BasicEvent && !(orderedVars.contains(node.variable)))
                orderedVars.add(node.variable)
            else if(node is StaticGate)
                node.inputs.sortedBy { -it.getOrderingWeight() }.forEach(::traverse)
        }
        traverse(topNode)

        return orderedVars
    }

    fun getKronsumComponents(): List<SimpleMatrix> =
            getOrderedVariables()
            .filterIsInstance<BasicEventVar>()
            .map { it.getKronsumTerm() }

    fun nonFailureAsMdd(): MddHandle {
        val vars = getOrderedVariables()
        val factory = DefaultJavaMddFactory()
        val order = factory.createMddVariableOrder(LatticeDefinition.forSets())
        var prev: MddVariable? = null
        for (variable in vars) {
            prev = if (prev == null) order.createOnTop(variable.variableDescriptor) else order.createBelow(prev, variable.variableDescriptor)
        }

        return topNode.nonFailureAsMdd(order)
    }

    /**
     * Calculates the generator matrix of the fault tree's corresponding Markov chain, without taking absorption in
     * failure states into account (so in this chain, a failed system can get worse).
     */
    fun getBaseGenerator(): TTSquareMatrix {
        //TODO: handle single event
        //TODO: don't care values?
        val cores = arrayListOf<CoreTensor>()
        val vars = getOrderedVariables()
        for ((idx, variable) in vars.withIndex()) {
            cores.add(variable.getBaseCore(cores.lastOrNull()?.cols ?: 1, idx == vars.size-1))
        }
        val Mpre = TTSquareMatrix(TensorTrain(cores), Array(vars.size) { vars[it].variableDescriptor.domainSize })
        return Mpre-TTSquareMatrix.diag(Mpre*TTVector.ones(Mpre.modes))
    }

    /**
     * Calculates the generator matrix of the fault tree's corresponding Markov chain in the TT format,
     * and performs the modification needed for MTTF calculation on it.
     * @return the modified generator matrix in TT format
     */
    fun getModifiedGenerator(): TTSquareMatrix {
        val M = getBaseGenerator()
        val S = getModifierForMTTF(M)
        val res = M - S
        res.tt.round(0.0)
        return res
    }

    fun getModifierForMTTF(M: TTSquareMatrix): TTSquareMatrix {
        val stateMaskVector = getStateMaskVector()
        val meanExitRate=M.diagVect().norm()/M.numCols
        // TODO: PAND and SPARE might introduce new absorbing states in the original Markov chain
        val origAbsorbingIndicatorVector = TTVector(TensorTrain(
                ArrayList(List(stateMaskVector.modes.size) {
                    CoreTensor(stateMaskVector.modes[it], 1, 1).apply {
                        set(1, mat[r[1]])
                    }
                })
        ))
        val failureIndicatorVector = TTVector.ones(stateMaskVector.modes) - stateMaskVector
        val failureIndicatorMatrix = TTSquareMatrix.diag(failureIndicatorVector)
        return failureIndicatorMatrix * M - M * failureIndicatorMatrix + meanExitRate * TTSquareMatrix.diag(origAbsorbingIndicatorVector) - 2.0 * TTSquareMatrix.diag(M.diagVect().hadamard(failureIndicatorVector))
    }

    private fun applyFunctionalDependency(orig: TTSquareMatrix, functionalDependency: FunctionalDependency): TTSquareMatrix {
        var res = orig
        val orderedVars = getOrderedVariables()
        val trigger = functionalDependency.trigger
        assert(orderedVars.contains(trigger.variable)) {
            "The fault tree must contain the trigger event! \n" +
            "Basic event not found in the tree: ${trigger.name}"
        }

        val triggerIdx = orderedVars.indexOf(trigger.variable)
        for (dependentEvent in functionalDependency.dependentEvents) {
            assert(orderedVars.contains(dependentEvent.variable)) {
                "The fault tree must contain the dependent event! \n" +
                "Basic event not found in the tree: ${dependentEvent.name}"
            }

            //TODO: index in the ordered event list aren't the same as the core index if the fault tree has stateful gates
            val depIdx = orderedVars.indexOf(dependentEvent.variable)
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