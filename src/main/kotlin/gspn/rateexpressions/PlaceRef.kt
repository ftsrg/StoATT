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