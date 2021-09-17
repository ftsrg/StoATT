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

import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*

/**
 * Represents a basic event with phase-type failure and repair distributions. The two distributions are given by the
 * rate matrix of a common underlying Markov chain (technically, the Markov chain represents the state space and
 * behaviour of the atomic component modelled by the basic event). The last numFailureStates states are considered
 * failed, the others are operational.
 */
class PHBasicEvent(name: String, val rateMatrix: SimpleMatrix, val numFailureStates: Int = 1) :
        AbstractBasicEvent(name, isRepairable(rateMatrix, numFailureStates) ) {
    override fun getVariable(): DFTVar {
        return variable
    }

    override fun getAbsorbingStatesAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        val absorbingStates = arrayListOf<Array<Int>>()
        for(i in 0 until rateMatrix.numCols()) {
            if(rateMatrix.row(i).elementSum() == 0.0) absorbingStates.add(arrayOf(i))
        }
        return builder.build(absorbingStates, true)
    }

    override fun getSteadyStateVector(): SimpleMatrix {
        val Q = rateMatrix
        val D = rateMatrix * ones(rateMatrix.numCols(), 1)
        for (i in 0 until Q.numRows()) Q[i,i] = -D[i]
        Q.setRow(Q.numRows()-1,0, *(DoubleArray(Q.numCols()) {1.0})) // normalization constraint
        val b = SimpleMatrix(rateMatrix.numCols(), 1)
        b[b.numElements-1] = 1.0
        return Q.T().solve(b)
    }

    companion object {
        // The basic event is repairable if at least one operational state is reachable from at least one failure state
        // in the BE's underlying Markov chain
        private fun isRepairable(rateMatrix: SimpleMatrix, numFailureStates: Int): Boolean {
            for (i in rateMatrix.numRows()-numFailureStates until rateMatrix.numRows()) {
                for (j in 0 until rateMatrix.numRows()-numFailureStates) {
                    if (rateMatrix[i,j] > 0.0) return true
                }
            }
            return false
        }

        class PHEventVar(val event: PHBasicEvent) : DFTVar(event.descriptor) {

            fun getKronsumTerm(): SimpleMatrix {
                return event.rateMatrix
            }

            override fun getBaseCore(prevRank: Int, isLast: Boolean): CoreTensor {
                when {
                    prevRank == 1 -> {
                        val newCore = CoreTensor(event.rateMatrix.numElements, 1, 2)
                        for (i in 0 until event.rateMatrix.numRows()) {
                            for (j in 0 until event.rateMatrix.numCols()) {
                                val otherChangePossible = if (i == j) 1.0 else 0.0
                                newCore.data[i * event.rateMatrix.numRows() + j] = mat[r[event.rateMatrix[i, j], otherChangePossible]]
                            }
                        }
                        return newCore
                    }
                    isLast -> {
                        val newCore = CoreTensor(event.rateMatrix.numElements, 2, 1)
                        for (i in 0 until event.rateMatrix.numRows()) {
                            for (j in 0 until event.rateMatrix.numCols()) {
                                val otherChangePossible = if (i == j) r[1.0] else r[0.0]
                                newCore.data[i * event.rateMatrix.numRows() + j] = mat[otherChangePossible, r[event.rateMatrix[i, j]]]
                            }
                        }
                        return newCore
                    }
                    else -> {
                        val newCore = CoreTensor(event.rateMatrix.numElements, 2, 2)
                        for (i in 0 until event.rateMatrix.numRows()) {
                            for (j in 0 until event.rateMatrix.numCols()) {
                                newCore.data[i * event.rateMatrix.numRows() + j] =
                                        if(i==j) eye(2)
                                        else mat[r[0, 0], r[event.rateMatrix[i, j], 0.0]]
                            }
                        }
                        return newCore
                    }
                }
            }

        }
    }

    private val descriptor = MddVariableDescriptor.create(name, rateMatrix.numRows())
    private val variable = PHEventVar(this)

    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build((0 until descriptor.domainSize-numFailureStates).map { arrayOf(it) }, true)
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build((descriptor.domainSize-numFailureStates until descriptor.domainSize).map { arrayOf(it) }, true)
    }

    override fun getBasicEvents(): Set<AbstractBasicEvent> {
        return hashSetOf(this)
    }

    override fun getOrderingWeight(): Double = 0.5

    override fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
        return hashMapOf(descriptor to this.variable)
    }
}