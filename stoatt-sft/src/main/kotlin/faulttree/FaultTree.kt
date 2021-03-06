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

import MDDExtensions.toSparseTTDiagMatrix
import MDDExtensions.toTensorTrain
import MDDExtensions.union
import MDDExtensions.withoutVar
import faulttree.BasicEvent.Companion.BasicEventVar
import hu.bme.mit.delta.java.mdd.MddVariableOrder
import hu.bme.mit.delta.java.mdd.impl.DefaultJavaMddFactory
import hu.bme.mit.delta.mdd.LatticeDefinition
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable
import org.ejml.data.DMatrixSparseCSC
import org.ejml.ops.ConvertDMatrixStruct
import org.ejml.simple.SimpleMatrix
import solver.*
import kotlin.math.max


class FaultTree(val topNode: FaultTreeNode) {

    private val nonFailureAsMdd: MddHandle
    private val varOrdering: MddVariableOrder
    init {
        val start = System.currentTimeMillis()
        val vars = getOrderedVariables()
        val factory = DefaultJavaMddFactory()
        val order = factory.createMddVariableOrder(LatticeDefinition.forSets())
        var prev: MddVariable? = null
        for (variable in vars) {
            prev = if (prev == null) order.createOnTop(variable.variableDescriptor) else order.createBelow(prev, variable.variableDescriptor)
        }
        nonFailureAsMdd = topNode.nonFailureAsMdd(order)
        val end = System.currentTimeMillis()
        println("nonFailureAsMdd: ${end-start}ms")
        varOrdering = order
    }

    fun getVariableOrdering(): MddVariableOrder {
        return varOrdering
    }

    /**
     * Returns an MDD representing the set of states which are either working or
     * are reachable in one step from a working state.
     */
    fun getWorkingAndJustFailedAsMDD(): MddHandle {
        val working = nonFailureAsMdd
        return varOrdering.map { withoutVar(working, it) }
                .reduce { acc, mdd -> acc union mdd }
    }

    /**
     * Returns the highest exit rate in the underlying base Markov-chain, without taking the failure function into
     * account.
     */
    fun getHighestExitRate() =
        getOrderedVariables().filterIsInstance<BasicEventVar>().sumByDouble { max(it.event.failureRate, it.event.repairRate) }


    fun getOrderedVariables(): List<DFTVar> {
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
            if(node is AbstractBasicEvent && !(orderedVars.contains(node.getVariable())))
                orderedVars.add(node.getVariable())
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
        return nonFailureAsMdd
    }

    fun getBaseRateMatrix(): TTSquareMatrix {
        //TODO: handle single event
        val cores = arrayListOf<CoreTensor>()
        val vars = getOrderedVariables()
        for ((idx, variable) in vars.withIndex()) {
            cores.add(variable.getBaseCore(cores.lastOrNull()?.cols ?: 1, idx == vars.size-1))
        }
        return TTSquareMatrix(TensorTrain(cores), Array(vars.size) { vars[it].variableDescriptor.domainSize })
    }

    /**
     * Calculates the generator matrix of the fault tree's corresponding Markov chain, without taking absorption in
     * failure states into account (so in this chain, a failed system can get worse).
     */
    fun getBaseGenerator(): TTSquareMatrix {
        val Mpre = getBaseRateMatrix()
        return Mpre- TTSquareMatrix.diag(Mpre* TTVector.ones(Mpre.modes))
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
//        res.tt.roundRelative(0.0)
        return res
    }


    fun getModifiedGeneratorAsSparseCores(): List<Sparse2DCoreTensor> {
        //TODO: this is just a quick proto to check if sparse AMEn performs better
        //      then the dense one. Creating the dense cores and then transforming
        //      to sparse is very inefficient and should be avoided.
        val M = getBaseGenerator()
        val S = getModifierForMTTF(M)
        val res = M - S
        val sparseCores = res.tt.cores.mapIndexed { idx, core ->
            val data = Array(res.modes[idx]) { i ->
                Array(res.modes[idx]) { j ->
                    core[i,j].toSparse()
                }
            }
            Sparse2DCoreTensor(res.modes[idx], core.rows, core.cols, data)
        }
        return sparseCores
    }

    /***
     * Returns the indicator vector of the absorbing state set without taking the system failure formula into account.
     */
    fun getStrictAbsorbingIndicatorVector(): TTVector {
        val order = getVariableOrdering()
        val origAbsorbingMdd =
                topNode.getBasicEvents().stream()
                        .map { it.getAbsorbingStatesAsMdd(order) }
                        .reduce(MddHandle::intersection).get()
        return TTVector(origAbsorbingMdd.toTensorTrain())
    }

    fun getModifierForMTTF(M: TTSquareMatrix): TTSquareMatrix {
        val stateMaskVector = getOperationalIndicatorVector()
        val D = M.diagVect()
        for (core in D.tt.cores) {
            for (i in (0 until core.data.size)){
                core.data[i] = core.data[i] / core.modeLength.toDouble()
            }
        }
        val meanExitRate=-(D* TTVector.ones(D.modes))
        // TODO: PAND and SPARE might introduce new absorbing states in the original Markov chain
        val origAbsorbingIndicatorVector = getStrictAbsorbingIndicatorVector()
        val failureIndicatorVector = TTVector.ones(stateMaskVector.modes) - stateMaskVector
        val failureIndicatorMatrix = TTSquareMatrix.diag(failureIndicatorVector)
        return failureIndicatorMatrix * M + M * failureIndicatorMatrix + meanExitRate * TTSquareMatrix.diag(origAbsorbingIndicatorVector) - 2.0 * TTSquareMatrix.diag(M.diagVect().hadamard(failureIndicatorVector))
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

    public fun getOperationalIndicatorVector(): TTVector {
        val cores = arrayListOf<CoreTensor>()
        val mdd = nonFailureAsMdd()

        return TTVector(mdd.toTensorTrain())
    }
}