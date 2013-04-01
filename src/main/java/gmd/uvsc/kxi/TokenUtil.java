package gmd.uvsc.kxi;

import java.util.Map;
import java.util.HashMap;

public class TokenUtil {
    private static Map<String, TokenType> keywordMap;

    static {
        keywordMap = new HashMap<String, TokenType>();
        keywordMap.put("while", TokenType.WHILE);
        keywordMap.put("if", TokenType.IF);
        keywordMap.put("else", TokenType.ELSE);
        keywordMap.put("atoi", TokenType.ATOI);
        keywordMap.put("bool", TokenType.BOOL);
        keywordMap.put("class", TokenType.CLASS);
        keywordMap.put("char", TokenType.CHAR);
        keywordMap.put("cin", TokenType.CIN);
        keywordMap.put("cout", TokenType.COUT);
        keywordMap.put("false", TokenType.FALSE);
        keywordMap.put("int", TokenType.INT);
        keywordMap.put("itoa", TokenType.ITOA);
        keywordMap.put("main", TokenType.MAIN);
        keywordMap.put("new", TokenType.NEW);
        keywordMap.put("null", TokenType.NULL);
        keywordMap.put("object", TokenType.OBJECT);
        keywordMap.put("public", TokenType.PUBLIC);
        keywordMap.put("private", TokenType.PRIVATE);
        keywordMap.put("return", TokenType.RETURN);
        keywordMap.put("string", TokenType.STRING);
        keywordMap.put("this", TokenType.THIS);
        keywordMap.put("true", TokenType.TRUE);
        keywordMap.put("void", TokenType.VOID);
    }

    public static TokenType lookupTokenType(String value) {
        return keywordMap.get(value);
    }
}
