package gmd.uvsc.kxi.semantic;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 11, 2008
 * Time: 6:59:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class TypeSAR extends SemanticActionRecord{
    private String type;

    public TypeSAR(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
