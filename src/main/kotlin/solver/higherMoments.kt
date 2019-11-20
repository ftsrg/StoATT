package solver

import faulttree.FaultTree
import java.util.*

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