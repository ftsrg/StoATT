package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import faulttree.galileoParser
import solver.*
import solver.solvers.AMEnALSSolve
import java.io.FileInputStream

class Calc : CliktCommand(help =
"""Used for performing the analysis of a fault tree model.""".trimMargin()
) {
    val file by option("-f", "--file",
            help = "The path of the Galileo file describing the model to analyze.")
            .required()

    object MomentArgs : OptionGroup() {
        val moment by option("-m", "--moment").int().restrictTo(min = 1).required()
        val solver by option("-s", "--solver")
                .choice("DMRG", "Neumann", "GMRES", "Jacobi", "AMEn", "AMEn-ALS").default("DMRG")
        val preconditioner by option("-prec", "--preconditioner").choice("NS", "DMRG", "Jacobi", "none")
        val threshold by option("-th", "--threshold").double().default(1e-7)
        val method by option("--method").choice("1", "2").int().default(2)
        val sweeps by option("--sweeps").int()
        val enrichmentRank by option("--enrichment").int().restrictTo(min=0)
        val residDamp by option("--damp").double().restrictTo(min=0.0, max=1.0).default(1e-2)
        val expinvterms by option("--expinvterms").int().restrictTo(min = 0)
        val neumannterms by option("--neumannterms").int().restrictTo(min = 0)
    }

    val momentArgs by MomentArgs.cooccurring()
    val steady by option("-st", "--steady").flag()

    override fun run() {
        println("Fault tree file: $file")
        val tree = FileInputStream(file).use { galileoParser.parse(it) }
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
            } else {
                val solverFunc: (TTSquareMatrix, TTVector, Double) -> TTSolution = when (MomentArgs.solver) {
                    "DMRG" -> { M, b, threshold ->
                        DMRGSolve(
                                M,
                                b,
                                absoluteResidualThreshold = threshold,
                                truncationRelativeThreshold = MomentArgs.threshold / rho,
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
                                residDamp = momentArgs.residDamp
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