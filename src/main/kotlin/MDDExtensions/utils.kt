package MDDExtensions

import hu.bme.mit.delta.java.mdd.JavaMddFactory
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable
import com.koloboke.collect.map.hash.HashObjObjMaps
import java.lang.UnsupportedOperationException
import java.math.BigInteger


infix fun MddHandle.union(other: MddHandle) = union(other)
infix fun MddHandle.intersection(other: MddHandle) = intersection(other)
infix fun MddHandle.minus(other: MddHandle) = minus(other)

/**
 * Returns the BDD representing the function that is true whenever the original is true not considering
 * the given variable. Works only with traceInfo-defined variables.
 */
fun withoutVar(mdd: MddHandle, ignoredVar: MddVariable): MddHandle {
    if(mdd.isTerminal) return mdd
    val builder = JavaMddFactory.getDefault().createTemplateBuilder()

    return if(mdd.variableHandle.variable.get().traceInfo == ignoredVar.traceInfo) {
        val combined = mdd[0].union(mdd[1])
        mdd.variableHandle.checkIn(builder.set(0, combined).set(1, combined).buildAndReset())
    } else {
        val thenNode = mdd[1]
        val elseNode = mdd[0]
        val new = builder.set(0, withoutVar(elseNode, ignoredVar))
                .set(1, withoutVar(thenNode, ignoredVar))
                .buildAndReset()
        mdd.variableHandle.checkIn(new)
    }
}

fun MddHandle.calculateNonzeroCount(): BigInteger {
    val height = this.variableHandle.height
    val cache: MutableMap<MddHandle, BigInteger> = HashObjObjMaps.newMutableMap()
    return calculateNonzeroCount(this, height, cache)
}

private fun calculateNonzeroCount(node: MddHandle, level: Int, cache: MutableMap<MddHandle, BigInteger>): BigInteger {
    val cached = cache.getOrDefault(node, null)
    if (cached != null) {
        return cached
    }
    if (node.isTerminal) {
        assert(level == 0)
        return if (!node.isTerminalZero) {
            BigInteger.ONE
        } else {
            BigInteger.ZERO
        }
    }
    //node.defaultValue()
    if (!node.defaultValue().isTerminalZero) {
        throw UnsupportedOperationException("Infinite set size - do not count it")
    }
    var ret: BigInteger = BigInteger.ZERO
    val width = node.variableHandle.variable.orElseThrow { AssertionError() }.domainSize
    for (i in 0 until width) {
        val res = calculateNonzeroCount(node[i], level - 1, cache)
        ret += res
    }
    val lRet = ret
    cache[node] = lRet
    return lRet
}