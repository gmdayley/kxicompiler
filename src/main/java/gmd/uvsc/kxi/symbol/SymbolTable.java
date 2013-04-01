package gmd.uvsc.kxi.symbol;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Jan 28, 2008
 * Time: 5:54:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SymbolTable {
    private static final Logger logger = Logger.getLogger(SymbolTable.class);

    public static final String GLOBAL_SCOPE = "global";
    public static final String SCOPE_DELIM = ".";

    private StringBuffer currentScope;
    private Map<String, Map<String, Symbol>> table;
    private int scopeCount;

    public SymbolTable() {
        currentScope = new StringBuffer(GLOBAL_SCOPE);
        table = new HashMap<String, Map<String, Symbol>>();
        scopeCount = 0;
        table.put(GLOBAL_SCOPE, new HashMap<String, Symbol>());
    }

    public void pushScope(String scopeName){
        logger.debug("pushScope(), push: " + scopeName);
        //todo check for valid scope name
        currentScope.append(".");
        currentScope.append(scopeName);
//        currentScope.append(scopeCount);
        scopeCount++;

        logger.debug("scope after push: " + currentScope);
    }

    public void popScope(){
        logger.debug("popScope()");
        if(!GLOBAL_SCOPE.equalsIgnoreCase(currentScope.toString())){
            //todo check that current scope is valid (must contain . )
            currentScope.delete(currentScope.lastIndexOf("."), currentScope.length());
        }
        logger.debug("scope after pop: " + currentScope);
    }

    public String getCurrentScope(){
        return currentScope.toString();
    }

    public void add(String name, Symbol symbol){
        logger.debug("add()");
        logger.debug("scope: " + currentScope);
        logger.debug("name = " + name);
        logger.debug("symbol = " + symbol);

        if(!table.containsKey(currentScope.toString())){
            table.put(currentScope.toString(), new HashMap<String, Symbol>());
        }
        table.get(currentScope.toString()).put(name, symbol);
    }      

    //todo - dont add if it is already in there
    public void addGlobal(String name, Symbol symbol){
        logger.debug("addGlobal()");
        logger.debug("name = " + name);
        logger.debug("symbol = " + symbol);
      
        table.get(GLOBAL_SCOPE).put(name, symbol);
    }

    public Symbol find(String id) {
        logger.debug("find(), id: " + id);

        StringBuffer searchScope = new StringBuffer(currentScope);
        do{
            logger.debug("Searching scope: " + searchScope);
            Map<String, Symbol> map = table.get(searchScope.toString());
            if(map != null && map.containsKey(id)){
                logger.debug("Symbol found in scope: " + searchScope);
                return map.get(id);
            }
            int idx = searchScope.lastIndexOf(".");
            searchScope.delete(idx < 0 ? 0 : idx, searchScope.length());
        }
        while(searchScope.length() > 0);

        logger.debug("Symbol not found");
        return null;
    }

    public Symbol findAnywhere(String id) {
        for(String scope : table.keySet()){
            for(String symbolId : table.get(scope).keySet()){
                if(id.equals(symbolId)){
                    return table.get(scope).get(symbolId);
                }
            }
        }
        return null;
    }



    public Symbol findByValue(String value){
        StringBuffer searchScope = new StringBuffer(currentScope);
        do{
            logger.debug("Searching scope: " + searchScope);
            Map<String, Symbol> map = table.get(searchScope.toString());
            if(map != null){
                for(Symbol symbol : map.values()){
                    if(symbol.getValue() != null && symbol.getValue().equals(value)){
                        logger.debug("Symbol found in scope: " + searchScope);
                        return symbol;
                    }
                }
            }
            int idx = searchScope.lastIndexOf(".");
            searchScope.delete(idx < 0 ? 0 : idx, searchScope.length());
        }
        while(searchScope.length() > 0);
        
        logger.debug("Symbol not found");
        return null;    //not found
    }

    public List<Symbol> getAllSymbols(){
        List<Symbol> symbols = new LinkedList<Symbol>();

        for(String scopeName : table.keySet()){
            Map<String, Symbol> syms = table.get(scopeName);
            for (String id : syms.keySet()) {
                symbols.add(syms.get(id));
            }
        }
        return symbols;
    }

    public void printSymbolTable(PrintWriter ps){
        for (String scope : table.keySet()) {
            ps.println(">> " + scope);
            Map<String, Symbol> symbols = table.get(scope);
            for (String id : symbols.keySet()) {
                Symbol s = symbols.get(id);
                ps.println("\t" + s);
//                ps.printf("%s.%s[%s] = %s\n", scope, s.getName(), id, s.getValue());
            }
        }
    }


}
