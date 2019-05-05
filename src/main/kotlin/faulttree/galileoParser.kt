package faulttree

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import parser.GalileoLexer
import parser.GalileoListenerImpl
import parser.GalileoParser
import java.io.InputStream

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