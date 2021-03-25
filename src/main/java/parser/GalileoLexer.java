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
		T__0=1, T__1=2, T__2=3, T__3=4, INT=5, EQ=6, OR=7, AND=8, OF=9, TOPLEVEL=10, 
		LAMBDA=11, PH=12, PROBABILITY=13, DORMANCY=14, REPAIR=15, FAILURE_STATES=16, 
		DOUBLE=17, NAME=18, IDENTIFIER=19, COMMENT=20, WS=21;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "INT", "EQ", "OR", "AND", "OF", "TOPLEVEL", 
			"LAMBDA", "PH", "PROBABILITY", "DORMANCY", "REPAIR", "FAILURE_STATES", 
			"DIGIT", "DOUBLE", "NAME", "IDENTIFIER", "COMMENT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'['", "']'", "','", null, "'='", "'or'", "'and'", "'of'", 
			"'toplevel'", "'lambda'", "'ph'", "'prob'", "'dorm'", "'repair'", "'failurestates'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "INT", "EQ", "OR", "AND", "OF", "TOPLEVEL", 
			"LAMBDA", "PH", "PROBABILITY", "DORMANCY", "REPAIR", "FAILURE_STATES", 
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\27\u00cf\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\5\3\5\3\6\6\69\n\6\r\6\16\6:\3\7\3\7\3\b\3\b\3\b\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17\3\17"+
		"\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\23\7\23"+
		"~\n\23\f\23\16\23\u0081\13\23\3\23\3\23\6\23\u0085\n\23\r\23\16\23\u0086"+
		"\3\23\6\23\u008a\n\23\r\23\16\23\u008b\3\23\3\23\5\23\u0090\n\23\3\23"+
		"\6\23\u0093\n\23\r\23\16\23\u0094\3\23\7\23\u0098\n\23\f\23\16\23\u009b"+
		"\13\23\3\23\3\23\6\23\u009f\n\23\r\23\16\23\u00a0\3\23\3\23\5\23\u00a5"+
		"\n\23\3\23\6\23\u00a8\n\23\r\23\16\23\u00a9\5\23\u00ac\n\23\3\24\3\24"+
		"\7\24\u00b0\n\24\f\24\16\24\u00b3\13\24\3\24\3\24\3\25\3\25\7\25\u00b9"+
		"\n\25\f\25\16\25\u00bc\13\25\3\26\3\26\3\26\3\26\7\26\u00c2\n\26\f\26"+
		"\16\26\u00c5\13\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\4\u00ba"+
		"\u00c3\2\30\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33"+
		"\17\35\20\37\21!\22#\2%\23\'\24)\25+\26-\27\3\2\6\3\2\62;\5\2C\\aac|\6"+
		"\2\62;C\\aac|\5\2\13\f\17\17\"\"\2\u00dc\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35"+
		"\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+"+
		"\3\2\2\2\2-\3\2\2\2\3/\3\2\2\2\5\61\3\2\2\2\7\63\3\2\2\2\t\65\3\2\2\2"+
		"\138\3\2\2\2\r<\3\2\2\2\17>\3\2\2\2\21A\3\2\2\2\23E\3\2\2\2\25H\3\2\2"+
		"\2\27Q\3\2\2\2\31X\3\2\2\2\33[\3\2\2\2\35`\3\2\2\2\37e\3\2\2\2!l\3\2\2"+
		"\2#z\3\2\2\2%\u00ab\3\2\2\2\'\u00ad\3\2\2\2)\u00b6\3\2\2\2+\u00bd\3\2"+
		"\2\2-\u00cb\3\2\2\2/\60\7=\2\2\60\4\3\2\2\2\61\62\7]\2\2\62\6\3\2\2\2"+
		"\63\64\7_\2\2\64\b\3\2\2\2\65\66\7.\2\2\66\n\3\2\2\2\679\5#\22\28\67\3"+
		"\2\2\29:\3\2\2\2:8\3\2\2\2:;\3\2\2\2;\f\3\2\2\2<=\7?\2\2=\16\3\2\2\2>"+
		"?\7q\2\2?@\7t\2\2@\20\3\2\2\2AB\7c\2\2BC\7p\2\2CD\7f\2\2D\22\3\2\2\2E"+
		"F\7q\2\2FG\7h\2\2G\24\3\2\2\2HI\7v\2\2IJ\7q\2\2JK\7r\2\2KL\7n\2\2LM\7"+
		"g\2\2MN\7x\2\2NO\7g\2\2OP\7n\2\2P\26\3\2\2\2QR\7n\2\2RS\7c\2\2ST\7o\2"+
		"\2TU\7d\2\2UV\7f\2\2VW\7c\2\2W\30\3\2\2\2XY\7r\2\2YZ\7j\2\2Z\32\3\2\2"+
		"\2[\\\7r\2\2\\]\7t\2\2]^\7q\2\2^_\7d\2\2_\34\3\2\2\2`a\7f\2\2ab\7q\2\2"+
		"bc\7t\2\2cd\7o\2\2d\36\3\2\2\2ef\7t\2\2fg\7g\2\2gh\7r\2\2hi\7c\2\2ij\7"+
		"k\2\2jk\7t\2\2k \3\2\2\2lm\7h\2\2mn\7c\2\2no\7k\2\2op\7n\2\2pq\7w\2\2"+
		"qr\7t\2\2rs\7g\2\2st\7u\2\2tu\7v\2\2uv\7c\2\2vw\7v\2\2wx\7g\2\2xy\7u\2"+
		"\2y\"\3\2\2\2z{\t\2\2\2{$\3\2\2\2|~\5#\22\2}|\3\2\2\2~\u0081\3\2\2\2\177"+
		"}\3\2\2\2\177\u0080\3\2\2\2\u0080\u0082\3\2\2\2\u0081\177\3\2\2\2\u0082"+
		"\u0084\7\60\2\2\u0083\u0085\5#\22\2\u0084\u0083\3\2\2\2\u0085\u0086\3"+
		"\2\2\2\u0086\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u00ac\3\2\2\2\u0088"+
		"\u008a\5#\22\2\u0089\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b\u0089\3\2"+
		"\2\2\u008b\u008c\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008f\7g\2\2\u008e"+
		"\u0090\7/\2\2\u008f\u008e\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0092\3\2"+
		"\2\2\u0091\u0093\5#\22\2\u0092\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094"+
		"\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u00ac\3\2\2\2\u0096\u0098\5#"+
		"\22\2\u0097\u0096\3\2\2\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2\2\2\u0099"+
		"\u009a\3\2\2\2\u009a\u009c\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u009e\7\60"+
		"\2\2\u009d\u009f\5#\22\2\u009e\u009d\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0"+
		"\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a4\7g"+
		"\2\2\u00a3\u00a5\7/\2\2\u00a4\u00a3\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5"+
		"\u00a7\3\2\2\2\u00a6\u00a8\5#\22\2\u00a7\u00a6\3\2\2\2\u00a8\u00a9\3\2"+
		"\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ac\3\2\2\2\u00ab"+
		"\177\3\2\2\2\u00ab\u0089\3\2\2\2\u00ab\u0099\3\2\2\2\u00ac&\3\2\2\2\u00ad"+
		"\u00b1\7$\2\2\u00ae\u00b0\5)\25\2\u00af\u00ae\3\2\2\2\u00b0\u00b3\3\2"+
		"\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b4\3\2\2\2\u00b3"+
		"\u00b1\3\2\2\2\u00b4\u00b5\7$\2\2\u00b5(\3\2\2\2\u00b6\u00ba\t\3\2\2\u00b7"+
		"\u00b9\t\4\2\2\u00b8\u00b7\3\2\2\2\u00b9\u00bc\3\2\2\2\u00ba\u00bb\3\2"+
		"\2\2\u00ba\u00b8\3\2\2\2\u00bb*\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bd\u00be"+
		"\7\61\2\2\u00be\u00bf\7,\2\2\u00bf\u00c3\3\2\2\2\u00c0\u00c2\13\2\2\2"+
		"\u00c1\u00c0\3\2\2\2\u00c2\u00c5\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c3\u00c1"+
		"\3\2\2\2\u00c4\u00c6\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c6\u00c7\7,\2\2\u00c7"+
		"\u00c8\7\61\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00ca\b\26\2\2\u00ca,\3\2\2"+
		"\2\u00cb\u00cc\t\5\2\2\u00cc\u00cd\3\2\2\2\u00cd\u00ce\b\27\2\2\u00ce"+
		".\3\2\2\2\21\2:\177\u0086\u008b\u008f\u0094\u0099\u00a0\u00a4\u00a9\u00ab"+
		"\u00b1\u00ba\u00c3\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}