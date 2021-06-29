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

package benchmark

import org.ejml.simple.SimpleMatrix
import solver.row
import java.util.*

fun complexTreeString(numModules: Int, controllerProps: ()->String, voterProps: ()->String): String {
    val builder = StringBuilder()
    builder.appendln("toplevel \"System\";")
    val listBuilder = StringBuilder("or")
    repeat(numModules) {
        listBuilder.append(" \"Module$it\"")
    }
    builder.appendln("\"System\" $listBuilder;")
    builder.appendln()
    repeat(numModules) { moduleId ->
        builder.appendln("/* Module $moduleId */")
        builder.appendln("\"Module$moduleId\" or \"Channels$moduleId\" \"Voters$moduleId\";")
        builder.appendln("\"Channels$moduleId\" 2of3 \"ChannelR$moduleId\" \"ChannelG$moduleId\" \"ChannelB$moduleId\";")
        builder.appendln("\"Voters$moduleId\" and \"VoterA$moduleId\" \"VoterB$moduleId\";")
        builder.appendln("\"ChannelR$moduleId\" or \"IOR$moduleId\" \"LGR\";")
        builder.appendln("\"ChannelG$moduleId\" or \"IOG$moduleId\" \"LGG\";")
        builder.appendln("\"ChannelB$moduleId\" or \"IOB$moduleId\" \"LGB\";")
        builder.appendln("\"IOR$moduleId\" ${controllerProps()};")
        builder.appendln("\"IOG$moduleId\" ${controllerProps()};")
        builder.appendln("\"IOB$moduleId\" ${controllerProps()};")
        builder.appendln("\"VoterA$moduleId\" ${voterProps()};")
        builder.appendln("\"VoterB$moduleId\" ${voterProps()};")
        builder.appendln()
    }
    builder.appendln("\"LGR\" ${controllerProps()};")
    builder.appendln("\"LGG\" ${controllerProps()};")
    builder.appendln("\"LGB\" ${controllerProps()};")
    return builder.toString()
}

fun expFixed(lambda: Double): () -> String = { "lambda=$lambda" }
fun expFixed(failureRate: Double, repairRate: Double): () -> String = { "lambda=$failureRate repair=$repairRate" }
fun expRandom(min: Double, max: Double, random: Random? = null): () -> String {
    val rand = (random ?: Random()).doubles(min, max).iterator()
    return { "lambda=${rand.next()}" }
}
fun markov(rateMatrix: SimpleMatrix, numFailureStates: Int): ()->String {
    val ph= rateMatrix.matlabFormat()
    return { "ph=$ph failurestates=$numFailureStates" }
}

fun SimpleMatrix.matlabFormat() =
    (0 until this.numRows()).map { row(it) }
            .joinToString(prefix = "[", postfix = "]", separator = ";") { row ->
                (0 until row.numCols()).map { row[it] }.joinToString(separator = ",") { value -> value.toString() }
            }

fun getExponentialTree(numModules: Int,
                       controllerFailureRate: Double, controllerRepairRate: Double,
                       voterFailureRate: Double, voterRepairRate: Double) =
        complexTreeString(
                numModules,
                expFixed(controllerFailureRate, controllerRepairRate),
                expFixed(voterFailureRate, voterRepairRate)
        )

fun configJson(
        modelPath: String,
        moment: Int,
        steadyState: Boolean = false,
        solver: String = "DMRG",
        preconditioner: String = "none",
        method: Int = 2,
        threshold: Double = 1e-7,
        otherOptions: String? = null
) = """ {
    "path" : "${modelPath.replace("\\", "\\\\")}",
    "moment" : $moment,
    ${if (steadyState) "\"steady\" : true," else ""}
    "threshold" : $threshold,
    "solver" : "$solver",
    "method" : $method,
    "preconditioner" : "$preconditioner"
    ${if (otherOptions != null) ",$otherOptions" else ""}
    }
""".trimIndent()

fun configJsonSteadyOnly(
        modelPath: String
) = """ {
    "path" : "${modelPath.replace("\\", "\\\\")}",
    "steady" : true
    }
""".trimIndent()

fun configArgs(
        modelPath: String,
        moment: Int,
        solver: String = "DMRG",
        preconditioner: String = "none",
        method: Int = 2,
        threshold: Double = 1e-7,
        steady: Boolean = false,
        otherOptions: String? = null
) = """
    --file=${modelPath.replace("\\", "\\\\")}
    --moment=$moment
    --solver=$solver
    --preconditioner=$preconditioner
    --threshold=$threshold
    --method=$method
    ${if (steady) "--steady" else ""}
    ${otherOptions ?: ""}
""".trimIndent()

fun configArgsSteadyOnly(modelPath: String) = """
    --file=${modelPath.replace("\\", "\\\\")} --steady
""".trimIndent()