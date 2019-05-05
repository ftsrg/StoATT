// Generated from C:/Users/Daniel/Documents/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.7.2
package parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GalileoParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EQ=1, OR=2, AND=3, TOPLEVEL=4, LAMBDA=5, PROBABILITY=6, DORMANCY=7, NUMBER=8, 
		NAME=9, IDENTIFIER=10, COMMENT=11, WS=12;
	public static final int
		RULE_faulttree = 0, RULE_top = 1, RULE_gate = 2, RULE_basicevent = 3, 
		RULE_property = 4, RULE_lambda = 5, RULE_probability = 6, RULE_dormancy = 7;
	private static String[] makeRuleNames() {
		return new String[] {
			"faulttree", "top", "gate", "basicevent", "property", "lambda", "probability", 
			"dormancy"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "'or'", "'and'", "'toplevel'", "'lambda'", "'prob'", "'dorm'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "EQ", "OR", "AND", "TOPLEVEL", "LAMBDA", "PROBABILITY", "DORMANCY", 
			"NUMBER", "NAME", "IDENTIFIER", "COMMENT", "WS"
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
	}

	public final FaulttreeContext faulttree() throws RecognitionException {
		FaulttreeContext _localctx = new FaulttreeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_faulttree);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			top();
			setState(21);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				setState(19);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(17);
					gate();
					}
					break;
				case 2:
					{
					setState(18);
					basicevent();
					}
					break;
				}
				}
				setState(23);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(24);
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
	}

	public final TopContext top() throws RecognitionException {
		TopContext _localctx = new TopContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_top);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			match(TOPLEVEL);
			setState(27);
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
		public Token op;
		public List<TerminalNode> NAME() { return getTokens(GalileoParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(GalileoParser.NAME, i);
		}
		public TerminalNode OR() { return getToken(GalileoParser.OR, 0); }
		public TerminalNode AND() { return getToken(GalileoParser.AND, 0); }
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
	}

	public final GateContext gate() throws RecognitionException {
		GateContext _localctx = new GateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_gate);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			((GateContext)_localctx).name = match(NAME);
			setState(30);
			((GateContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==OR || _la==AND) ) {
				((GateContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(34);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(31);
					match(NAME);
					}
					} 
				}
				setState(36);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
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
	}

	public final BasiceventContext basicevent() throws RecognitionException {
		BasiceventContext _localctx = new BasiceventContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_basicevent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			((BasiceventContext)_localctx).name = match(NAME);
			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LAMBDA) | (1L << PROBABILITY) | (1L << DORMANCY))) != 0)) {
				{
				{
				setState(38);
				property();
				}
				}
				setState(43);
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
		public ProbabilityContext probability() {
			return getRuleContext(ProbabilityContext.class,0);
		}
		public DormancyContext dormancy() {
			return getRuleContext(DormancyContext.class,0);
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
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LAMBDA:
				{
				setState(44);
				lambda();
				}
				break;
			case PROBABILITY:
				{
				setState(45);
				probability();
				}
				break;
			case DORMANCY:
				{
				setState(46);
				dormancy();
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
		public TerminalNode NUMBER() { return getToken(GalileoParser.NUMBER, 0); }
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
	}

	public final LambdaContext lambda() throws RecognitionException {
		LambdaContext _localctx = new LambdaContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_lambda);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			match(LAMBDA);
			setState(50);
			match(EQ);
			setState(51);
			((LambdaContext)_localctx).val = match(NUMBER);
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
		public TerminalNode NUMBER() { return getToken(GalileoParser.NUMBER, 0); }
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
	}

	public final ProbabilityContext probability() throws RecognitionException {
		ProbabilityContext _localctx = new ProbabilityContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_probability);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			match(PROBABILITY);
			setState(54);
			match(EQ);
			setState(55);
			((ProbabilityContext)_localctx).val = match(NUMBER);
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
		public TerminalNode NUMBER() { return getToken(GalileoParser.NUMBER, 0); }
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
	}

	public final DormancyContext dormancy() throws RecognitionException {
		DormancyContext _localctx = new DormancyContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_dormancy);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			match(DORMANCY);
			setState(58);
			match(EQ);
			setState(59);
			((DormancyContext)_localctx).val = match(NUMBER);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\16@\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\3\2\3\2\7\2\26"+
		"\n\2\f\2\16\2\31\13\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\7\4#\n\4\f\4\16"+
		"\4&\13\4\3\5\3\5\7\5*\n\5\f\5\16\5-\13\5\3\6\3\6\3\6\5\6\62\n\6\3\7\3"+
		"\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\2\2\n\2\4\6\b\n\f\16\20"+
		"\2\3\3\2\4\5\2=\2\22\3\2\2\2\4\34\3\2\2\2\6\37\3\2\2\2\b\'\3\2\2\2\n\61"+
		"\3\2\2\2\f\63\3\2\2\2\16\67\3\2\2\2\20;\3\2\2\2\22\27\5\4\3\2\23\26\5"+
		"\6\4\2\24\26\5\b\5\2\25\23\3\2\2\2\25\24\3\2\2\2\26\31\3\2\2\2\27\25\3"+
		"\2\2\2\27\30\3\2\2\2\30\32\3\2\2\2\31\27\3\2\2\2\32\33\7\2\2\3\33\3\3"+
		"\2\2\2\34\35\7\6\2\2\35\36\7\13\2\2\36\5\3\2\2\2\37 \7\13\2\2 $\t\2\2"+
		"\2!#\7\13\2\2\"!\3\2\2\2#&\3\2\2\2$\"\3\2\2\2$%\3\2\2\2%\7\3\2\2\2&$\3"+
		"\2\2\2\'+\7\13\2\2(*\5\n\6\2)(\3\2\2\2*-\3\2\2\2+)\3\2\2\2+,\3\2\2\2,"+
		"\t\3\2\2\2-+\3\2\2\2.\62\5\f\7\2/\62\5\16\b\2\60\62\5\20\t\2\61.\3\2\2"+
		"\2\61/\3\2\2\2\61\60\3\2\2\2\62\13\3\2\2\2\63\64\7\7\2\2\64\65\7\3\2\2"+
		"\65\66\7\n\2\2\66\r\3\2\2\2\678\7\b\2\289\7\3\2\29:\7\n\2\2:\17\3\2\2"+
		"\2;<\7\t\2\2<=\7\3\2\2=>\7\n\2\2>\21\3\2\2\2\7\25\27$+\61";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}