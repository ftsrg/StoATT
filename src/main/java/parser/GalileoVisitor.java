// Generated from E:/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.9.1
package parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link GalileoParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface GalileoVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link GalileoParser#faulttree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFaulttree(GalileoParser.FaulttreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#top}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTop(GalileoParser.TopContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#gate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGate(GalileoParser.GateContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#basicevent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicevent(GalileoParser.BasiceventContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(GalileoParser.PropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#lambda}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(GalileoParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#phase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPhase(GalileoParser.PhaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#rateMatrix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRateMatrix(GalileoParser.RateMatrixContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#matrixRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatrixRow(GalileoParser.MatrixRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#numFailureStates}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumFailureStates(GalileoParser.NumFailureStatesContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#probability}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProbability(GalileoParser.ProbabilityContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#dormancy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDormancy(GalileoParser.DormancyContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#repair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepair(GalileoParser.RepairContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperation(GalileoParser.OperationContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(GalileoParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(GalileoParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#of}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOf(GalileoParser.OfContext ctx);
	/**
	 * Visit a parse tree produced by {@link GalileoParser#wsp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWsp(GalileoParser.WspContext ctx);
}