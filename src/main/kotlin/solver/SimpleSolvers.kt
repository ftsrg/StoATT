package solver

import org.ejml.simple.SimpleMatrix
import kotlin.math.sqrt

data class SolverResult(val solution: SimpleMatrix, val residualNorm: Double)

fun ReGMRES(linearMap: (SimpleMatrix) -> SimpleMatrix, b: SimpleMatrix, m: Int,
            x0: SimpleMatrix = SimpleMatrix(b.numRows(), 1), threshold: Double, maxIters: Int = 200): SimpleMatrix {
    var res = x0
    var iter = 0
    do {
        val iterResult = GMRES(linearMap, b, m, res)
        res = iterResult.solution
        val realResidualNorm = (linearMap(res)-b).normF()
        iter++
    } while ((iterResult.residualNorm > threshold || (linearMap(res)-b).normF() > threshold) && iter < maxIters)
    return res
}

fun biCGStab(linearMap: (SimpleMatrix) -> SimpleMatrix, b: SimpleMatrix, m: Int,
             x0: SimpleMatrix = ones(b.numElements), threshold: Double): SimpleMatrix {
    var result = x0.copy()
    var r = b - linearMap(x0);
    val rstar0 = ones(r.numElements)
    var p = r
    var u = r
    for (j in 0 until m) {
        val Ap = linearMap(p)
        val alpha = r.scalarProduct(rstar0) / Ap.scalarProduct(rstar0)
        val q = u - alpha * Ap
        val update = alpha * (u + q)
        result += update
        val temp = r.scalarProduct(rstar0) // TODO: keep from prev iter
        r -= linearMap(update)
        val beta = r.scalarProduct(rstar0) / temp
        u = r + beta * q
        p = u + beta * (q+beta*p)
        if(r.normF() < threshold)
            return result
    }
    return result
}

fun BiCGStabL(l: Int, linearMap: (SimpleMatrix) -> SimpleMatrix, b: SimpleMatrix, m: Int,
              x0: SimpleMatrix = SimpleMatrix(b.numRows(), 1), threshold: Double): SolverResult {
    //TODO: not working

    // Based on:
    // Sleipjen, Fokkema: BICGSTAB(L) FOR LINEAR EQUATIONS INVOLVING UNSYMMETRIC MATRICES WITH COMPLEX SPECTRUM

    var k = -l
    val r0 = ones(b.numElements)
    var r = b-linearMap(x0)
    var rho0 = 1.0
    var alpha = 0.0
    var omega = 1.0
    var u = x0.createLike()
    var x = x0.copy()
    var iter = -1
    while( (iter++) < m && r.normF() > threshold) {
        k += l
        rho0 *= -omega
        val rhats = Array(l+1) { SimpleMatrix(0,0) }
        val uhats = Array(l+1) {SimpleMatrix(0,0)}
        var xhat = x.copy()
        uhats[0] = u.copy()
        rhats[0] = r.copy()

        // Bi-CG part
        for(j in 0 until l) {
            val rho1 = rhats[0].scalarProduct(r0)
            val beta =  alpha*rho1/rho0
            rho0 = rho1
            for(i in 0..j) {
                uhats[i] = rhats[i]-beta*uhats[i]
            }
            uhats[j+1] = linearMap(uhats[j])
            val gamma = uhats[j+1].scalarProduct(r0)
            alpha = rho0/gamma
            for(i in 0..j){
                rhats[i] -= alpha*uhats[i+1]
            }
            rhats[j+1]=linearMap(rhats[j])
            xhat += alpha * uhats[0]
        }

        // MR part
        val taus = Array(l+1) { Array(l+1) { 0.0 } }
        val sigmas = Array(l+1) {0.0}
        val gammaprimes = Array(l+1) {0.0}
        for(j in 1..l) {
            for (i in 1 until j) {
                taus[i][j]=rhats[j].scalarProduct(rhats[i])/sigmas[i]
                rhats[j] -= taus[i][j]*rhats[i]
            }
            sigmas[j] = rhats[j].scalarProduct(rhats[j])
            gammaprimes[j] = rhats[0].scalarProduct(rhats[j])/sigmas[j]
        }

        val gammas = Array(l+1) { 0.0 }
        gammas[l] = gammaprimes[l]
        omega=gammas[l]
        for (j in l-1 downTo 1) {
            gammas[j] = gammaprimes[j]
            for(i in j+1 until l) {
                gammas[j] -= taus[j][i]*gammas[i+1]
            }
        }
        val gammadprimes = Array(l) { 0.0 }
        for(j in 1 until l) {
            gammadprimes[j] = gammas[j+1]
            for(i in j+1 until l) {
                gammadprimes[j] += taus[j][i]*gammas[i+1]
            }
        }

        // update
        xhat += gammas[1]*rhats[0]
        rhats[0] -= gammaprimes[l]*rhats[l]
        uhats[0] -= gammas[l]*uhats[l]

        for (j in 1 until l) {
            uhats[0] -= gammas[j]*uhats[j]
            xhat += gammadprimes[j]*rhats[j]
            rhats[0] -= gammaprimes[j]*rhats[j]
        }

        u = uhats[0]
        r = rhats[0]
        x = xhat
    }
    return SolverResult(x, r.normF())
}

fun GMRES(linearMap: (SimpleMatrix)->SimpleMatrix, b: SimpleMatrix, m: Int,
          x0: SimpleMatrix = SimpleMatrix(b.numRows(), 1)): SolverResult {
    val r0 = b - linearMap(x0)
    val beta = r0.vecNorm2()
    var V = SimpleMatrix(b.numRows(), m)
    V[0, 0] = r0 / beta
    var H = SimpleMatrix(m + 1, m)
    for (j in 0 until m) {
        var w = linearMap(V.col(j))
        for (i in 0..j) {
            val vi = V.col(i)
            H[i, j] = w.T() * vi
            w -= H[i, j] * vi
        }
        H[j + 1, j] = w.vecNorm2()
        if (H[j + 1, j].nearZero(10E-14)) {
            H = H[0..j + 1, 0..j]
            V = V[0..V.numRows(), 0..j]
            break
        }
        if (j < m - 1)
            V[0, j + 1] = w / H[j + 1, j]
    }
    val g = beta * eye(H.numRows()).col(0)
    for (i in 0 until H.numCols()) {
        val rowNext = H[i + 1, i]
        val rowCurr = H[i, i]
        val denom = sqrt(rowCurr * rowCurr + rowNext * rowNext)
        val s = rowNext / denom
        val c = rowCurr / denom
        val r1 = H.row(i)
        val r2 = H.row(i + 1)
        H[i, 0] = c * r1 + s * r2
        H[i + 1, 0] = c * r2 - s * r1
        val g1 = g[i]
        val g2 = g[i + 1]
        g[i] = c * g1 + s * g2
        g[i + 1] = c * g2 - s * g1
    }
    val residualNorm = H[H.numElements-1]
    H = H[0..H.numRows() - 1, 0..H.numCols()]
    val y = g[0..g.numElements - 1, 0..1]
    for (i in y.numElements - 1 downTo 0) {
        for (j in y.numElements - 1 downTo i + 1) {
            y[i] -= H[i, j] * y[j]
        }
        y[i] /= H[i, i]
    }
    return SolverResult(x0 + V * y, residualNorm)
}

fun createJacobiPreconditioner(A: (SimpleMatrix)->SimpleMatrix, numCols: Int): (SimpleMatrix)->SimpleMatrix {
    val res = SimpleMatrix(numCols, 1)
    val E = eye(numCols)
    for(i in 0 until numCols) {
        val v = A(E.col(i))
        res[i] = if(v[i]==0.0) 0.0 else 1.0/v[i]
    }
    return {res.elementMult(it)}
}
