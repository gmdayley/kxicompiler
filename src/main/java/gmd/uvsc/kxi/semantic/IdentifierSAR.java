package gmd.uvsc.kxi.semantic;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 11, 2008
 * Time: 6:56:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class IdentifierSAR extends SemanticActionRecord {
    private String identifier;

    public IdentifierSAR(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
