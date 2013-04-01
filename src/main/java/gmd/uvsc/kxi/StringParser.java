package gmd.uvsc.kxi;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 28, 2008
 * Time: 3:38:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringParser extends Parser{
    public StringParser(String code){
        super(new BufferedReader(new StringReader(code)));
    }
}
