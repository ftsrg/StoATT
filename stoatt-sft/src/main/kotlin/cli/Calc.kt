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

import MDDExtensions.calculateNonzeroCount
import MDDExtensions.toTensorTrain
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import faulttree.*
import solver.*
import solver.solvers.AMEnALSSolve
import java.io.FileInputStream
import java.lang.Double.min
import java.math.BigInteger

class Calc : CliktCommand(help =
"""Used for performing the analysis of a fault tree model.""".trimMargin()
) {
    val file by option("-f", "--file",
            help = "The path of the Galileo file describing the model to analyze.")
            .required()

    object MomentArgs : OptionGroup() {
        val moment by option("-m", "--moment",
                help="Sets which moment to calculate (e.g. 1 for mean)")
                .int().restrictTo(min = 1).required()
        val solver by option("-s", "--solver",
                help="Sets the TT-based linear system solver used for the calculation")
                .choice("DMRG", "Neumann", "GMRES", "Jacobi", "AMEn", "AMEn-ALS", "SAMEn").default("DMRG")
        val threshold by option("-th", "--threshold",
                help = "Sets residual norm threshold for stopping.")
                .double().default(1e-7)
        val sweeps by option("--sweeps",
                help = "Sets the maximum number of sweeps for ALS-based solvers (e.g. DMRG, AMEn, AMEn-ALS).")
                .int()
        val enrichmentRank by option("--enrichment",
                help = "Sets the enrichment rank for AMEn methods (AMEn, AMEn-ALS).")
                .int().restrictTo(min=0)
        val useDirectForSmall by option("--usedirect",
                help = "Sets whether to use a direct solver for small local systems in AMEn-ALS")
                .flag()
        val residDamp by option("--damp",
                help="Sets the dampening factor used for truncations in DMRG and AMEn-ALS. The truncation threshold used is residualThreshold*dampening")
                .double().restrictTo(min=0.0, max=1.0).default(1e-2)
        val expinvterms by option("--expinvterms")
                .int().restrictTo(min = 0)
        val neumannterms by option("--neumannterms")
                .int().restrictTo(min = 0)

        val method by option("--method",
                help = "Deprecated, don't use it. (Used to set the formula used for structured MTFF calculation)")
                .choice("1", "2").int().default(2)
                .deprecated()
        val preconditioner by option("-prec", "--preconditioner",
                help = "Deprecated, don't use it. (Used to set which type of preconditioner to use for linear system solution.)")
                .choice("NS", "DMRG", "Jacobi", "none")
                .deprecated()
    }

    val momentArgs by MomentArgs.cooccurring()
    val steady by option("-st", "--steady").flag()
    val stats by option("--stat").flag()

    override fun run() {
        println("Fault tree file: $file")
        val tree = FileInputStream(file).use { galileoParser.parse(it) }
        if (stats) {
            val operationalSize = tree.nonFailureAsMdd().calculateNonzeroCount()
            println("operational size: ${operationalSize.toDouble()}")
            val train = tree.nonFailureAsMdd().toTensorTrain()
            val mddSize = train.ranks().max()
            println("mdd size: $mddSize")
            val potentialStateSpace = train.cores.map { BigInteger.valueOf(it.modeLength.toLong()) }.reduce(BigInteger::multiply)
            println("potential state space: ${potentialStateSpace.toDouble()}")

            val Q = tree.getModifiedGenerator()
            Q.tt.roundAbsolute(1e-16)
            Q.tt.roundRelative(1e-16)
            println("modified rounded generator max rank: ${Q.ttRanks().max()}")
        }
        if (steady) {
            val start = System.currentTimeMillis()
            val ssvector = tree.getSteadyStateDistribution()
            val ssmetrics = tree.computeSteadyStateMetrics(ssvector)
            val end = System.currentTimeMillis()
            println("MTTF: ${ssmetrics.MTTF}, MTTR: ${ssmetrics.MTTR}, MTBF: ${ssmetrics.MTBF}")
            println("Computation time: ${end - start}ms")
        }
        if (momentArgs != null) {
            val momentArgs = momentArgs!!
            val rho = tree.getHighestExitRate()
            val start = System.currentTimeMillis()
            val res = if (MomentArgs.moment == 1 && MomentArgs.solver == "Neumann") {
                val expInvTerms = MomentArgs.expinvterms ?: throw RuntimeException("expinvterms argument needed")
                val neumannTerms = MomentArgs.neumannterms ?: throw RuntimeException("neumannterms argument needed")
                val approxInvRounding = 1e-16
                tree.mttfThroughKronsumMethod(
                        neumannTerms,
                        expInvTerms,
                        approxInvRounding,
                        MomentArgs.threshold,
                        convergenceThreshold = MomentArgs.threshold, //TODO: separate parameter
                        verbose = true
                )
            } else if(MomentArgs.solver == "SAMEn") {
                tree.getNthMomentSparse(MomentArgs.moment, MomentArgs.threshold) { M, b, threshold ->
                    AMEnALSSolve(
                            M as Array<Abstract2DCoreTensor>,
                            b,
                            residualThreshold = threshold,
                            maxSweeps = momentArgs.sweeps ?: 0,
                            enrichmentRank = momentArgs.enrichmentRank ?: 1,
                            useApproxResidualForStopping = false,
                            residDamp = momentArgs.residDamp
                    )
                }
            } else {
                val solverFunc: (TTSquareMatrix, TTVector, Double) -> TTSolution = when (MomentArgs.solver) {
                    "DMRG" -> { M, b, threshold ->
                        DMRGSolve(
                                M,
                                b,
                                absoluteResidualThreshold = threshold,
                                truncationRelativeThreshold = MomentArgs.threshold * min(1.0/rho, MomentArgs.residDamp),
                                maxSweeps = MomentArgs.sweeps ?: 0,
                                verbose = true
                        )
                    }
                    "GMRES" -> { M, b, threshold ->
                        TTReGMRES(null,
                                M, b,
                                TTVector.ones(b.modes),
                                MomentArgs.threshold,
                                //maxInnerIters,
                                //maxOuterIters,
                                verbose = true,
                                approxSpectralRadius = rho
                        )
                    }
                    "Jacobi" -> { M, b, threshold ->
                        TTJacobi(
                                M, b,
                                threshold, //relativeResNormThreshold * pi0.norm(),
                                MomentArgs.threshold / rho,
                                log = true
                        )
                    }
                    "AMEn" -> { M, b, threshold ->
                        AMEnSolve(
                                M, b,
                                TTVector.ones(b.modes),
                                threshold,
                                MomentArgs.sweeps ?: 0,
                                MomentArgs.enrichmentRank ?: 1
                        )
                    }
                    "AMEn-ALS" -> { M, b, threshold ->
                        AMEnALSSolve(
                                M,
                                b,
                                residualThreshold = threshold,
                                maxSweeps = momentArgs.sweeps ?: 0,
                                enrichmentRank = momentArgs.enrichmentRank ?: 1,
                                useApproxResidualForStopping = false,
                                residDamp = momentArgs.residDamp,
                                useDirectForSmall = momentArgs.useDirectForSmall
                        )
                    }
                    else -> throw RuntimeException("Unknown solver")
                }
                tree.getNthMoment(MomentArgs.moment, MomentArgs.threshold, solverFunc)
            }
            val end = System.currentTimeMillis()
            println("${MomentArgs.moment}th moment: $res")
            println("Moment calculation time: ${end-start}ms")
        }
    }

}