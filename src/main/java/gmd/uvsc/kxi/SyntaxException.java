package gmd.uvsc.kxi;

import gmd.uvsc.kxi.Token;
import gmd.uvsc.kxi.TokenType;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Jan 21, 2008
 * Time: 6:53:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SyntaxException extends CompilerException{
    private Token offender;
    private TokenType[] expected;

    public SyntaxException(Token offender, TokenType[] expected){
        super();
        this.offender = offender;
        this.expected = expected;
    }

    public SyntaxException(String message, Token token){
        super(message);
    }

    public String getMessage() {
        StringBuffer errorMessage = new StringBuffer();
        errorMessage.append("A syntax error was found: '");
        errorMessage.append(this.offender.getLexeme());
        errorMessage.append("' is not one of expected tokens: [");

        for (TokenType type : expected) {
            errorMessage.append(type.getText()).append(", ");
        }
        errorMessage.append("], (");
        errorMessage.append(offender.getLineNumber());
        errorMessage.append(")");
        return errorMessage.toString();
    }
}
