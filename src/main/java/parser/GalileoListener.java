// Generated from C:/Users/Daniel/Documents/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.7.2
package parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link GalileoParser}.
 */
public interface GalileoListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link GalileoParser#faulttree}.
	 * @param ctx the parse tree
	 */
	void enterFaulttree(GalileoParser.FaulttreeContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#faulttree}.
	 * @param ctx the parse tree
	 */
	void exitFaulttree(GalileoParser.FaulttreeContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#top}.
	 * @param ctx the parse tree
	 */
	void enterTop(GalileoParser.TopContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#top}.
	 * @param ctx the parse tree
	 */
	void exitTop(GalileoParser.TopContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#gate}.
	 * @param ctx the parse tree
	 */
	void enterGate(GalileoParser.GateContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#gate}.
	 * @param ctx the parse tree
	 */
	void exitGate(GalileoParser.GateContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#basicevent}.
	 * @param ctx the parse tree
	 */
	void enterBasicevent(GalileoParser.BasiceventContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#basicevent}.
	 * @param ctx the parse tree
	 */
	void exitBasicevent(GalileoParser.BasiceventContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#property}.
	 * @param ctx the parse tree
	 */
	void enterProperty(GalileoParser.PropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#property}.
	 * @param ctx the parse tree
	 */
	void exitProperty(GalileoParser.PropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#lambda}.
	 * @param ctx the parse tree
	 */
	void enterLambda(GalileoParser.LambdaContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#lambda}.
	 * @param ctx the parse tree
	 */
	void exitLambda(GalileoParser.LambdaContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#probability}.
	 * @param ctx the parse tree
	 */
	void enterProbability(GalileoParser.ProbabilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#probability}.
	 * @param ctx the parse tree
	 */
	void exitProbability(GalileoParser.ProbabilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#dormancy}.
	 * @param ctx the parse tree
	 */
	void enterDormancy(GalileoParser.DormancyContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#dormancy}.
	 * @param ctx the parse tree
	 */
	void exitDormancy(GalileoParser.DormancyContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#operation}.
	 * @param ctx the parse tree
	 */
	void enterOperation(GalileoParser.OperationContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#operation}.
	 * @param ctx the parse tree
	 */
	void exitOperation(GalileoParser.OperationContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(GalileoParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(GalileoParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(GalileoParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(GalileoParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link GalileoParser#of}.
	 * @param ctx the parse tree
	 */
	void enterOf(GalileoParser.OfContext ctx);
	/**
	 * Exit a parse tree produced by {@link GalileoParser#of}.
	 * @param ctx the parse tree
	 */
	void exitOf(GalileoParser.OfContext ctx);
}