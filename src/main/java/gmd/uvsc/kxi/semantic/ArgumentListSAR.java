package gmd.uvsc.kxi.semantic;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 17, 2008
 * Time: 3:12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ArgumentListSAR extends SemanticActionRecord{
//    private List<String> arguments = new ArrayList<String>();
    private LinkedList<String> arguments = new LinkedList<String>();

    public void addArgument(String symbolId){
        //Need to add them in reverse since they were on a stack, and are being popped off
        arguments.addFirst(symbolId);
    }

    public List<String> getArguments() {
        return arguments;
    }
}
