
import faulttree.galileoParser
import solver.*
import java.io.FileInputStream
import java.util.*
import kotlin.math.max

private val rand = Random(10)
private val solvers = listOf(
        solver("TTReGMRES") { A, b -> TTReGMRES(A, b, TTVector.ones(A.modes), 1e-16) },
        solver("ALS") { A, b -> ALSSolve(A, b, residualThreshold = b.norm()*1e-16, maxSweeps = 50) },
        solver("DMRG") { A, b -> DMRGSolve(A, b, residualThreshold = 1e-16, maxSweeps = 50)},
        solver("Jacobi") { A, b -> TTJacobi(A, b, b.norm()*1e-16, 1e-16) }
)

fun main(args: Array<String>) {
    val FT = galileoParser.parse(FileInputStream(args.firstOrNull() ?: "tree.galileo"))

    // Basic method
    val Qmod = FT.getModifiedGenerator()
    val pi0Cores = Array(Qmod.modes.size) {
        val core = CoreTensor(Qmod.modes[it], 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val pi0 = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))
    val ones = TTVector.ones(pi0.modes)
    for (result in applySolvers(Qmod.T(), pi0)) {
        println("${result.methodDescription}: " +
                "MTTF=${result.solution * ones}, " +
                "residual norm=${result.residualNorm}, " +
                "solution ranks = ${result.solution.ttRanks()}, " +
                "time spent: ${result.durationMillis/1000.0}s")
    }
    FT.mttfThroughKronsumMethod(50, 50, 1e-16, 1e-16)

    // Newer method
    val kronsumComponents = FT.getKronsumComponents()
    val Rinv = approxInvertKronsum(kronsumComponents, 50, 1e-16)
    val m = FT.getStateMaskVector()
    val M = TTSquareMatrix.diag(m)
    val lF = 1.0
    val R =
            FT.getBaseRateMatrix() +
            lF * TTSquareMatrix.diag(FT.getStrictAbsorbingIndicatorVector())
    val D = TTSquareMatrix.diag(R * TTVector.ones(R.modes))
    val matToInv = M - Rinv*D
    var REigMax = 0.0
    for (component in kronsumComponents) {
        val lambda = component[0, 1]
        val mu = component[1,0]
        REigMax += max(lambda, mu)
    }
    val DMax = max(REigMax, lF)
    val rhoApprox = 1.0+DMax*1.0/REigMax
    val gamma = 1.0/rhoApprox/rhoApprox


}


data class SolverResult(
        val methodDescription: String,
        val solution: TTVector,
        val residualNorm: Double,
        val durationMillis: Long
)
fun solver(
        description: String,
        method: (TTSquareMatrix, TTVector) -> TTSolution
): (TTSquareMatrix, TTVector) -> SolverResult
{
    return { A, b ->
        val start = System.currentTimeMillis()
        val s = method(A, b)
        val end = System.currentTimeMillis()
        SolverResult(description, s.solution, s.resNorm, end - start)
    }
}

fun applySolvers(A: TTSquareMatrix, b: TTVector): Sequence<SolverResult> = solvers.asSequence().map { it(A, b) }