package gmd.uvsc.kxi;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Jan 14, 2008
 * Time: 7:13:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompilerException extends RuntimeException{
    public CompilerException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CompilerException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CompilerException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CompilerException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
