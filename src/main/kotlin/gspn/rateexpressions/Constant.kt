package gspn.rateexpressions

import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.TTVector

class Constant(val value: Double): RateExpression() {

    override fun toCanonical() = this
    override fun deepCopy() = Constant(value)
    override fun toTT(varOrder: MddVariableOrder): TTVector {
        val modes = varOrder.map { it.domainSize }
        return TTVector.ones(modes.toTypedArray()) * value
    }
}