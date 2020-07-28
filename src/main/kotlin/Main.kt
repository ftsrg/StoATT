
import MDDExtensions.GSCompaction
import MDDExtensions.toTensorTrain
import benchmark.generateKanban
import benchmark.generateLongKanban
import faulttree.BasicEvent
import faulttree.FaultTree
import faulttree.FaultTreeNode
import gspn.*
import gspn.rateexpressions.Constant
import hu.bme.mit.delta.mdd.LatticeDefinition
import hu.bme.mit.delta.mdd.MddBuilder
import hu.bme.mit.delta.mdd.MddVariable
import hu.bme.mit.delta.mdd.MddVariableDescriptor
import org.ejml.simple.SimpleMatrix
import solver.ALSSolve
import solver.TTSquareMatrix
import solver.TTVector
import java.util.*
import kotlin.math.abs

fun main(args: Array<String>) {

    val rand = Random()
    val N = generateLongKanban(4, 2, {rand.nextDouble()*9.0+1.0})
    N.run {
        computeCapacities()
        val variableOrder = GSPN.mddFactory.createMddVariableOrder(LatticeDefinition.forSets())
        var last: MddVariable? = null
        for (place in places) {
            last =
                    if (last == null) variableOrder.createOnTop(MddVariableDescriptor.create(place.name, place.capacity + 1))
                    else variableOrder.createBelow(last, MddVariableDescriptor.create(place.name, place.capacity + 1))
        }
        val capacities = places.map(Place::capacity)
        val reachableMdd = stateSpace.reachableStatesRoot().toDelta(variableOrder)
        var p0mdd = stateSpace.calculateTangible().toDelta(variableOrder)
        val p0mask: TTSquareMatrix = TTSquareMatrix.diag(TTVector(p0mdd.toTensorTrain()))
        val R0 = transitions.filterIsInstance<ExponentialTransition>().map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
        val threshold = 1e-8
        val p0maskRounded = p0mask.copy()
        p0maskRounded.tt.roundAbsolute(threshold/R0.frobenius())
        val R0Rounded = R0.copy()
        R0Rounded.tt.roundAbsolute(threshold/p0mask.frobenius())
        val res: TTSquareMatrix = p0mask*R0
        val resCopy = res.copy()
        var start = System.currentTimeMillis()
        resCopy.tt.roundAbsolute(1e-12)
        val fullSVDTime = System.currentTimeMillis()-start
        val resOtherCopy = res.copy()
        start = System.currentTimeMillis()
        resOtherCopy.tt.roundAbsolute(1e-12, true)
        val iterSVDTime = System.currentTimeMillis()-start
        val i = 0 // NOP
    }
    return

    val l1 = 1.0
    val l2 = 0.1
    val l3 = 2.0
    val l4 = 0.2
    val p = arrayListOf(
            Place("p0", 2, 1),
            Place("p1", 2, 1),
            Place("p2", 1, 0)
    )
    val t = arrayListOf<Transition>(
            ExponentialTransition("t1", arrayListOf(Arc.ConstantArc(p[0],1)), arrayListOf(Arc.ConstantArc(p[1], 1)), arrayListOf(Arc.ConstantArc(p[2], 1)), Constant(4.0)),
            ExponentialTransition("t2", arrayListOf(Arc.ConstantArc(p[1],1)), arrayListOf(Arc.ConstantArc(p[0], 1)), arrayListOf(), Constant(1.0)),
            ExponentialTransition("t3", arrayListOf(), arrayListOf(Arc.ConstantArc(p[2], 1)), arrayListOf(Arc.ConstantArc(p[2], 1)), Constant(2.0)),
            ExponentialTransition("t4", arrayListOf(Arc.ConstantArc(p[2],1)), arrayListOf(), arrayListOf(), Constant(3.0))
    )
    val g = generateKanban(1, {1.0})
    val R1 = g.getRateMatrix()
    val R2 = g.getRateMatrix(useCompaction = true)
//    g.computeCapacities()
//    val mdd = g.stateSpace.reachableStatesRoot()
//    val order = JavaMddFactory.getDefault().createMddVariableOrder(LatticeDefinition.forSets())
//    for (place in g.places.reversed()) {
//        order.createOnTop(MddVariableDescriptor.create(place.name, place.capacity+1))
//    }
//    val deltamdd = mdd.toDelta(order, 0)
    return
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
    var topNode: FaultTreeNode = BasicEvent("ev0", rand.nextDouble()*10.0)
    for (i in 1..40) {
        println("Number of leaves: ${i+1}")
        topNode = topNode and BasicEvent("ev$i", rand.nextDouble()*10.0)
        if(i < 28) continue
        val ft = FaultTree(topNode)
        val A = ft.getModifiedGenerator()
        A.tt.roundRelative(1e-30)
        val b = ft.getOperationalIndicatorVector()
        val r = 3
        val ones = TTVector.ones(b.modes)
        var x0 = TTVector.ones(b.modes)
        for (j in 0 until r) {
            x0 = x0+x0.hadamard(ones)
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

fun report(linearMap: (TTVector)->TTVector, b: TTVector, x: TTVector, threshold: Double) {
    println("results:")
    val resNorm = (b-linearMap(x)).norm()
    println("residual norm: $resNorm ${if(resNorm<threshold) "<" else ">"} $threshold (threshold)")
    println("relative residual norm: ${resNorm/b.norm()}")
    print("solution vector: ")
    if(x.numElements < 100) {
        x.printElements(); println()
    }
    else
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