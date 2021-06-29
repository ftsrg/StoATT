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

package cli

import benchmark.configArgs
import benchmark.configArgsSteadyOnly
import benchmark.configJson
import benchmark.configJsonSteadyOnly
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import solver.mat
import solver.r
import java.io.File
import java.io.FileWriter

class Gen : CliktCommand(help = "Used for generating benchmark model files and corresponding config files.") {
    val modules by option("-m", "--modules").int().restrictTo(min = 1).multiple().validate { it.isNotEmpty() }
    val name by option("--name")
    val folder by option().default("")
//    sealed class BESpec {
//        data class Exponential(val lambda: Double, val mu: Double) : BESpec()
//        data class Markov(val rateMatrix: String) : BESpec()
//    }
    val controllerRates by option("--ctrl").double().pair().required()
    val voterRates by option("--voter").double().pair().required()
    val phaseType by option().flag()
    val json by option(help = "Generate config files in json format").flag()
    val configFolder by option("--cfgfolder")
    val argConfig by option("--argcfg",
            help = "Generate config files as @-files (text files specifying CLI arguments)").flag()

    override fun run() {
        val folder = if (folder == "" || folder.endsWith(File.separator)) folder else "$folder${File.separator}"
        for(modules in modules) {
            val treeString =
                    if(phaseType)
                        benchmark.complexTreeString(modules,
                                benchmark.markov(
                                        mat[
//                                                r[0.0,2.0,0.0,0.5,0.0],
//                                                r[0.0,0.0,3.0,0.7,0.2],
//                                                r[0.0,0.0,0.0,0.0,0.8],
//                                                r[30.0,0.0,0.0,0.0,0.5],
//                                                r[20.0,10.0,0.0,0.0,0.0]
                                                r[0.0,2.0,0.0],
                                                r[1.0,0.0,3.0],
                                                r[5.0,0.0,0.0]
                                        ], 1),
                                benchmark.expFixed(voterRates.first, voterRates.second)
                        )
                    else
                        benchmark.getExponentialTree(modules,
                    controllerRates.first, controllerRates.second,
                    voterRates.first, voterRates.second)
            val name = name ?: "tree_with_${modules}_modules"
            val path = "$folder${name}.galileo"
            FileWriter(path).use { file ->
                file.write(treeString)
            }


            // Config files use the default values for now (Unpreconditioned DMRG using method 2 with threshold 1e-7)
            // Separate config files are generated for computing each moment from 1st to 5th, and for the steady state metrics
            val configFolder =
                    configFolder?.let { if (it == "" || it.endsWith(File.separator)) it else "$it${File.separator}" }
                    ?: folder
            if (json) {
                repeat(5) { moment ->
                    val cfgPath = "$configFolder${name}_cfg_moment_${moment + 1}.json"
                    FileWriter(cfgPath).use {
                        it.write(configJson(path, moment + 1, otherOptions = """"sweeps" : 100 """))
                    }
                }
                val cfgPath = "$configFolder${name}_cfg_moment_steady.json"
                FileWriter(cfgPath).use {
                    it.write(configJsonSteadyOnly(path))
                }
            }
            if (argConfig) {
                repeat(5) { moment ->
                    val cfgPath = "$configFolder${name}_cfg_moment_${moment + 1}.args"
                    FileWriter(cfgPath).use {
                        it.write(configArgs(path, moment + 1, otherOptions = "--sweeps=100"))
                    }
                }
                val cfgPath = "$configFolder${name}_cfg_steady.args"
                FileWriter(cfgPath).use {
                    it.write(configArgsSteadyOnly(path))
                }
            }
        }
    }
}