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
		EQ=1, OR=2, AND=3, TOPLEVEL=4, LAMBDA=5, PROBABILITY=6, DORMANCY=7, NUMBER=8, 
		NAME=9, IDENTIFIER=10, COMMENT=11, WS=12;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"EQ", "OR", "AND", "TOPLEVEL", "LAMBDA", "PROBABILITY", "DORMANCY", "DIGIT", 
			"NUMBER", "NAME", "IDENTIFIER", "COMMENT", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\16o\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3"+
		"\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\n\6\nD\n\n\r\n\16\nE\3\n\7\n"+
		"I\n\n\f\n\16\nL\13\n\3\n\3\n\6\nP\n\n\r\n\16\nQ\5\nT\n\n\3\13\3\13\3\13"+
		"\3\f\6\fZ\n\f\r\f\16\f[\3\r\3\r\3\r\3\r\7\rb\n\r\f\r\16\re\13\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3c\2\17\3\3\5\4\7\5\t\6\13\7\r\b\17"+
		"\t\21\2\23\n\25\13\27\f\31\r\33\16\3\2\6\3\2\62;\4\2C\\c|\6\2\62;C\\a"+
		"ac|\5\2\13\f\17\17\"\"\2s\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2"+
		"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\3\35\3\2\2\2\5\37\3\2\2\2\7\"\3"+
		"\2\2\2\t&\3\2\2\2\13/\3\2\2\2\r\66\3\2\2\2\17;\3\2\2\2\21@\3\2\2\2\23"+
		"S\3\2\2\2\25U\3\2\2\2\27Y\3\2\2\2\31]\3\2\2\2\33k\3\2\2\2\35\36\7?\2\2"+
		"\36\4\3\2\2\2\37 \7q\2\2 !\7t\2\2!\6\3\2\2\2\"#\7c\2\2#$\7p\2\2$%\7f\2"+
		"\2%\b\3\2\2\2&\'\7v\2\2\'(\7q\2\2()\7r\2\2)*\7n\2\2*+\7g\2\2+,\7x\2\2"+
		",-\7g\2\2-.\7n\2\2.\n\3\2\2\2/\60\7n\2\2\60\61\7c\2\2\61\62\7o\2\2\62"+
		"\63\7d\2\2\63\64\7f\2\2\64\65\7c\2\2\65\f\3\2\2\2\66\67\7r\2\2\678\7t"+
		"\2\289\7q\2\29:\7d\2\2:\16\3\2\2\2;<\7f\2\2<=\7q\2\2=>\7t\2\2>?\7o\2\2"+
		"?\20\3\2\2\2@A\t\2\2\2A\22\3\2\2\2BD\5\21\t\2CB\3\2\2\2DE\3\2\2\2EC\3"+
		"\2\2\2EF\3\2\2\2FT\3\2\2\2GI\5\21\t\2HG\3\2\2\2IL\3\2\2\2JH\3\2\2\2JK"+
		"\3\2\2\2KM\3\2\2\2LJ\3\2\2\2MO\7\60\2\2NP\5\21\t\2ON\3\2\2\2PQ\3\2\2\2"+
		"QO\3\2\2\2QR\3\2\2\2RT\3\2\2\2SC\3\2\2\2SJ\3\2\2\2T\24\3\2\2\2UV\t\3\2"+
		"\2VW\5\27\f\2W\26\3\2\2\2XZ\t\4\2\2YX\3\2\2\2Z[\3\2\2\2[Y\3\2\2\2[\\\3"+
		"\2\2\2\\\30\3\2\2\2]^\7\61\2\2^_\7,\2\2_c\3\2\2\2`b\13\2\2\2a`\3\2\2\2"+
		"be\3\2\2\2cd\3\2\2\2ca\3\2\2\2df\3\2\2\2ec\3\2\2\2fg\7,\2\2gh\7\61\2\2"+
		"hi\3\2\2\2ij\b\r\2\2j\32\3\2\2\2kl\t\5\2\2lm\3\2\2\2mn\b\16\2\2n\34\3"+
		"\2\2\2\t\2EJQS[c\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}