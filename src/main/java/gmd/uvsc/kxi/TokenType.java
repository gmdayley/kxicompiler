package gmd.uvsc.kxi;

public enum TokenType {
    NUMERIC_LITERAL("NUMERIC_LITERAL"),
    CHARACTER_LITERAL("CHARACTER_LITERAL"),
    STRING_LITERAL("STRING_LITERAL"),
    IDENTIFIER("IDENTIFIER"),
    PUCTUATION("PUNCTUATION"),
    ATOI("atoi"),
    BOOL("bool"),
    CLASS("class"),
    CHAR("char"),
    CIN("cin"),
    COUT("cout"),
    ELSE("else"),
    IF("if"),
    INT("int"),
    ITOA("itoa"),
    FALSE("false"),
    MAIN("main"),
    NEW("new"),
    NULL("null"),
    OBJECT("object"),
    PUBLIC("public"),
    PRIVATE("private"),
    RETURN("return"),
    STRING("string"),
    THIS("this"),
    TRUE("true"),
    NOT("!"),
    VOID("void"),
    WHILE("while"),
    MATH_OPERATOR("{+, -, / , *, %}"),
    RELATIONAL_OPERATOR("RELATIONAL_OPERATOR"),
    BOOLEAN_OPERATOR("BOOLEAN_OPERATOR"),
    ASSIGNMENT_OPERATOR("="),
    DOUBLE_ARROW_LEFT("<<"),
    DOUBLE_ARROW_RIGHT(">>"),
    ARRAY_BEGIN("["),
    ARRAY_END("]"),
    BLOCK_BEGIN("{"),
    BLOCK_END("}"),
    PAREN_BEGIN("("),
    PAREN_END(")"),
    SEMICOLON(";"),
    COMMA(","),
    PERIOD("."),
    EOF("EOF"),
    EOL("EOL"),
    AND("&"),
    OR("|"),
    UNKNOWN("UNKNOWN");

    private String text;
    TokenType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
