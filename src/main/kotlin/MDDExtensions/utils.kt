package MDDExtensions

import hu.bme.mit.delta.java.mdd.JavaMddFactory
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable

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
        mdd.variableHandle.checkIn(builder.setDefault(combined).buildAndReset())
    } else {
        val thenNode = mdd[1]
        val elseNode = mdd[0]
        val new = builder.setDefault(withoutVar(elseNode, ignoredVar))
                .set(1, withoutVar(thenNode, ignoredVar))
                .buildAndReset()
        mdd.variableHandle.checkIn(new)
    }
}

/**
 * Returns the cardinality of the set represented by this MDD considered as a BDD. In other words, returns the number
 * of paths from the root of the BDD to a non-zero leaf.
 */
fun MddHandle.cardinality(): Int {
    if(this.isTerminalZero) return 0
    if(this.isTerminal) return 1
    //TODO: cache
    return this[0].cardinality() + this[1].cardinality()
}