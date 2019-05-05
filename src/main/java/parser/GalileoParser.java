// Generated from C:/Users/Daniel/Documents/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.7.2
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
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, EQ=2, OR=3, AND=4, TOPLEVEL=5, LAMBDA=6, PROBABILITY=7, DORMANCY=8, 
		NUMBER=9, NAME=10, IDENTIFIER=11, COMMENT=12, WS=13;
	public static final int
		RULE_faulttree = 0, RULE_top = 1, RULE_gate = 2, RULE_basicevent = 3, 
		RULE_property = 4, RULE_lambda = 5, RULE_probability = 6, RULE_dormancy = 7, 
		RULE_operation = 8, RULE_or = 9, RULE_and = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"faulttree", "top", "gate", "basicevent", "property", "lambda", "probability", 
			"dormancy", "operation", "or", "and"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'='", "'or'", "'and'", "'toplevel'", "'lambda'", "'prob'", 
			"'dorm'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "EQ", "OR", "AND", "TOPLEVEL", "LAMBDA", "PROBABILITY", "DORMANCY", 
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
			setState(22);
			top();
			setState(23);
			match(T__0);
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(26);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(24);
					gate();
					}
					break;
				case 2:
					{
					setState(25);
					basicevent();
					}
					break;
				}
				setState(28);
				match(T__0);
				}
				}
				setState(34);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(35);
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
			setState(37);
			match(TOPLEVEL);
			setState(38);
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
	}

	public final GateContext gate() throws RecognitionException {
		GateContext _localctx = new GateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_gate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			((GateContext)_localctx).name = match(NAME);
			setState(41);
			operation();
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(42);
				((GateContext)_localctx).NAME = match(NAME);
				((GateContext)_localctx).inputs.add(((GateContext)_localctx).NAME);
				}
				}
				setState(47);
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
	}

	public final BasiceventContext basicevent() throws RecognitionException {
		BasiceventContext _localctx = new BasiceventContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_basicevent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			((BasiceventContext)_localctx).name = match(NAME);
			setState(52);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LAMBDA) | (1L << PROBABILITY) | (1L << DORMANCY))) != 0)) {
				{
				{
				setState(49);
				property();
				}
				}
				setState(54);
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
			setState(58);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LAMBDA:
				{
				setState(55);
				lambda();
				}
				break;
			case PROBABILITY:
				{
				setState(56);
				probability();
				}
				break;
			case DORMANCY:
				{
				setState(57);
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
			setState(60);
			match(LAMBDA);
			setState(61);
			match(EQ);
			setState(62);
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
			setState(64);
			match(PROBABILITY);
			setState(65);
			match(EQ);
			setState(66);
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
			setState(68);
			match(DORMANCY);
			setState(69);
			match(EQ);
			setState(70);
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

	public static class OperationContext extends ParserRuleContext {
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
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
	}

	public final OperationContext operation() throws RecognitionException {
		OperationContext _localctx = new OperationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_operation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OR:
				{
				setState(72);
				or();
				}
				break;
			case AND:
				{
				setState(73);
				and();
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
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
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
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17S\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\3\2\3\2\3\2\3\2\5\2\35\n\2\3\2\3\2\7\2!\n\2\f\2\16\2$\13\2\3\2"+
		"\3\2\3\3\3\3\3\3\3\4\3\4\3\4\7\4.\n\4\f\4\16\4\61\13\4\3\5\3\5\7\5\65"+
		"\n\5\f\5\16\58\13\5\3\6\3\6\3\6\5\6=\n\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3"+
		"\b\3\t\3\t\3\t\3\t\3\n\3\n\5\nM\n\n\3\13\3\13\3\f\3\f\3\f\2\2\r\2\4\6"+
		"\b\n\f\16\20\22\24\26\2\2\2N\2\30\3\2\2\2\4\'\3\2\2\2\6*\3\2\2\2\b\62"+
		"\3\2\2\2\n<\3\2\2\2\f>\3\2\2\2\16B\3\2\2\2\20F\3\2\2\2\22L\3\2\2\2\24"+
		"N\3\2\2\2\26P\3\2\2\2\30\31\5\4\3\2\31\"\7\3\2\2\32\35\5\6\4\2\33\35\5"+
		"\b\5\2\34\32\3\2\2\2\34\33\3\2\2\2\35\36\3\2\2\2\36\37\7\3\2\2\37!\3\2"+
		"\2\2 \34\3\2\2\2!$\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#%\3\2\2\2$\"\3\2\2\2"+
		"%&\7\2\2\3&\3\3\2\2\2\'(\7\7\2\2()\7\f\2\2)\5\3\2\2\2*+\7\f\2\2+/\5\22"+
		"\n\2,.\7\f\2\2-,\3\2\2\2.\61\3\2\2\2/-\3\2\2\2/\60\3\2\2\2\60\7\3\2\2"+
		"\2\61/\3\2\2\2\62\66\7\f\2\2\63\65\5\n\6\2\64\63\3\2\2\2\658\3\2\2\2\66"+
		"\64\3\2\2\2\66\67\3\2\2\2\67\t\3\2\2\28\66\3\2\2\29=\5\f\7\2:=\5\16\b"+
		"\2;=\5\20\t\2<9\3\2\2\2<:\3\2\2\2<;\3\2\2\2=\13\3\2\2\2>?\7\b\2\2?@\7"+
		"\4\2\2@A\7\13\2\2A\r\3\2\2\2BC\7\t\2\2CD\7\4\2\2DE\7\13\2\2E\17\3\2\2"+
		"\2FG\7\n\2\2GH\7\4\2\2HI\7\13\2\2I\21\3\2\2\2JM\5\24\13\2KM\5\26\f\2L"+
		"J\3\2\2\2LK\3\2\2\2M\23\3\2\2\2NO\7\5\2\2O\25\3\2\2\2PQ\7\6\2\2Q\27\3"+
		"\2\2\2\b\34\"/\66<L";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}