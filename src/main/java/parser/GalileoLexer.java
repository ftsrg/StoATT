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
		DORMANCY=10, NUMBER=11, NAME=12, IDENTIFIER=13, COMMENT=14, WS=15;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "INT", "EQ", "OR", "AND", "OF", "TOPLEVEL", "LAMBDA", "PROBABILITY", 
			"DORMANCY", "DIGIT", "NUMBER", "NAME", "IDENTIFIER", "COMMENT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", null, "'='", "'or'", "'and'", "'of'", "'toplevel'", "'lambda'", 
			"'prob'", "'dorm'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "INT", "EQ", "OR", "AND", "OF", "TOPLEVEL", "LAMBDA", "PROBABILITY", 
			"DORMANCY", "NUMBER", "NAME", "IDENTIFIER", "COMMENT", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\21\u0084\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3"+
		"\2\3\3\6\3\'\n\3\r\3\16\3(\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7"+
		"\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\r\6\rT\n\r\r\r\16"+
		"\rU\3\r\7\rY\n\r\f\r\16\r\\\13\r\3\r\3\r\6\r`\n\r\r\r\16\ra\5\rd\n\r\3"+
		"\16\3\16\6\16h\n\16\r\16\16\16i\3\17\3\17\7\17n\n\17\f\17\16\17q\13\17"+
		"\3\20\3\20\3\20\3\20\7\20w\n\20\f\20\16\20z\13\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\21\3\21\3\21\3\21\4ox\2\22\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n"+
		"\23\13\25\f\27\2\31\r\33\16\35\17\37\20!\21\3\2\7\3\2\62;\4\2C\\c|\5\2"+
		"C\\aac|\6\2\62;C\\aac|\5\2\13\f\17\17\"\"\2\u008a\2\3\3\2\2\2\2\5\3\2"+
		"\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2"+
		"\2\2\2\37\3\2\2\2\2!\3\2\2\2\3#\3\2\2\2\5&\3\2\2\2\7*\3\2\2\2\t,\3\2\2"+
		"\2\13/\3\2\2\2\r\63\3\2\2\2\17\66\3\2\2\2\21?\3\2\2\2\23F\3\2\2\2\25K"+
		"\3\2\2\2\27P\3\2\2\2\31c\3\2\2\2\33e\3\2\2\2\35k\3\2\2\2\37r\3\2\2\2!"+
		"\u0080\3\2\2\2#$\7=\2\2$\4\3\2\2\2%\'\5\27\f\2&%\3\2\2\2\'(\3\2\2\2(&"+
		"\3\2\2\2()\3\2\2\2)\6\3\2\2\2*+\7?\2\2+\b\3\2\2\2,-\7q\2\2-.\7t\2\2.\n"+
		"\3\2\2\2/\60\7c\2\2\60\61\7p\2\2\61\62\7f\2\2\62\f\3\2\2\2\63\64\7q\2"+
		"\2\64\65\7h\2\2\65\16\3\2\2\2\66\67\7v\2\2\678\7q\2\289\7r\2\29:\7n\2"+
		"\2:;\7g\2\2;<\7x\2\2<=\7g\2\2=>\7n\2\2>\20\3\2\2\2?@\7n\2\2@A\7c\2\2A"+
		"B\7o\2\2BC\7d\2\2CD\7f\2\2DE\7c\2\2E\22\3\2\2\2FG\7r\2\2GH\7t\2\2HI\7"+
		"q\2\2IJ\7d\2\2J\24\3\2\2\2KL\7f\2\2LM\7q\2\2MN\7t\2\2NO\7o\2\2O\26\3\2"+
		"\2\2PQ\t\2\2\2Q\30\3\2\2\2RT\5\27\f\2SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV"+
		"\3\2\2\2Vd\3\2\2\2WY\5\27\f\2XW\3\2\2\2Y\\\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2"+
		"[]\3\2\2\2\\Z\3\2\2\2]_\7\60\2\2^`\5\27\f\2_^\3\2\2\2`a\3\2\2\2a_\3\2"+
		"\2\2ab\3\2\2\2bd\3\2\2\2cS\3\2\2\2cZ\3\2\2\2d\32\3\2\2\2eg\t\3\2\2fh\5"+
		"\35\17\2gf\3\2\2\2hi\3\2\2\2ig\3\2\2\2ij\3\2\2\2j\34\3\2\2\2ko\t\4\2\2"+
		"ln\t\5\2\2ml\3\2\2\2nq\3\2\2\2op\3\2\2\2om\3\2\2\2p\36\3\2\2\2qo\3\2\2"+
		"\2rs\7\61\2\2st\7,\2\2tx\3\2\2\2uw\13\2\2\2vu\3\2\2\2wz\3\2\2\2xy\3\2"+
		"\2\2xv\3\2\2\2y{\3\2\2\2zx\3\2\2\2{|\7,\2\2|}\7\61\2\2}~\3\2\2\2~\177"+
		"\b\20\2\2\177 \3\2\2\2\u0080\u0081\t\6\2\2\u0081\u0082\3\2\2\2\u0082\u0083"+
		"\b\21\2\2\u0083\"\3\2\2\2\13\2(UZaciox\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}