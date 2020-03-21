package gspn.rateexpressions

import gspn.Place
import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.TTVector

class
PlaceRef(val place: Place, val function: (Int)->Double = {it.toDouble()}): RateExpression() {
    override fun toCanonical() = this
    override fun deepCopy() = PlaceRef(place, function)
    override fun toTT(varOrder: MddVariableOrder): TTVector {
        val v = TTVector.ones(varOrder.map { it.domainSize }.toTypedArray())
        val idx = varOrder.indexOfFirst { it.traceInfo == place.name }
        repeat(v.tt.cores[idx].modeLength) {
            v.tt.cores[idx][it][0,0] = function(it)
        }
        return v
    }
}