
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo

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

object TTReliabilityTool: CliktCommand() {
    override fun run() = Unit
}

object Gen: CliktCommand(help = "Used for generating benchmark model files and corresponding config files.") {
    val modules by option("-m", "--modules").int().restrictTo(min=1).required()
    val path by option().default("")
//    sealed class BESpec {
//        data class Exponential(val lambda: Double, val mu: Double) : BESpec()
//        data class Markov(val rateMatrix: String) : BESpec()
//    }
    val controllerRates by option("--ctrl").double().pair().required()
    val voterRates by option("--voter").double().pair().required()
    val json by option(help = "Generate config files in json format").flag()
    val argConfig by option("--argcfg",
            help = "Generate config files as @-files (text files specifying CLI arguments)").flag()
    override fun run() {

    }
}

object Calc: CliktCommand(help =
"""Used for performing the analysis of a model. If a config file is given as input, all the other options are ignored.
""".trimMargin()
) {
    val file by option("-f", "--file",
            help = "The file path of either a json config file or the Galileo file describing the model to analyze.")
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
        TODO()
    }
}

fun main(args: Array<String>) = TTReliabilityTool
        .subcommands(Gen)
        .main(args)