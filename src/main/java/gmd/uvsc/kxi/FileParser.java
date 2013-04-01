package gmd.uvsc.kxi;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 28, 2008
 * Time: 3:36:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileParser extends Parser{
    public FileParser(File file) throws FileNotFoundException {
        super(new BufferedReader(new FileReader(file)));
    }
}
