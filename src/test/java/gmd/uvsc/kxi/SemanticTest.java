package gmd.uvsc.kxi;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 17, 2008
 * Time: 6:16:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemanticTest extends TestCase {
    public static final String RESOURCE_PATH = "src/test/resources/kxisrc/semantic/";

    private void parseFile(String filename){
        try {
            Parser p = new FileParser(new File(RESOURCE_PATH + filename));
            p.parse();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testCMinus(){
            Parser p = new Parser("src/test/resources/kxisrc/final/cminus.kxi");
            p.parse();

    }

     public void testFrog(){
            Parser p = new Parser("src/test/resources/kxisrc/semantic/test.kxi");
            p.parse();

    }


    public void testIntermediateCode(){
//        parseFile("ic.kxi");
        parseFile("ic2.kxi");
    }

    public void testWorking(){
        parseFile("working.kxi");
    }

    public void testClass(){
        parseFile("class.kxi");
    }

    public void testRef(){
        parseFile("ref.kxi");
    }

    public void testArray(){
        parseFile("array.kxi");
    }

    public void test(){
       parseFile("semantic_one.kxi");
    }

}
