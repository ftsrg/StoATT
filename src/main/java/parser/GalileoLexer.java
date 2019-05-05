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
		T__0=1, EQ=2, OR=3, AND=4, TOPLEVEL=5, LAMBDA=6, PROBABILITY=7, DORMANCY=8, 
		NUMBER=9, NAME=10, IDENTIFIER=11, COMMENT=12, WS=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "EQ", "OR", "AND", "TOPLEVEL", "LAMBDA", "PROBABILITY", "DORMANCY", 
			"DIGIT", "NUMBER", "NAME", "IDENTIFIER", "COMMENT", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\17s\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3"+
		"\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13\6\13H\n"+
		"\13\r\13\16\13I\3\13\7\13M\n\13\f\13\16\13P\13\13\3\13\3\13\6\13T\n\13"+
		"\r\13\16\13U\5\13X\n\13\3\f\3\f\3\f\3\r\6\r^\n\r\r\r\16\r_\3\16\3\16\3"+
		"\16\3\16\7\16f\n\16\f\16\16\16i\13\16\3\16\3\16\3\16\3\16\3\16\3\17\3"+
		"\17\3\17\3\17\3g\2\20\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\13\27"+
		"\f\31\r\33\16\35\17\3\2\6\3\2\62;\4\2C\\c|\6\2\62;C\\aac|\5\2\13\f\17"+
		"\17\"\"\2w\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\3\37\3\2\2\2\5!\3\2\2\2\7#\3\2\2\2"+
		"\t&\3\2\2\2\13*\3\2\2\2\r\63\3\2\2\2\17:\3\2\2\2\21?\3\2\2\2\23D\3\2\2"+
		"\2\25W\3\2\2\2\27Y\3\2\2\2\31]\3\2\2\2\33a\3\2\2\2\35o\3\2\2\2\37 \7="+
		"\2\2 \4\3\2\2\2!\"\7?\2\2\"\6\3\2\2\2#$\7q\2\2$%\7t\2\2%\b\3\2\2\2&\'"+
		"\7c\2\2\'(\7p\2\2()\7f\2\2)\n\3\2\2\2*+\7v\2\2+,\7q\2\2,-\7r\2\2-.\7n"+
		"\2\2./\7g\2\2/\60\7x\2\2\60\61\7g\2\2\61\62\7n\2\2\62\f\3\2\2\2\63\64"+
		"\7n\2\2\64\65\7c\2\2\65\66\7o\2\2\66\67\7d\2\2\678\7f\2\289\7c\2\29\16"+
		"\3\2\2\2:;\7r\2\2;<\7t\2\2<=\7q\2\2=>\7d\2\2>\20\3\2\2\2?@\7f\2\2@A\7"+
		"q\2\2AB\7t\2\2BC\7o\2\2C\22\3\2\2\2DE\t\2\2\2E\24\3\2\2\2FH\5\23\n\2G"+
		"F\3\2\2\2HI\3\2\2\2IG\3\2\2\2IJ\3\2\2\2JX\3\2\2\2KM\5\23\n\2LK\3\2\2\2"+
		"MP\3\2\2\2NL\3\2\2\2NO\3\2\2\2OQ\3\2\2\2PN\3\2\2\2QS\7\60\2\2RT\5\23\n"+
		"\2SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2\2VX\3\2\2\2WG\3\2\2\2WN\3\2\2"+
		"\2X\26\3\2\2\2YZ\t\3\2\2Z[\5\31\r\2[\30\3\2\2\2\\^\t\4\2\2]\\\3\2\2\2"+
		"^_\3\2\2\2_]\3\2\2\2_`\3\2\2\2`\32\3\2\2\2ab\7\61\2\2bc\7,\2\2cg\3\2\2"+
		"\2df\13\2\2\2ed\3\2\2\2fi\3\2\2\2gh\3\2\2\2ge\3\2\2\2hj\3\2\2\2ig\3\2"+
		"\2\2jk\7,\2\2kl\7\61\2\2lm\3\2\2\2mn\b\16\2\2n\34\3\2\2\2op\t\5\2\2pq"+
		"\3\2\2\2qr\b\17\2\2r\36\3\2\2\2\t\2INUW_g\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}