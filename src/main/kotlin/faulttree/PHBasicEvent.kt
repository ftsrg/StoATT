package faulttree

import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import hu.bme.mit.delta.mdd.MddVariableOrder
import org.ejml.simple.SimpleMatrix
import solver.CoreTensor
import solver.mat
import solver.r
import java.util.*

class PHBasicEvent(name: String, val rateMatrix: SimpleMatrix) :
        /* The BE is repairable, if the exit rate of the failed state, which is the 1st one by convention, is non-zero */
        AbstractBasicEvent(name, rateMatrix[1,1]==0.0) {

    companion object {
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
                                val otherChangePossible = if (i == j) r[1.0, 0.0] else r[0.0, 0.0]
                                newCore.data[i * event.rateMatrix.numRows() + j] = mat[
                                        otherChangePossible, r[event.rateMatrix[i, j], 0.0]
                                ]
                            }
                        }
                        return newCore
                    }
                }
            }

        }
    }

    private val descriptor = MddVariableDescriptor.create(name, 2)
    private val variable = PHEventVar(this)

    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build(((0 until descriptor.domainSize) - listOf(1)).map { arrayOf(it) }, true)
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(listOf(name)))
        return builder.build(arrayOf(1), true)
    }

    override fun getBasicEvents(): Set<AbstractBasicEvent> {
        return hashSetOf(this)
    }

    override fun getOrderingWeight(): Double = 0.5

    override fun getVariables(): HashMap<MddVariableDescriptor, DFTVar> {
        return hashMapOf(descriptor to this.variable)
    }
}