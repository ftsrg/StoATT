package gspn.rateexpressions

import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.TTVector

abstract class RateExpression {

    abstract fun toCanonical(): RateExpression
    abstract fun deepCopy(): RateExpression
    abstract fun toTT(varOrder: MddVariableOrder): TTVector

    open operator fun times(other: RateExpression) = Product(listOf(this, other))
    open operator fun plus(other: RateExpression) = Sum(listOf(this, other))
}