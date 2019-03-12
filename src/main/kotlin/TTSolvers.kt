/**
 * Inverts the elements of a TT-Vector using the Newton-Schulz iterative algorithm
 * @param V Vector to invert in TensorTrain format
 * @param thresh Threshold of the residual's Frobenius norm used for convergence check
 * @return Element-wise inverse of the input in TensorTrain format
 */
fun NSInvertVect(V: TTVector, thresh: Double, roundingAccuracy: Double): TTVector {
    val ones = TTVector.ones(V.modes)
    var Vinv = ones * (1.0/V.tt.frobenius())
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

fun TTJacobi(A: TTSquareMatrix, b: TTVector, thresh: Double, roundingAccuracy: Double, log: Boolean = false): TTVector{
    for ((idx, mode) in A.modes.withIndex()) {
        assert(mode == b.modes[idx]) { "The modes of A and b must be identical!" }
    }
    val D = A.diagVect()
    val Dinv = TTSquareMatrix.diag(
            NSInvertVect(D, 0.00001 * D.tt.frobenius(), 0.0001)
    )
    val R = A - A.diag()
    var x = TTVector.zeros(A.modes)
    var residual: TTVector
    do {
        x = Dinv * (b - R*x)
        if (log) {
            println("Exact:")
            x.printElements()
            println()
        }
        x.tt.round(roundingAccuracy)
        if (log) {
            println("Rounded:")
            x.printElements()
            println()
        }
        residual = b - A*x
    } while (residual.tt.frobenius() > thresh)
    if (log) {
        println()
        println("Final residual:")
        residual.printElements()
    }
    return x
}

fun AMEn(A: TTSquareMatrix, b: TTVector): TTVector {
    TODO()
}

fun newtonSchulz(A: TTSquareMatrix): TTSquareMatrix {
    TODO()
}