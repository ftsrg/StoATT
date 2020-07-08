package cli

import benchmark.generateKanban
import benchmark.generateLongKanban
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import solver.AMEnSolve
import solver.TTVector
import solver.product
import kotlin.random.Random

class GSPN: CliktCommand() {
    override fun run() = Unit
}

class Kanban: CliktCommand() {
    val N by argument().int().restrictTo(min = 1)
    val random by option().flag(default = false)
    val enrichment by option().int().restrictTo(min = 1).default(1)
    val blocks by option().int().restrictTo(min = -1).default(-1)
    val tolerance by option().double().restrictTo(min=0.0, max=0.1).default(0.0)
    override fun run() {
        val getNextRate = if(random) {{Random.nextDouble(0.1, 1.0)}} else {{1.0}}
        val model =
                if(blocks == -1) generateKanban(N, getNextRate)
                else generateLongKanban(blocks, N, getNextRate)
        model.computeCapacities()
        println("Reachable statespace size: ${model.stateSpace.nReachable()}")
        println("Direct product statespace size: ${model.places.map{it.capacity+1}.product()}")
        val start = System.currentTimeMillis()
        val steadyStateDistribution = model.getSteadyStateDistribution(true, tolerance) { A ->
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
        println(steadyStateDistribution*TTVector.ones(steadyStateDistribution.modes))
        val end = System.currentTimeMillis()
        println("Duration: ${end-start}ms")
    }
}