
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import faulttree.galileoParser
import java.io.FileInputStream

enum class SolverType { GMRES, JACOBI }

class FTSolver: CliktCommand() {
    val file by option("--file", "-f", metavar = "FILENAME")
    val solver by option("--solver", "-s")
            .choice(
                    "gmres" to SolverType.GMRES,
                    "jacobi" to SolverType.JACOBI
            ).default(SolverType.JACOBI)
    val preconditioner by option("--precond", "-p")
            .choice(
                    "jacobi",
                    "ns",
                    "none")
            .default("none")
    val threshold by option("--threshold", "-t").double().validate { require(it>0) { "Threshold value must be positive!" } }
    override fun run() {
        val inputStream = when(file) {
            null -> System.`in`
            else -> FileInputStream(file)
        }
        val faulttree = galileoParser.parse(inputStream)
        val transientGeneratorMatrix = faulttree.getTransientGenerator()
        val stateMaskVector = faulttree.getStateMaskVector()
        when(solver) {
            SolverType.GMRES -> {
                TODO()
            }
            SolverType.JACOBI -> TODO()
        }
    }
}

fun main(args: Array<String>) = FTSolver().main(args)