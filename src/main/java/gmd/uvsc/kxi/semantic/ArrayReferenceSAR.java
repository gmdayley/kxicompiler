package gmd.uvsc.kxi.semantic;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 26, 2008
 * Time: 7:21:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ArrayReferenceSAR extends ReferenceSAR{
    IdentifierSAR identifierSar;        //Should hold the name of the array
    ReferenceSAR referenceSar;          //Should hold the index

    public ArrayReferenceSAR(String symbolId) {
        super(symbolId);
    }

    public IdentifierSAR getIdentifierSar() {
        return identifierSar;
    }

    public void setIdentifierSar(IdentifierSAR identifierSar) {
        this.identifierSar = identifierSar;
    }

    public ReferenceSAR getReferenceSar() {
        return referenceSar;
    }

    public void setReferenceSar(ReferenceSAR referenceSar) {
        this.referenceSar = referenceSar;
    }
}
