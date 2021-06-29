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

object BCompaction {
    operator fun invoke(f: MddHandle, c: MddHandle) = apply(f, c)
    fun apply(f: MddHandle, c: MddHandle): MddHandle {
        if(c.isTerminalZero) return c
        val thenMarkings = hashSetOf<MddHandle>()
        val elseMarkings = hashSetOf<MddHandle>()
        markEdges(f, c, thenMarkings, elseMarkings)
        return buildResult(f, thenMarkings, elseMarkings)
    }

    private fun buildResult(f: MddHandle, thenMarkings: Set<MddHandle>, elseMarkings: Set<MddHandle>): MddHandle {
        val builder = JavaMddFactory.getDefault().createTemplateBuilder()
        if(f.isTerminal) return f
        val x = f.variableHandle
        if(thenMarkings.contains(f) && !elseMarkings.contains(f))
            return x.checkIn(builder.setDefault(buildResult(f[1], thenMarkings, elseMarkings)).buildAndReset())
        if(!thenMarkings.contains(f) && elseMarkings.contains(f))
            return x.checkIn(builder.setDefault(buildResult(f[0], thenMarkings, elseMarkings)).buildAndReset())
        return x.checkIn(builder.setDefault(buildResult(f[0], thenMarkings, elseMarkings)).set(1, buildResult(f[1], thenMarkings, elseMarkings)).buildAndReset())
    }

    private fun markEdges(f: MddHandle, c: MddHandle, thenMarkings: HashSet<MddHandle>, elseMarkings: HashSet<MddHandle>) {
//        if (c.isTerminalZero) return
        if (c.toLowestSignificantVariable().isTerminalZero) return
        if (f.isTerminal) return
        val x = f.variableHandle
        if(!c[1].toLowestSignificantVariable().isTerminalZero) {
            if(!f.semanticEquals(f[1])) thenMarkings.add(f)
            markEdges(f[1], c[1], thenMarkings, elseMarkings)
        }
        if (!c[0].toLowestSignificantVariable().isTerminalZero) {
            if(!f.semanticEquals(f[0])) elseMarkings.add(f)
            markEdges(f[0], c[0], thenMarkings, elseMarkings)
        }
    }
}