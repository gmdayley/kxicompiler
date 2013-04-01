package gmd.uvsc.kxi.semantic;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 17, 2008
 * Time: 3:51:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class FunctionSAR extends ReferenceSAR{
    IdentifierSAR identifierSar;
    ArgumentListSAR argumentListSar;

    public FunctionSAR(String symbolId) {
        super(symbolId);
    }

    public IdentifierSAR getIdentifierSar() {
        return identifierSar;
    }

    public void setIdentifierSar(IdentifierSAR identifierSar) {
        this.identifierSar = identifierSar;
    }

    public ArgumentListSAR getArgumentListSar() {
        return argumentListSar;
    }

    public void setArgumentListSar(ArgumentListSAR argumentListSar) {
        this.argumentListSar = argumentListSar;
    }
}
