package solver

import faulttree.BasicEvent.Companion.BasicEventVar
import faulttree.FaultTree
import faulttree.PHBasicEvent.Companion.PHEventVar

fun FaultTree.mtff(solver: (TTSquareMatrix, TTVector)->TTSolution) =
        getNthMoment(1, solver)

fun FaultTree.getNthMoment(n: Int, solver: (TTSquareMatrix, TTVector)->TTSolution): Double {

    val variables = this.getOrderedVariables()
    val pi0Cores = Array(variables.size) {
        val core = CoreTensor(variables[it].variableDescriptor.domainSize, 1, 1)
        core[0][0] = 1.0
        return@Array core
    }
    val Q = this.getModifiedGenerator()
    val QT = Q.T()
    var left = TTVector(TensorTrain(ArrayList(pi0Cores.toList())))
    var right = TTVector.ones(left.modes)

    repeat(n) {
        if( (left.ttRanks().max() ?: 0) < (right.ttRanks().max() ?: 0) )
            left = solver(QT, left).solution
        else
            right = solver(Q, right).solution
    }

    return left * right
}

fun FaultTree.getSteadyStateDistribution(solver: (TTSquareMatrix, TTVector) -> TTSolution): TTVector {
//    this will be needed only for DFT-s
//    val R = this.getBaseRateMatrix()
//    val Q = R - TTSquareMatrix.diag(R*TTVector.ones(R.modes))
//    val s = solver(Q.T(), TTVector.zeros(Q.modes)).solution

    // the components in static fault trees are independent,
    // so the steady state can be computed as the Kronecker product of
    // the individual steady states
    val cores = getOrderedVariables().map {
        // TODO: common ancestor for the BE var classes
        ((it as? BasicEventVar)?.event ?: (it as PHEventVar).event).getSteadyStateVector()
    }.map { ssVector ->
        val core = CoreTensor(ssVector.numElements, 1,1)
        repeat(ssVector.numElements) { idx -> core[idx][0] = ssVector[idx]}
        return@map core
    }
    return TTVector(TensorTrain(ArrayList(cores)))
}

data class SteadyStateMetrics(val MTBF: Double, val MTTF: Double, val MTTR: Double)
fun FaultTree.computeSteadyStateMetrics(steadyState: TTVector): SteadyStateMetrics {
    val R = this.getBaseRateMatrix()
    val ones = TTVector.ones(R.modes)
    val Q = R - TTSquareMatrix.diag(R * ones)
    Q.tt.roundAbsolute(1e-16)
    val nonFailure = getOperationalIndicatorVector()
    val ssMod = steadyState.hadamard(nonFailure)
    ssMod.tt.roundAbsolute(1e-16)
    val MTBF = ssMod*(Q*(ones-nonFailure))
    val A = steadyState*nonFailure
    val MTTF = MTBF*A
    val MTTR = MTBF-MTTF
    return SteadyStateMetrics(MTBF, MTTF, MTTR)
}