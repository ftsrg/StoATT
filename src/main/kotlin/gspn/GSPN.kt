package gspn

import MDDExtensions.GSCompaction
import MDDExtensions.toTensorTrain
import hu.bme.mit.delta.java.mdd.JavaMddFactory
import hu.bme.mit.delta.java.mdd.impl.DefaultJavaMddFactory
import hu.bme.mit.delta.mdd.*
import hu.bme.mit.inf.turnout.petrinet.Out
import hu.bme.mit.inf.turnout.petrinet.PetriNet
import hu.bme.mit.inf.turnout.petrinet.Reset
import hu.bme.mit.inf.turnout.petrinet.`NoEffect$`
import hu.bme.mit.inf.turnout.petrinet.saturation.StateSpaceExplorer
import hu.bme.mit.inf.turnout.symbolic.diagram.Mdd
import org.ejml.data.DMatrixSparse
import org.ejml.data.DMatrixSparseCSC
import org.ejml.data.DMatrixSparseTriplet
import org.ejml.ops.ConvertDMatrixStruct
import org.ejml.sparse.csc.CommonOps_DSCC
import scala.*
import scala.collection.JavaConverters
import solver.*
import solver.solvers.AMEnALSSolve
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
        val order = TODO()
        var currSet = stateSpace.reachableStatesRoot().toDelta(order)
//        currSet = SetOperations.SetOperationsLevel.`subtract$`.`MODULE$`.apply(currSet, stateSpace.calculateTangible())
        currSet -= stateSpace.calculateTangible().toDelta(order)
        for (i in 1..(transitions.map { if (it is ImmediateTransition) it.priority else 0 }.max() ?: 0)) {
//            currSet = SetOperations.SetOperationsLevel.`subtract$`.`MODULE$`.apply(currSet, stateSpace.calculatePriority(i))
            currSet -= stateSpace.calculatePriority(i).toDelta(order)
        }
        return !currSet.isTerminalZero
    }

    fun getRateMatrix(
            tolerancePerTerm: Double = 0.0,
            immediateRate: Double = 1.0,
            useCompaction: Boolean = false
    ): TTSquareMatrix {
        computeCapacities()
        val variableOrder = mddFactory.createMddVariableOrder(LatticeDefinition.forSets())
        var last: MddVariable? = null
        for (place in places) {
            last =
                    if (last == null) variableOrder.createOnTop(MddVariableDescriptor.create(place.name, place.capacity + 1))
                    else variableOrder.createBelow(last, MddVariableDescriptor.create(place.name, place.capacity + 1))
        }
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
            useCompaction: Boolean = false
    ): Array<Sparse2DCoreTensor> {
        computeCapacities()
        val variableOrder = mddFactory.createMddVariableOrder(LatticeDefinition.forSets())
        var last: MddVariable? = null
        for (place in places) {
            last =
                    if (last == null) variableOrder.createOnTop(MddVariableDescriptor.create(place.name, place.capacity + 1))
                    else variableOrder.createBelow(last, MddVariableDescriptor.create(place.name, place.capacity + 1))
        }
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
            currMddNodes = nextMddNodes
            nextMddNodes = currMddNodes.flatMap { node -> Array(node.size()) { node[it] }.toList() }.toSet().toList()
            if(nextMddNodes[0].isTerminal)
                // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                nextMddNodes = nextMddNodes.filter { it.data == true }

            Sparse2DCoreTensor(R0.modes[k], data[0][0].numRows, data[0][0].numCols, data)
        }

        val prios = transitions.filterIsInstance<ImmediateTransition>().groupBy(ImmediateTransition::priority)

        for ((prio, ts) in prios) {
            val Rp = ts.map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
            var prioMdd = stateSpace.calculatePriority(prio).toDelta(variableOrder)
            if (useCompaction) prioMdd = GSCompaction.apply(prioMdd, reachableMdd)

            currMddNodes = listOf(prioMdd)
            nextMddNodes = Array(prioMdd.variableHandle.variable.get().domainSize) { p0mdd[it] }.toSet().toList()
            val newCores = Array(places.size) { k ->
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
                        for (k1 in 0 until currMddNodes.size) {
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
                currMddNodes = nextMddNodes
                nextMddNodes = currMddNodes.flatMap { node -> Array(node.size()) { node[it] }.toList() }.toSet().toList()
                if(nextMddNodes[0].isTerminal)
                // keep only the terminal 1 node -- maybe there is a better way, but this works for now
                    nextMddNodes = nextMddNodes.filter { it.data == true }

                Sparse2DCoreTensor(R0.modes[k], data[0][0].numRows, data[0][0].numCols, data)
            }

            for(i in res.indices)
                res[i] += newCores[i]
        }
        return res
    }

    private fun getSteadyStateDistributionDense(verbose: Boolean = false, tolerancePerTerm: Double = 0.0, solver: (TTSquareMatrix) -> TTSolution): TTVector {
        if (verbose) println("Computing rate matrix")
        val timeStart = System.currentTimeMillis()
        val rateMatrix = getRateMatrix(tolerancePerTerm)
        val timeRateEnd = System.currentTimeMillis()
        if (verbose) println("Rate matrix computation time: ${timeRateEnd - timeStart}")
        if (verbose) println("Rounding rate matrix, original largest rank: ${rateMatrix.ttRanks().max()}")
        rateMatrix.tt.roundAbsolute(1e-14)
        val timeRoundingEnd = System.currentTimeMillis()
        if (verbose) println("Rounding time: ${timeRoundingEnd - timeRateEnd} \nRounded largest rank: ${rateMatrix.ttRanks().max()}")
        if (verbose) println("Computing steady state")
        val Q = rateMatrix - TTSquareMatrix.diag(rateMatrix * TTVector.ones(rateMatrix.modes))
        val s = solver(Q.T())
        val pi = s.solution.hadamard(getTangibleMaskVector())
        return pi / (pi * TTVector.ones(pi.modes))
    }

    private fun getSteadyStateDistributionSparse(verbose: Boolean = false): TTVector {
        if (verbose) println("Computing rate matrix")
        val timeStart = System.currentTimeMillis()
        val rateMatrix = getRateMatrixAsSparseCores() //TODO: immediate rate
        val timeRateEnd = System.currentTimeMillis()
        if (verbose) println("Rate matrix computation time: ${timeRateEnd - timeStart}")
        if (verbose) println("Computing steady state")
        val diag = Array(rateMatrix.size) { idx ->
            val rateCore = rateMatrix[idx]
            val diagCore = Sparse2DCoreTensor(rateCore.modeLength, rateCore.rows, rateCore.cols)
            for(i in 0 until diagCore.modeLength) {
                var sum = DMatrixSparseCSC(diagCore.rows, diagCore.cols,0)
                for(j in 0 until rateCore.modeLength) {
                    //TODO: check if this works without temp storage
                    val nextSum = DMatrixSparseCSC(diagCore.rows, diagCore.cols, max(rateCore[i,j].nz_length, sum.nz_length))
                    CommonOps_DSCC.add(1.0, sum, 1.0, rateCore[i,j], nextSum, null, null)
                    sum = nextSum
                }
                if(idx == 1) CommonOps_DSCC.scale(-1.0, sum, diagCore[i, i])
                diagCore[i, i] = sum
            }
            diagCore
        }
//        val Q = rateMatrix - TTSquareMatrix.diag(rateMatrix * TTVector.ones(rateMatrix.modes))
        val QT: Array<Abstract2DCoreTensor> = Array(rateMatrix.size) {
            (rateMatrix[it] + diag[it]).transpose()
        }
        val s = AMEnALSSolve(
                QT,
                TTVector.zeros(QT.map(Abstract2DCoreTensor::modeLength).toTypedArray()),
                residualThreshold = 1e-8,
                maxSweeps = 100,
                enrichmentRank = 4
        )
        val pi = s.solution.hadamard(getTangibleMaskVector())
        return pi / (pi * TTVector.ones(pi.modes))
    }

    fun getSteadyStateDistribution(
            verbose: Boolean = false,
            tolerancePerTerm: Double = 0.0,
            useSparseSolver: Boolean = false,
            solver: (TTSquareMatrix) -> TTSolution
    ): TTVector {
        return if(useSparseSolver)
            getSteadyStateDistributionSparse(verbose)
        else
            getSteadyStateDistributionDense(verbose, tolerancePerTerm, solver)
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