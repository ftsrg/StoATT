// Generated from E:/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.9.1
package parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GalileoParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, INT=5, EQ=6, OR=7, EXP=8, AND=9, OF=10, 
		TOPLEVEL=11, LAMBDA=12, PH=13, PROBABILITY=14, DORMANCY=15, REPAIR=16, 
		FAILURE_STATES=17, DOUBLE=18, NAME=19, IDENTIFIER=20, COMMENT=21, WS=22;
	public static final int
		RULE_faulttree = 0, RULE_top = 1, RULE_gate = 2, RULE_basicevent = 3, 
		RULE_property = 4, RULE_lambda = 5, RULE_phase = 6, RULE_rateMatrix = 7, 
		RULE_matrixRow = 8, RULE_numFailureStates = 9, RULE_probability = 10, 
		RULE_dormancy = 11, RULE_repair = 12, RULE_operation = 13, RULE_or = 14, 
		RULE_and = 15, RULE_of = 16;
	private static String[] makeRuleNames() {
		return new String[] {
			"faulttree", "top", "gate", "basicevent", "property", "lambda", "phase", 
			"rateMatrix", "matrixRow", "numFailureStates", "probability", "dormancy", 
			"repair", "operation", "or", "and", "of"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'['", "']'", "','", null, "'='", "'or'", null, "'and'", 
			"'of'", "'toplevel'", "'lambda'", "'ph'", "'prob'", "'dorm'", "'repair'", 
			"'failurestates'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "INT", "EQ", "OR", "EXP", "AND", "OF", 
			"TOPLEVEL", "LAMBDA", "PH", "PROBABILITY", "DORMANCY", "REPAIR", "FAILURE_STATES", 
			"DOUBLE", "NAME", "IDENTIFIER", "COMMENT", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Galileo.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public GalileoParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class FaulttreeContext extends ParserRuleContext {
		public TopContext top() {
			return getRuleContext(TopContext.class,0);
		}
		public TerminalNode EOF() { return getToken(GalileoParser.EOF, 0); }
		public List<GateContext> gate() {
			return getRuleContexts(GateContext.class);
		}
		public GateContext gate(int i) {
			return getRuleContext(GateContext.class,i);
		}
		public List<BasiceventContext> basicevent() {
			return getRuleContexts(BasiceventContext.class);
		}
		public BasiceventContext basicevent(int i) {
			return getRuleContext(BasiceventContext.class,i);
		}
		public FaulttreeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_faulttree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterFaulttree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitFaulttree(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitFaulttree(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FaulttreeContext faulttree() throws RecognitionException {
		FaulttreeContext _localctx = new FaulttreeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_faulttree);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			top();
			setState(35);
			match(T__0);
			setState(44);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(38);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(36);
					gate();
					}
					break;
				case 2:
					{
					setState(37);
					basicevent();
					}
					break;
				}
				setState(40);
				match(T__0);
				}
				}
				setState(46);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(47);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TopContext extends ParserRuleContext {
		public Token name;
		public TerminalNode TOPLEVEL() { return getToken(GalileoParser.TOPLEVEL, 0); }
		public TerminalNode NAME() { return getToken(GalileoParser.NAME, 0); }
		public TopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_top; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterTop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitTop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitTop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopContext top() throws RecognitionException {
		TopContext _localctx = new TopContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_top);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			match(TOPLEVEL);
			setState(50);
			((TopContext)_localctx).name = match(NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GateContext extends ParserRuleContext {
		public Token name;
		public Token NAME;
		public List<Token> inputs = new ArrayList<Token>();
		public OperationContext operation() {
			return getRuleContext(OperationContext.class,0);
		}
		public List<TerminalNode> NAME() { return getTokens(GalileoParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(GalileoParser.NAME, i);
		}
		public GateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterGate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitGate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitGate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GateContext gate() throws RecognitionException {
		GateContext _localctx = new GateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_gate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			((GateContext)_localctx).name = match(NAME);
			setState(53);
			operation();
			setState(57);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(54);
				((GateContext)_localctx).NAME = match(NAME);
				((GateContext)_localctx).inputs.add(((GateContext)_localctx).NAME);
				}
				}
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BasiceventContext extends ParserRuleContext {
		public Token name;
		public TerminalNode NAME() { return getToken(GalileoParser.NAME, 0); }
		public List<PropertyContext> property() {
			return getRuleContexts(PropertyContext.class);
		}
		public PropertyContext property(int i) {
			return getRuleContext(PropertyContext.class,i);
		}
		public BasiceventContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basicevent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterBasicevent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitBasicevent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitBasicevent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BasiceventContext basicevent() throws RecognitionException {
		BasiceventContext _localctx = new BasiceventContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_basicevent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			((BasiceventContext)_localctx).name = match(NAME);
			setState(64);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LAMBDA) | (1L << PH) | (1L << PROBABILITY) | (1L << DORMANCY) | (1L << REPAIR) | (1L << FAILURE_STATES))) != 0)) {
				{
				{
				setState(61);
				property();
				}
				}
				setState(66);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyContext extends ParserRuleContext {
		public LambdaContext lambda() {
			return getRuleContext(LambdaContext.class,0);
		}
		public PhaseContext phase() {
			return getRuleContext(PhaseContext.class,0);
		}
		public ProbabilityContext probability() {
			return getRuleContext(ProbabilityContext.class,0);
		}
		public DormancyContext dormancy() {
			return getRuleContext(DormancyContext.class,0);
		}
		public RepairContext repair() {
			return getRuleContext(RepairContext.class,0);
		}
		public NumFailureStatesContext numFailureStates() {
			return getRuleContext(NumFailureStatesContext.class,0);
		}
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LAMBDA:
				{
				setState(67);
				lambda();
				}
				break;
			case PH:
				{
				setState(68);
				phase();
				}
				break;
			case PROBABILITY:
				{
				setState(69);
				probability();
				}
				break;
			case DORMANCY:
				{
				setState(70);
				dormancy();
				}
				break;
			case REPAIR:
				{
				setState(71);
				repair();
				}
				break;
			case FAILURE_STATES:
				{
				setState(72);
				numFailureStates();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LambdaContext extends ParserRuleContext {
		public Token val;
		public TerminalNode LAMBDA() { return getToken(GalileoParser.LAMBDA, 0); }
		public TerminalNode EQ() { return getToken(GalileoParser.EQ, 0); }
		public TerminalNode DOUBLE() { return getToken(GalileoParser.DOUBLE, 0); }
		public TerminalNode INT() { return getToken(GalileoParser.INT, 0); }
		public LambdaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambda; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaContext lambda() throws RecognitionException {
		LambdaContext _localctx = new LambdaContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_lambda);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(LAMBDA);
			setState(76);
			match(EQ);
			setState(77);
			((LambdaContext)_localctx).val = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==DOUBLE) ) {
				((LambdaContext)_localctx).val = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PhaseContext extends ParserRuleContext {
		public RateMatrixContext val;
		public TerminalNode PH() { return getToken(GalileoParser.PH, 0); }
		public TerminalNode EQ() { return getToken(GalileoParser.EQ, 0); }
		public RateMatrixContext rateMatrix() {
			return getRuleContext(RateMatrixContext.class,0);
		}
		public PhaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_phase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterPhase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitPhase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitPhase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PhaseContext phase() throws RecognitionException {
		PhaseContext _localctx = new PhaseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_phase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(PH);
			setState(80);
			match(EQ);
			setState(81);
			((PhaseContext)_localctx).val = rateMatrix();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RateMatrixContext extends ParserRuleContext {
		public List<MatrixRowContext> matrixRow() {
			return getRuleContexts(MatrixRowContext.class);
		}
		public MatrixRowContext matrixRow(int i) {
			return getRuleContext(MatrixRowContext.class,i);
		}
		public RateMatrixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rateMatrix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterRateMatrix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitRateMatrix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitRateMatrix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RateMatrixContext rateMatrix() throws RecognitionException {
		RateMatrixContext _localctx = new RateMatrixContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_rateMatrix);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(T__1);
			setState(89);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(84);
					matrixRow();
					setState(85);
					match(T__0);
					}
					} 
				}
				setState(91);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(92);
			matrixRow();
			setState(93);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatrixRowContext extends ParserRuleContext {
		public Token DOUBLE;
		public List<Token> vals = new ArrayList<Token>();
		public Token INT;
		public Token _tset137;
		public Token _tset149;
		public List<TerminalNode> DOUBLE() { return getTokens(GalileoParser.DOUBLE); }
		public TerminalNode DOUBLE(int i) {
			return getToken(GalileoParser.DOUBLE, i);
		}
		public List<TerminalNode> INT() { return getTokens(GalileoParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(GalileoParser.INT, i);
		}
		public MatrixRowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matrixRow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterMatrixRow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitMatrixRow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitMatrixRow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatrixRowContext matrixRow() throws RecognitionException {
		MatrixRowContext _localctx = new MatrixRowContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_matrixRow);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					{
					setState(95);
					((MatrixRowContext)_localctx)._tset137 = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==INT || _la==DOUBLE) ) {
						((MatrixRowContext)_localctx)._tset137 = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					((MatrixRowContext)_localctx).vals.add(((MatrixRowContext)_localctx)._tset137);
					}
					setState(96);
					match(T__3);
					}
					} 
				}
				setState(101);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			setState(102);
			((MatrixRowContext)_localctx)._tset149 = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==DOUBLE) ) {
				((MatrixRowContext)_localctx)._tset149 = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			((MatrixRowContext)_localctx).vals.add(((MatrixRowContext)_localctx)._tset149);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumFailureStatesContext extends ParserRuleContext {
		public Token val;
		public TerminalNode FAILURE_STATES() { return getToken(GalileoParser.FAILURE_STATES, 0); }
		public TerminalNode EQ() { return getToken(GalileoParser.EQ, 0); }
		public TerminalNode INT() { return getToken(GalileoParser.INT, 0); }
		public NumFailureStatesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numFailureStates; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterNumFailureStates(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitNumFailureStates(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitNumFailureStates(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumFailureStatesContext numFailureStates() throws RecognitionException {
		NumFailureStatesContext _localctx = new NumFailureStatesContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_numFailureStates);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(FAILURE_STATES);
			setState(105);
			match(EQ);
			setState(106);
			((NumFailureStatesContext)_localctx).val = match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ProbabilityContext extends ParserRuleContext {
		public Token val;
		public TerminalNode PROBABILITY() { return getToken(GalileoParser.PROBABILITY, 0); }
		public TerminalNode EQ() { return getToken(GalileoParser.EQ, 0); }
		public TerminalNode DOUBLE() { return getToken(GalileoParser.DOUBLE, 0); }
		public TerminalNode INT() { return getToken(GalileoParser.INT, 0); }
		public ProbabilityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_probability; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterProbability(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitProbability(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitProbability(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProbabilityContext probability() throws RecognitionException {
		ProbabilityContext _localctx = new ProbabilityContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_probability);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			match(PROBABILITY);
			setState(109);
			match(EQ);
			setState(110);
			((ProbabilityContext)_localctx).val = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==DOUBLE) ) {
				((ProbabilityContext)_localctx).val = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DormancyContext extends ParserRuleContext {
		public Token val;
		public TerminalNode DORMANCY() { return getToken(GalileoParser.DORMANCY, 0); }
		public TerminalNode EQ() { return getToken(GalileoParser.EQ, 0); }
		public TerminalNode DOUBLE() { return getToken(GalileoParser.DOUBLE, 0); }
		public TerminalNode INT() { return getToken(GalileoParser.INT, 0); }
		public DormancyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dormancy; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterDormancy(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitDormancy(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitDormancy(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DormancyContext dormancy() throws RecognitionException {
		DormancyContext _localctx = new DormancyContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_dormancy);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(DORMANCY);
			setState(113);
			match(EQ);
			setState(114);
			((DormancyContext)_localctx).val = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==DOUBLE) ) {
				((DormancyContext)_localctx).val = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RepairContext extends ParserRuleContext {
		public Token val;
		public TerminalNode REPAIR() { return getToken(GalileoParser.REPAIR, 0); }
		public TerminalNode EQ() { return getToken(GalileoParser.EQ, 0); }
		public TerminalNode DOUBLE() { return getToken(GalileoParser.DOUBLE, 0); }
		public TerminalNode INT() { return getToken(GalileoParser.INT, 0); }
		public RepairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterRepair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitRepair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitRepair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RepairContext repair() throws RecognitionException {
		RepairContext _localctx = new RepairContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_repair);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			match(REPAIR);
			setState(117);
			match(EQ);
			setState(118);
			((RepairContext)_localctx).val = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==DOUBLE) ) {
				((RepairContext)_localctx).val = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperationContext extends ParserRuleContext {
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public OfContext of() {
			return getRuleContext(OfContext.class,0);
		}
		public OperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperationContext operation() throws RecognitionException {
		OperationContext _localctx = new OperationContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_operation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OR:
				{
				setState(120);
				or();
				}
				break;
			case AND:
				{
				setState(121);
				and();
				}
				break;
			case INT:
				{
				setState(122);
				of();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(GalileoParser.OR, 0); }
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			match(OR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AndContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(GalileoParser.AND, 0); }
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(AND);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OfContext extends ParserRuleContext {
		public Token k;
		public Token n;
		public TerminalNode OF() { return getToken(GalileoParser.OF, 0); }
		public List<TerminalNode> INT() { return getTokens(GalileoParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(GalileoParser.INT, i);
		}
		public OfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_of; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterOf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitOf(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitOf(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OfContext of() throws RecognitionException {
		OfContext _localctx = new OfContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_of);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(129);
			((OfContext)_localctx).k = match(INT);
			}
			{
			setState(130);
			match(OF);
			}
			{
			setState(131);
			((OfContext)_localctx).n = match(INT);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\30\u0088\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\2\3\2\5\2)\n\2\3\2\3\2\7\2-\n\2\f\2\16\2\60\13\2\3\2\3\2\3"+
		"\3\3\3\3\3\3\4\3\4\3\4\7\4:\n\4\f\4\16\4=\13\4\3\5\3\5\7\5A\n\5\f\5\16"+
		"\5D\13\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6L\n\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b"+
		"\3\b\3\t\3\t\3\t\3\t\7\tZ\n\t\f\t\16\t]\13\t\3\t\3\t\3\t\3\n\3\n\7\nd"+
		"\n\n\f\n\16\ng\13\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3"+
		"\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\5\17~\n\17\3\20\3\20\3\21"+
		"\3\21\3\22\3\22\3\22\3\22\3\22\2\2\23\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \"\2\3\4\2\7\7\24\24\2\u0083\2$\3\2\2\2\4\63\3\2\2\2\6\66\3\2\2"+
		"\2\b>\3\2\2\2\nK\3\2\2\2\fM\3\2\2\2\16Q\3\2\2\2\20U\3\2\2\2\22e\3\2\2"+
		"\2\24j\3\2\2\2\26n\3\2\2\2\30r\3\2\2\2\32v\3\2\2\2\34}\3\2\2\2\36\177"+
		"\3\2\2\2 \u0081\3\2\2\2\"\u0083\3\2\2\2$%\5\4\3\2%.\7\3\2\2&)\5\6\4\2"+
		"\')\5\b\5\2(&\3\2\2\2(\'\3\2\2\2)*\3\2\2\2*+\7\3\2\2+-\3\2\2\2,(\3\2\2"+
		"\2-\60\3\2\2\2.,\3\2\2\2./\3\2\2\2/\61\3\2\2\2\60.\3\2\2\2\61\62\7\2\2"+
		"\3\62\3\3\2\2\2\63\64\7\r\2\2\64\65\7\25\2\2\65\5\3\2\2\2\66\67\7\25\2"+
		"\2\67;\5\34\17\28:\7\25\2\298\3\2\2\2:=\3\2\2\2;9\3\2\2\2;<\3\2\2\2<\7"+
		"\3\2\2\2=;\3\2\2\2>B\7\25\2\2?A\5\n\6\2@?\3\2\2\2AD\3\2\2\2B@\3\2\2\2"+
		"BC\3\2\2\2C\t\3\2\2\2DB\3\2\2\2EL\5\f\7\2FL\5\16\b\2GL\5\26\f\2HL\5\30"+
		"\r\2IL\5\32\16\2JL\5\24\13\2KE\3\2\2\2KF\3\2\2\2KG\3\2\2\2KH\3\2\2\2K"+
		"I\3\2\2\2KJ\3\2\2\2L\13\3\2\2\2MN\7\16\2\2NO\7\b\2\2OP\t\2\2\2P\r\3\2"+
		"\2\2QR\7\17\2\2RS\7\b\2\2ST\5\20\t\2T\17\3\2\2\2U[\7\4\2\2VW\5\22\n\2"+
		"WX\7\3\2\2XZ\3\2\2\2YV\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\^\3\2\2"+
		"\2][\3\2\2\2^_\5\22\n\2_`\7\5\2\2`\21\3\2\2\2ab\t\2\2\2bd\7\6\2\2ca\3"+
		"\2\2\2dg\3\2\2\2ec\3\2\2\2ef\3\2\2\2fh\3\2\2\2ge\3\2\2\2hi\t\2\2\2i\23"+
		"\3\2\2\2jk\7\23\2\2kl\7\b\2\2lm\7\7\2\2m\25\3\2\2\2no\7\20\2\2op\7\b\2"+
		"\2pq\t\2\2\2q\27\3\2\2\2rs\7\21\2\2st\7\b\2\2tu\t\2\2\2u\31\3\2\2\2vw"+
		"\7\22\2\2wx\7\b\2\2xy\t\2\2\2y\33\3\2\2\2z~\5\36\20\2{~\5 \21\2|~\5\""+
		"\22\2}z\3\2\2\2}{\3\2\2\2}|\3\2\2\2~\35\3\2\2\2\177\u0080\7\t\2\2\u0080"+
		"\37\3\2\2\2\u0081\u0082\7\13\2\2\u0082!\3\2\2\2\u0083\u0084\7\7\2\2\u0084"+
		"\u0085\7\f\2\2\u0085\u0086\7\7\2\2\u0086#\3\2\2\2\n(.;BK[e}";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}