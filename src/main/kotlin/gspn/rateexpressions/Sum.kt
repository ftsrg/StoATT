package gspn.rateexpressions

import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.TTVector

class Sum(val terms: List<RateExpression>): RateExpression() {
    override fun toCanonical(): RateExpression {
        val canonTerms =
                terms.map(RateExpression::toCanonical)
                        .flatMap { if(it is Sum) it.terms else listOf(it) }.toMutableList()
        val const = canonTerms.filterIsInstance<Constant>().fold(0.0) { acc, c -> acc + c.value}
        if(const != 0.0) {
            canonTerms.removeAll { it is Constant }
            canonTerms += Constant(const)
        }
        val placeRefs = canonTerms.filterIsInstance<PlaceRef>()
        val places = placeRefs.map(PlaceRef::place).toSet()
        val reducedPlaceRefs = arrayListOf<PlaceRef>()
        for (place in places) {
            val relevantFunctions = placeRefs.filter { it.place == place }.map { it.function }
            if(relevantFunctions.size == 1)
                reducedPlaceRefs.add(PlaceRef(place, relevantFunctions[0]))
            else if(relevantFunctions.isNotEmpty())
                reducedPlaceRefs.add(PlaceRef(place) { x -> relevantFunctions.fold(0.0) { acc, f -> acc + f(x) } })
        }
        canonTerms.removeAll { it is PlaceRef }
        canonTerms.addAll(reducedPlaceRefs)
        return Sum(canonTerms)
    }

    override fun deepCopy() = Sum(terms.map { it.deepCopy() })

    override fun toTT(varOrder: MddVariableOrder): TTVector {
//        val idxOf = hashMapOf<String, Int>()
//        for ((idx, variable) in varOrder.withIndex()) {
//            idxOf[variable.traceInfo as String] = idx
//        }
//        val products = terms.filterIsInstance<Product>()
//        val allRefs =
//                (products.flatMap { it.terms.filterIsInstance<PlaceRef>() } +
//                 terms.filterIsInstance<PlaceRef>()).sortedBy { idxOf[it.place.name] }
//
//        for (prod in products){
//        }
        //TODO: compute smaller representation
        return terms.map { it.toTT(varOrder) }.reduce(TTVector::plus)
    }

    override operator fun plus(other: RateExpression) = Sum(terms+listOf(other))
}