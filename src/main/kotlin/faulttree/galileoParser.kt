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

package faulttree

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import parser.GalileoLexer
import parser.GalileoListenerImpl
import parser.GalileoParser
import java.io.InputStream
import java.util.*

object galileoParser {
    fun parse(stream: InputStream): FaultTree {
        val lexer = GalileoLexer(CharStreams.fromStream(stream))
        val parser = GalileoParser(CommonTokenStream(lexer))
        val parseTree = parser.faulttree()
        val extractor = GalileoListenerImpl()
        ParseTreeWalker.DEFAULT.walk(extractor, parseTree)
        return extractor.faultTree
    }
}