
import benchmark.configArgs
import benchmark.configArgsSteadyOnly
import benchmark.configJson
import benchmark.configJsonSteadyOnly
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import faulttree.galileoParser
import solver.*
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

class TTReliabilityTool : CliktCommand() {
    override fun run() = Unit
}

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

class Calc : CliktCommand(help =
"""Used for performing the analysis of a model. If a config file is given as input, all the other options are ignored.
""".trimMargin()
) {
    val file by option("-f", "--file",
            help = "The path of the Galileo file describing the model to analyze.")
            .required()

    object MomentArgs : OptionGroup() {
        val moment by option("-m", "--moment").int().restrictTo(min = 1).required()
        val solver by option("-s", "--solver")
                .choice("DMRG", "Neumann", "GMRES", "Jacobi").default("DMRG")
        val preconditioner by option("-prec", "--preconditioner").choice("NS", "DMRG", "Jacobi", "none")
        val threshold by option("-th", "--threshold").double().default(1e-7)
        val method by option("--method").choice("1", "2").int().default(2)
        val sweeps by option("--sweeps").int()
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
            val res = if (momentArgs.moment == 1 && momentArgs.solver == "Neumann") {
                val expInvTerms = momentArgs.expinvterms ?: throw RuntimeException("expinvterms argument needed")
                val neumannTerms = momentArgs.neumannterms ?: throw RuntimeException("neumannterms argument needed")
                val approxInvRounding = 1e-16
                tree.mttfThroughKronsumMethod(
                        neumannTerms,
                        expInvTerms,
                        approxInvRounding,
                        momentArgs.threshold,
                        convergenceThreshold = momentArgs.threshold, //TODO: separate parameter
                        verbose = true
                )
            } else {
                val solverFunc: (TTSquareMatrix, TTVector, Double) -> TTSolution = when (momentArgs.solver) {
                    "DMRG" -> { M, b, threshold ->
                        DMRGSolve(
                                M,
                                b,
                                absoluteResidualThreshold = threshold,
                                truncationRelativeThreshold = momentArgs.threshold / rho,
                                maxSweeps = momentArgs.sweeps ?: 0,
                                verbose = true
                        )
                    }
                    "GMRES" -> { M, b, threshold ->
                        TTReGMRES(null,
                                M, b,
                                TTVector.ones(b.modes),
                                momentArgs.threshold,
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
                                momentArgs.threshold / rho,
                                log = true
                        )
                    }
                    else -> throw RuntimeException("Unknown solver")
                }
                tree.getNthMoment(momentArgs.moment, momentArgs.threshold, solverFunc)
            }
            val end = System.currentTimeMillis()
            println("${momentArgs.moment}th moment: $res")
            println("Moment calculation time: ${end-start}ms")
        }
    }

    private data class MTFFMethod(val sysMatrix: TTSquareMatrix, val rightVector: TTVector)

//    fun computeMoment() {
//        val momentArgs = momentArgs ?: throw RuntimeException("No moment calculation arguments given")
//        println("Fault tree file: $file")
//        val creationStart = System.currentTimeMillis()
//        val FT = FileInputStream(file).use {
//            val start = System.currentTimeMillis()
//            val ft = galileoParser.parse(it)
//            val end = System.currentTimeMillis()
//            println("parsing: ${end - start}ms")
//            return@use ft
//        }
//        val pi0Cores = Array(FT.getOrderedVariables().size) {
//            val core = solver.CoreTensor(2, 1, 1)
//            core[0][0] = 1.0
//            return@Array core
//        }
//        val pi0 = solver.TTVector(solver.TensorTrain(ArrayList(pi0Cores.toList())))
//
//        val method = when (momentArgs.method) {
//            1 -> configedNewerMethod(FT, config)
//            2 -> configedBasicMethod(FT, config)
//            else -> throw RuntimeException()
//        }
//
//        val creationEnd = System.currentTimeMillis()
//        println("system creation: ${creationEnd - creationStart}ms")
//
//        val rho = if (momentArgs.method == 1) 1.0 else FT.getHighestExitRate()
//        val A = method.sysMatrix.T()
//
//        val precondStart = System.currentTimeMillis()
//        val preconditioner = when (momentArgs.preconditioner) {
//            "NS" -> solver.NSInvertMat(A, 10, 1e-8)
//            "jacobi" -> solver.jacobiPreconditioner(A, solver.TTVector.ones(A.modes))
//            "DMRG" -> solver.DMRGInvert(A, 5, truncationRelativeThreshold = 1e-8, verbose = true)
//            else -> null
//        }
//        val precondEnd = System.currentTimeMillis()
//        if (preconditioner != null) println("preconditioner creation: ${precondEnd - precondStart}ms")
//
//        if (momentArgs.moment == 1) {
//            val solutionStart = System.currentTimeMillis()
//            val sysSolution = when (momentArgs.solver) {
//                "DMRG" -> {
//                    val maxSweeps = config.getInt("sweeps")
//                    val localIters = config.getInt("local_iters", 100)
//                    solver.DMRGSolve(
//                            preconditioner?.times(A)?.apply {
//                                tt.roundAbsolute(1e-16)
//                                tt.roundRelative(1e-16)
//                            } ?: A,
//                            preconditioner?.times(pi0)?.apply {
//                                tt.roundAbsolute(1e-16)
//                                tt.roundRelative(1e-16)
//                            } ?: pi0,
//                            absoluteResidualThreshold = relativeResNormThreshold * pi0.norm(),
//                            maxSweeps = maxSweeps,
//                            truncationRelativeThreshold = relativeResNormThreshold / rho,
//                            maxLocalIters = localIters,
//                            verbose = true
//                    )
//                }
//                "GMRES" -> {
//                    val maxInnerIters = config.getInt("inner_iters", 5)
//                    val maxOuterIters = config.getInt("outer_iters", 200)
//                    solver.TTReGMRES(
//                            preconditioner,
//                            A,
//                            pi0,
//                            solver.TTVector.ones(pi0.modes),
//                            relativeResNormThreshold,
//                            maxInnerIters,
//                            maxOuterIters,
//                            verbose = true,
//                            approxSpectralRadius = rho
//                    )
//                }
//                "jacobi" -> {
//                    solver.TTJacobi(
//                            preconditioner?.times(A)?.apply {
//                                tt.roundAbsolute(1e-16)
//                                tt.roundRelative(1e-16)
//                            } ?: A,
//                            preconditioner?.times(pi0)?.apply {
//                                tt.roundAbsolute(1e-16)
//                                tt.roundRelative(1e-16)
//                            } ?: pi0,
//                            relativeResNormThreshold * pi0.norm(),
//                            relativeResNormThreshold / rho,
//                            log = true
//                    )
//                }
//                "neumann" -> null
//                else -> throw RuntimeException("Solver not found")
//            }
//            if (sysSolution != null) {
//                println()
//                val MTFF = -1.0 * sysSolution.solution * method.rightVector
//                val solutionEnd = System.currentTimeMillis()
//                println("residual norm: ${sysSolution.resNorm}")
//                println("solution time: ${solutionEnd - solutionStart}ms")
//                println("MTFF: $MTFF")
//            } else {
//                val expInvTerms = config.getInt("expinv_terms", 0)
//                val neumannTerms = config.getInt("neumann_terms", 0)
//                val approxInvRounding = 1e-16
//                val MTFF = FT.mttfThroughKronsumMethod(
//                        neumannTerms,
//                        expInvTerms,
//                        approxInvRounding,
//                        relativeResNormThreshold,
//                        convergenceThreshold = relativeResNormThreshold, //TODO: separate parameter
//                        verbose = true
//                )
//                val solutionEnd = System.currentTimeMillis()
//                println("solution time: ${solutionEnd - solutionStart}ms")
//                println("MTFF: $MTFF")
//            }
//        } else {
//            val solutionStart = System.currentTimeMillis()
//
//            val nthMoment = when (solver) {
//                "DMRG" -> {
//                    val maxSweeps = config.getInt("sweeps")
//                    val localIters = config.getInt("local_iters", 100)
//                    FT.getNthMoment(moment) { M, b ->
//                        solver.DMRGSolve(
//                                M,
//                                b,
//                                absoluteResidualThreshold = relativeResNormThreshold * pi0.norm(), //TODO: something more relevant
//                                maxSweeps = maxSweeps,
//                                verbose = true,
//                                maxLocalIters = localIters
//                        )
//                    }
//                }
//                "GMRES" -> {
//                    val maxInnerIters = config.getInt("inner_iters", 5)
//                    val maxOuterIters = config.getInt("outer_iters", 200)
//                    FT.getNthMoment(moment) { M, b ->
//                        solver.TTReGMRES(null,
//                                M, b,
//                                solver.TTVector.ones(pi0.modes),
//                                relativeResNormThreshold,
//                                maxInnerIters,
//                                maxOuterIters,
//                                verbose = true,
//                                approxSpectralRadius = rho
//                        )
//                    }
//                }
//                "jacobi" -> FT.getNthMoment(moment) { M, b ->
//                    solver.TTJacobi(
//                            M, b,
//                            relativeResNormThreshold * pi0.norm(),
//                            relativeResNormThreshold / rho,
//                            log = true
//                    )
//                }
//                else -> throw RuntimeException("Solver not found")
//            }
//
//            val solutionEnd = System.currentTimeMillis()
//            println("solution time: ${solutionEnd - solutionStart}ms")
//            println("${moment}th moment: $nthMoment")
//
//        }
//
//
//    }

}

fun main(args: Array<String>) =
        TTReliabilityTool()
                .subcommands(Gen(), Calc())
                .main(args)

