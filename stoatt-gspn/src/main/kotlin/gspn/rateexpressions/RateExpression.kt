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
import solver.TTVector

abstract class RateExpression {

    abstract fun toCanonical(): RateExpression
    abstract fun deepCopy(): RateExpression
    abstract fun toTT(varOrder: MddVariableOrder): TTVector

    open operator fun times(other: RateExpression) = Product(listOf(this, other))
    open operator fun plus(other: RateExpression) = Sum(listOf(this, other))
}