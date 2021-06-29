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

package MDDExtensions

import hu.bme.mit.delta.java.mdd.JavaMddFactory
import hu.bme.mit.delta.mdd.MddHandle

object GSCompaction {
    private enum class Substitutability { NEG_TO_PON, PON_TO_NEG, NONE }
    private data class FCS(val f: MddHandle, val c: MddHandle, val s: Substitutability)

    fun apply(f: MddHandle, c: MddHandle): MddHandle {
        if(c.isTerminalZero) return c
        val fcsList = arrayListOf<FCS>()
        val thenMarkings = hashSetOf<MddHandle>()
        val elseMarkings = hashSetOf<MddHandle>()
        markEssentialEdges(f, c, fcsList, thenMarkings, elseMarkings)
        markSupplementalEdges(fcsList, thenMarkings, elseMarkings)
        return buildResult(f, thenMarkings, elseMarkings)
    }

    private fun buildResult(f: MddHandle, thenMarkings: Set<MddHandle>, elseMarkings: Set<MddHandle>): MddHandle {
        val builder = JavaMddFactory.getDefault().createTemplateBuilder()
        if(f.isTerminal) return f
        val x = f.variableHandle
        if(thenMarkings.contains(f) && !elseMarkings.contains(f)) {
            val newF1 = buildResult(f[1], thenMarkings, elseMarkings)
            val newF = x.checkIn(builder.set(0,newF1).set(1, newF1).buildAndReset())
            return newF
        }
        if(!thenMarkings.contains(f) && elseMarkings.contains(f)) {
            val newF0 = buildResult(f[0], thenMarkings, elseMarkings)
            val newF = x.checkIn(builder.set(0,newF0).set(1, newF0).buildAndReset())
            return newF
        }
        val newF0 = buildResult(f[0], thenMarkings, elseMarkings)
        val newF1 = buildResult(f[1], thenMarkings, elseMarkings)
        val newF = x.checkIn(builder.set(0, newF0).set(1, newF1).buildAndReset())
        return newF
    }

    private tailrec fun markEssentialEdges(f: MddHandle, c: MddHandle, fcsList: ArrayList<FCS>, thenMarkings: HashSet<MddHandle>, elseMarkings: HashSet<MddHandle>) {
        if(c.isTerminalZero || f.isTerminal) return
        val x = f.variableHandle
//        if(!f.semanticEquals(f[1])) { // TODO: two different level handles are never semanticEqual
        if(f[1] != f[0]) {
            val s = checkSubstitutability(f, c)
            if(!thenMarkings.contains(f)) {
                if(s == Substitutability.PON_TO_NEG) fcsList.add(FCS(f, c, s))
                else thenMarkings.add(f)
            }
            if(!elseMarkings.contains(f)) {
                if(s == Substitutability.NEG_TO_PON) fcsList.add(FCS(f, c, s))
                else elseMarkings.add(f)
            }
        } else {
            thenMarkings.add(f)
        }

        if(thenMarkings.contains(f) || f.semanticEquals(f[1])) markEssentialEdges(f[1], c[1], fcsList, thenMarkings, elseMarkings)
        if(elseMarkings.contains(f) || f.semanticEquals(f[0])) markEssentialEdges(f[0], c[0], fcsList, thenMarkings, elseMarkings)
    }

    private fun markSupplementalEdges(fcsList: ArrayList<FCS>, thenMarkings: HashSet<MddHandle>, elseMarkings: HashSet<MddHandle>) {
        while (!fcsList.isEmpty()) {
            fcsList.sortBy { -it.f.variableHandle.height }
            val fcs = fcsList.first()
            fcsList.removeAt(0)
            val f = fcs.f
            val c = fcs.c
            val s = fcs.s

            if(s == Substitutability.PON_TO_NEG) {
                if(!thenMarkings.contains(f)) markEssentialEdges(f[0], c[1], fcsList, thenMarkings, elseMarkings)
                else markEssentialEdges(f[1], c[1], fcsList, thenMarkings, elseMarkings)
            }
            else if(s== Substitutability.NEG_TO_PON) {
                if(!elseMarkings.contains(f)) markEssentialEdges(f[1], c[0], fcsList, thenMarkings, elseMarkings)
                else markEssentialEdges(f[0], c[0], fcsList, thenMarkings, elseMarkings)
            }
        }
    }

    private fun checkSubstitutability(f: MddHandle, c: MddHandle): Substitutability {
        if(c[1].isTerminalZero) return Substitutability.PON_TO_NEG
        if(c[0].isTerminalZero) return Substitutability.NEG_TO_PON
        val fdiff = f[0].minus(f[1]).union(f[1].minus(f[0]))
        val temp1 = fdiff.intersection(c[1])
        val temp2 = fdiff.intersection(c[0])
        if(temp1.isTerminalZero && temp2.isTerminalZero)
            if(f[1].toLowestSignificantVariable().variableHandle.height < f[0].toLowestSignificantVariable().variableHandle.height) return Substitutability.PON_TO_NEG
            else return Substitutability.NEG_TO_PON
        if (temp1.isTerminalZero) return Substitutability.PON_TO_NEG
        if (temp2.isTerminalZero) return Substitutability.NEG_TO_PON
        return Substitutability.NONE
    }
}