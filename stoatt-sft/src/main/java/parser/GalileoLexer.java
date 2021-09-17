// Generated from E:/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.9.1
package parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GalileoLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, INT=5, EQ=6, OR=7, EXP=8, AND=9, OF=10, 
		WSP=11, TOPLEVEL=12, LAMBDA=13, PH=14, PROBABILITY=15, DORMANCY=16, REPAIR=17, 
		FAILURE_STATES=18, DOUBLE=19, NAME=20, IDENTIFIER=21, COMMENT=22, WS=23;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "INT", "EQ", "OR", "EXP", "AND", "OF", 
			"WSP", "TOPLEVEL", "LAMBDA", "PH", "PROBABILITY", "DORMANCY", "REPAIR", 
			"FAILURE_STATES", "DIGIT", "DOUBLE", "NAME", "IDENTIFIER", "COMMENT", 
			"WS"
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


	public GalileoLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Galileo.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\31\u00d9\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\6\6=\n\6\r\6\16\6>\3\7\3\7\3"+
		"\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\22\3"+
		"\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25\7\25\u0088\n\25\f\25\16\25"+
		"\u008b\13\25\3\25\3\25\6\25\u008f\n\25\r\25\16\25\u0090\3\25\6\25\u0094"+
		"\n\25\r\25\16\25\u0095\3\25\3\25\5\25\u009a\n\25\3\25\6\25\u009d\n\25"+
		"\r\25\16\25\u009e\3\25\7\25\u00a2\n\25\f\25\16\25\u00a5\13\25\3\25\3\25"+
		"\6\25\u00a9\n\25\r\25\16\25\u00aa\3\25\3\25\5\25\u00af\n\25\3\25\6\25"+
		"\u00b2\n\25\r\25\16\25\u00b3\5\25\u00b6\n\25\3\26\3\26\7\26\u00ba\n\26"+
		"\f\26\16\26\u00bd\13\26\3\26\3\26\3\27\3\27\7\27\u00c3\n\27\f\27\16\27"+
		"\u00c6\13\27\3\30\3\30\3\30\3\30\7\30\u00cc\n\30\f\30\16\30\u00cf\13\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\4\u00c4\u00cd\2\32\3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\2)\25+\26-\27/\30\61\31\3\2\7\4\2GGgg\3\2\62;\5\2C\\aa"+
		"c|\6\2\62;C\\aac|\5\2\13\f\17\17\"\"\2\u00e6\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2)\3\2\2\2"+
		"\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\3\63\3\2\2\2\5\65\3\2\2"+
		"\2\7\67\3\2\2\2\t9\3\2\2\2\13<\3\2\2\2\r@\3\2\2\2\17B\3\2\2\2\21E\3\2"+
		"\2\2\23G\3\2\2\2\25K\3\2\2\2\27N\3\2\2\2\31R\3\2\2\2\33[\3\2\2\2\35b\3"+
		"\2\2\2\37e\3\2\2\2!j\3\2\2\2#o\3\2\2\2%v\3\2\2\2\'\u0084\3\2\2\2)\u00b5"+
		"\3\2\2\2+\u00b7\3\2\2\2-\u00c0\3\2\2\2/\u00c7\3\2\2\2\61\u00d5\3\2\2\2"+
		"\63\64\7=\2\2\64\4\3\2\2\2\65\66\7]\2\2\66\6\3\2\2\2\678\7_\2\28\b\3\2"+
		"\2\29:\7.\2\2:\n\3\2\2\2;=\5\'\24\2<;\3\2\2\2=>\3\2\2\2><\3\2\2\2>?\3"+
		"\2\2\2?\f\3\2\2\2@A\7?\2\2A\16\3\2\2\2BC\7q\2\2CD\7t\2\2D\20\3\2\2\2E"+
		"F\t\2\2\2F\22\3\2\2\2GH\7c\2\2HI\7p\2\2IJ\7f\2\2J\24\3\2\2\2KL\7q\2\2"+
		"LM\7h\2\2M\26\3\2\2\2NO\7y\2\2OP\7u\2\2PQ\7r\2\2Q\30\3\2\2\2RS\7v\2\2"+
		"ST\7q\2\2TU\7r\2\2UV\7n\2\2VW\7g\2\2WX\7x\2\2XY\7g\2\2YZ\7n\2\2Z\32\3"+
		"\2\2\2[\\\7n\2\2\\]\7c\2\2]^\7o\2\2^_\7d\2\2_`\7f\2\2`a\7c\2\2a\34\3\2"+
		"\2\2bc\7r\2\2cd\7j\2\2d\36\3\2\2\2ef\7r\2\2fg\7t\2\2gh\7q\2\2hi\7d\2\2"+
		"i \3\2\2\2jk\7f\2\2kl\7q\2\2lm\7t\2\2mn\7o\2\2n\"\3\2\2\2op\7t\2\2pq\7"+
		"g\2\2qr\7r\2\2rs\7c\2\2st\7k\2\2tu\7t\2\2u$\3\2\2\2vw\7h\2\2wx\7c\2\2"+
		"xy\7k\2\2yz\7n\2\2z{\7w\2\2{|\7t\2\2|}\7g\2\2}~\7u\2\2~\177\7v\2\2\177"+
		"\u0080\7c\2\2\u0080\u0081\7v\2\2\u0081\u0082\7g\2\2\u0082\u0083\7u\2\2"+
		"\u0083&\3\2\2\2\u0084\u0085\t\3\2\2\u0085(\3\2\2\2\u0086\u0088\5\'\24"+
		"\2\u0087\u0086\3\2\2\2\u0088\u008b\3\2\2\2\u0089\u0087\3\2\2\2\u0089\u008a"+
		"\3\2\2\2\u008a\u008c\3\2\2\2\u008b\u0089\3\2\2\2\u008c\u008e\7\60\2\2"+
		"\u008d\u008f\5\'\24\2\u008e\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u008e"+
		"\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u00b6\3\2\2\2\u0092\u0094\5\'\24\2"+
		"\u0093\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096"+
		"\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0099\5\21\t\2\u0098\u009a\7/\2\2\u0099"+
		"\u0098\3\2\2\2\u0099\u009a\3\2\2\2\u009a\u009c\3\2\2\2\u009b\u009d\5\'"+
		"\24\2\u009c\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009c\3\2\2\2\u009e"+
		"\u009f\3\2\2\2\u009f\u00b6\3\2\2\2\u00a0\u00a2\5\'\24\2\u00a1\u00a0\3"+
		"\2\2\2\u00a2\u00a5\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4"+
		"\u00a6\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a6\u00a8\7\60\2\2\u00a7\u00a9\5"+
		"\'\24\2\u00a8\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00a8\3\2\2\2\u00aa"+
		"\u00ab\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00ae\5\21\t\2\u00ad\u00af\7"+
		"/\2\2\u00ae\u00ad\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b1\3\2\2\2\u00b0"+
		"\u00b2\5\'\24\2\u00b1\u00b0\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b1\3"+
		"\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\u00b6\3\2\2\2\u00b5\u0089\3\2\2\2\u00b5"+
		"\u0093\3\2\2\2\u00b5\u00a3\3\2\2\2\u00b6*\3\2\2\2\u00b7\u00bb\7$\2\2\u00b8"+
		"\u00ba\5-\27\2\u00b9\u00b8\3\2\2\2\u00ba\u00bd\3\2\2\2\u00bb\u00b9\3\2"+
		"\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00be\3\2\2\2\u00bd\u00bb\3\2\2\2\u00be"+
		"\u00bf\7$\2\2\u00bf,\3\2\2\2\u00c0\u00c4\t\4\2\2\u00c1\u00c3\t\5\2\2\u00c2"+
		"\u00c1\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c4\u00c2\3\2"+
		"\2\2\u00c5.\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c7\u00c8\7\61\2\2\u00c8\u00c9"+
		"\7,\2\2\u00c9\u00cd\3\2\2\2\u00ca\u00cc\13\2\2\2\u00cb\u00ca\3\2\2\2\u00cc"+
		"\u00cf\3\2\2\2\u00cd\u00ce\3\2\2\2\u00cd\u00cb\3\2\2\2\u00ce\u00d0\3\2"+
		"\2\2\u00cf\u00cd\3\2\2\2\u00d0\u00d1\7,\2\2\u00d1\u00d2\7\61\2\2\u00d2"+
		"\u00d3\3\2\2\2\u00d3\u00d4\b\30\2\2\u00d4\60\3\2\2\2\u00d5\u00d6\t\6\2"+
		"\2\u00d6\u00d7\3\2\2\2\u00d7\u00d8\b\31\2\2\u00d8\62\3\2\2\2\21\2>\u0089"+
		"\u0090\u0095\u0099\u009e\u00a3\u00aa\u00ae\u00b3\u00b5\u00bb\u00c4\u00cd"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}