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

package gspn

import gspn.rateexpressions.RateExpression
import hu.bme.mit.delta.mdd.MddVariableOrder
import solver.CoreTensor
import solver.TTSquareMatrix
import solver.TensorTrain

class ExponentialTransition(
    name: String,
    inputs: ArrayList<Arc>,
    outputs: ArrayList<Arc>,
    inhibitors: ArrayList<Arc>,
    val rate: RateExpression
) : Transition(name, inputs, outputs, inhibitors) {

    override fun toTT(varOrder: MddVariableOrder, places: ArrayList<Place>): TTSquareMatrix {
        val rateVector = rate.toTT(varOrder)
        val matrixCores = arrayListOf<CoreTensor>()
        for ((idx, variable) in varOrder.withIndex()) {
            val core = rateVector.tt.cores[idx]
            val place = places.firstOrNull{it.name == variable.traceInfo} ?: throw RuntimeException("Place ${variable.traceInfo} not found")
            // TODO: these searches assume that at most one arc exists from a given place - this should be enforced
            val inp = inputs.firstOrNull { it.place == place}?.weightFunction ?: {0}
            val out = outputs.firstOrNull {it.place.name == variable.traceInfo }?.weightFunction ?: {0}
            val inh = inhibitors.firstOrNull { it.place.name == variable.traceInfo}?.weightFunction ?: {Int.MAX_VALUE}
            val newMatCore = CoreTensor(core.modeLength*core.modeLength, core.rows, core.cols)
            for(m in 0..place.capacity) {
                val result = m - inp(m) + out(m)
                if(m >= inp(m) && m < inh(m) && result <= place.capacity)
                newMatCore[m, result] = core[m]
            }
            matrixCores.add(newMatCore)
        }
        return TTSquareMatrix(TensorTrain(matrixCores), rateVector.modes)
    }
}