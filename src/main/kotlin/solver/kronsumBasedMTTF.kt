package solver

import faulttree.FaultTree
import org.ejml.simple.SimpleMatrix
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

fun FaultTree.mttfThroughKronsumMethod(
        numNeumannTerms: Int, numExpInvTerms: Int,
        approxInvRounding: Double = 0.0, neumannTermsRound: Double  = 0.0
): Double {
    //TODO: all of this works only with static fault trees
    val kronsumComponents = this.getKronsumComponents()
    val M = getBaseGenerator()
    val delta = M.diag()
    var deltaInfNorm = 0.0
    for (component in kronsumComponents) {
        val lambda = component[0, 1]
        val mu = component[1,0]
        deltaInfNorm += max(lambda, mu)
    }
    val gamma = deltaInfNorm
    val modifiedKronsumComponents = kronsumComponents.map {
        -(it - gamma / kronsumComponents.size * eye(2))
    }

    val Q1Inv = -approxInvertKronsum(modifiedKronsumComponents, numExpInvTerms, approxInvRounding)
//    var term = Q1Inv * getStateMaskVector()
    var term = Q1Inv * TTVector.ones(Q1Inv.modes)
    term.tt.roundRelative(1e-10)
    var res = term.copy()
    val S = this.getModifierForMTTF(M)
    S.tt.roundRelative(1e-10)
    val Q2 = delta + gamma * TTSquareMatrix.eye(M.modes)
    val coeff = -Q1Inv*(Q2-S)
    coeff.tt.roundRelative(1e-10)
    var res0 = term[0]
    for (i in 0 until numNeumannTerms - 1) {
        term = coeff*term
        res0 += term[0]
        term.tt.roundRelative(neumannTermsRound)
        println(res0)
    }
    return res0
}

/**
 * Calculates approximation of the inverse of a matrix given as a Kronecker sum of 2x2 matrices by exponential sums
 * @param components Terms of the Kronecker sum; each matrix must be 2x2
 * @param n maximum n of the returned tensortrain (number of exponential terms to use in the approximation)
 * @param tolerance relative tolerance of TT rounding after each addition
 */
fun approxInvertKronsum(components: List<SimpleMatrix>, n: Int, tolerance: Double): TTSquareMatrix {
    val alpha = (1..n).map { (2 * it - 1).toDouble() }
    val beta = (1..n-1).toList().map { it.toDouble() }
    val T = SimpleMatrix.diag(*alpha.toDoubleArray())
    // Fill Jacobi matrix
    for (i in 0 until n-1) {
        T[i, i+1] = beta[i]
        T[i+1, i] = beta[i]
    }
    val eig = T.eig()
    val V = SimpleMatrix(eig.numberOfEigenvalues, eig.numberOfEigenvalues)
    for (i in 0 until eig.numberOfEigenvalues) {
        V[0, i] = eig.getEigenVector(i)
    }
    val a_pre = V.row (0).elementPower(2.0)
    val eigenPairs = Array(eig.numberOfEigenvalues) { Pair(eig.eigenvalues[it].real, a_pre[it]) }.toMutableList().sortedBy { it.first }
//    val eigs = Array(eig.numberOfEigenvalues) { Pair(eig.eigenvalues[it].real, eig.getEigenVector(it)) }.toMutableList().sortedBy { it.first }
    val b = eigenPairs.map { it.first }
    val a = eigenPairs.map { it.second }.mapIndexed {idx, a -> exp(ln(a)+b[idx])} //a*exp(b[idx]) changed to be more stable

    val modes = Array(components.size) { 2 }
    var res = TTSquareMatrix.zeros(modes)
    for (i in 0 until n) {
        val expComponents = components.map { Qk -> exp2by2(-b[i]*Qk) }
        val cores = Array<CoreTensor>(expComponents.size) {
            val core = CoreTensor(4, 1, 1)
            val component = expComponents[it]
            core[0][0] = component[0]
            core[1][0] = component[1]
            core[2][0] = component[2]
            core[3][0] = component[3]
            return@Array core
        }
        val M = TTSquareMatrix(TensorTrain(ArrayList(cores.toList())), modes)
        res.plusAssign(a[i]*M)
        res.tt.roundRelative(tolerance)
    }

    return res
}

fun kronSumAsTT(components: List<SimpleMatrix>): TTSquareMatrix {
    val eye = TTSquareMatrix.eye(components.map { it.numRows() }.toTypedArray())
    val kronProds = components.mapIndexed { idx, component ->
        val ttComp = eye.copy()
        for (i in 0 until ttComp.tt.cores[idx].data.size) {
            ttComp.tt.cores[idx].data[i][0] = component[i]
        }
        return@mapIndexed ttComp
    }
    return kronProds.reduce(TTSquareMatrix::plus)
}