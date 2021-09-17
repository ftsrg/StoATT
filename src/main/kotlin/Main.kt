/*
 *
 *   Copyright 2021 Budapest University of Technology and Economics
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */


import MDDExtensions.GSCompaction
import faulttree.BasicEvent
import faulttree.FaultTree
import faulttree.FaultTreeNode
import gspn.PetriNet
import gspn.Place
import gspn.Transition
import gspn.arc
import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddHandle
import org.ejml.simple.SimpleMatrix
import solver.ALSSolve
import solver.TTSquareMatrix
import solver.TTVector
import solver.solvers.AMEnALSSolve
import java.util.*
import kotlin.math.abs

fun main(args: Array<String>) {

    val N = PetriNet {
        val A = p("A", 1)
        val B = p("B", 0)
        timed("AtoB", 30.0) {
            input(arc(A, 1))
            out(arc(B, 1))
        }
        timed("BtoA", 10.0) {
            input(arc(B, 1))
            out(arc(A, 1))
        }
    }
    val sparse = true
//    val sparse = false
    val ss = if(!sparse) N.getSteadyStateDistribution(true, 0.0) { A ->
        AMEnALSSolve(
                A = A,
                y = TTVector.zeros(A.modes),
                residualThreshold = 1e-7,
                maxSweeps = 50,
                enrichmentRank = 2,
                normalize = true,
                verbose = true,
                useApproxResidualForStopping = false
        )
    } else N.getSteadyStateDistributionSparse(true, false)
    ss.printElements()
}

private fun compactionTest() {
    val A = BasicEvent("A", 0.5)
    val B = BasicEvent("B", 0.5)
    val C = BasicEvent("C", 0.5)
    val D = BasicEvent("D", 0.5)
    val E = BasicEvent("E", 0.5)
    val FT = FaultTree((A and B) or (C and D) and E)
    val f = FT.nonFailureAsMdd()
    val varOrdering = FT.getVariableOrdering()
    val builder = MddBuilder<Boolean>(varOrdering.createSignatureFromTraceInfos(listOf("A", "B")))
    var c = builder.build(listOf(arrayOf(1, 1), arrayOf(1, 0), arrayOf(0, 1)), true)
    c = c.union(MddBuilder<Boolean>(varOrdering.defaultSetSignature).build(Array(varOrdering.size) { 0 }, false))
    val compacted = GSCompaction.apply(f, c)
    for (a in 0..1)
        for (b in 0..1)
            for (c1 in 0..1)
                for (d in 0..1)
                    for (e in 0..1)
                        if (compacted[a, b, c1, d, e].data != f[a, b, c1, d, e].data) {
                            println("[$a, $b, $c1, $d, $e]: f=${f[a, b, c1, d, e].data} comp=${compacted[a, b, c1, d, e].data} care=${c[a, b, c1, d, e].data}")
                        }
    return
}

fun faultTreeGrowthTest() {
    val rand = Random(123)
    var topNode: FaultTreeNode = BasicEvent("ev0", rand.nextDouble() * 10.0)
    for (i in 1..40) {
        println("Number of leaves: ${i + 1}")
        topNode = topNode and BasicEvent("ev$i", rand.nextDouble() * 10.0)
        if (i < 28) continue
        val ft = FaultTree(topNode)
        val A = ft.getModifiedGenerator()
        A.tt.roundRelative(1e-30)
        val b = ft.getOperationalIndicatorVector()
        val r = 3
        val ones = TTVector.ones(b.modes)
        var x0 = TTVector.ones(b.modes)
        for (j in 0 until r) {
            x0 = x0 + x0.hadamard(ones)
        }
        x0.divAssign(r.toDouble())
        val relativeThreshold = 0.0001
        val residualThreshold = relativeThreshold * b.norm()
        println(residualThreshold)
//        val y = TTReGMRES(A, b, x0, 0.0001, verbose = true)
        val y = ALSSolve(A, b, x0, residualThreshold, 15)
        println()
    }
}

fun generateSPN(nPlaces: Int, nTransitions: Int, capacities: Int, minRate: Double = 1.0, maxRate: Double = 10.0) {
    val rand = Random()
    val p = arrayListOf<Place>()
    repeat(nPlaces) {
        p.add(Place("p$it", capacities))
    }
    val t = arrayListOf<Transition>()
    repeat(nTransitions) {
        val inps = rand.nextInt(4)
        val outs = rand.nextInt(4)
    }
}

fun report(A: TTSquareMatrix, b: TTVector, x: TTVector, threshold: Double) =
        report(A::times, b, x, threshold)

fun report(linearMap: (TTVector) -> TTVector, b: TTVector, x: TTVector, threshold: Double) {
    println("results:")
    val resNorm = (b - linearMap(x)).norm()
    println("residual norm: $resNorm ${if (resNorm < threshold) "<" else ">"} $threshold (threshold)")
    println("relative residual norm: ${resNorm / b.norm()}")
    print("solution vector: ")
    if (x.numElements < 100) {
        x.printElements(); println()
    } else
        println("First element: ${x[0]}")
    println("TT ranks of the result: ${x.tt.cores.map { it.rows }}")
    print("Non-nullness in absorbing states: ${x.tt.hadamard((TTVector.ones(x.modes) - b).tt).frobenius()}")
    println()
    println()
}

private val rand = Random()
fun randSquareMtx(size: Int): SimpleMatrix {
    return SimpleMatrix.random_DDRM(size, size, 0.0, 10.0, rand)
}

fun SimpleMatrix.roundZeros(threshold: Double = 1E-14) {
    for (i in 0 until numRows()) {
        for (j in 0 until numCols()) {
            if (abs(this[i, j]) < threshold) this[i, j] = 0.0
        }
    }
}