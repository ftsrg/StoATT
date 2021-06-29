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

package gspn.rateexpressions

import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.CoreTensor
import solver.TTVector
import solver.TensorTrain

class Product(val terms: List<RateExpression>) : RateExpression() {
    override fun toCanonical(): RateExpression {
        val canonTerms =
                terms.map(RateExpression::toCanonical)
                        .flatMap { if (it is Product) it.terms else listOf(it) }.toMutableList()
        if (canonTerms.size <= 1) return Product(canonTerms)
        val firstSum = canonTerms.firstOrNull { it is Sum }
        val const = canonTerms.filterIsInstance<Constant>().fold(1.0) { acc, c -> acc * c.value }
        if (const != 1.0) {
            canonTerms.removeAll { it is Constant }
            canonTerms += Constant(const)
        }
        if (firstSum == null) return Product(canonTerms)
        val newTerms = canonTerms
                .filter { it != firstSum }
                .map { Product(listOf(it, firstSum.deepCopy())).toCanonical() }
                .toMutableList()
        val placeRefs = newTerms.filterIsInstance<PlaceRef>()
        val places = placeRefs.map(PlaceRef::place).toSet()
        val reducedPlaceRefs = arrayListOf<PlaceRef>()
        for (place in places) {
            val relevantFunctions = placeRefs.filter { it.place == place }.map { it.function }
            if(relevantFunctions.isNotEmpty())
                reducedPlaceRefs.add(PlaceRef(place) { x -> relevantFunctions.fold(1.0) { acc, f -> acc * f(x) } })
        }
        newTerms.removeAll { it is PlaceRef }
        newTerms.addAll(reducedPlaceRefs)
        return Sum(newTerms)
    }

    override fun deepCopy() = Product(terms.map { it.deepCopy() })

    override fun toTT(varOrder: MddVariableOrder): TTVector {
        val cores = varOrder.map { variable ->
            val core = CoreTensor(variable.domainSize, 1, 1)
            repeat(variable.domainSize) {idx ->
                core[idx][0,0] = (terms
                        .firstOrNull { it is PlaceRef && it.place.name == variable.traceInfo } as? PlaceRef)
                        ?.function?.invoke(idx) ?: 1.0
            }
            return@map core
        }.toMutableList()
        val c = terms.filterIsInstance<Constant>().fold(1.0) {acc, next -> acc * next.value}
        return TTVector(TensorTrain(ArrayList(cores))) * c
    }

    override operator fun times(other: RateExpression) = Product(this.terms + listOf(other))
}