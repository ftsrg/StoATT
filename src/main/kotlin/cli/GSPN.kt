package cli

import benchmark.generateKanban
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import solver.AMEnSolve
import solver.TTVector
import kotlin.random.Random

class GSPN: CliktCommand() {
    override fun run() = Unit
}

class Kanban: CliktCommand() {
    val N by argument().int().restrictTo(min = 1)
    val random by option().flag(default = false)
    val enrichment by option().int().restrictTo(min = 1).default(1)
    override fun run() {
        val model = generateKanban(N, if(random) {{Random.nextDouble(0.1, 1.0)}} else {{1.0}})
        val start = System.currentTimeMillis()
        val steadyStateDistribution = model.getSteadyStateDistribution { A ->
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