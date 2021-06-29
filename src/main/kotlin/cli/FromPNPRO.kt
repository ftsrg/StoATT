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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import gspn.PNPROParser
import solver.AMEnSolve
import solver.TTVector
import java.io.FileInputStream

class FromPNPRO : CliktCommand() {
    val file by argument()
    val enrichment by option().int().restrictTo(min = 1).default(1)
    val tolerance by option().double().restrictTo(min=0.0, max=0.1).default(0.0)

    override fun run() {
        val model = PNPROParser.parse(FileInputStream(file))

//        if(model.hasDeadlock()) {
//            println("The model has at least one deadlock state.")
//            return
//        }

        val ss = model.getSteadyStateDistribution(true, tolerance) { A ->
            AMEnSolve(
                    A = A,
                    y = TTVector.zeros(A.modes),
                    residualThreshold = 1e-7,
                    maxSweeps = 50,
                    enrichmentRank = enrichment,
                    normalize = true,
                    verbose = true
            )
        }

        println("Just a test for the distribution: ${ss*TTVector.ones(ss.modes)}")
    }

}
