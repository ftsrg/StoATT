package MDDExtensions

import hu.bme.mit.delta.java.mdd.JavaMddFactory
import hu.bme.mit.delta.mdd.MddHandle
import hu.bme.mit.delta.mdd.MddVariable

infix fun MddHandle.union(other: MddHandle) = union(other)
infix fun MddHandle.intersection(other: MddHandle) = intersection(other)

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