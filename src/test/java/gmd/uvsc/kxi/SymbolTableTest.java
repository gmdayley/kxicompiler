package gmd.uvsc.kxi;

import junit.framework.TestCase;

import gmd.uvsc.kxi.symbol.SymbolTable;
import gmd.uvsc.kxi.symbol.Symbol;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Jan 28, 2008
 * Time: 6:20:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class SymbolTableTest extends TestCase {


    public void test() {
        SymbolTable st = new SymbolTable();
        st.pushScope("one");    //global.one
        st.pushScope("two");    //global.one.two

        //Add local variable
        Symbol s = new Symbol();
        s.setSymbolId("sym1");
        s.setValue("x");
        st.add("sym1", s);

        assertNotNull(st.find("sym1"));
        assertNotNull(st.findByValue("x"));

        st.pushScope("three");  //global.one.two.three

        //Add Global Variable
        Symbol t = new Symbol();
        t.setSymbolId("g1");
        t.setValue("3.0");
        st.addGlobal("g1", t);

        assertNotNull(st.find("g1"));
        assertNotNull(st.find("sym1"));
        assertNotNull(st.findByValue("x"));

        st.popScope();  //global.one.two
        st.popScope();  //global.one

        assertNull(st.find("sym1"));
        assertNull(st.findByValue("x"));
        assertNotNull(st.findByValue("3.0"));

        st.popScope();  //global

        assertNull(st.find("sym1"));
        assertNull(st.findByValue("x"));
        assertNotNull(st.findByValue("3.0"));
    }
}
