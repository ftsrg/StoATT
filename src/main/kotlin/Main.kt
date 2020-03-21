
import MDDExtensions.GSCompaction
import benchmark.generateKanban
import benchmark.largeTreeString
import faulttree.BasicEvent
import faulttree.FaultTree
import faulttree.FaultTreeNode
import faulttree.galileoParser
import gspn.*
import gspn.rateexpressions.Constant
import hu.bme.mit.delta.mdd.MddBuilder
import org.ejml.simple.SimpleMatrix
import solver.*
import java.util.*
import kotlin.math.abs

fun main(args: Array<String>) {

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
    val R = g.getRateMatrix()
    val eps = TTSquareMatrix.diag(TTVector.ones(R.modes)-g.stateSpace.reachableStatesRoot().toTT(g.places.map { it.capacity }))
    val Q = R - TTSquareMatrix.diag(R*TTVector.ones(R.modes))-eps
    val Qorig = Q.copy()
    Q.tt.roundRelative(1e-12)
    val pi = AMEnSolve(
            A = Q.T(),
            y = TTVector.zeros(R.modes),
            x0 = TTVector.ones(R.modes) / R.numCols.toDouble(),
            residualThreshold = 1e-7,
            maxSweeps = 50,
            enrichmentRank = 2,
            normalize = true
    )
    println((pi.solution * TTVector.ones(pi.solution.modes)))
    println((Qorig.T() * pi.solution).norm())
    println("solution:")
    pi.solution.printElements()
    println("steady state: ")
    val ss = pi.solution.hadamard(g.getTangibleMaskVector())
    ss /= (ss * TTVector.ones(ss.modes))
    ss.printElements()
    return


//    val M = TTSquareMatrix.rand(Array(4) {2}, arrayOf(1,3,3,3,1), random =  Random(10))
//    val MInv = DMRGInvert(M, 50, verbose = false, truncationRelativeThreshold = 1e-16)
//    println(((MInv * M) - TTSquareMatrix.eye(M.modes)).frobenius())

//    val n = 3
//    val random = Random(1)
//    val mats = Array(n) {SimpleMatrix.random_DDRM(2, 2, 0.0, 10.0, random)}
//    val T = kronSumAsTT(mats.toList())
//    val TInv = approxInvertKronsum(mats.toList(), 100, 0.0)
//    T.printElements()
//    (TInv*T).printElements(numDecimals = 5)
//    println(TInv.ttRanks())
//    val eye = TTSquareMatrix.eye(T.modes)
//    println((TInv * T - eye).frobenius()/eye.frobenius())
//    return

    val testTreeDesc = largeTreeString

    val Ft = galileoParser.parse(testTreeDesc.byteInputStream())
    println(Ft.getNthMoment(1, 1e-7, { M, b, threshold ->
        AMEnSolve(
                M, b,
                TTVector.ones(b.modes),
                threshold,
                20,
                4
        )
    }))
    return

//    val T = TTSquareMatrix.rand(Array(4) {2}, arrayOf(1,3,3,3,1))
//    val inv = DMRGInvert(T, 100)
//    println((inv*T-TTSquareMatrix.eye(T.modes)).frobenius())
//    return

//    println(Ft.mttfThroughKronsumMethod(500, 50, 1e-16, 1e-16))
//    return

//    val kronsumComponents = Ft.getKronsumComponents()
//    FileWriter("kronsumComponents.txt").use { kronsumFile ->
//        for (component in kronsumComponents) {
//            component.reshape(1, component.numElements)
//            kronsumFile.write(component.toString())
//        }
//    }
//    FileWriter("modifier.tt").use { modifierFile ->
//        val modifierForMTTF = Ft.getModifierForMTTF(Ft.getBaseGenerator())
//        modifierForMTTF.tt.roundRelative(1e-20)
//        println(modifierForMTTF.ttRanks())
//        modifierFile.write(modifierForMTTF.tt.dataAsString())
//    }
//    return

    val baseGenerator = Ft.getBaseGenerator()
    val stateMaskVector = Ft.getOperationalIndicatorVector()
    stateMaskVector.tt.roundRelative(0.0001)
    val residualThreshold = 0.00001 * stateMaskVector.norm()
    val perturbedGeneratorMatrix = Ft.getModifiedGenerator()
    perturbedGeneratorMatrix.tt.roundRelative(0.0001)
//
//    val matFile = FileWriter("modifiedGenerator.tt")
//    matFile.write(perturbedGeneratorMatrix.tt.dataAsString())
//    matFile.close()
//    val vectFile = FileWriter("stateMaskVector.tt")
//    vectFile.write(stateMaskVector.tt.dataAsString())
//    vectFile.close()
//
//
//    println(perturbedGeneratorMatrix.tt.dataAsString())
//    return

    println("ALS:")
    val r = 4
    val ones = TTVector.ones(stateMaskVector.modes)
    var x0 = TTVector.ones(stateMaskVector.modes)
    for (i in 0 until r) {
        x0 = x0+x0.hadamard(ones)
    }
    x0.divAssign(r.toDouble())
    x0 = TTVector.rand(x0.modes, x0.tt.ranks().toTypedArray())
    val alsRes = ALSSolve(perturbedGeneratorMatrix, stateMaskVector, x0=x0, residualThreshold = residualThreshold, maxSweeps = 10).solution
    report(perturbedGeneratorMatrix, stateMaskVector, alsRes, residualThreshold)

    println("DMRG:")
    var x0DMRG = TTVector.ones(stateMaskVector.modes)
    for (i in 0 until r) {
        x0DMRG = x0DMRG+ x0DMRG.hadamard(ones)
    }
    x0.divAssign(r.toDouble())
    val dmrgRes = DMRGSolve(perturbedGeneratorMatrix, stateMaskVector, x0=x0DMRG, absoluteResidualThreshold = residualThreshold, maxSweeps = 10).solution
    report(perturbedGeneratorMatrix, stateMaskVector, dmrgRes, residualThreshold)

    println("GMRES without preconditioner:")
    val res = TTGMRES(perturbedGeneratorMatrix, stateMaskVector, TTVector.zeros(stateMaskVector.modes), 0.00001, maxIter = 10000, verbose = true)
    report(perturbedGeneratorMatrix, stateMaskVector, res.solution, residualThreshold)

    println("GMRES with Jacobi preconditioner:")
    val prec = jacobiPreconditioner(perturbedGeneratorMatrix, stateMaskVector)
//    val precdMtx = prec * perturbedGeneratorMatrix
//    precdMtx.tt.roundRelative(0.0001)
    val precdVect = prec*stateMaskVector
    precdVect.tt.roundRelative(0.0001)
    val resPrecd = TTGMRES(prec, perturbedGeneratorMatrix, precdVect, TTVector.zeros(stateMaskVector.modes), 0.0001)
    report(perturbedGeneratorMatrix, stateMaskVector, resPrecd.solution, residualThreshold)

    println("TT-Jacobi: ")
    val res2 = TTJacobi(perturbedGeneratorMatrix, stateMaskVector, 0.0001 * stateMaskVector.norm(), 0.00001, stateMaskVector).solution
    report(perturbedGeneratorMatrix, stateMaskVector, res2, residualThreshold)

    println()
    println("MTFF = ${-res2[0]}")
    println()
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