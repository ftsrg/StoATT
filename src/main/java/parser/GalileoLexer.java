// Generated from C:/Users/Daniel/Documents/egyetem/onlab_tdk/Impl/Linalg/src/main/antlr\Galileo.g4 by ANTLR 4.7.2
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
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, INT=2, EQ=3, OR=4, AND=5, OF=6, TOPLEVEL=7, LAMBDA=8, PROBABILITY=9, 
		DORMANCY=10, REPAIR=11, NUMBER=12, NAME=13, IDENTIFIER=14, COMMENT=15, 
		WS=16;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "INT", "EQ", "OR", "AND", "OF", "TOPLEVEL", "LAMBDA", "PROBABILITY", 
			"DORMANCY", "REPAIR", "DIGIT", "NUMBER", "NAME", "IDENTIFIER", "COMMENT", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", null, "'='", "'or'", "'and'", "'of'", "'toplevel'", "'lambda'", 
			"'prob'", "'dorm'", "'repair'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "INT", "EQ", "OR", "AND", "OF", "TOPLEVEL", "LAMBDA", "PROBABILITY", 
			"DORMANCY", "REPAIR", "NUMBER", "NAME", "IDENTIFIER", "COMMENT", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\22\u0091\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\3\2\3\2\3\3\6\3)\n\3\r\3\16\3*\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3"+
		"\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\r\3\r\3\16\6\16]\n\16\r\16\16\16^\3\16\7\16b\n\16\f\16"+
		"\16\16e\13\16\3\16\3\16\6\16i\n\16\r\16\16\16j\5\16m\n\16\3\17\3\17\3"+
		"\17\7\17r\n\17\f\17\16\17u\13\17\3\17\3\17\3\20\3\20\7\20{\n\20\f\20\16"+
		"\20~\13\20\3\21\3\21\3\21\3\21\7\21\u0084\n\21\f\21\16\21\u0087\13\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\4|\u0085\2\23\3\3\5\4\7"+
		"\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\2\33\16\35\17\37\20!\21#"+
		"\22\3\2\7\3\2\62;\4\2C\\c|\5\2C\\aac|\6\2\62;C\\aac|\5\2\13\f\17\17\""+
		"\"\2\u0097\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2"+
		"\3%\3\2\2\2\5(\3\2\2\2\7,\3\2\2\2\t.\3\2\2\2\13\61\3\2\2\2\r\65\3\2\2"+
		"\2\178\3\2\2\2\21A\3\2\2\2\23H\3\2\2\2\25M\3\2\2\2\27R\3\2\2\2\31Y\3\2"+
		"\2\2\33l\3\2\2\2\35n\3\2\2\2\37x\3\2\2\2!\177\3\2\2\2#\u008d\3\2\2\2%"+
		"&\7=\2\2&\4\3\2\2\2\')\5\31\r\2(\'\3\2\2\2)*\3\2\2\2*(\3\2\2\2*+\3\2\2"+
		"\2+\6\3\2\2\2,-\7?\2\2-\b\3\2\2\2./\7q\2\2/\60\7t\2\2\60\n\3\2\2\2\61"+
		"\62\7c\2\2\62\63\7p\2\2\63\64\7f\2\2\64\f\3\2\2\2\65\66\7q\2\2\66\67\7"+
		"h\2\2\67\16\3\2\2\289\7v\2\29:\7q\2\2:;\7r\2\2;<\7n\2\2<=\7g\2\2=>\7x"+
		"\2\2>?\7g\2\2?@\7n\2\2@\20\3\2\2\2AB\7n\2\2BC\7c\2\2CD\7o\2\2DE\7d\2\2"+
		"EF\7f\2\2FG\7c\2\2G\22\3\2\2\2HI\7r\2\2IJ\7t\2\2JK\7q\2\2KL\7d\2\2L\24"+
		"\3\2\2\2MN\7f\2\2NO\7q\2\2OP\7t\2\2PQ\7o\2\2Q\26\3\2\2\2RS\7t\2\2ST\7"+
		"g\2\2TU\7r\2\2UV\7c\2\2VW\7k\2\2WX\7t\2\2X\30\3\2\2\2YZ\t\2\2\2Z\32\3"+
		"\2\2\2[]\5\31\r\2\\[\3\2\2\2]^\3\2\2\2^\\\3\2\2\2^_\3\2\2\2_m\3\2\2\2"+
		"`b\5\31\r\2a`\3\2\2\2be\3\2\2\2ca\3\2\2\2cd\3\2\2\2df\3\2\2\2ec\3\2\2"+
		"\2fh\7\60\2\2gi\5\31\r\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2\2km\3\2"+
		"\2\2l\\\3\2\2\2lc\3\2\2\2m\34\3\2\2\2no\7$\2\2os\t\3\2\2pr\5\37\20\2q"+
		"p\3\2\2\2ru\3\2\2\2sq\3\2\2\2st\3\2\2\2tv\3\2\2\2us\3\2\2\2vw\7$\2\2w"+
		"\36\3\2\2\2x|\t\4\2\2y{\t\5\2\2zy\3\2\2\2{~\3\2\2\2|}\3\2\2\2|z\3\2\2"+
		"\2} \3\2\2\2~|\3\2\2\2\177\u0080\7\61\2\2\u0080\u0081\7,\2\2\u0081\u0085"+
		"\3\2\2\2\u0082\u0084\13\2\2\2\u0083\u0082\3\2\2\2\u0084\u0087\3\2\2\2"+
		"\u0085\u0086\3\2\2\2\u0085\u0083\3\2\2\2\u0086\u0088\3\2\2\2\u0087\u0085"+
		"\3\2\2\2\u0088\u0089\7,\2\2\u0089\u008a\7\61\2\2\u008a\u008b\3\2\2\2\u008b"+
		"\u008c\b\21\2\2\u008c\"\3\2\2\2\u008d\u008e\t\6\2\2\u008e\u008f\3\2\2"+
		"\2\u008f\u0090\b\22\2\2\u0090$\3\2\2\2\13\2*^cjls|\u0085\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}