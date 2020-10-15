package gspn

import MDDExtensions.GSCompaction
import MDDExtensions.toTensorTrain
import gspn.rateexpressions.Constant
import hu.bme.mit.delta.java.mdd.JavaMddFactory
import hu.bme.mit.delta.java.mdd.impl.DefaultJavaMddFactory
import hu.bme.mit.delta.mdd.*
import hu.bme.mit.inf.turnout.petrinet.Out
import hu.bme.mit.inf.turnout.petrinet.PetriNet
import hu.bme.mit.inf.turnout.petrinet.Reset
import hu.bme.mit.inf.turnout.petrinet.`NoEffect$`
import hu.bme.mit.inf.turnout.petrinet.saturation.StateSpaceExplorer
import hu.bme.mit.inf.turnout.symbolic.diagram.Mdd
import mapTuples
import org.ejml.data.DMatrixSparseCSC
import org.ejml.data.DMatrixSparseTriplet
import org.ejml.ops.ConvertDMatrixStruct
import org.ejml.simple.SimpleMatrix
import org.ejml.sparse.csc.CommonOps_DSCC
import scala.*
import scala.collection.JavaConverters
import solver.*
import solver.solvers.AMEnALSSolve
import solver.solvers.ConstrainedAMEnSolver
import java.lang.Integer.max
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import hu.bme.mit.inf.turnout.petrinet.Place as TurnoutPlace
import hu.bme.mit.inf.turnout.petrinet.Transition as TurnoutTransition
import hu.bme.mit.inf.turnout.petrinet.Weights as TurnoutWeights

fun Mdd.Node.toTT(capacities: List<Int>): TTVector {
    val cores = arrayListOf<CoreTensor>()
    var nodes = listOf(this as Mdd.InnerNode)
    for (capacity in capacities) {
        val nextNodes = nodes.flatMap { it.children().toList() }.distinct().filter { it !is Mdd.ZeroNode }
        val core = CoreTensor(capacity + 1, nodes.size, nextNodes.size)
        for ((row, node) in nodes.withIndex()) {
            for ((m, next) in node.children().withIndex()) {
                if (next !is Mdd.ZeroNode)
                    core[m][row, nextNodes.indexOf(next)] = 1.0
            }
        }
        cores.add(core)
        nodes = nextNodes.filterIsInstance<Mdd.InnerNode>()
    }
    return TTVector(TensorTrain(cores))
}

fun Mdd.Node.toDelta(order: MddVariableOrder) = this.toDelta(order, 0)
fun Mdd.Node.toDelta(order: MddVariableOrder, levelFromTop: Int): MddHandle {
    val signature = order.createSignatureFromVariables(order.toList().drop(levelFromTop))
    return when (this) {
        is Mdd.InnerNode -> {
            val builder = JavaMddFactory.getDefault().createTemplateBuilder()
            for ((idx, child) in this.children().withIndex()) {
                builder.set(idx, child.toDelta(order, levelFromTop + 1))
            }
            signature.topVariableHandle.checkIn(builder.buildAndReset())
        }
        is Mdd.ZeroNode -> {
            order.mddGraph.getTerminalZeroHandle(signature.topVariableHandle)
        }
        is Mdd.OneNode -> {
            (order.mddGraph as MddGraph<Boolean>).getHandleFor(true)
        }
        else -> throw RuntimeException("Unknown mdd node type: ${this.javaClass.name}")
    }
}

class GSPN(val places: ArrayList<Place>, val transitions: ArrayList<Transition>) {
    companion object {
        val mddFactory = DefaultJavaMddFactory()
    }

    fun getTangibleMaskVector() =
            stateSpace.calculateTangible().toTT(places.map { it.capacity })

    fun hasDeadlock(): Boolean {
        val order = getVariableOrder()
        var currSet = stateSpace.reachableStatesRoot().toDelta(order)
//        currSet = SetOperations.SetOperationsLevel.`subtract$`.`MODULE$`.apply(currSet, stateSpace.calculateTangible())
        currSet -= stateSpace.calculateTangible().toDelta(order)
        for (i in 1..(transitions.map { if (it is ImmediateTransition) it.priority else 0 }.max() ?: 0)) {
//            currSet = SetOperations.SetOperationsLevel.`subtract$`.`MODULE$`.apply(currSet, stateSpace.calculatePriority(i))
            currSet -= stateSpace.calculatePriority(i).toDelta(order)
        }
        return !currSet.isTerminalZero
    }

    fun getVariableOrder(): MddVariableOrder {
        computeCapacities()
        val variableOrder = mddFactory.createMddVariableOrder(LatticeDefinition.forSets())
        var last: MddVariable? = null
        for (place in places) {
            last =
                    if (last == null) variableOrder.createOnTop(MddVariableDescriptor.create(place.name, place.capacity + 1))
                    else variableOrder.createBelow(last, MddVariableDescriptor.create(place.name, place.capacity + 1))
        }
        return variableOrder
    }

    fun getRateMatrix(
            tolerancePerTerm: Double = 0.0,
            immediateRate: Double = 1.0,
            useCompaction: Boolean = false,
            variableOrder: MddVariableOrder = getVariableOrder()
    ): TTSquareMatrix {
        computeCapacities()
        val capacities = places.map(Place::capacity)
        val reachableMdd = stateSpace.reachableStatesRoot().toDelta(variableOrder)
        var p0mdd = stateSpace.calculateTangible().toDelta(variableOrder)
        if (useCompaction) p0mdd = GSCompaction.apply(p0mdd, reachableMdd)
        val p0mask: TTSquareMatrix = TTSquareMatrix.diag(TTVector(p0mdd.toTensorTrain()))
        val R0 = transitions.filterIsInstance<ExponentialTransition>().map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
        val res: TTSquareMatrix = p0mask * R0
        res.tt.roundAbsolute(0.0)
        if (tolerancePerTerm > 0.0)
            res.tt.roundRelative(tolerancePerTerm)
        val prios = transitions.filterIsInstance<ImmediateTransition>().groupBy(ImmediateTransition::priority)
        for ((prio, ts) in prios) {
            var prioMdd = stateSpace.calculatePriority(prio).toDelta(variableOrder)
            if (useCompaction) prioMdd = GSCompaction.apply(prioMdd, reachableMdd)
            val term =
                    TTSquareMatrix.diag(TTVector(prioMdd.toTensorTrain())) *
                    ts.map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
            term.tt.roundAbsolute(0.0)
            if (tolerancePerTerm > 0.0)
                term.tt.roundRelative(tolerancePerTerm)
            res += immediateRate * term
            res.tt.roundAbsolute(0.0)
        }
        return res
    }

    fun getRateMatrixAsSparseCores(
            immediateRate: Double = 1.0,
            variableOrder: MddVariableOrder,
            useCompaction: Boolean = false
    ): Array<Sparse2DCoreTensor> {
        val capacities = places.map(Place::capacity)
        val reachableMdd = stateSpace.reachableStatesRoot().toDelta(variableOrder)
        var p0mdd = stateSpace.calculateTangible().toDelta(variableOrder)
        if (useCompaction) p0mdd = GSCompaction.apply(p0mdd, reachableMdd)
        val R0 = transitions.filterIsInstance<ExponentialTransition>().map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
        var currMddNodes = listOf(p0mdd)
        var nextMddNodes = Array(p0mdd.variableHandle.variable.get().domainSize) { p0mdd[it] }.toSet().toList()
        val res = Array(places.size) { k ->
            val R0Core = R0.tt.cores[k]
            val data = Array(R0.modes[k]) { i ->
                Array(R0.modes[k]) { j ->
                    var nonZero = 0
                    val R0Mat = R0Core[i, j]
                    for (elem in 0 until R0Mat.numElements) {
                        if (R0Mat[elem] != 0.0) nonZero++
                    }
                    val mat = DMatrixSparseTriplet(
                            R0Core.rows * currMddNodes.size,
                            R0Core.cols * nextMddNodes.size,
                            currMddNodes.size * nonZero
                    )
                    for (k1 in currMddNodes.indices) {
                        if (currMddNodes[k1][i].isTerminalZero) continue
                        val l1 = nextMddNodes.indexOf(currMddNodes[k1][i])
                        for (k2 in 0 until R0Mat.numRows()) {
                            for (l2 in 0 until R0Mat.numCols()) {
                                val v = R0Mat[k2, l2]
                                if (v != 0.0) {
                                    val rowIdx = k1 * R0Mat.numRows() + k2
                                    val colIdx = l1 * R0Mat.numCols() + l2
                                    // (rowIdx, colIdx) should not be set yet, so
                                    // no need for the more expensive "set" method
                                    mat.addItemCheck(rowIdx, colIdx, v)
                                }
                            }
                        }
                    }
                    ConvertDMatrixStruct.convert(mat, null as DMatrixSparseCSC?)
                }
            }
            if (k < places.size - 1) {
                currMddNodes = nextMddNodes
                nextMddNodes = currMddNodes.flatMap { node ->
                    Array(node.variableHandle.variable.get().domainSize) { node[it] }.toList()
                }.toSet().toList()
                if (nextMddNodes[0].isTerminal)
                // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                    nextMddNodes = nextMddNodes.filter { it.data == true }
            }
            Sparse2DCoreTensor(R0.modes[k], data[0][0].numRows, data[0][0].numCols, data)
        }

        val prios = transitions.filterIsInstance<ImmediateTransition>().groupBy(ImmediateTransition::priority)

        for ((prio, ts) in prios) {
            val Rp = ts.map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
            var prioMdd = stateSpace.calculatePriority(prio).toDelta(variableOrder)
            if (useCompaction) prioMdd = GSCompaction.apply(prioMdd, reachableMdd)

            currMddNodes = listOf(prioMdd)
            nextMddNodes = Array(prioMdd.variableHandle.variable.get().domainSize) { prioMdd[it] }.toSet().toList()
            val newCores = Array(places.size) { k ->
                val RpCore = Rp.tt.cores[k]
                val data = Array(Rp.modes[k]) { i ->
                    Array(Rp.modes[k]) { j ->
                        var nonZero = 0
                        val RpMat = immediateRate * RpCore[i, j]
                        for (elem in 0 until RpMat.numElements) {
                            if (RpMat[elem] != 0.0) nonZero++
                        }
                        val mat = DMatrixSparseTriplet(
                                RpCore.rows * currMddNodes.size,
                                RpCore.cols * nextMddNodes.size,
                                currMddNodes.size * nonZero
                        )
                        for (k1 in 0 until currMddNodes.size) {
                            if (currMddNodes[k1][i].isTerminalZero) continue
                            val l1 = nextMddNodes.indexOf(currMddNodes[k1][i])
                            for (k2 in 0 until RpMat.numRows()) {
                                for (l2 in 0 until RpMat.numCols()) {
                                    val v = RpMat[k2, l2]
                                    if (v != 0.0) {
                                        val rowIdx = k1 * RpMat.numRows() + k2
                                        val colIdx = l1 * RpMat.numCols() + l2
                                        // (rowIdx, colIdx) should not be set yet, so
                                        // no need for the more expensive "set" method
                                        mat.addItemCheck(rowIdx, colIdx, v)
                                    }
                                }
                            }
                        }
                        ConvertDMatrixStruct.convert(mat, null as DMatrixSparseCSC?)
                    }
                }
                if (k < places.size - 1) {
                    currMddNodes = nextMddNodes
                    nextMddNodes = currMddNodes.flatMap { node ->
                        Array(node.variableHandle.variable.get().domainSize) { node[it] }.toList()
                    }.toSet().toList()
                    if (nextMddNodes[0].isTerminal)
                    // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                        nextMddNodes = nextMddNodes.filter { it.data == true }
                }
                Sparse2DCoreTensor(R0.modes[k], data[0][0].numRows, data[0][0].numCols, data)
            }

            for (i in res.indices)
                res[i] = res[i].plus(newCores[i],
                        when (i) {
                            0 -> CoreTensorPosition.FIRST
                            res.size - 1 -> CoreTensorPosition.LAST
                            else -> CoreTensorPosition.MIDDLE
                        })
        }
        return res
    }

    private fun getSteadyStateDistributionDense(verbose: Boolean = false, tolerancePerTerm: Double = 0.0, solver: (TTSquareMatrix) -> TTSolution): TTVector {
        val varOrder = getVariableOrder()
        if (verbose) println("Computing rate matrix")
        val timeStart = System.currentTimeMillis()
        val rateMatrix = getRateMatrix(tolerancePerTerm, variableOrder = varOrder)
        val timeRateEnd = System.currentTimeMillis()
        if (verbose) println("Rate matrix computation time: ${timeRateEnd - timeStart}")
        if (verbose) println("Rounding rate matrix, original largest rank: ${rateMatrix.ttRanks().max()}")
//        rateMatrix.tt.roundAbsolute(1e-14)
        val timeRoundingEnd = System.currentTimeMillis()
        if (verbose) println("Rounding time: ${timeRoundingEnd - timeRateEnd} \nRounded largest rank: ${rateMatrix.ttRanks().max()}")
        if (verbose) println("Computing steady state")
        val Q = rateMatrix - TTSquareMatrix.diag(rateMatrix * TTVector.ones(rateMatrix.modes))
        val s = solver(Q.T())
        val pi = s.solution.hadamard(getTangibleMaskVector())
        return pi / (pi * TTVector.ones(pi.modes))
    }

    fun getGeneratorAsSparseCores(immediateRate: Double, variableOrder: MddVariableOrder, useCompaction: Boolean): Array<Sparse2DCoreTensor> {
        val rateMatrix = getRateMatrixAsSparseCores(immediateRate, variableOrder, useCompaction)
        val diag = Array(rateMatrix.size) { idx ->
            val rateCore = rateMatrix[idx]
            val diagCore = Sparse2DCoreTensor(rateCore.modeLength, rateCore.rows, rateCore.cols)
            for (i in 0 until diagCore.modeLength) {
                var sum = DMatrixSparseCSC(diagCore.rows, diagCore.cols, 0)
                for (j in 0 until rateCore.modeLength) {
                    //TODO: check if this works without temp storage
                    val nextSum = DMatrixSparseCSC(diagCore.rows, diagCore.cols, max(rateCore[i, j].nz_length, sum.nz_length))
                    CommonOps_DSCC.add(1.0, sum, 1.0, rateCore[i, j], nextSum, null, null)
                    sum = nextSum
                }
                if (idx == 0) CommonOps_DSCC.scale(-1.0, sum, diagCore[i, i])
                else diagCore[i, i] = sum
            }
            diagCore
        }
        val Q: Array<Sparse2DCoreTensor> = Array(rateMatrix.size) {
            val pos: CoreTensorPosition = when (it) {
                0 -> CoreTensorPosition.FIRST
                rateMatrix.size - 1 -> CoreTensorPosition.LAST
                else -> CoreTensorPosition.MIDDLE
            }
            rateMatrix[it].plus(diag[it], pos)
        }
        return Q
    }

    fun getSteadyStateDistributionSparse(
            verbose: Boolean = false,
            useCompaction: Boolean = false,
            useConstrainedAMEn: Boolean = false,
            enrichmentRank: Int = 4
    ): TTVector {
        if (verbose) println("Computing rate matrix")
        val timeStart = System.currentTimeMillis()
        val varOrder = getVariableOrder()
        val immediateRate = this.transitions
                .filterIsInstance<ExponentialTransition>()
                .map(ExponentialTransition::rate)
                .filterIsInstance<Constant>()
                .map(Constant::value).average()
        val Q = getGeneratorAsSparseCores(immediateRate, varOrder, useCompaction) //TODO: immediate rate
        val QT = Q.map(Sparse2DCoreTensor::transpose).toTypedArray()

        val diagMultiplier = 1.0
        val reachableMdd = stateSpace.reachableStatesRoot().toDelta(varOrder)
        val unreachableMdd =
                (reachableMdd.variableHandle.mddGraph as MddGraph<Boolean>)
                        .getHandleFor(true).minus(reachableMdd)
        var currMddNodes = listOf(unreachableMdd)
        var nextMddNodes = Array(unreachableMdd.variableHandle.variable.get().domainSize) { unreachableMdd[it] }.toSet().toList()
        val unreachableDiag = Array(places.size) { k ->
            val data = Array(Q[k].modeLength) { i ->
                Array(Q[k].modeLength) { j ->
                    val mat = DMatrixSparseTriplet(
                            currMddNodes.size,
                            nextMddNodes.size,
                            currMddNodes.size
                    )
                    for (row in currMddNodes.indices) {
                        if (currMddNodes[row][i].isTerminalZero) continue
                        val col = nextMddNodes.indexOf(currMddNodes[row][i])
                        mat.addItemCheck(row, col, diagMultiplier)
                    }
                    ConvertDMatrixStruct.convert(mat, null as DMatrixSparseCSC?)
                }
            }
            if (k < places.size - 1) {
                currMddNodes = nextMddNodes
                nextMddNodes = currMddNodes.flatMap { node ->
                    Array(node.variableHandle.variable.get().domainSize) { node[it] }.toList()
                }.toSet().toList()
                if (nextMddNodes[0].isTerminal)
                // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                    nextMddNodes = nextMddNodes.filter { it.data == true }
            }
            Sparse2DCoreTensor(Q[k].modeLength, data[0][0].numRows, data[0][0].numCols, data)
        }
        val QTMod = QT.mapIndexed { idx, it ->
            it.plus(unreachableDiag[idx],
                    if (idx == 0) CoreTensorPosition.FIRST
                    else if (idx == QT.size - 1) CoreTensorPosition.LAST
                    else CoreTensorPosition.MIDDLE)
        }.toTypedArray()

        val timeRateEnd = System.currentTimeMillis()
        if (verbose) println("Rate matrix computation time: ${timeRateEnd - timeStart}")
        if (verbose) println("Computing steady state")
        val reachableMask = TTVector(reachableMdd.toTensorTrain())
//        val normalizer = reachableMask * TTVector.ones(reachableMask.modes)
        val init = getInitialStateVectorAsTT()
        init.tt.roundAbsolute(1e-8)
        val s =
                if (useConstrainedAMEn) ConstrainedAMEnSolver.solve(
//                QT as Array<Abstract2DCoreTensor>,
                        QTMod as Array<Abstract2DCoreTensor>,
                        TTVector.zeros(QT.map(Abstract2DCoreTensor::modeLength).toTypedArray()),
//                x0 = reachableMask / normalizer,
//                        x0 = init,
                        residualThreshold = 1e-8,
                        maxSweeps = 100,
                        enrichmentRank = enrichmentRank,
                        normalize = true,
                        truncateBasedOnResidual = true,
                        useApproxResidualForStopping = false
//                        , reachableStateSpaceIndicator = reachableMask
                        , constraintCores = reachableMask
                )
                else AMEnALSSolve(
//                QT as Array<Abstract2DCoreTensor>,
                        QTMod as Array<Abstract2DCoreTensor>,
                        TTVector.zeros(QT.map(Abstract2DCoreTensor::modeLength).toTypedArray()),
//                x0 = reachableMask / normalizer,
                        x0 = init,
                        residualThreshold = 1e-8,
                        maxSweeps = 100,
                        enrichmentRank = enrichmentRank,
                        normalize = true,
                        truncateBasedOnResidual = true,
                        useApproxResidualForStopping = false
                        , reachableStateSpaceIndicator = reachableMask
                )

//        val denseR = getRateMatrix(1e-10, immediateRate, false, varOrder)
//        denseR.tt.roundAbsolute(1e-10)
//        val denseQT = (denseR - TTSquareMatrix.diag(denseR*TTVector.ones(denseR.modes))).T()
//        println("Check: ${(denseQT * s.solution.hadamard(reachableMask)).norm()}")

        val pi = s.solution.hadamard(getTangibleMaskVector())

//        MddInterpreter.forEachNonzeroTuple(stateSpace.reachableStatesRoot().toDelta(varOrder), { t, b ->
//            print(s.solution.tt.get(*t.toIntArray())); println("; "); true
//        }, Boolean::class.javaObjectType)

        return pi / (pi * TTVector.ones(pi.modes))
    }

    fun getSteadyStateDistribution(
            verbose: Boolean = false,
            tolerancePerTerm: Double = 0.0,
            useSparseSolver: Boolean = false,
            solver: (TTSquareMatrix) -> TTSolution
    ): TTVector {
        return if (useSparseSolver)
            getSteadyStateDistributionSparse(verbose)
        else
            getSteadyStateDistributionDense(verbose, tolerancePerTerm, solver)
    }


    fun getMTTA(absorbingStates: MddHandle, rho: Double = 1.0, enrichmentRank: Int = 4): Double {
        val varOrder = getVariableOrder()
        val modifiedGeneratorCores=
                getMatrixForMtta(absorbingStates, rho, varOrder)

//        val explicitMttaMatrix = SimpleMatrix(stateSpace.nReachable().toInt(), stateSpace.nReachable().toInt())
//        val reachableState = stateSpace.reachableStatesRoot().toDelta(varOrder).mapTuples {it}
//        for((idxI, i) in reachableState.withIndex()) {
//            for((idxJ, j) in reachableState.withIndex()) {
//                var v = ones(1)
//                for (k in i.indices) {
//                    v = modifiedGeneratorCores[k].multFromLeft(i[k], j[k], v)
//                }
//                explicitMttaMatrix[idxI, idxJ] = v[0,0]
//            }
//        }

        val tangibleMaskVector = TTVector(stateSpace.calculateTangible().toDelta(varOrder).toTensorTrain())
        val initialStateVector = getInitialStateVectorAsTT()

        val meanTimeVector = AMEnALSSolve(
                A = modifiedGeneratorCores.toTypedArray<Abstract2DCoreTensor>(),
                y = initialStateVector,
//                x0 = initialStateVector,
                residualThreshold = 1e-8,
                maxSweeps = 100,
                enrichmentRank = enrichmentRank,
                normalize = false
        ).solution
        return meanTimeVector * tangibleMaskVector
    }

    private fun getMatrixForMtta(absorbingStates: MddHandle, rho: Double, varOrder: MddVariableOrder): MutableList<Sparse2DCoreTensor> {
        if (absorbingStates.isTerminalZero)
            throw java.lang.IllegalArgumentException("The set of absorbing states is empty!")
        val immediateRate = this.transitions
                .filterIsInstance<ExponentialTransition>()
                .map(ExponentialTransition::rate)
                .filterIsInstance<Constant>()
                .map(Constant::value).average()

        val Q = getGeneratorAsSparseCores(immediateRate, varOrder, false)
        var currMddNodes = listOf(absorbingStates)
        var nextMddNodes =
                Array(absorbingStates.variableHandle.variable.get().domainSize) { absorbingStates[it] }.toSet().toList()
        val modifiedGeneratorCores = Q.mapIndexed { k, Qk ->
            val maskCoreData = Array(Qk.modeLength) { i ->
                Array(Qk.modeLength) { j ->
                    val mat = DMatrixSparseTriplet(
                            currMddNodes.size,
                            nextMddNodes.size,
                            currMddNodes.size
                    )
                    for (row in currMddNodes.indices) {
                        if (currMddNodes[row][i].isTerminalZero) continue
                        val col = nextMddNodes.indexOf(currMddNodes[row][i])
                        mat.addItemCheck(row, col, 1.0)
                    }
                    ConvertDMatrixStruct.convert(mat, null as DMatrixSparseCSC?)
                }
            }
            if (k < places.size - 1) {
                currMddNodes = nextMddNodes
                nextMddNodes = currMddNodes.flatMap { node ->
                    Array(node.variableHandle.variable.get().domainSize) { node[it] }.toList()
                }.toSet().toList()
                if (nextMddNodes[0].isTerminal)
                // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                    nextMddNodes = nextMddNodes.filter { it.data == true }
            }

            val maskCore = Sparse2DCoreTensor(Qk.modeLength, maskCoreData[0][0].numRows, maskCoreData[0][0].numCols)
            val pos = when (k) {
                0 -> CoreTensorPosition.FIRST
                Q.size - 1 -> CoreTensorPosition.LAST
                else -> CoreTensorPosition.MIDDLE
            }

            Qk.plus(Qk.mult(maskCore), pos).plus(maskCore.mult(Qk), pos)
        }.toMutableList()

        val originalAbsorbing = getDeadlockSet(varOrder)
        if (!originalAbsorbing.isTerminalZero) {
            currMddNodes = listOf(originalAbsorbing)
            nextMddNodes =
                    Array(absorbingStates.variableHandle.variable.get().domainSize) { originalAbsorbing[it] }.toSet().toList()
            for (k in modifiedGeneratorCores.indices) {
                val modeLength = modifiedGeneratorCores[k].modeLength
                val origAbsCoreData = Array(modeLength) { i ->
                    Array(modeLength) { j ->
                        val mat = DMatrixSparseTriplet(
                                currMddNodes.size,
                                nextMddNodes.size,
                                currMddNodes.size
                        )
                        if (i == j) {
                            for (row in currMddNodes.indices) {
                                if (currMddNodes[row][i].isTerminalZero) continue
                                val col = nextMddNodes.indexOf(currMddNodes[row][i])
                                mat.addItemCheck(row, col, if (i == 0) rho else 1.0)
                            }
                        }
                        ConvertDMatrixStruct.convert(mat, null as DMatrixSparseCSC?)
                    }
                }
                if (k < places.size - 1) {
                    currMddNodes = nextMddNodes
                    nextMddNodes = currMddNodes.flatMap { node ->
                        Array(node.variableHandle.variable.get().domainSize) { node[it] }.toList()
                    }.toSet().toList()
                    if (nextMddNodes[0].isTerminal)
                    // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                        nextMddNodes = nextMddNodes.filter { it.data == true }
                }

                val origAbsorbCore = Sparse2DCoreTensor(
                        modeLength,
                        origAbsCoreData[0][0].numRows,
                        origAbsCoreData[0][0].numCols,
                        origAbsCoreData
                )
                val pos = when (k) {
                    0 -> CoreTensorPosition.FIRST
                    modifiedGeneratorCores.size - 1 -> CoreTensorPosition.LAST
                    else -> CoreTensorPosition.MIDDLE
                }
                modifiedGeneratorCores[k] = modifiedGeneratorCores[k].plus(origAbsorbCore, pos).transpose()
            }
        }
        return modifiedGeneratorCores
    }

    fun getInitialStateVectorAsTT(): TTVector {
        val cores = arrayListOf<CoreTensor>()
        val order = getVariableOrder()
        for (v in order) {
            val name = v.traceInfo as String
            val p = places.first { it.name == name }
            val core = CoreTensor(v.domainSize, 1, 1)
            core.data[p.initialMarking] = ones(1)
            cores.add(core)
        }
        return TTVector(TensorTrain(cores))
    }

    fun getDeadlockSet(order: MddVariableOrder): MddHandle {
        var deadlockSet = stateSpace.reachableStatesRoot().toDelta(order)
        deadlockSet -= stateSpace.calculateTangible().toDelta(order)
        for (i in 1..(transitions.map { if (it is ImmediateTransition) it.priority else 0 }.max() ?: 0)) {
            deadlockSet -= stateSpace.calculatePriority(i).toDelta(order)
        }
        return deadlockSet
    }

    fun getMTTA(rho: Double = 1.0): Double {
        val order = getVariableOrder()
        val deadlockSet = getDeadlockSet(order)
        return getMTTA(deadlockSet, rho)
    }

    fun steadyStateExpectedTokenNumbers(verbose: Boolean = false, solver: (TTSquareMatrix) -> TTSolution): List<Double> {
        val ss = getSteadyStateDistribution(verbose, 0.0, false, solver)
        return places.indices.map {
            val m = TTVector.ones(ss.modes)
            m.tt.cores[it] = CoreTensor(m.tt.cores[it].modeLength, 1, 1).apply {
                repeat(this.modeLength) { n ->
                    data[n] = mat[r[n]]
                }
            }
            ss * m
        }
    }

    private val turnoutPN by lazy { toTurnoutPetriNet() }
    val stateSpace by lazy {
        StateSpaceExplorer.default().apply(turnoutPN)
    }

    private fun toTurnoutPetriNet(): PetriNet {
        fun TurnoutWeights.plus(other: TurnoutWeights): TurnoutWeights {
            val inh = when (this.inhibitor() as Any) {
                is Some<*> -> Option.apply((if (other.inhibitor() is Some)
                    (this.inhibitor().get() as Int) + (other.inhibitor().get() as Int)
                else (this.inhibitor().get() as Int)) as Any)
                is `None$` -> other.inhibitor()
                else -> throw RuntimeException("This should really not happen")
            }
            val otherEffect = other.effect()
            val thisEffect = this.effect()
            when (thisEffect) {
                is `NoEffect$` -> return TurnoutWeights(this.`in`() + other.`in`(), otherEffect, inh)
                is Out -> when (otherEffect) {
                    is `NoEffect$` -> return TurnoutWeights(this.`in`() + other.`in`(), thisEffect, inh)
                    is Out -> return TurnoutWeights(this.`in`() + other.`in`(), Out(thisEffect.out() + otherEffect.out()), inh)
                    else -> throw IllegalArgumentException()
                }
                else -> when (otherEffect) {
                    is `NoEffect$` -> return TurnoutWeights(this.`in`() + other.`in`(), thisEffect, inh)
                    else -> throw java.lang.IllegalArgumentException()
                }
            }
        }

        val p = arrayListOf<TurnoutPlace>()
        val placeMap = hashMapOf<Place, TurnoutPlace>()
        for (place in places) {
            val turnoutPlace = TurnoutPlace(place.name, place.initialMarking)
            p.add(turnoutPlace)
            placeMap.put(place, turnoutPlace)
        }
        val t = hashSetOf<TurnoutTransition>()
        for (transition in transitions) {
            val w = hashMapOf<TurnoutPlace, TurnoutWeights>()
            val prew = HashMap<Place, TurnoutWeights>()
            for (arc in transition.inputs) {
                //TODO: weights
                val arcWeight = when (arc) {
                    is Arc.ConstantArc -> TurnoutWeights(arc.value, `NoEffect$`.`MODULE$`, `None$`.`MODULE$` as Option<Any>)
                    else -> throw RuntimeException()
                }
                if (prew.containsKey(arc.place)) {
                    prew[arc.place] = prew[arc.place]!!.plus(arcWeight)
                } else {
                    prew[arc.place] = arcWeight
                }
            }
            for (arc in transition.outputs) {
                //TODO: weights
                val arcWeight = when (arc) {
                    is Arc.ConstantArc -> TurnoutWeights(0, Out(arc.value), `None$`.`MODULE$` as Option<Any>)
                    is Arc.ResetArc -> TurnoutWeights(0, Reset(arc.resetTo), `None$`.`MODULE$` as Option<Any>)
                    else -> throw RuntimeException()
                }
                if (prew.containsKey(arc.place)) {
                    prew[arc.place] = prew[arc.place]!!.plus(arcWeight)
                } else {
                    prew[arc.place] = arcWeight
                }
            }
            for (arc in transition.inhibitors) {
                //TODO: weights
                val arcWeight = when (arc) {
                    is Arc.ConstantArc -> TurnoutWeights(0, `NoEffect$`.`MODULE$`, Option.apply(arc.value))
                    else -> throw RuntimeException()
                }
                if (prew.containsKey(arc.place)) {
                    prew[arc.place] = prew[arc.place]!!.plus(arcWeight)
                } else {
                    prew[arc.place] = arcWeight
                }
            }
            for ((k, v) in prew) {
                w.put(placeMap[k]!!, v)
            }
            t.add(TurnoutTransition(transition.name,
                    JavaConverters.mapAsScalaMap<TurnoutPlace, TurnoutWeights>(w).toMap(
                            `Predef$`.`MODULE$`.conforms<Tuple2<TurnoutPlace, TurnoutWeights>>()
                    ), if (transition is ImmediateTransition) transition.priority else 0))
        }
        return PetriNet(JavaConverters.asScalaBuffer<TurnoutPlace>(p), JavaConverters.asScalaSet<TurnoutTransition>(t).toSet())
    }

    fun computeCapacities() {
        var nodes = setOf(stateSpace.reachableStatesRoot() as Mdd.InnerNode)
        for (place in places) {
            place.capacity = nodes.map { it.nChildren() - 1 }.max() ?: 0
            nodes = nodes.flatMap { it.children().toList() }.filterIsInstance<Mdd.InnerNode>().toSet()
        }
    }
}