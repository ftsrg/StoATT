/**
 * Inverts the elements of a TT-Vector using the Newton-Schulz iterative algorithm
 * @param V Vector to invert in TensorTrain format
 * @param thresh Threshold of the residual's Frobenius norm used for convergence check
 * @return Element-wise inverse of the input in TensorTrain format
 */
fun NSInvertVect(V: TTVector, thresh: Double, roundingAccuracy: Double): TTVector {
    val ones = TTVector.ones(V.modes)
    var Vinv = ones * (1.0/V.tt.frobenius())
    var trunc = 0
    do {
        val residual = ones - TTVector(V.tt.hadamard(Vinv.tt))
        Vinv += TTVector(Vinv.tt.hadamard(residual.tt))
        Vinv.tt.round(roundingAccuracy)
    } while (residual.tt.frobenius() > thresh)
    return Vinv
}

fun TTGMRES(A: TTSquareMatrix, b: TTVector): TTVector {
    TODO()
}

fun TTJacobi(A: TTSquareMatrix, b: TTVector, thresh: Double, roundingAccuracy: Double): TTVector{
    for ((idx, mode) in A.modes.withIndex()) {
        assert(mode == b.modes[idx]) { "The modes of A and b must be identical!" }
    }
    val D = A.diagVect()
    val Dinv = TTSquareMatrix.diag(
            NSInvertVect(D, 0.001 * D.tt.frobenius(), 0.001)
    )
    val R = A - A.diag()
    var x = TTVector.zeros(A.modes)
    var residual: TTVector
    do {
        x = Dinv * (b - R*x)
        x.tt.round(roundingAccuracy)
        residual = b - A*x
        println(residual.tt.frobenius()) //TODO: remove log
    } while (residual.tt.frobenius() > thresh)
    return x
}

fun AMEn(A: TTSquareMatrix, b: TTVector): TTVector {
    TODO()
}

fun newtonSchulz(A: TTSquareMatrix): TTSquareMatrix {
    TODO()
}