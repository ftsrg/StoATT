
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
import faulttree.FaultTree
import faulttree.galileoParser
import solver.*
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

//enum class SolverType { GMRES, JACOBI }
//class FTSolver: CliktCommand() {
//    val file by option("--file", "-f", metavar = "FILENAME")
//    val solver by option("--solver", "-s")
//            .choice(
//                    "gmres" to SolverType.GMRES,
//                    "jacobi" to SolverType.JACOBI
//            ).default(SolverType.JACOBI)
//    val preconditioner by option("--precond", "-p")
//            .choice(
//                    "jacobi",
//                    "ns",
//                    "none")
//            .default("none")
//    val threshold by option("--threshold", "-t").double().validate { require(it>0) { "Threshold value must be positive!" } }
//    override fun run() {
//        val inputStream = when(file) {
//            null -> System.`in`
//            else -> FileInputStream(file)
//        }
//        val faulttree = galileoParser.parse(inputStream)
//        val transientGeneratorMatrix = faulttree.getModifiedGenerator()
//        val stateMaskVector = faulttree.getOperationalIndicatorVector()
//        when(solver) {
//            SolverType.GMRES -> {
//                TODO()
//            }
//            SolverType.JACOBI -> TODO()
//        }
//    }
//}

class TTReliabilityTool: CliktCommand() {
    override fun run() = Unit
}

class Gen: CliktCommand(help = "Used for generating benchmark model files and corresponding config files.") {
    val modules by option("-m", "--modules").int().restrictTo(min=1).required()
    val name by option("--name")
    val folder by option().default("")
//    sealed class BESpec {
//        data class Exponential(val lambda: Double, val mu: Double) : BESpec()
//        data class Markov(val rateMatrix: String) : BESpec()
//    }
    val controllerRates by option("--ctrl").double().pair().required()
    val voterRates by option("--voter").double().pair().required()
    val json by option(help = "Generate config files in json format").flag()
    val configFolder by option("--cfgfolder")
    val argConfig by option("--argcfg",
            help = "Generate config files as @-files (text files specifying CLI arguments)").flag()
    override fun run() {
        val folder = if(folder == "" || folder.endsWith(File.separator)) folder else "$folder${File.separator}"
        val treeString = benchmark.getExponentialTree(modules,
                controllerRates.first, controllerRates.second,
                voterRates.first, voterRates.second)
        val name = name ?: "tree_with_${modules}_modules.galileo"
        val path = "$folder$name"
        FileWriter(path).use { file ->
            file.write(treeString)
        }

        // Config files use the default values for now (Unpreconditioned DMRG using method 2 with threshold 1e-7)
        // Separate config files are generated for computing each moment from 1st to 5th, and for the steady state metrics
        val configFolder =
                configFolder?.let { if(it=="" || it.endsWith(File.separator)) it else "$it${File.separator}" }
                ?: folder
        if(json) {
            repeat(5) {moment ->
                val cfgPath = "$configFolder${name}_cfg_moment_${moment+1}.json"
                FileWriter(cfgPath).use {
                    it.write(configJson(path, moment+1))
                }
            }
            val cfgPath = "$configFolder${name}_cfg_moment_steady.json"
            FileWriter(cfgPath).use {
                it.write(configJsonSteadyOnly(path))
            }
        }
        if (argConfig) {
            repeat(5) {moment ->
                val cfgPath = "$configFolder${name}_cfg_moment_${moment+1}.args"
                FileWriter(cfgPath).use {
                    it.write(configArgs(path, moment+1))
                }
            }
            val cfgPath = "$configFolder${name}_cfg_steady.args"
            FileWriter(cfgPath).use {
                it.write(configArgsSteadyOnly(path))
            }
        }
    }
}

class Calc: CliktCommand(help =
"""Used for performing the analysis of a model. If a config file is given as input, all the other options are ignored.
""".trimMargin()
) {
    val file by option("-f", "--file",
            help = "The file path of the Galileo file describing the model to analyze.")
            .required()
    object MomentArgs : OptionGroup() {
        val moment by option("-m", "--moment").int().restrictTo(min=1).required()
        val solver by option("-s", "--solver")
                .choice("DMRG", "Neumann", "GMRES", "Jacobi").default("DMRG")
        val preconditioner by option("-prec", "--preconditioner").choice("NS", "DMRG", "Jacobi")
        val threshold by option("-th", "--threshold").double().default(1e-7)
        val method by option("--method").choice("1", "2").int().default(2)
    }
    val momentArgs by MomentArgs.cooccurring()
    val steady by option("-st", "--steady").flag()

    override fun run() {
        println("Fault tree file: $file")
        val tree = FileInputStream(file).use { galileoParser.parse(it) }
        if(steady) {
            val start = System.currentTimeMillis()
            val ssvector = tree.getSteadyStateDistribution()
            val ssmetrics = tree.computeSteadyStateMetrics(ssvector)
            val end = System.currentTimeMillis()
            println("MTTF: ${ssmetrics.MTTF}, MTTR: ${ssmetrics.MTTR}, MTBF: ${ssmetrics.MTBF}")
            println("Computation time: ${end-start}ms")
        }
        if(momentArgs != null) {
            val start = System.currentTimeMillis()
            TODO()
            val end = System.currentTimeMillis()
        }
    }

    private data class MTFFMethod(val sysMatrix: TTSquareMatrix, val rightVector: TTVector)
    private fun symmetricMethod(FT: FaultTree): MTFFMethod {
        val Qmod = FT.getModifiedGenerator()
        Qmod.tt.roundAbsolute(1e-10)
        Qmod.tt.roundRelative(1e-10)
        return MTFFMethod(Qmod, TTVector.ones(Qmod.modes))
    }
    private fun nonSymmetricMethod(FT: FaultTree): MTFFMethod {
        val m = FT.getOperationalIndicatorVector()
        val M = TTSquareMatrix.diag(m)
        val lF = 1.0
        var gamma = FT.getHighestExitRate()
        val R = FT.getBaseRateMatrix() - gamma * tteye(m.modes)
        val kronsumComponents = FT.getKronsumComponents()
        var modifiedKronsumComponents = kronsumComponents.map {
            -(it - gamma / kronsumComponents.size * eye(2))
        }
        val minEig = modifiedKronsumComponents.flatMap { it.eig().eigenvalues.map { it.real } }.min() ?: 0.0
        if (minEig < 0.0) {
            gamma -= minEig * kronsumComponents.size
            modifiedKronsumComponents = modifiedKronsumComponents.map { it - minEig * eye(2) }
        }
        val Rinv0 = -approxInvertKronsum(modifiedKronsumComponents, 200, 1e-16)
        val Rinv = DMRGInvert(
                R,
                3,
                TTVector(Rinv0.tt),
                truncationRelativeThreshold = 1e-10 / (2 * FT.getHighestExitRate())
        )

        Rinv.tt.roundAbsolute(1e-16)
        println("Inversion relative residual: ${((Rinv * R) - tteye(R.modes)).frobenius() / R.numCols}")
        val D = TTSquareMatrix.diag(R * TTVector.ones(R.modes))
        val matToInv = M - Rinv * D
        matToInv.tt.roundAbsolute(1e-16)

        return MTFFMethod(matToInv, Rinv * TTVector.ones(Rinv.modes))
    }

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

