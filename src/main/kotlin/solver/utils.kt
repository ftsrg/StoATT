/*
 *
 *   Copyright 2021 Budapest University of Technology and Economics
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package solver

import kotlin.math.abs

fun Double.nearZero(thresh: Double = 10E-14) = abs(this) < thresh

fun Array<Int>.product() = fold(1) {prod, curr -> prod*curr}
fun Iterable<Int>.product() = fold(1L) {prod, curr -> prod*curr}

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