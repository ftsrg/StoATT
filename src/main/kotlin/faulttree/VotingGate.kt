package faulttree

import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariableOrder

/**
 * @param k The number of inputs that must fail for the gate to fail
 */
class VotingGate(val k: Int, vararg inputs: FaultTreeNode): StaticGate(*inputs) {
    private var weightCached = -1.0
    override fun getOrderingWeight(): Double {
        if(weightCached == -1.0) {
            var prod = 1.0
            fun combHelper(prev: Array<Int>) {
                if (prev.size == k) {
                    prod *= 1.0 - prev.fold(1.0) { agg, nodeIdx -> agg * (1.0 - inputs[nodeIdx].getOrderingWeight()) }
                } else {
                    for (i in (prev.lastOrNull() ?: -1) + 1 until inputs.size - (k - prev.size) + 1)
                        combHelper(arrayOf(*prev, i))
                }
            }
            combHelper(arrayOf())
            weightCached = 1.0-prod
        }
        return weightCached
    }

    override fun getBasicEvents(): Set<AbstractBasicEvent> {
        var ret = inputs[0].getBasicEvents()
        for (idx in 1 until inputs.size)
            ret = ret.union(inputs[idx].getBasicEvents())
        return ret
    }

    override fun failureAsMdd(order: MddVariableOrder): MddHandle {
        val basicEvents = getBasicEvents()
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(basicEvents.map { it.name }))
        var ret = builder.build(Array(basicEvents.size) {1}, false)

        val inputMdds = inputs.map { it.failureAsMdd(order) }

        val combinations = arrayListOf<Collection<MddHandle>>()
        fun combHelper(prev: Array<Int>) {
            if(prev.size == k) {
                combinations.add(prev.map { idx -> inputMdds[idx] })
            } else {
                for(i in (prev.lastOrNull() ?: -1)+1 until inputs.size-(k-prev.size)+1)
                    combHelper(arrayOf(*prev, i))
            }
        }
        combHelper(arrayOf())

        for (combination in combinations) {
            val currTerm = combination.reduce {acc, curr -> acc.intersection(curr)}
            ret = ret.union(currTerm)
        }

        return ret
    }

    override fun nonFailureAsMdd(order: MddVariableOrder): MddHandle {
        val basicEvents = getBasicEvents()
        val builder = MddBuilder<Boolean>(order.createSignatureFromTraceInfos(basicEvents.map { it.name }))
        var ret = builder.build(Array(basicEvents.size) {0}, true)

        val inputMdds = inputs.map { it.nonFailureAsMdd(order) }

        val m = inputs.size - k + 1 //At least m inputs must be functioning
        val combinations = arrayListOf<Collection<MddHandle>>()
        fun combHelper(prev: Array<Int>) {
            if(prev.size == m) {
                combinations.add(prev.map { idx -> inputMdds[idx] })
            } else {
                for(i in (prev.lastOrNull() ?: -1)+1 until inputs.size-(m - prev.size)+1)
                    combHelper(arrayOf(*prev, i))
            }
        }
        combHelper(arrayOf())

        for (combination in combinations) {
            val currTerm = combination.reduce {acc, curr -> acc.intersection(curr)}
            ret = ret.union(currTerm)
        }

        return ret
    }

}