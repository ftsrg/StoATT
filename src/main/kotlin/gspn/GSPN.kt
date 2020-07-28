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
import hu.bme.mit.inf.turnout.symbolic.diagram.SetOperations
import scala.*
import scala.collection.JavaConverters
import solver.*
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
        val core = CoreTensor(capacity+1, nodes.size, nextNodes.size)
        for ((row, node) in nodes.withIndex()) {
            for((m, next) in node.children().withIndex()) {
                if(next !is Mdd.ZeroNode)
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
    return when(this) {
        is Mdd.InnerNode -> {
            val builder = JavaMddFactory.getDefault().createTemplateBuilder()
            for ((idx, child) in this.children().withIndex()) {
                builder.set(idx, child.toDelta(order, levelFromTop+1))
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
        for(i in 1..(transitions.map { if(it is ImmediateTransition) it.priority else 0 }.max()?:0)) {
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
        if(useCompaction) p0mdd = GSCompaction.apply(p0mdd, reachableMdd)
        val p0mask: TTSquareMatrix = TTSquareMatrix.diag(TTVector(p0mdd.toTensorTrain()))
        val R0 = transitions.filterIsInstance<ExponentialTransition>().map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
        val res: TTSquareMatrix = p0mask*R0
        res.tt.roundAbsolute(0.0)
        if(tolerancePerTerm > 0.0)
            res.tt.roundRelative(tolerancePerTerm)
        val prios = transitions.filterIsInstance<ImmediateTransition>().groupBy(ImmediateTransition::priority)
        for ((prio, ts) in prios) {
            var prioMdd = stateSpace.calculatePriority(prio).toDelta(variableOrder)
            if(useCompaction) prioMdd = GSCompaction.apply(prioMdd, reachableMdd)
            val term =
                    TTSquareMatrix.diag(TTVector(prioMdd.toTensorTrain())) *
                    ts.map { it.toTT(variableOrder, places) }.reduce(TTSquareMatrix::plus)
            term.tt.roundAbsolute(0.0)
            if(tolerancePerTerm > 0.0)
                term.tt.roundRelative(tolerancePerTerm)
            res += immediateRate * term
            res.tt.roundAbsolute(0.0)
        }
        return res
    }

    fun getSteadyStateDistribution(verbose: Boolean = false, tolerancePerTerm: Double=0.0, solver: (TTSquareMatrix) -> TTSolution): TTVector {
        if(verbose) println("Computing rate matrix")
        val timeStart = System.currentTimeMillis()
        val rateMatrix = getRateMatrix(tolerancePerTerm)
        val timeRateEnd = System.currentTimeMillis()
        if(verbose) println("Rate matrix computation time: ${timeRateEnd-timeStart}")
        if(verbose) println("Rounding rate matrix, original largest rank: ${rateMatrix.ttRanks().max()}")
        rateMatrix.tt.roundAbsolute(1e-14)
        val timeRoundingEnd = System.currentTimeMillis()
        if(verbose) println("Rounding time: ${timeRoundingEnd-timeRateEnd} \nRounded largest rank: ${rateMatrix.ttRanks().max()}")
        if(verbose) println("Computing steady state")
        val Q = rateMatrix - TTSquareMatrix.diag(rateMatrix * TTVector.ones(rateMatrix.modes))
        val s = solver(Q.T())
        val pi = s.solution.hadamard(getTangibleMaskVector())
        return pi/(pi*TTVector.ones(pi.modes))
    }

    fun steadyStateExpectedTokenNumbers(verbose: Boolean = false, solver: (TTSquareMatrix) -> TTSolution): List<Double> {
        val ss = getSteadyStateDistribution(verbose, 0.0, solver)
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
            place.capacity = nodes.map { it.nChildren()-1 }.max() ?: 0
            nodes = nodes.flatMap { it.children().toList() }.filterIsInstance<Mdd.InnerNode>().toSet()
        }
    }
}