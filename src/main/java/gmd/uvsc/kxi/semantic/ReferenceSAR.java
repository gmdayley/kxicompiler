package gmd.uvsc.kxi.semantic;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 11, 2008
 * Time: 6:58:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReferenceSAR extends SemanticActionRecord {
    private String symbolId;

    public ReferenceSAR(String symbolId) {
        this.symbolId = symbolId;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }
}
