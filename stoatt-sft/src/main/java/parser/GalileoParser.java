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

// Generated from E:/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.9.1
package parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GalileoParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, INT=5, EQ=6, OR=7, EXP=8, AND=9, OF=10, 
		WSP=11, TOPLEVEL=12, LAMBDA=13, PH=14, PROBABILITY=15, DORMANCY=16, REPAIR=17, 
		FAILURE_STATES=18, DOUBLE=19, NAME=20, IDENTIFIER=21, COMMENT=22, WS=23;
	public static final int
		RULE_faulttree = 0, RULE_top = 1, RULE_gate = 2, RULE_basicevent = 3, 
		RULE_property = 4, RULE_lambda = 5, RULE_phase = 6, RULE_rateMatrix = 7, 
		RULE_matrixRow = 8, RULE_numFailureStates = 9, RULE_probability = 10, 
		RULE_dormancy = 11, RULE_repair = 12, RULE_operation = 13, RULE_or = 14, 
		RULE_and = 15, RULE_of = 16, RULE_wsp = 17;
	private static String[] makeRuleNames() {
		return new String[] {
			"faulttree", "top", "gate", "basicevent", "property", "lambda", "phase", 
			"rateMatrix", "matrixRow", "numFailureStates", "probability", "dormancy", 
			"repair", "operation", "or", "and", "of", "wsp"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'['", "']'", "','", null, "'='", "'or'", null, "'and'", 
			"'of'", "'wsp'", "'toplevel'", "'lambda'", "'ph'", "'prob'", "'dorm'", 
			"'repair'", "'failurestates'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "INT", "EQ", "OR", "EXP", "AND", "OF", 
			"WSP", "TOPLEVEL", "LAMBDA", "PH", "PROBABILITY", "DORMANCY", "REPAIR", 
			"FAILURE_STATES", "DOUBLE", "NAME", "IDENTIFIER", "COMMENT", "WS"
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
			setState(36);
			top();
			setState(37);
			match(T__0);
			setState(46);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(40);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(38);
					gate();
					}
					break;
				case 2:
					{
					setState(39);
					basicevent();
					}
					break;
				}
				setState(42);
				match(T__0);
				}
				}
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(49);
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
			setState(51);
			match(TOPLEVEL);
			setState(52);
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
			setState(54);
			((GateContext)_localctx).name = match(NAME);
			setState(55);
			operation();
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(56);
				((GateContext)_localctx).NAME = match(NAME);
				((GateContext)_localctx).inputs.add(((GateContext)_localctx).NAME);
				}
				}
				setState(61);
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
			setState(62);
			((BasiceventContext)_localctx).name = match(NAME);
			setState(66);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LAMBDA) | (1L << PH) | (1L << PROBABILITY) | (1L << DORMANCY) | (1L << REPAIR) | (1L << FAILURE_STATES))) != 0)) {
				{
				{
				setState(63);
				property();
				}
				}
				setState(68);
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
			setState(75);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LAMBDA:
				{
				setState(69);
				lambda();
				}
				break;
			case PH:
				{
				setState(70);
				phase();
				}
				break;
			case PROBABILITY:
				{
				setState(71);
				probability();
				}
				break;
			case DORMANCY:
				{
				setState(72);
				dormancy();
				}
				break;
			case REPAIR:
				{
				setState(73);
				repair();
				}
				break;
			case FAILURE_STATES:
				{
				setState(74);
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
			setState(77);
			match(LAMBDA);
			setState(78);
			match(EQ);
			setState(79);
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
			setState(81);
			match(PH);
			setState(82);
			match(EQ);
			setState(83);
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
			setState(85);
			match(T__1);
			setState(91);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(86);
					matrixRow();
					setState(87);
					match(T__0);
					}
					} 
				}
				setState(93);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(94);
			matrixRow();
			setState(95);
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
			setState(101);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					{
					setState(97);
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
					setState(98);
					match(T__3);
					}
					} 
				}
				setState(103);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			setState(104);
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
			setState(106);
			match(FAILURE_STATES);
			setState(107);
			match(EQ);
			setState(108);
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
			setState(110);
			match(PROBABILITY);
			setState(111);
			match(EQ);
			setState(112);
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
			setState(114);
			match(DORMANCY);
			setState(115);
			match(EQ);
			setState(116);
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
			setState(118);
			match(REPAIR);
			setState(119);
			match(EQ);
			setState(120);
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
		public WspContext wsp() {
			return getRuleContext(WspContext.class,0);
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
			setState(126);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OR:
				{
				setState(122);
				or();
				}
				break;
			case AND:
				{
				setState(123);
				and();
				}
				break;
			case INT:
				{
				setState(124);
				of();
				}
				break;
			case WSP:
				{
				setState(125);
				wsp();
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
			setState(128);
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
			setState(130);
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
			setState(132);
			((OfContext)_localctx).k = match(INT);
			}
			{
			setState(133);
			match(OF);
			}
			{
			setState(134);
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

	public static class WspContext extends ParserRuleContext {
		public TerminalNode WSP() { return getToken(GalileoParser.WSP, 0); }
		public WspContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wsp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).enterWsp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GalileoListener ) ((GalileoListener)listener).exitWsp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof GalileoVisitor ) return ((GalileoVisitor<? extends T>)visitor).visitWsp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WspContext wsp() throws RecognitionException {
		WspContext _localctx = new WspContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_wsp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			match(WSP);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\31\u008d\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\2\3\2\5\2+\n\2\3\2\3\2\7\2/\n\2\f\2\16\2\62\13\2"+
		"\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\7\4<\n\4\f\4\16\4?\13\4\3\5\3\5\7\5C"+
		"\n\5\f\5\16\5F\13\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6N\n\6\3\7\3\7\3\7\3\7\3"+
		"\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\7\t\\\n\t\f\t\16\t_\13\t\3\t\3\t\3\t\3"+
		"\n\3\n\7\nf\n\n\f\n\16\ni\13\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f"+
		"\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\5\17\u0081"+
		"\n\17\3\20\3\20\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23\2\2\24\2"+
		"\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$\2\3\4\2\7\7\25\25\2\u0088\2"+
		"&\3\2\2\2\4\65\3\2\2\2\68\3\2\2\2\b@\3\2\2\2\nM\3\2\2\2\fO\3\2\2\2\16"+
		"S\3\2\2\2\20W\3\2\2\2\22g\3\2\2\2\24l\3\2\2\2\26p\3\2\2\2\30t\3\2\2\2"+
		"\32x\3\2\2\2\34\u0080\3\2\2\2\36\u0082\3\2\2\2 \u0084\3\2\2\2\"\u0086"+
		"\3\2\2\2$\u008a\3\2\2\2&\'\5\4\3\2\'\60\7\3\2\2(+\5\6\4\2)+\5\b\5\2*("+
		"\3\2\2\2*)\3\2\2\2+,\3\2\2\2,-\7\3\2\2-/\3\2\2\2.*\3\2\2\2/\62\3\2\2\2"+
		"\60.\3\2\2\2\60\61\3\2\2\2\61\63\3\2\2\2\62\60\3\2\2\2\63\64\7\2\2\3\64"+
		"\3\3\2\2\2\65\66\7\16\2\2\66\67\7\26\2\2\67\5\3\2\2\289\7\26\2\29=\5\34"+
		"\17\2:<\7\26\2\2;:\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>\7\3\2\2\2?="+
		"\3\2\2\2@D\7\26\2\2AC\5\n\6\2BA\3\2\2\2CF\3\2\2\2DB\3\2\2\2DE\3\2\2\2"+
		"E\t\3\2\2\2FD\3\2\2\2GN\5\f\7\2HN\5\16\b\2IN\5\26\f\2JN\5\30\r\2KN\5\32"+
		"\16\2LN\5\24\13\2MG\3\2\2\2MH\3\2\2\2MI\3\2\2\2MJ\3\2\2\2MK\3\2\2\2ML"+
		"\3\2\2\2N\13\3\2\2\2OP\7\17\2\2PQ\7\b\2\2QR\t\2\2\2R\r\3\2\2\2ST\7\20"+
		"\2\2TU\7\b\2\2UV\5\20\t\2V\17\3\2\2\2W]\7\4\2\2XY\5\22\n\2YZ\7\3\2\2Z"+
		"\\\3\2\2\2[X\3\2\2\2\\_\3\2\2\2][\3\2\2\2]^\3\2\2\2^`\3\2\2\2_]\3\2\2"+
		"\2`a\5\22\n\2ab\7\5\2\2b\21\3\2\2\2cd\t\2\2\2df\7\6\2\2ec\3\2\2\2fi\3"+
		"\2\2\2ge\3\2\2\2gh\3\2\2\2hj\3\2\2\2ig\3\2\2\2jk\t\2\2\2k\23\3\2\2\2l"+
		"m\7\24\2\2mn\7\b\2\2no\7\7\2\2o\25\3\2\2\2pq\7\21\2\2qr\7\b\2\2rs\t\2"+
		"\2\2s\27\3\2\2\2tu\7\22\2\2uv\7\b\2\2vw\t\2\2\2w\31\3\2\2\2xy\7\23\2\2"+
		"yz\7\b\2\2z{\t\2\2\2{\33\3\2\2\2|\u0081\5\36\20\2}\u0081\5 \21\2~\u0081"+
		"\5\"\22\2\177\u0081\5$\23\2\u0080|\3\2\2\2\u0080}\3\2\2\2\u0080~\3\2\2"+
		"\2\u0080\177\3\2\2\2\u0081\35\3\2\2\2\u0082\u0083\7\t\2\2\u0083\37\3\2"+
		"\2\2\u0084\u0085\7\13\2\2\u0085!\3\2\2\2\u0086\u0087\7\7\2\2\u0087\u0088"+
		"\7\f\2\2\u0088\u0089\7\7\2\2\u0089#\3\2\2\2\u008a\u008b\7\r\2\2\u008b"+
		"%\3\2\2\2\n*\60=DM]g\u0080";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}