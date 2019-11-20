import faulttree.FaultTree
import faulttree.galileoParser
import solver.*
import java.io.FileInputStream
import java.util.*
import javax.json.Json
import javax.json.JsonObject
import kotlin.math.absoluteValue

private val rand = Random(10)
private val defaultSolvers = mapOf(
//        solver("ALS") { A, b, rho ->
//            ALSSolve(A, b,
//                    residualThreshold = b.norm() * 1e-10,
//                    maxSweeps = 50,
//                    x0 = TTVector.rand(A.modes, arrayOf(1) + Array(A.modes.size - 1) { 3 } + arrayOf(1), random = rand))
//        },
        solver("TTReGMRES") { A, b, rho ->
            TTReGMRES(null, A, b,
                    TTVector.ones(A.modes),
                    1e-10,
                    approxSpectralRadius = rho,
                    verbose = true)
        },
        solver("DMRG") { A, b, rho ->
            DMRGSolve(A, b,
                    absoluteResidualThreshold = b.norm() * 1e-10,
                    maxSweeps = 50,
                    truncationRelativeThreshold = 1e-10 / rho,
                    verbose = true)
        },
        solver("Jacobi") { A, b, rho -> TTJacobi(A, b, b.norm() * 1e-10, 1e-16, log = true) }
)

fun main(args: Array<String>) {
    System.gc()
    Thread.sleep(500)

    val input = args.firstOrNull() ?: "tree.galileo"
    println("Using input file $input")
    if (input.endsWith(".galileo"))
        defaultMain(input)
    else if (input.endsWith(".json"))
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
    println("Fault tree file: $ftPath")
    val creationStart = System.currentTimeMillis()
    val FT = FileInputStream(ftPath).use {
        val start = System.currentTimeMillis()
        val ft = galileoParser.parse(it)
        val end = System.currentTimeMillis()
        println("parsing: ${end - start}ms")
        return@use ft
    }
    val methodId = config.getInt("method", -1).takeIf { it == 1 || it == 2 }
                   ?: throw RuntimeException("Method number must be 1 or 2!")
    val solver = config.getString("solver", null) ?: throw java.lang.RuntimeException("No solver specified!")
    val relativeResNormThreshold = config.getJsonNumber("threshold").doubleValue().absoluteValue
    val pi0Cores = Array(FT.getOrderedVariables().size) {
        val core = CoreTensor(2, 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val pi0 = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))

    val method = when (methodId) {
        1 -> configedNewerMethod(FT, config)
        2 -> configedBasicMethod(FT, config)
        else -> throw RuntimeException()
    }

    val creationEnd = System.currentTimeMillis()
    println("system creation: ${creationEnd - creationStart}ms")

    val rho = if (methodId == 1) 1.0 else FT.getHighestExitRate()
    val preconditionerType = config.getString("preconditioner", "")
    val A = method.sysMatrix.T()

    val precondStart = System.currentTimeMillis()
    val preconditioner = when (preconditionerType) {
        "NS" -> NSInvertMat(A, 10, 1e-8)
        "jacobi" -> jacobiPreconditioner(A, TTVector.ones(A.modes))
        "DMRG" -> DMRGInvert(A, 5, truncationRelativeThreshold = 1e-8, verbose = true)
        else -> null
    }
    val precondEnd = System.currentTimeMillis()
    if (preconditioner != null) println("preconditioner creation: ${precondEnd - precondStart}ms")

    val moment = config.getInt("moment", 1)

    if (moment == 1) {
        val solutionStart = System.currentTimeMillis()
        val sysSolution = when (solver) {
            "DMRG" -> {
                val maxSweeps = config.getInt("sweeps")
                val localIters = config.getInt("local_iters", 100)
                DMRGSolve(
                        preconditioner?.times(A)?.apply {
                            tt.roundAbsolute(1e-16)
                            tt.roundRelative(1e-16)
                        } ?: A,
                        preconditioner?.times(pi0)?.apply {
                            tt.roundAbsolute(1e-16)
                            tt.roundRelative(1e-16)
                        } ?: pi0,
                        absoluteResidualThreshold = relativeResNormThreshold * pi0.norm(),
                        maxSweeps = maxSweeps,
                        truncationRelativeThreshold = relativeResNormThreshold / rho,
                        maxLocalIters = localIters,
                        verbose = true
                )
            }
            "GMRES" -> {
                val maxInnerIters = config.getInt("inner_iters", 5)
                val maxOuterIters = config.getInt("outer_iters", 200)
                TTReGMRES(
                        preconditioner,
                        A,
                        pi0,
                        TTVector.ones(pi0.modes),
                        relativeResNormThreshold,
                        maxInnerIters,
                        maxOuterIters,
                        verbose = true,
                        approxSpectralRadius = rho
                )
            }
            "jacobi" -> {
                TTJacobi(
                        preconditioner?.times(A)?.apply {
                            tt.roundAbsolute(1e-16)
                            tt.roundRelative(1e-16)
                        } ?: A,
                        preconditioner?.times(pi0)?.apply {
                            tt.roundAbsolute(1e-16)
                            tt.roundRelative(1e-16)
                        } ?: pi0,
                        relativeResNormThreshold * pi0.norm(),
                        relativeResNormThreshold / rho,
                        log = true
                )
            }
            "neumann" -> null
            else -> throw RuntimeException("Solver not found")
        }
        if (sysSolution != null) {
            println()
            val MTFF = -1.0 * sysSolution.solution * method.rightVector
            val solutionEnd = System.currentTimeMillis()
            println("residual norm: ${sysSolution.resNorm}")
            println("solution time: ${solutionEnd - solutionStart}ms")
            println("MTFF: $MTFF")
        } else {
            val expInvTerms = config.getInt("expinv_terms", 0)
            val neumannTerms = config.getInt("neumann_terms", 0)
            val approxInvRounding = 1e-16
            val MTFF = FT.mttfThroughKronsumMethod(
                    neumannTerms,
                    expInvTerms,
                    approxInvRounding,
                    relativeResNormThreshold,
                    convergenceThreshold = relativeResNormThreshold, //TODO: separate parameter
                    verbose = true
            )
            val solutionEnd = System.currentTimeMillis()
            println("solution time: ${solutionEnd - solutionStart}ms")
            println("MTFF: $MTFF")
        }
    } else {
        val solutionStart = System.currentTimeMillis()

        val nthMoment = when (solver) {
            "DMRG" -> {
                val maxSweeps = config.getInt("sweeps")
                val localIters = config.getInt("local_iters", 100)
                FT.getNthMoment(moment) { M, b -> DMRGSolve(
                        M,
                        b,
                        absoluteResidualThreshold = relativeResNormThreshold * pi0.norm(), //TODO: something more relevant
                        maxSweeps = maxSweeps,
                        verbose = true,
                        maxLocalIters = localIters
                ) }
            }
            "GMRES" -> {
                val maxInnerIters = config.getInt("inner_iters", 5)
                val maxOuterIters = config.getInt("outer_iters", 200)
                FT.getNthMoment(moment) { M, b ->
                    TTReGMRES( null,
                            M, b,
                            TTVector.ones(pi0.modes),
                            relativeResNormThreshold,
                            maxInnerIters,
                            maxOuterIters,
                            verbose = true,
                            approxSpectralRadius = rho
                    ) }
            }
            "jacobi" -> FT.getNthMoment(moment) { M, b ->
                TTJacobi(
                        M, b,
                        relativeResNormThreshold * pi0.norm(),
                        relativeResNormThreshold / rho,
                        log = true
                )
            }
            else -> throw RuntimeException("Solver not found")
        }

        val solutionEnd = System.currentTimeMillis()
        println("solution time: ${solutionEnd - solutionStart}ms")
        println("${moment}th moment: $nthMoment")

    }


}

data class MTFFMethod(val sysMatrix: TTSquareMatrix, val rightVector: TTVector)

private fun configedBasicMethod(FT: FaultTree, config: JsonObject): MTFFMethod {
    val Qmod = FT.getModifiedGenerator()
    Qmod.tt.roundAbsolute(1e-10)
    Qmod.tt.roundRelative(1e-10)
    return MTFFMethod(Qmod, TTVector.ones(Qmod.modes))
}

private fun configedNewerMethod(FT: FaultTree, config: JsonObject): MTFFMethod {
    val m = FT.getStateMaskVector()
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
    Qmod.tt.roundRelative(1e-16)
    for (result in applySolvers(Qmod.T(), pi0, approxSpectralRadius)) {
        report(result)
    }
    FT.mttfThroughKronsumMethod(50, 50, 1e-16, 1e-16,
            convergenceThreshold = 1e-7, verbose = true)
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
            truncationRelativeThreshold = 1e-10 / (2 * FT.getHighestExitRate())
    )

//    val Rinv = DMRGInvert(
//            R,
//            50,
//            verbose = true,
//            truncationRelativeThreshold = 1e-10,
//            preconditioner = preconditioner
//    )
    Rinv.tt.roundAbsolute(1e-16)
    println("Inversion relative residual: ${((Rinv * R) - tteye(R.modes)).frobenius() / R.numCols}")
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

fun applySolvers(A: TTSquareMatrix, b: TTVector, approxSpectralRadius: Double) =
        defaultSolvers.values.asSequence().map { it(A, b, approxSpectralRadius) }