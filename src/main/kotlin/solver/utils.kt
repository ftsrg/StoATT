package solver

import kotlin.math.abs

fun Double.nearZero(thresh: Double = 10E-14) = abs(this) < thresh

fun Array<Int>.product() = fold(1) {prod, curr -> prod*curr}
fun Iterable<Int>.product() = fold(1) {prod, curr -> prod*curr}

fun <T> Iterable<T>.indexOfFirst(default: Int, predicate: (T)->Boolean): Int {
    val idx = indexOfFirst(predicate)
    return if(idx == -1) default else idx
}
fun <T> Array<T>.indexOfFirst(default: Int, predicate: (T)->Boolean): Int {
    val idx = indexOfFirst(predicate)
    return if(idx == -1) default else idx
}
fun DoubleArray.indexOfFirst(default: Int, predicate: (Double)->Boolean): Int {
    val idx = indexOfFirst(predicate)
    return if(idx == -1) default else idx
}