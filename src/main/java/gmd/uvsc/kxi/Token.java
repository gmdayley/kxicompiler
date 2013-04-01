package gmd.uvsc.kxi;

public class Token {
    private String lexeme;          //The string lexeme value of the token
    private TokenType type;         //The type of token found
    private int lineNumber;         //The line number that the token appears on

    public Token(String lexeme, TokenType type, int lineNumber) {
        this.lexeme = lexeme;
        this.type = type;
        this.lineNumber = lineNumber;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String toString(){
        return "TOKEN[" + type + ", " + lineNumber + "] = '"+ lexeme + "'";
    }
}