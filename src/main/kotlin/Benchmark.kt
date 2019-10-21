
import faulttree.FaultTree
import faulttree.galileoParser
import solver.*
import java.io.FileInputStream
import java.util.*
import javax.json.Json
import kotlin.math.absoluteValue

private val rand = Random(10)
private val defaultSolvers = mapOf(
//        solver("ALS") { A, b, rho ->
//            ALSSolve(A, b,
//                    residualThreshold = b.norm() * 1e-10,
//                    maxSweeps = 50,
//                    x0 = TTVector.rand(A.modes, arrayOf(1) + Array(A.modes.size - 1) { 3 } + arrayOf(1), random = rand))
//        },
        solver("DMRG") { A, b, rho ->
            DMRGSolve(A, b,
                    absoluteResidualThreshold = b.norm() * 1e-10,
                    maxSweeps = 50,
                    truncationRelativeThreshold = 1e-10 / rho,
                    verbose = true)
        },
        solver("TTReGMRES") { A, b, rho ->
            TTReGMRES(A, b,
                    TTVector.ones(A.modes),
                    1e-10,
                    approxSpectralRadius = rho,
                    verbose = true)
        },
        solver("Jacobi") { A, b, rho -> TTJacobi(A, b, b.norm() * 1e-10, 1e-16, log=true) }
)

fun main(args: Array<String>) {
    System.gc()
    Thread.sleep(500)

    val input = args.firstOrNull() ?: "tree.galileo"
    if(input.endsWith(".galileo"))
        defaultMain(input)
    else if(input.endsWith(".jsong"))
        configedMain(input)
}

fun defaultMain(file: String) {
    val start = System.currentTimeMillis()
    val FT = FileInputStream(file).use { galileoParser.parse(it) }
    val end = System.currentTimeMillis()
    println("parsing: ${end - start}ms")
    runBasicMethod(FT)
    runNewerMethod(FT)
}

fun configedMain(configPath: String) {
    val config = Json.createReader(FileInputStream(configPath)).use { reader -> reader.readObject() }
    val ftPath = config.getString("path", null) ?: "tree.galileo"
    val FT = FileInputStream(ftPath).use {
        val start = System.currentTimeMillis()
        val ft = galileoParser.parse(it)
        val end = System.currentTimeMillis()
        println("parsing: ${end-start}ms")
        return@use ft
    }
    val method = config.getInt("method", -1).takeIf { it == 1 || it == 2 } ?: throw RuntimeException("Method number must be 1 or 2!")
    val solver = config.getString("solver", null) ?: throw java.lang.RuntimeException("No solver specified!")
    val relativeResNormThreshold = config.getJsonNumber("threshold").doubleValue().absoluteValue
    val pi0Cores = Array(FT.getOrderedVariables().size) {
        val core = CoreTensor(2, 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val pi0 = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))

    when(method) {
        1 -> configedNewerMethod(FT)
        2 -> configedBasicMethod(FT)
    }
}

private fun configedBasicMethod(FT: FaultTree) {
    val Qmod = FT.getModifiedGenerator()
    val pi0Cores = Array(Qmod.modes.size) {
        val core = CoreTensor(Qmod.modes[it], 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val pi0 = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))
    val approxSpectralRadius = 2 * FT.getHighestExitRate() // overapproximation based on the Gerschgorin circles
    Qmod.tt.roundAbsolute(1e-16)
}

private fun configedNewerMethod(FT: FaultTree) {
    val m = FT.getStateMaskVector()
    val M = TTSquareMatrix.diag(m)
    val lF = 1.0
    val gamma = FT.getHighestExitRate()
    val R = FT.getBaseRateMatrix() - gamma * tteye(m.modes)
    val kronsumComponents = FT.getKronsumComponents()
    val modifiedKronsumComponents = kronsumComponents.map {
        -(it - gamma / kronsumComponents.size * eye(2))
    }
    val Rinv0 = -approxInvertKronsum(modifiedKronsumComponents, 200, 1e-16)
    val Rinv = DMRGInvert(
            R,
            3,
            TTVector(Rinv0.tt),
            truncationRelativeThreshold = 1e-10/(2*FT.getHighestExitRate())
    )

    Rinv.tt.roundAbsolute(1e-16)
    println("Inversion relative residual: ${((Rinv * R) - tteye(R.modes)).frobenius()/R.numCols}")
    val D = TTSquareMatrix.diag(R * TTVector.ones(R.modes))
    val matToInv = M - Rinv * D
    matToInv.tt.roundAbsolute(1e-16)

}

private fun runBasicMethod(FT: FaultTree) {
    val Qmod = FT.getModifiedGenerator()
    val pi0Cores = Array(Qmod.modes.size) {
        val core = CoreTensor(Qmod.modes[it], 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val pi0 = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))
    val approxSpectralRadius = 2 * FT.getHighestExitRate() // overapproximation based on the Gerschgorin circles
    Qmod.tt.roundAbsolute(1e-16)
    for (result in applySolvers(Qmod.T(), pi0, approxSpectralRadius)) {
        report(result)
    }
    FT.mttfThroughKronsumMethod(50, 50, 1e-16, 1e-16)
}

private fun runNewerMethod(FT: FaultTree) {
    val m = FT.getStateMaskVector()
    val M = TTSquareMatrix.diag(m)
    val lF = 1.0
//    val R =
//            FT.getBaseRateMatrix() +
//            lF * TTSquareMatrix.diag(FT.getStrictAbsorbingIndicatorVector())
    val gamma = FT.getHighestExitRate()
    val R = FT.getBaseRateMatrix() - gamma * tteye(m.modes)
    val kronsumComponents = FT.getKronsumComponents()
    val modifiedKronsumComponents = kronsumComponents.map {
        -(it - gamma / kronsumComponents.size * eye(2))
    }
    val Rinv0 = -approxInvertKronsum(modifiedKronsumComponents, 200, 1e-16)
    val Rinv = DMRGInvert(
            R,
            3,
            TTVector(Rinv0.tt),
            truncationRelativeThreshold = 1e-10/(2*FT.getHighestExitRate())
    )

//    val Rinv = DMRGInvert(
//            R,
//            50,
//            verbose = true,
//            truncationRelativeThreshold = 1e-10,
//            preconditioner = preconditioner
//    )
    Rinv.tt.roundAbsolute(1e-16)
    println("Inversion relative residual: ${((Rinv * R) - tteye(R.modes)).frobenius()/R.numCols}")
    val D = TTSquareMatrix.diag(R * TTVector.ones(R.modes))
    val matToInv = M - Rinv * D
    val pi0Cores = Array(R.modes.size) {
        val core = CoreTensor(R.modes[it], 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val pi0 = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))
    matToInv.tt.roundAbsolute(1e-16)

    for (solverResult in applySolvers(matToInv.T(), pi0, 1.0)) {
        report2(solverResult, Rinv)
    }
}


private fun report(result: SolverResult) =
        println("${result.methodDescription}: " +
                "MTTF=${-(result.solution * TTVector.ones(result.solution.modes))}, " +
                "residual norm=${result.residualNorm}, " +
                "solution ranks = ${result.solution.ttRanks()}, " +
                "time spent: ${result.durationMillis / 1000.0}s")
private fun report2(result: SolverResult, Rinv: TTSquareMatrix) =
        println("${result.methodDescription}: " +
                "MTTF=${-(result.solution * (Rinv * TTVector.ones(result.solution.modes)))}, " +
                "residual norm=${result.residualNorm}, " +
                "solution ranks = ${result.solution.ttRanks()}, " +
                "time spent: ${result.durationMillis / 1000.0}s")

data class SolverResult(
        val methodDescription: String,
        val solution: TTVector,
        val residualNorm: Double,
        val durationMillis: Long
)

fun solver(
        name: String,
        method: (TTSquareMatrix, TTVector, Double) -> TTSolution
): Pair<String, (TTSquareMatrix, TTVector, Double) -> SolverResult> {
    return name to { A, b, rho ->
        val start = System.currentTimeMillis()
        val s = method(A, b, rho)
        val end = System.currentTimeMillis()
        SolverResult(name, s.solution, s.resNorm, end - start)
    }
}

fun applySolvers(A: TTSquareMatrix, b: TTVector, approxSpectralRadius: Double): Sequence<SolverResult> =
        defaultSolvers.values.asSequence().map { it(A, b, approxSpectralRadius) }