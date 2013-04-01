package gmd.uvsc.kxi;

import java.io.*;
import java.util.*;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import gmd.uvsc.kxi.symbol.SymbolTable;
import gmd.uvsc.kxi.symbol.Symbol;
import gmd.uvsc.kxi.semantic.*;
import gmd.uvsc.kxi.codegen.Quad;
import gmd.uvsc.kxi.codegen.CodeGenerator;

public class Parser {
    private static final Logger LOG = Logger.getLogger(Parser.class);
    private static final int SYNTAX_PASS = 1;
    private static final int SEMANTIC_PASS = 2;

    private static final String CONFIG_FILE = "config.properties";

    //Symbol Id Prefix(s)
    private static final String TEMP_PREFIX = "temp";
    private static final String VAR_PREFIX = "variable";
    private static final String LITERAL_PREFIX = "literal";
    private static final String CONSTRUCTOR_PREFIX = "constructor";
    private static final String CLASS_PREFIX = "class";
    private static final String FUNCTION_PREFIX = "function";

    //Misc Symbols
    private static final String LEFT_PAREN = "(";
    private static final String ARRAY_TYPE = "@";
    private static final String LEFT_BRACKET = "[";
    private static final String PARAM_TYPE_SEPERATOR = "_";

    //Types
    private static final String INT_TYPE = "int";
    private static final String BOOL_TYPE = "bool";
    private static final String VOID_TYPE = "void";
    private static final String CHAR_TYPE = "char";

    //Sizes
    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_CHAR = 1;
    public static final int SIZEOF_BOOL = 1;
    public static final int SIZEOF_REFERENCE = 4;

    //Modifier
    private static final String PUBLIC_MODIFIER = "public";

    private SymbolTable symbolTable = new SymbolTable();    //Symbol Table
    private Scanner scanner;                                //Tokenizer

    private Token currentToken;                             //Holds the current token being parsed
    private PrintStream out = System.out;                   //Used to write props out
    private int currentPass = SYNTAX_PASS;
    private boolean syntaxErrors = false;
    private String fileName;

    //The following are used during construction of the symbol table
    private Queue<Symbol> paramaterQueue = new LinkedList<Symbol>();
    private String lastType;            //holds the last type parsed
    private String lastIdentifier;      //holds the last identifier parsed
    private String lastModifier;        //holds the last modifier parsed

    private String lastClassName;       //holds the last class name parsed
    private int currentClassSize = 0;
    private String lastReturnType;      //holds the last return type parsed
    private String lastReturnSymbolId;

    private Stack<SemanticActionRecord> sas = new Stack<SemanticActionRecord>();    //Semantic Action Stack
    private Stack<Operator> os = new Stack<Operator>(); //Operator stack

    Properties props = new Properties();
    
    List<Quad> intCode = new LinkedList<Quad>();
    Stack<String> labelStack = new Stack<String>();
    Stack<String> labelStack2 = new Stack<String>();

    private boolean addLabel = false;

    public Parser(BufferedReader in){
        this.scanner = new Scanner(in);
        this.currentToken = scanner.nextToken();

        try {
            props.load(ClassLoader.getSystemResource(CONFIG_FILE).openStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(props.getProperty("startup.message"));
    }

    public Parser(String fileName){
        this.fileName = fileName;
        this.scanner = new Scanner(fileName);
        this.currentToken = scanner.nextToken();

         try {
            props.load(ClassLoader.getSystemResource(CONFIG_FILE).openStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(props.getProperty("startup.message"));
    }

    public void setOutputStream(PrintStream out){
        this.out = out;
    }

    public void parse(){
        LOG.debug("parse() called");
        syntaxPass();

        if(!syntaxErrors){
            semanticPass();


            cleanUpNoOps();
            //Print intermediatei
            for(Quad line : intCode){
                /*System.out.printf("%s %s %s %s %s",
                        line.getLabel(),
                        line.getInstruction(),
                        symbolTable.findAnywhere(line.getOperand1()).getValue(),
                        symbolTable.findAnywhere(line.getOperand2()).getValue(),
                        symbolTable.findAnywhere(line.getOperand3()).getValue());*/


                System.out.println(line);
            }

            printSymbolTable();
            printIntermediateCode();

            CodeGenerator cg = new CodeGenerator("out.asm");
            cg.generateTargetCode(intCode, symbolTable);
        }
    }

    public void syntaxPass(){
        currentPass = SYNTAX_PASS;
        parseCompilationUnit();

        if (!currentToken.getType().equals(TokenType.EOF)) {
            reportSyntaxError(currentToken, TokenType.EOF);
        }
    }

    public void semanticPass(){
        currentPass = SEMANTIC_PASS;
        scopeCount = 0;
        scanner = new Scanner(fileName);
        currentToken = scanner.nextToken();
        parseCompilationUnit();
    }


    protected void parseCompilationUnit() {
        LOG.debug("parseCompilationUnit()");


        if(isSemanticPass()){
            addQuad(new Quad(Quad.FRAME, "MAIN"));
            addQuad(new Quad(Quad.CALL, "MAIN"));
        }

        while (currentToken.getType().equals(TokenType.CLASS)) {
            LOG.debug("found start of class_declaration");
            parseClassDeclaration();
        }

        accept(TokenType.VOID);
        accept(TokenType.MAIN);
        accept(TokenType.PAREN_BEGIN);
        accept(TokenType.PAREN_END);
        if(isSemanticPass()){
            addQuad(new Quad("MAIN", Quad.NOP, Quad.EMPTY, Quad.EMPTY, Quad.EMPTY));
        }
        parseMethodBody();

        if(isSemanticPass()){
            //addQuad(new Quad(Quad.FRAME, "F_MAIN", Quad.EMPTY));
            //addQuad(new Quad(Quad.CALL, "F_MAIN"));
            addQuad(new Quad(Quad.EXIT, "0"));
        }
    }

    protected void parseMethodBody() {
        LOG.debug("parseMethodBody()");
        lastReturnSymbolId = "";

        accept(TokenType.BLOCK_BEGIN);

        //Here we need to peek ahead to determine which non-terminal to parse: variable_declaration or statement
        Token peekToken = scanner.peekToken();

        while (isType() && peekToken.getType().equals(TokenType.IDENTIFIER)) {
            LOG.debug("found start of variable_declaration");
            parseVariableDeclaration();
            peekToken = scanner.peekToken();
        }

        while (isStatement()) {
            LOG.debug("found start of statement");
            parseStatement();
        }

        accept(TokenType.BLOCK_END);
        symbolTable.popScope();

        if(isSemanticPass()){
            //addQuad(new Quad(Quad.RETURN, lastReturnSymbolId));
        }
    }

    protected void parseModifier() {
        LOG.debug("parseModifier()");

        lastModifier = currentToken.getType().getText();
        switch (currentToken.getType()) {
            case PRIVATE:
            case PUBLIC:
                accept();
                break;
            default:
                reportSyntaxError(currentToken, TokenType.PRIVATE, TokenType.PUBLIC);
        }
    }

    protected void parseStatement() {
        LOG.debug("parseStatement()");
        switch (currentToken.getType()) {
            case BLOCK_BEGIN:
                accept();
                symbolTable.pushScope(generateScopeName("block"));

                while (isStatement()) {
                    LOG.debug("found start of statement");
                    parseStatement();
                }
                accept(TokenType.BLOCK_END);
                symbolTable.popScope();
                break;
            case IF:
                accept();
                accept(TokenType.PAREN_BEGIN);
                parseExpression();
                accept(TokenType.PAREN_END);

                if (isSemanticPass()) {
                    //#eoe
                    eoe();

                    //#if
                    ReferenceSAR exp = (ReferenceSAR) sas.pop();
                    Symbol s = symbolTable.find(exp.getSymbolId());
                    if (!s.getType().equals(BOOL_TYPE)) {
                        reportSemanticError("illegal.boolean.expression", s.getType());
                    }

                    //Generate a label to jump to the end of the if
                    String label = generateSymbolId(Quad.LABEL_SKIPIF);
                    labelStack.push(label);
                    addQuad(new Quad(Quad.BRANCH_FALSE, s.getSymbolId(), label));
                }

                parseStatement();

                //hmm-mark where a skipelse would go, if an else was encountered later
                int elseMarker = intCode.size();

                //hmm - ugly, but it works :)
                if(isSemanticPass()){
                    //addQuad(new Quad(labelStack.pop(), "NOP", EMPTY, EMPTY, EMPTY));
                    intCode.add(new Quad(labelStack.pop(), Quad.NOP, Quad.EMPTY, Quad.EMPTY, Quad.EMPTY));
                }

                if (currentToken.getType().equals(TokenType.ELSE)) {
                    LOG.debug("found start of else");
                    accept();
                    parseStatement();

                    if(isSemanticPass()){
                        String label = generateSymbolId(Quad.LABEL_SKIPELSE);
                        labelStack.push(label);
                        intCode.add(elseMarker, new Quad(Quad.JUMP, label));
                        checkForLabel(true);
                    }
                }
                break;
            case WHILE:
                accept();
                accept(TokenType.PAREN_BEGIN);
                parseExpression();
                accept(TokenType.PAREN_END);

                if (isSemanticPass()) {
                    //#eoe
                    eoe();

                    //#while
                    ReferenceSAR exp = (ReferenceSAR) sas.pop();
                    Symbol s = symbolTable.find(exp.getSymbolId());
                    if (!s.getType().equals(BOOL_TYPE)) {
                        reportSemanticError("illegal.boolean.expression", s.getType());
                    }

                    //add label to last quad
                    String beginLabel = generateSymbolId(Quad.LABEL_BEGINWHILE);
                    addLabelToLastQuad(beginLabel);
                    String endLabel = generateSymbolId(Quad.LABEL_ENDWHILE);
                    labelStack.push(endLabel);
                    labelStack2.push(beginLabel);
                    addQuad(new Quad(Quad.BRANCH_FALSE, s.getSymbolId(), endLabel));
                }

                parseStatement();

                if(isSemanticPass()){
                     //intCode.add(new Quad(labelStack.pop(), "JMP", labelStack.peek(), EMPTY, EMPTY));
                    addQuad(new Quad(Quad.JUMP, labelStack2.pop()));
                    checkForLabel(true);
                }
                
                break;
            case RETURN:
                accept();

                boolean foundExpression = false;
                if (isExpression()) {
                    LOG.debug("found start of expression");
                    foundExpression = true;
                    parseExpression();
                }
                accept(TokenType.SEMICOLON);

                //#EOE
                if (isSemanticPass()) {
                    eoe();
                }

                //#return
                if (isSemanticPass()) {
                    if (foundExpression) {
                        ReferenceSAR refSar = (ReferenceSAR) sas.pop();
                        Symbol s = symbolTable.findAnywhere(refSar.getSymbolId());
                        if(!lastReturnType.equalsIgnoreCase(s.getType())){
                            reportSemanticError("incorrect.return.type", s.getType(), lastReturnType);
                        }

                        //Keep track of last returned symbol
                        lastReturnSymbolId = s.getSymbolId();
                    }
                    else {
                        //return type should be void
                        if (!lastReturnType.equals(VOID_TYPE)) {
                            reportSemanticError("incorrect.return.type", lastReturnType, VOID_TYPE);
                        }
                    }
                }

                break;
            case COUT:
                accept();
                accept(TokenType.DOUBLE_ARROW_LEFT);
                parseExpression();
                accept(TokenType.SEMICOLON);

                //#eoe
                if (isSemanticPass()) {
                    eoe();
                }

                //#cout
                if (isSemanticPass()) {
                    ReferenceSAR sar = (ReferenceSAR) sas.pop();
                    Symbol s = symbolTable.findAnywhere(sar.getSymbolId());

                    if(!s.getType().matches("int|char")){
                        reportSemanticError("illegal.cout.expression", s.getType());
                    }

                    addQuad(new Quad(Quad.WRITE, s.getType(), s.getSymbolId()));
                }

                break;
            case CIN:
                accept();
                accept(TokenType.DOUBLE_ARROW_RIGHT);
                parseExpression();
                accept(TokenType.SEMICOLON);

                //#eoe
                if (isSemanticPass()) {
                    eoe();
                }

                //#cin
                if (isSemanticPass()) {
                    ReferenceSAR sar = (ReferenceSAR) sas.pop();
                    Symbol s = symbolTable.findAnywhere(sar.getSymbolId());

                    if(!s.getType().matches("int|char")){
                        reportSemanticError("illegal.cin.expression", s.getType());
                    }

                    addQuad(new Quad(Quad.READ, s.getType(), s.getSymbolId()));
                }

                break;
            default:
                if (isExpression()) { //expression
                    LOG.debug("found start of expression");
                    parseExpression();
                    accept(TokenType.SEMICOLON);

                    //#EOE
                    if (isSemanticPass()) {
                        eoe();
                    }
                } else {
                    reportSyntaxError(currentToken,
                            TokenType.BLOCK_BEGIN,
                            TokenType.IF,
                            TokenType.WHILE,
                            TokenType.RETURN,
                            TokenType.COUT,
                            TokenType.CIN);
                }
        }
    }

    private void checkForLabel(boolean b) {
        if(isSemanticPass()){
            addLabel = b;
        }
    }


    protected void parseExpression() {
        LOG.debug("parseExpression()");
        switch (currentToken.getType()) {
            case PAREN_BEGIN:
                accept();

                //#oPush
                if (isSemanticPass()) {
                    oPush(createOperator(LEFT_PAREN));
                }

                parseExpression();
                accept(TokenType.PAREN_END);

                //#)
                if (isSemanticPass()) {
                    eop();
                }


                if (isExpressionZ()) {
                    LOG.debug("found start of expressionZ");
                    parseExpressionZ();
                }
                break;
            case TRUE:
            case FALSE:
            case NULL:
                accept();
                if (isExpressionZ()) {
                    LOG.debug("found start of expressionZ");
                    parseExpressionZ();
                }
                break;
            case NUMERIC_LITERAL:
                String numValue = currentToken.getLexeme();
                accept();

                //#lPush
                if (isSemanticPass()) {
                    Symbol numLiteral = symbolTable.findByValue(numValue);
                    sas.push(new LiteralSAR(numLiteral.getSymbolId()));
                }


                if (isExpressionZ()) {
                    LOG.debug("found start of expressionZ");
                    parseExpressionZ();
                }

                if (isSyntaxPass()) {
                    //hmm - first lets see if it is already there
                    Symbol sym = symbolTable.findByValue(numValue);
                    if(sym == null){
                        Symbol s = new Symbol();
                        s.setSymbolId(generateSymbolId(LITERAL_PREFIX));
                        s.setValue(numValue);
                        s.setType(INT_TYPE);
                        symbolTable.addGlobal(s.getSymbolId(), s);
                    }
                }

                break;

            case CHARACTER_LITERAL:
                String charValue = currentToken.getLexeme();
                accept();

                //#lPush
                if (isSemanticPass()) {
                    Symbol numLiteral = symbolTable.findByValue(charValue);
                    sas.push(new LiteralSAR(numLiteral.getSymbolId()));
                }

                if (isExpressionZ()) {
                    LOG.debug("found start of expressionZ");
                    parseExpressionZ();
                }

                if (isSyntaxPass()) {
                    Symbol sym = symbolTable.findByValue(charValue);
                        if(sym == null){
                        Symbol s = new Symbol();
                        s.setSymbolId(generateSymbolId(LITERAL_PREFIX));
                        s.setValue(charValue);
                        s.setType(CHAR_TYPE);
                        symbolTable.addGlobal(s.getSymbolId(), s);
                    }
                }

                break;
            case IDENTIFIER:
                parseIdentifier();

                //#iPush
                if (isSemanticPass()) {
                    sas.push(new IdentifierSAR(lastIdentifier));
                }

                if (isFnArrMember()) {
                    LOG.debug("found start of fn_arr_member");
                    parseFnArrMember();
                }

                //#iExist
                if (isSemanticPass()) {
                    //todo -implement
                    //Pop the top sar from the stack
                    //Can be a simple variable, function call, or an array reference
                    //Test to see if it exists in the current scope
                    //Push a sar onto the stack to indicate that the identifier exists

                    SemanticActionRecord sar = sas.pop();

                    if(sar instanceof IdentifierSAR){
                        //Found just an identifier, look in current scope
                        IdentifierSAR idSar = (IdentifierSAR) sar;
                        Symbol s = symbolTable.findByValue(idSar.getIdentifier());
                        if(s != null){//found it
                            sas.push(new ReferenceSAR(s.getSymbolId()));
                        }
                        else{
                            reportSemanticError("unknown.identifier", idSar.getIdentifier());
                        }
                    }
                    else if(sar instanceof ArrayReferenceSAR){
                        //Found an array reference
                        ArrayReferenceSAR arrSar = (ArrayReferenceSAR) sar;
                        Symbol s = symbolTable.findByValue(arrSar.getIdentifierSar().getIdentifier());
                        if(s!= null){
                            //Check if it of type array
                            String type = s.getType();
                            if(type.startsWith(ARRAY_TYPE)){
                                //Create a new temp symbol for this reference and push a ReferenceSAR on stack
                                Symbol t = new Symbol();
                                t.setSymbolId(generateSymbolId(TEMP_PREFIX));
                                t.setValue(t.getSymbolId());
                                t.setType(type.substring(type.indexOf(ARRAY_TYPE) + 1, type.length()));
                                symbolTable.add(t.getSymbolId(), t);
                                sas.push(new ReferenceSAR(t.getSymbolId()));
                            }
                            else{
                                reportSemanticError("type.mismatch.array", s.getValue());
                            }
                        }
                        else{
                            reportSemanticError("unknown.identifier", arrSar.getIdentifierSar().getIdentifier());
                        }
                    }
                    else if(sar instanceof FunctionSAR){
                        FunctionSAR funcSar = (FunctionSAR) sar;

                        String functionName = funcSar.getIdentifierSar().getIdentifier();
                        for(String arg :  funcSar.getArgumentListSar().getArguments()){
                            //These are symbolIds, so look up the symbol
                            Symbol a = symbolTable.findAnywhere(arg);
                            functionName += PARAM_TYPE_SEPERATOR + a.getType();
                        }

                        Symbol s = symbolTable.findByValue(functionName);
                        if(s != null){
                            Symbol t = new Symbol();
                            t.setSymbolId(generateSymbolId(TEMP_PREFIX));
                            t.setValue(t.getSymbolId());
                            t.setType(s.getReturnType());
                            symbolTable.add(t.getSymbolId(), t);
                            sas.push(new ReferenceSAR(t.getSymbolId()));
                        }
                        else{
                            reportSemanticError("unknown.function", functionName);
                        }
                    }
                }

                if (isMemberRefZ()) {
                    LOG.debug("found start of member_refZ");
                    parseMemberRefZ();
                }

                if (isExpressionZ()) {
                    LOG.debug("found start of expressionZ");
                    parseExpressionZ();
                }
                break;
            default:
                //todo fix error message to include other acceptable tokens
                reportSyntaxError(currentToken, TokenType.IDENTIFIER);
        }
    }

    protected void parseMemberRefZ() {
        LOG.debug("parseMemberRefZ()");

        accept(TokenType.PERIOD);
        parseIdentifier();

        //#iPush
        if (isSemanticPass()) {
            sas.push(new IdentifierSAR(lastIdentifier));
        }

        if (isFnArrMember()) {
            LOG.debug("found start of fn_arr_member");
            parseFnArrMember();
        }

        //#rExist
        if (isSemanticPass()) {
            SemanticActionRecord sar = sas.pop();
            ReferenceSAR refSar = (ReferenceSAR) sas.pop();
            Symbol r = symbolTable.find(refSar.getSymbolId());

            if(sar instanceof IdentifierSAR){
                IdentifierSAR idSar = (IdentifierSAR) sar;

                symbolTable.pushScope(r.getType());
                Symbol s = symbolTable.findByValue(idSar.getIdentifier());
                symbolTable.popScope();

                if(s != null){
                    if(PUBLIC_MODIFIER.equalsIgnoreCase(s.getModifier())){
                        sas.push(new ReferenceSAR(s.getSymbolId()));
                    }
                    else{
                        reportSemanticError("illegal.member.access");
                    }
                }
                else{
                    reportSemanticError("unknown.identifier", idSar.getIdentifier());
                }
            }
            else if(sar instanceof ArrayReferenceSAR){
                ArrayReferenceSAR arrSar = (ArrayReferenceSAR) sar;

                symbolTable.pushScope(r.getType());
                Symbol s = symbolTable.findByValue(arrSar.getIdentifierSar().getIdentifier());
                symbolTable.popScope();

                if(s != null){
                    if(PUBLIC_MODIFIER.equalsIgnoreCase(s.getModifier())){
                        String type = s.getType();
                        if(type.startsWith(ARRAY_TYPE)){
                            //Create a new temp symbol for this reference and push a ReferenceSAR on stack
                            Symbol t = new Symbol();
                            t.setSymbolId(generateSymbolId(TEMP_PREFIX));
                            t.setValue(t.getSymbolId());
                            t.setType(type.substring(type.indexOf(ARRAY_TYPE) + 1, type.length()));
                            symbolTable.add(t.getSymbolId(), t);
                            sas.push(new ReferenceSAR(t.getSymbolId()));
                        }
                        else{
                            reportSemanticError("type.mismatch.array", s.getValue());
                        }
                    }
                    else{
                        reportSemanticError("illegal.member.access");
                    }
                }
                else{
                    reportSemanticError("unknown.identifier", arrSar.getIdentifierSar().getIdentifier());
                }
            }
            else if(sar instanceof FunctionSAR){
                FunctionSAR funcSar = (FunctionSAR) sar;

                String functionName = funcSar.getIdentifierSar().getIdentifier();
                for(String arg :  funcSar.getArgumentListSar().getArguments()){
                    //These are symbolIds, so look up the symbol
                    Symbol a = symbolTable.findAnywhere(arg);
                    functionName += PARAM_TYPE_SEPERATOR + a.getType();
                }

                symbolTable.pushScope(r.getType());
                Symbol s = symbolTable.findByValue(functionName);
                symbolTable.popScope();

                if(s != null){
                    if(PUBLIC_MODIFIER.equalsIgnoreCase(s.getModifier())){
                        Symbol t = new Symbol();
                        t.setSymbolId(generateSymbolId(TEMP_PREFIX));
                        t.setValue(t.getSymbolId());
                        t.setType(s.getReturnType());
                        symbolTable.add(t.getSymbolId(), t);
                        sas.push(new ReferenceSAR(t.getSymbolId()));
                    }
                    else{
                        reportSemanticError("illegal.member.access");
                    }
                }
                else{
                    reportSemanticError("unknown.function", functionName);
                }
            }
        }

        if (isMemberRefZ()) {
            LOG.debug("found start of member_refZ");
            parseMemberRefZ();
        }
    }

    protected void parseFnArrMember() {
        LOG.debug("parseFnArrMember()");

        switch (currentToken.getType()) {
            case PAREN_BEGIN:
                accept();

                if (isArgumentList()) {
                    LOG.debug("found start of argument_list");
                    parseArgumentList();
                }
                else { //no arguments
                    if (isSemanticPass()) {
                        ArgumentListSAR argSar = new ArgumentListSAR();
                        sas.push(argSar);
                    }
                }

                accept(TokenType.PAREN_END);

                //#func
                if (isSemanticPass()) {
                    ArgumentListSAR argSar = (ArgumentListSAR) sas.pop();
                    IdentifierSAR idSar = (IdentifierSAR) sas.pop();

                    FunctionSAR funcSar = new FunctionSAR("bogus"); //todo - fix symbol id
                    funcSar.setArgumentListSar(argSar);
                    funcSar.setIdentifierSar(idSar);
                    sas.push(funcSar);
                }

                break;
            case ARRAY_BEGIN:
                accept();

                //#oPush
                if(isSemanticPass()){
                    oPush(createOperator(LEFT_BRACKET));
                }

                parseExpression();
                accept(TokenType.ARRAY_END);    //  ]

                //#]
                if (isSemanticPass()) {
                    eob();
                }


                //#arr
                if (isSemanticPass()) {
                    //Pop an expression from the stack
                    //Pop an identifier from the stack
                    //Test that the type of expression is an int
                    //Push a sar for the array ref onto the stack

                    ReferenceSAR idxSar = (ReferenceSAR) sas.pop();
                    IdentifierSAR idSar = (IdentifierSAR) sas.pop();

                    //Check that the array index is of type int
                    Symbol idxSymbol = symbolTable.find(idxSar.getSymbolId());
                    if(idxSymbol == null || !idxSymbol.getType().equalsIgnoreCase(INT_TYPE)){
                        reportSemanticError("illegal.index.type", idxSymbol.getType());
                    }

                    ArrayReferenceSAR arraySar = new ArrayReferenceSAR("bogus");    //todo - fix the symbol id
                    arraySar.setIdentifierSar(idSar);
                    arraySar.setReferenceSar(idxSar);
                    sas.push(arraySar);
                }

                break;
            default:
                reportSyntaxError(currentToken, TokenType.PAREN_BEGIN, TokenType.ARRAY_BEGIN);
        }
    }

    protected void parseArgumentList() {
        LOG.debug("parseArgumentList()");

        //#BAL
        if (isSemanticPass()) {
            sas.push(new BeginningArgumentListSAR());
        }

        //hmm - I am going to try to do a oPush (
        if(isSemanticPass()){
            oPush(createOperator(LEFT_PAREN));    
        }

        parseExpression();
        while (currentToken.getType().equals(TokenType.COMMA)) {
            //hmm - i need to do a
            if (isSemanticPass()) {
                eop();
            }

            LOG.debug("found start of expression");
            accept();
            parseExpression();
        }

        //hmm - do another
        if (isSemanticPass()) {
            eop();
        }



        //#EAL
        if (isSemanticPass()) {
            SemanticActionRecord top = sas.pop();
            ArgumentListSAR argSar = new ArgumentListSAR();

            while (!(top instanceof BeginningArgumentListSAR)) {
                ReferenceSAR ref = (ReferenceSAR) top;
                argSar.addArgument(ref.getSymbolId());
                top = sas.pop();
            }

            sas.push(argSar);
        }
    }

    protected void parseExpressionZ() {
        LOG.debug("parseExpressionZ()");
        Token tempToken = currentToken;

        switch (currentToken.getType()) {
            case ASSIGNMENT_OPERATOR:
                accept();

                //#oPush
                if (isSemanticPass()) {
                    oPush(createOperator(tempToken.getLexeme()));
                }

                parseAssignmentExpression();
                break;
            case BOOLEAN_OPERATOR:

                accept();

                //#oPush
                if (isSemanticPass()) {
                    oPush(createOperator(tempToken.getLexeme()));
                }

                parseExpression();
                break;
            case MATH_OPERATOR:
                accept();

                //#oPush
                if (isSemanticPass()) {
                    oPush(createOperator(tempToken.getLexeme()));
                }

                parseExpression();
                break;
            default:
                reportSyntaxError(currentToken, TokenType.ASSIGNMENT_OPERATOR, TokenType.BOOLEAN_OPERATOR, TokenType.MATH_OPERATOR);
        }
    }


    protected void parseVariableDeclaration() {
        LOG.debug("parseVariableDeclaration()");

        parseType();

        //#tExist
        if (isSemanticPass()) {
            SemanticActionRecord sar = sas.pop();
            TypeSAR type_sar = (TypeSAR) sar;

            //check to see it type really exists
            if (!type_sar.getType().matches("int|bool|void|char")) {    //todo - consider refactoring this regex
                Symbol s = symbolTable.findByValue(type_sar.getType());
                if (s == null || !s.getSymbolId().startsWith(CLASS_PREFIX)) {
                    reportSemanticError("unknown.type", type_sar.getType());
                }
            }
        }

        parseIdentifier();

        //hmm - added
        //#iPush
        if (isSemanticPass()) {
            sas.push(new IdentifierSAR(lastIdentifier));
        }

        //hmm - added
        //#iExist
        if(isSemanticPass()) {
            IdentifierSAR idSar = (IdentifierSAR) sas.pop();
            Symbol s = symbolTable.findByValue(idSar.getIdentifier());
            if(s == null){
                reportSemanticError("unknown.identifier", idSar.getIdentifier());
            }
            sas.push(new ReferenceSAR(s.getSymbolId()));
        }

        boolean isArray = false;
        if (currentToken.getType().equals(TokenType.ARRAY_BEGIN)) {
            LOG.debug("found start of array");
            isArray = true;
            accept();
            accept(TokenType.ARRAY_END);
        }

        if (isSyntaxPass()) {
            Symbol s = new Symbol();
            s.setSymbolId(generateSymbolId(VAR_PREFIX));
            s.setType(isArray ? ARRAY_TYPE + lastType : lastType);
            s.setValue(lastIdentifier);
            symbolTable.add(s.getSymbolId(), s);
        }

        if (currentToken.getType().equals((TokenType.ASSIGNMENT_OPERATOR))) {
            LOG.debug("found start of assignment_expression");
            accept();

            //hmm - this was added
            //#oPush
            if (isSemanticPass()) {
                oPush(createOperator("="));
            }

            parseAssignmentExpression();
        }
        accept(TokenType.SEMICOLON);

        //hmm - i added this, along with the #oPush
        if(isSemanticPass()){
            eoe();
        }
    }

    protected void parseIdentifier() {
        LOG.debug("parseIdentifier()");
        lastIdentifier = currentToken.getLexeme();
        accept(TokenType.IDENTIFIER);
    }

    protected void parseAssignmentExpression() {
        LOG.debug("parseAssignmentExpression");
        switch (currentToken.getType()) {
            case THIS:
                accept();
                break;
            case NEW:
                accept();
                parseType();
                parseNewDeclaration();
                break;
            case ATOI:
                accept();
                accept(TokenType.PAREN_BEGIN);

                //#oPush
                if(isSemanticPass()){
                    os.push(createOperator(LEFT_PAREN));
                }

                parseExpression();
                accept(TokenType.PAREN_END);

                //#)
                if (isSemanticPass()) {
                    eop();
                }


                //#atoi
                if (isSemanticPass()) {
                    //Pop an expression from the sas, test that the type can be converted to an int (probably a char)
//                    ReferenceSAR sar = (ReferenceSAR) sas.pop();
                    ReferenceSAR sar = (ReferenceSAR) sas.peek();
                    Symbol s = symbolTable.findAnywhere(sar.getSymbolId());
                    if(!s.getType().matches(CHAR_TYPE)){
                        reportSemanticError("illegal.atoi.expression", s.getType());
                    }

                    //hmm - shouldnt this push back onto the stack?
                    //I will just peek above
                }

                break;
            case ITOA:
                accept();
                accept(TokenType.PAREN_BEGIN);

                //#oPush
                if(isSemanticPass()){
                    os.push(createOperator(LEFT_PAREN));
                }

                parseExpression();
                accept(TokenType.PAREN_END);

                //#)
                if (isSemanticPass()) {
                    eop();
                }

                //#itoa
                if (isSemanticPass()) {
//                    ReferenceSAR sar = (ReferenceSAR) sas.pop();
                    ReferenceSAR sar = (ReferenceSAR) sas.peek();
                    Symbol s = symbolTable.findAnywhere(sar.getSymbolId());
                    if(!s.getType().matches(INT_TYPE)){
                        reportSemanticError("illegal.itoa.expression", s.getType());
                    }

                    //hmm - shouldnt this push back onto the stack?
                    //I will just peek above instead of pop
                }

                break;
            default:
                if (isExpression()) {
                    parseExpression();
                } else {
                    reportSyntaxError(currentToken, TokenType.THIS,
                            TokenType.NEW,
                            TokenType.ATOI,
                            TokenType.ITOA,
                            TokenType.PAREN_BEGIN,
                            TokenType.TRUE,
                            TokenType.FALSE,
                            TokenType.NULL,
                            TokenType.NUMERIC_LITERAL,
                            TokenType.IDENTIFIER);
                }
        }
    }

    protected void parseNewDeclaration() {
        LOG.debug("parseNewDeclaration");
        switch (currentToken.getType()) {
            case PAREN_BEGIN:
                accept();
                if (isArgumentList()) {
                    LOG.debug("found start of argument_list");
                    parseArgumentList();
                }
                else { //no arguments
                    if (isSemanticPass()) {
                        ArgumentListSAR argSar = new ArgumentListSAR();
                        sas.push(argSar);
                    }
                }

                accept(TokenType.PAREN_END);

                //#newObj
                if(isSemanticPass()){
                    //Pop an ArgumentListSAR from the stack
                    //Pop a TypeSAR from the stack
                    //Test to see a constructor exists that can create an instance of the type

                    ArgumentListSAR argSar = (ArgumentListSAR) sas.pop();
                    TypeSAR typeSar = (TypeSAR) sas.pop();

                    //Find symbol for class
                    Symbol clazz = symbolTable.findByValue(typeSar.getType());

                    if(clazz == null){
                        //type error
                        reportSemanticError("unknown.type", typeSar.getType());
                    }
                    

                    //Push scope
                    symbolTable.pushScope(typeSar.getType());

                    //Find symbol for constructor
                    String constructorName = clazz.getValue();

                    List<String> args = argSar.getArguments();

                    for(String arg : args){
                        //These are symbolIds, so look up the symbol
                        Symbol a = symbolTable.find(arg);
                        constructorName += PARAM_TYPE_SEPERATOR + a.getType();
                    }

                    Symbol constructor = symbolTable.findByValue(constructorName);

                    if(constructor == null){    //no matching constructor found
                        reportSemanticError("unknown.constructor", constructorName);
                    }

                    //Pop scope
                    symbolTable.popScope();

                    Symbol t = new Symbol();
                    t.setSymbolId(generateSymbolId(TEMP_PREFIX));
                    t.setType(constructor.getReturnType());
                    symbolTable.add(t.getSymbolId(), t);
                    sas.push(new ReferenceSAR(t.getSymbolId()));

                    //hmm-todo, implement frame, call
                    addQuad(new Quad("NEW", Integer.toString(clazz.getSizeof())));
                    addQuad(new Quad("FRAME", t.getSymbolId()));

                    for (String arg : args){
                        Symbol a = symbolTable.find(arg);
                        addQuad(new Quad("PUSH", a.getSymbolId()));
                    }
                    addQuad(new Quad("CALL", "???"));
                    addQuad(new Quad("POP", "???"));
                }

                break;
            case ARRAY_BEGIN:
                accept();

                //#oPush
                if(isSemanticPass()){
                    os.push(createOperator(LEFT_BRACKET));
                }

                parseExpression();
                accept(TokenType.ARRAY_END);

                //#]
                if (isSemanticPass()) {
                    eob();
                }

                //#new[]
                if (isSemanticPass()) {
                    //Pop an expression from the sas
                    ReferenceSAR ref = (ReferenceSAR) sas.pop();
                    Symbol refSymbol = symbolTable.find(ref.getSymbolId());

                    if (!refSymbol.getType().equals(INT_TYPE)) {
                        reportSemanticError("illegal.index.type", refSymbol.getType());
                    }

                    TypeSAR type = (TypeSAR) sas.pop();
                    //todo test if an array of this type can be generated

                    //hmm - I added this
                    Symbol t = new Symbol();
                    t.setSymbolId(generateSymbolId(TEMP_PREFIX));
                    t.setType(ARRAY_TYPE + type.getType());
                    symbolTable.add(t.getSymbolId(), t);
                    sas.push(new ReferenceSAR(t.getSymbolId()));
                }

                break;
            default:
                reportSyntaxError(currentToken, TokenType.PAREN_BEGIN, TokenType.ARRAY_BEGIN);
        }
    }

    protected void parseType() {
        LOG.debug("parseType()");
        //TODO - Is there a better way to handle parsing types?

        if (isType()) {
            if (currentToken.getType() == TokenType.IDENTIFIER) {
                lastType = currentToken.getLexeme();
            } else {
                lastType = currentToken.getType().getText();
            }
            accept();

            //#tPush
            if (isSemanticPass()) {
                sas.push(new TypeSAR(lastType));
            }

        } else {
            reportSyntaxError(currentToken, TokenType.INT, TokenType.CHAR, TokenType.BOOL, TokenType.VOID, TokenType.IDENTIFIER);
        }
    }

    protected void parseClassDeclaration() {
        LOG.debug("parseClassDeclaration()");

        accept(TokenType.CLASS);
        parseClassName();

        //Store this for semantic analysis
        lastClassName = lastIdentifier;
        currentClassSize = 0; //reset the current class size to zero

        Symbol s = new Symbol();
        if (isSyntaxPass()) {
            s.setSymbolId(generateSymbolId(CLASS_PREFIX));
            s.setValue(lastIdentifier);
            s.setReturnType(lastIdentifier);
        }

        symbolTable.pushScope(lastIdentifier);
        accept(TokenType.BLOCK_BEGIN);

        while (isClassMemberDeclaration()) {
            LOG.debug("found start of class_member_declaration");
            parseClassMemberDeclaration();
        }


        
        accept(TokenType.BLOCK_END);
        symbolTable.popScope();

        //hmm - save class size
        if(isSyntaxPass()){
            s.setSizeof(currentClassSize);
            symbolTable.add(s.getSymbolId(), s);
        }
    }

    protected void parseClassName() {
        LOG.debug("parseClassName()");
        parseIdentifier();
    }

    protected void parseClassMemberDeclaration() {
        LOG.debug("parseClassMemberDeclaration");
        switch (currentToken.getType()) {
            case PRIVATE:       //modifier
            case PUBLIC:        //modifier
                parseModifier();
                parseType();

                //Keep track of current class size
                if(isSyntaxPass()){
                    if(INT_TYPE.equalsIgnoreCase(lastType)){
                        currentClassSize += SIZEOF_INT;
                    }
                    else if(CHAR_TYPE.equalsIgnoreCase(lastType)){
                        currentClassSize += SIZEOF_CHAR;
                    }
                    else if(BOOL_TYPE.equalsIgnoreCase(lastType)){
                        currentClassSize += SIZEOF_BOOL;
                    }
                    else{ //must be a reference
                        currentClassSize += SIZEOF_REFERENCE;
                    }
                }

                //#tExist
                if (isSemanticPass()) {
                    SemanticActionRecord sar = sas.pop();
                    TypeSAR type_sar = (TypeSAR) sar;

                    //check to see it type really exists
                    if (!type_sar.getType().matches("int|bool|void|char")) {    //todo - consider refactoring this regex
                        Symbol s = symbolTable.findByValue(type_sar.getType());
                        if (s == null || !s.getSymbolId().startsWith(CLASS_PREFIX)) {
                            reportSemanticError("unknown.type", type_sar.getType());
                        }
                    }
                }

                parseIdentifier();
                parseFieldDeclaration();
                break;
            case IDENTIFIER:    //constructor
                parseConstructorDeclaration();
                break;
            default:
                reportSyntaxError(currentToken, TokenType.PRIVATE, TokenType.PUBLIC, TokenType.IDENTIFIER);
        }
    }

    protected void parseFieldDeclaration() {
        LOG.debug("parseFieldDeclaration()");

        switch (currentToken.getType()) {
            case ARRAY_BEGIN:       //This is optional
                String ttType = lastType;

                accept();
                accept(TokenType.ARRAY_END);

                if (currentToken.getType() == TokenType.ASSIGNMENT_OPERATOR) {
                    accept();
                    parseAssignmentExpression();
                }

                accept(TokenType.SEMICOLON);

                if (isSyntaxPass()) {
                    Symbol s = new Symbol();
                    s.setSymbolId(generateSymbolId(VAR_PREFIX));
                    s.setValue(lastIdentifier);
                    s.setModifier(lastModifier);
                    s.setType(ARRAY_TYPE + ttType);
                    symbolTable.add(s.getSymbolId(), s);
                }
                break;
            case ASSIGNMENT_OPERATOR:
                LOG.debug("found start of assignment_expression");
                accept();

                if(isSyntaxPass()){
                    Symbol s = new Symbol();
                    s.setSymbolId(generateSymbolId(VAR_PREFIX));
                    s.setValue(lastIdentifier);
                    s.setModifier(lastModifier);
                    s.setType(lastType);
                    symbolTable.add(s.getSymbolId(), s);
                }

                parseAssignmentExpression();
                accept(TokenType.SEMICOLON);
                break;
            case SEMICOLON:
                accept(TokenType.SEMICOLON);

                if (isSyntaxPass()) {
                    Symbol s = new Symbol();
                    s.setSymbolId(generateSymbolId(VAR_PREFIX));
                    s.setModifier(lastModifier);
                    s.setType(lastType);
                    s.setValue(lastIdentifier);
                    symbolTable.add(s.getSymbolId(), s);
                }
                break;
            case PAREN_BEGIN:       //function
                String tempModifier = lastModifier;
                String tempType = lastType;
                String tempIdentifier = lastIdentifier;
                lastReturnType = lastType;

/*                Symbol s = new Symbol();
                if(isSyntaxPass()){
                    s.setSymbolId(generateSymbolId(FUNCTION_PREFIX));
                }*/


                //symbolTable.pushScope("method");
                symbolTable.pushScope(generateScopeName(lastIdentifier));

                accept();
                if (isType()) {
                    LOG.debug("found start of parameter_list");
                    parseParameterList();
                }
                accept(TokenType.PAREN_END);

                //Parse the body of the method.  This will also pop the latest scope
                parseMethodBody();

                if (isSyntaxPass()) {
                    Symbol s = new Symbol();
                    s.setSymbolId(generateSymbolId(FUNCTION_PREFIX));
                    s.setModifier(tempModifier);
                    s.setReturnType(tempType);

                    String paramString = "";
                    while (!paramaterQueue.isEmpty()) {
                        Symbol t = paramaterQueue.remove();
                        paramString += PARAM_TYPE_SEPERATOR + t.getType();
                    }
                    s.setParams(paramString);

                    //hmm - lets just make the value the name of the function plus its arg types
                    //hmm - ex:  add_int_int
                    //hmm - this will make it easier to look up a function and match its arguments
                    String value = tempIdentifier + paramString;
                    s.setValue(value);

                    symbolTable.add(s.getSymbolId(), s);
                }
                break;
            default:
                reportSyntaxError(currentToken, TokenType.ARRAY_BEGIN, TokenType.PAREN_BEGIN);
        }
    }

    protected void parseParameterList() {
        LOG.debug("parseParameterList()");
        parseParameter();
        while (currentToken.getType().equals(TokenType.COMMA)) {
            LOG.debug("found start of parameter");
            accept();
            parseParameter();
        }
    }


    protected void parseParameter() {
        LOG.debug("parseParameter()");

        parseType();

        //#tExist
        if (isSemanticPass()) {
            SemanticActionRecord sar = sas.pop();
            TypeSAR type_sar = (TypeSAR) sar;

            //check to see it type really exists
            if (!type_sar.getType().matches("int|bool|void|char")) {
                Symbol s = symbolTable.findByValue(type_sar.getType());
                if (s == null || !s.getSymbolId().startsWith(CLASS_PREFIX)) {
                    reportSemanticError("unknown.type", type_sar.getType());
                }
            }
        }

        parseIdentifier();

        boolean isArray = false;
        if (currentToken.getType().equals(TokenType.ARRAY_BEGIN)) {
            LOG.debug("found start of array");
            isArray = true;
            accept();
            accept(TokenType.ARRAY_END);
        }

        if (isSyntaxPass()) {
            Symbol s = new Symbol();
            s.setSymbolId(generateSymbolId(VAR_PREFIX));
            s.setType(isArray ? ARRAY_TYPE + lastType : lastType);
            s.setValue(lastIdentifier);
            symbolTable.add(s.getSymbolId(), s);
            paramaterQueue.add(s);
        }
    }

    protected void parseConstructorDeclaration() {
        LOG.debug("parseConstructorDeclaration()");

        parseClassName();   //This will store the last

        //#CD
        if (isSemanticPass()) {
            //Check that the constructor name matches the name of the class
            if (!lastClassName.equals(lastIdentifier)) {
                reportSemanticError("illegal.constructor.declaration",  lastClassName, lastIdentifier);
            }
        }

        String tempIdentifier = lastIdentifier;

        symbolTable.pushScope(lastIdentifier);

        accept(TokenType.PAREN_BEGIN);

        if (isType()) {   //parameter list is optional, so check if it exists
            LOG.debug("found start of parameter_list");
            parseParameterList();
        }

        accept(TokenType.PAREN_END);
        parseMethodBody();

        if (isSyntaxPass()) {
            Symbol s = new Symbol();
            s.setSymbolId(generateSymbolId(CONSTRUCTOR_PREFIX));
            s.setReturnType(tempIdentifier);
            s.setModifier(PUBLIC_MODIFIER);

            String paramString = "";
            while (!paramaterQueue.isEmpty()) {
                Symbol t = paramaterQueue.remove();
                paramString += PARAM_TYPE_SEPERATOR + t.getType();
            }
            s.setParams(paramString);


            //hmm - lets just make the value the name of the function plus its arg types
            //hmm - ex:  add_int_int
            //hmm - this will make it easier to look up a function and match its arguments
            String value = tempIdentifier + paramString;
            s.setValue(value);

            symbolTable.add(s.getSymbolId(), s);
        }
    }

    //TODO: instead of these is.. methods how about a special accept method that takes a list of acceptable types?
    //ISs
    private boolean isClassMemberDeclaration() {
        switch (currentToken.getType()) {
            case PRIVATE:
            case PUBLIC:
            case IDENTIFIER:
                return true;
        }
        return false;
    }

    private boolean isType() {
        switch (currentToken.getType()) {
            case INT:
            case CHAR:
            case BOOL:
            case VOID:
            case IDENTIFIER:
                return true;
        }
        return false;
    }





    /*--------------  Semantic Action Routines -------------------*/
    //#oPush
    private void oPush(Operator toAdd) {
        if (os.size() > 0) {
            Operator top = os.peek();

            if (toAdd.getInputPrecedence() < top.getOutputPrecedence()) {
                os.pop();
                checkOperation(top);
            }

            os.push(toAdd);
        }
        else {//nothing on the stack, so go ahead and push the operator on
            os.push(toAdd);
        }
    }

    //#)
    private void eop(){
        Operator top = os.pop();
        while (!top.getValue().equals(LEFT_PAREN)) {
            checkOperation(top);
            top = os.pop();
        }
    }

    //#]
    private void eob(){
        Operator top = os.pop();
        while (!top.getValue().equals(LEFT_BRACKET)) {
            checkOperation(top);
            top = os.pop();
        }
    }

    //#EOE
    private void eoe() {
        //empty the operator stack

        while (!os.isEmpty()) {
            Operator top = os.pop();
            checkOperation(top);
        }
    }

    private void checkOperation(Operator top) {
        ReferenceSAR op1 = (ReferenceSAR) sas.pop();
        ReferenceSAR op2 = (ReferenceSAR) sas.pop();
        Symbol s1 = symbolTable.find(op1.getSymbolId());
        Symbol s2 = symbolTable.find(op2.getSymbolId());

        String op = top.getValue();
        if("=".equals(op)){//#=
            checkAssignment(s1, s2);
        }
        else if("+".equals(op)){//#+
            checkAddition(s1, s2);
        }
        else if("-".equals(op)){//#-
            checkSubtraction(s1, s2);
        }
        else if("/".equals(op)){//#/
            checkDivision(s1, s2);
        }
        else if("*".equals(op)){//#*
            checkMultiplication(s1, s2);
        }
        else if("%".equals(op)){//#*
            checkModulus(s1, s2);
        }
        else if("<".equals(op)){//#<
            checkLessThan(s1, s2);
        }
        else if(">".equals(op)){//#>
            checkGreaterThan(s1, s2);
        }
        else if("<=".equals(op)){//#<=
            checkLessThanOrEqualTo(s1, s2);
        }
        else if(">=".equals(op)){//#>=
            checkGreaterThanOrEqualTo(s1, s2);
        }
        else if("==".equals(op)){//#==
            checkEqualTo(s1, s2);
        }
        else if("&&".equals(op)){//#&&
            checkAnd(s1, s2);
        }
        else if("||".equals(op)){//#||
            checkOr(s1, s2);
        }
        else if("!=".equals(op)){//#!=
            checkNotEqualTo(s1, s2);
        }
    }

    private void checkModulus(Symbol s, Symbol t) {
        //Check that both types are int(s)
        if((!s.getType().equals(INT_TYPE)) || (!t.getType().equals(INT_TYPE))){
            reportSemanticError("illegal.mathmatical.expression");
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(INT_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        addQuad(new Quad(Quad.MOD, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkNotEqualTo(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.NOT_EQUALS, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkOr(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.OR, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkAnd(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.AND, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkEqualTo(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.EQUALS, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkGreaterThanOrEqualTo(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.GREATER_EQUALS, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkLessThanOrEqualTo(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.LESS_EQUALS, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkGreaterThan(Symbol s, Symbol t) {
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.GREATER, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkLessThan(Symbol s, Symbol t){
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.boolean.expression", s.getType(), t.getType());
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(BOOL_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check the order of the operands
        addQuad(new Quad(Quad.LESS, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkAssignment(Symbol s, Symbol t){
        //Check to make sure that the types are equal
        if(!s.getType().equals(t.getType())){
            reportSemanticError("illegal.binary.expression", s.getType(), t.getType());
        }

        addQuad(new Quad(Quad.MOV, s.getSymbolId(), t.getSymbolId()));
//        addQuad(new Quad(Quad.MOV, t.getSymbolId(), t.getSymbolId()));
    }

    private void checkMultiplication(Symbol s, Symbol t){
        //Check that both types are int(s)
        if((!s.getType().equals(INT_TYPE)) || (!t.getType().equals(INT_TYPE))){
            reportSemanticError("illegal.mathmatical.expression");
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(INT_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        addQuad(new Quad(Quad.MUL, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkDivision(Symbol s, Symbol t) {
        //Check that both types are int(s)
        if((!s.getType().equals(INT_TYPE)) || (!t.getType().equals(INT_TYPE))){
            reportSemanticError("illegal.mathmatical.expression");
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(INT_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        //todo - check op order
        addQuad(new Quad(Quad.DIV, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkSubtraction(Symbol s, Symbol t) {
        //Check that both types are int(s)
        if((!s.getType().equals(INT_TYPE)) || (!t.getType().equals(INT_TYPE))){
            reportSemanticError("illegal.mathmatical.expression");
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(INT_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        addQuad(new Quad(Quad.SUB, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }

    private void checkAddition(Symbol s, Symbol t) {
        //Check that both types are int(s)
        if((!s.getType().equals(INT_TYPE)) || (!t.getType().equals(INT_TYPE))){
            reportSemanticError("illegal.mathmatical.expression");
        }

        Symbol result = new Symbol();
        result.setSymbolId(generateSymbolId(TEMP_PREFIX));
        result.setValue(result.getSymbolId());
        result.setType(INT_TYPE);
        symbolTable.add(result.getSymbolId(), result);
        sas.push(new ReferenceSAR(result.getSymbolId()));

        addQuad(new Quad(Quad.ADD, t.getSymbolId(), s.getSymbolId(), result.getSymbolId()));
    }





    /*--------------  End of Semantic Action Routines -----------------*/

    private void addQuad(Quad q){
        if(addLabel){
            q.setLabel(labelStack.pop());
            addLabel = false;
        }
        intCode.add(q);
    }

    private void addLabelToLastQuad(String beginLabel) {
        intCode.get(intCode.size()-1).setLabel(beginLabel);
    }


    private boolean isBooleanOperator(Operator op) {
        return op.getValue().matches("&&|\\|\\||==|!=|<=|>=|<|>");
    }

    private boolean isMathOperator(Operator op) {
        return op.getValue().matches("\\+|-|\\*|/|%");
    }

    private Operator createOperator(String operator) {
        int input = -1;
        int output = -1;

        if ("=".equalsIgnoreCase(operator)) {
            input = output = 1;
        } else if ("&&".equalsIgnoreCase(operator)) {
            input = output = 5;
        } else if ("||".equalsIgnoreCase(operator)) {
            input = output = 3;
        } else if ("==".equalsIgnoreCase(operator)) {
            input = output = 7;
        } else if ("!=".equalsIgnoreCase(operator)) {
            input = output = 7;
        } else if ("<=".equalsIgnoreCase(operator)) {
            input = output = 9;
        } else if (">=".equalsIgnoreCase(operator)) {
            input = output = 9;
        } else if ("<".equalsIgnoreCase(operator)) {
            input = output = 9;
        } else if (">".equalsIgnoreCase(operator)) {
            input = output = 9;
        } else if ("+".equalsIgnoreCase(operator)) {
            input = output = 11;
        } else if ("-".equalsIgnoreCase(operator)) {
            input = output = 11;
        } else if ("*".equalsIgnoreCase(operator)) {
            input = output = 13;
        } else if ("/".equalsIgnoreCase(operator)) {
            input = output = 13;
        } else if ("%".equalsIgnoreCase(operator)) {
            input = output = 13;
        } else if (LEFT_PAREN.equalsIgnoreCase(operator)) {
            input = 15;
            output = 2;
        } else if (LEFT_BRACKET.equalsIgnoreCase(operator)){
            input = 15;
            output = 2;
        } else {
            throw new CompilerException("Unknown Operator: " + operator);
        }

        return new Operator(operator, input, output);
    }


    //Accept Methods
    private void accept(TokenType expectedType) {
        LOG.debug("accept(): " + expectedType);

        if (currentToken.getType().equals(expectedType)) {
            currentToken = scanner.nextToken();
        }
        else {
            reportSyntaxError(currentToken, expectedType);
        }
    }

    private void accept() {
        LOG.debug("accept()");
        currentToken = scanner.nextToken();
    }

    //Methods that report props found
    private void reportSyntaxError(Token token, TokenType ... expectedTypes) {
        LOG.debug("reporting error");
        LOG.debug("syntax error occurred, parsedToken: " + token + ", expected: " + Arrays.toString(expectedTypes));

        syntaxErrors = true;

        out.println(MessageFormat.format(props.getProperty("syntax.error"),
                                        MessageFormat.format(props.getProperty("syntax.expected.token"), expectedTypes),
                                        currentToken.getLineNumber()));

        //throw new CompilerException("Syntax Error");
    }

    private void reportSemanticError(String message, Object ... a) {
        out.println(MessageFormat.format(props.getProperty("semantic.error"),
                                         MessageFormat.format(props.getProperty(message), a),
                                         currentToken.getLineNumber()));

        //throw new CompilerException("Semantic Error");
    }



    private boolean isArgumentList() {
        return isExpression();
    }

    private boolean isFnArrMember() {
        switch (currentToken.getType()) {
            case PAREN_BEGIN:
            case ARRAY_BEGIN:
                return true;
        }
        return false;
    }

    private boolean isMemberRefZ() {
        return currentToken.getType().equals(TokenType.PERIOD);
    }

    private boolean isExpressionZ() {
        switch (currentToken.getType()) {
            case ASSIGNMENT_OPERATOR:
            case BOOLEAN_OPERATOR:
            case MATH_OPERATOR:
                return true;
        }
        return false;
    }

    private boolean isExpression() {
        switch (currentToken.getType()) {
            case PAREN_BEGIN:
            case TRUE:
            case FALSE:
            case NULL:
            case NUMERIC_LITERAL:
            case CHARACTER_LITERAL:
            case IDENTIFIER:
                return true;
        }
        return false;
    }

    private boolean isStatement() {
        switch (currentToken.getType()) {
            case BLOCK_BEGIN:
            case IF:
            case WHILE:
            case RETURN:
            case COUT:
            case CIN:
                return true;
            default:
                if (isExpression()) {
                    return true;
                }
        }
        return false;
    }


    private static int count = 0;
    private String generateSymbolId(String kind) {
        return kind + count++;
    }

    private static int scopeCount = 0;
    private String generateScopeName(String name) {
        return name + scopeCount++;
    }


    private boolean isSyntaxPass() {
        return currentPass == SYNTAX_PASS;
    }

    private boolean isSemanticPass() {
        return currentPass == SEMANTIC_PASS;
    }


    //Getters, Setters
    public boolean hasErrors() {
        return syntaxErrors;
    }

    private void cleanUpNoOps(){
        for (int i=0; i < intCode.size(); i++) {
            Quad q = intCode.get(i);

            if(Quad.NOP.equalsIgnoreCase(q.getInstruction())){
                //get the next instruction
                Quad r = intCode.get(i+1);

                if(r.getLabel() != null && r.getLabel().length() > 0){
                    q.setInstruction(Quad.JUMP);
                    q.setOperand1(r.getLabel());
                    q.setOperand2(Quad.EMPTY);
                    q.setOperand3(Quad.EMPTY);
                }
                else{
                    r.setLabel(q.getLabel());
                    intCode.remove(i);
                }
            }
        }

    }


    private void printSymbolTable(){
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("st.debug"));
            symbolTable.printSymbolTable(pw);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally{
            pw.close();
        }

    }

    private void printIntermediateCode(){
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("ic.debug"));
           for (Quad q : intCode){
               pw.println(q);
           }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally{
            pw.close();
        }

    }

}
