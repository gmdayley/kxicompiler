package gmd.uvsc.kxi;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;

import gmd.uvsc.kxi.Parser;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Jan 20, 2008
 * Time: 4:24:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParserTest extends TestCase {
    //TODO  - fix this

    public static final String RESOURCE_PATH = "src/test/resources/";

    private Parser parser(String code) {
        return new Parser(new BufferedReader(new StringReader(code)));
    }

    private boolean parseType(String code) {
        Parser p = parser(code);
        p.parseType();
        return p.hasErrors();
    }

    public void testParseType() {
        assertFalse(parseType("int"));
        assertFalse(parseType("char"));
        assertFalse(parseType("bool"));
        assertFalse(parseType("void"));
        assertFalse(parseType("test"));
        assertTrue(parseType("12345"));
    }

    private boolean parseClassName(String code) {
        Parser p = parser(code);
        p.parseClassName();
        return p.hasErrors();
    }

    public void testParseClassName() {
        assertFalse(parseClassName("Dog"));
        assertTrue(parseClassName("int"));
    }

    private boolean parseModifier(String code) {
        Parser p = parser(code);
        p.parseModifier();
        return p.hasErrors();
    }

    public void testParseModifier() {
        assertFalse(parseModifier("public"));
        assertFalse(parseModifier("private"));
        assertTrue(parseModifier("{"));
    }

    private boolean parseClassDeclarartion(String code){
        Parser p = parser(code);
        p.parseClassDeclaration();
        return p.hasErrors();
    }

    public void testParseClassDeclaration(){
        assertFalse(parseClassDeclarartion("class Dog{}"));
//TODO        assertFalse(parseClassDeclarartion("class Dog {private int age = 14; private char name[] = \"Sparky\"; Dog(int age, char[] name)}"));
        assertFalse(parseClassDeclarartion("class Dog {private int age = 14; private char name[] = 14; Dog(int age, char name[]){} }"));
        assertTrue(parseClassDeclarartion("class Dog{Dog(){}"));
    }

    private boolean parseClassMemberDeclaration(String code){
        Parser p = parser(code);
        p.parseClassMemberDeclaration();
        return p.hasErrors();
    }

    public void testParseClassMemberDeclaration(){
        assertFalse(parseClassMemberDeclaration("private int age;"));
        assertFalse(parseClassMemberDeclaration("public void getAge(){}"));
        assertFalse(parseClassMemberDeclaration("Tree(){char leafs;}"));
        assertTrue(parseClassMemberDeclaration("int age;"));    //missing modifier
        assertTrue(parseClassMemberDeclaration("x=14"));        //x is an identifier, but is not followed by a (
    }

    private boolean parseFieldDeclaration(String code){
        Parser p = parser(code);
        p.parseFieldDeclaration();
        return p.hasErrors();
    }

    public void testParseFieldDeclaration(){
        assertFalse(parseFieldDeclaration("[];"));
        assertFalse(parseFieldDeclaration("= null;"));
        assertFalse(parseFieldDeclaration("[]=this;"));
        assertFalse(parseFieldDeclaration(";"));
        assertFalse(parseFieldDeclaration("(){}"));
        assertFalse(parseFieldDeclaration("(int age){int realAge = age+2;}"));
        assertTrue(parseFieldDeclaration("[23]"));  //should expect ]
        assertTrue(parseFieldDeclaration("="));     //missing assignment_expression 
    }


    private boolean parseConstructorDeclaration(String code){
        Parser p = parser(code);
        p.parseConstructorDeclaration();
        return p.hasErrors();
    }

    public void testParseConstructorDeclaration(){
        assertFalse(parseConstructorDeclaration("Automobile(){    }"));
        assertFalse(parseConstructorDeclaration("Automobile(int year, char make[], char model[]){}"));
        assertFalse(parseConstructorDeclaration("Automobile(int year, char make[], char model[]){int year = year; make = make; model = model; }"));

        assertTrue(parseConstructorDeclaration("true(){}"));
        assertTrue(parseConstructorDeclaration("Automobile[]"));
    }

    private boolean parseVariableDeclaration(String code){
        Parser p = parser(code);
        p.parseVariableDeclaration();
        return p.hasErrors();
    }

    public void testParseVariableDeclaration(){
        assertFalse(parseVariableDeclaration("int test = 2;"));
        assertFalse(parseVariableDeclaration("int year = year;"));
        assertFalse(parseVariableDeclaration("int children[];"));
        assertFalse(parseVariableDeclaration("int rate = (5 * (2 / age));"));
        assertTrue(parseVariableDeclaration("int children[] = ;"));
        assertTrue(parseVariableDeclaration("int test = 2"));   //missing semicolon
    }

    private boolean parseMethodBody(String code){
        Parser p = parser(code);
        p.parseMethodBody();
        return p.hasErrors();
    }

    public void testParseMethodBody(){
        assertFalse(parseMethodBody("{make2 == make;}"));
        assertFalse(parseMethodBody("{int year = year; make = make; model = model;}"));
        assertFalse(parseMethodBody("{bool yes = (1 == 2);}"));
        assertFalse(parseMethodBody("{int year;}"));

//        assertFalse(parseMethodBody("{if(true){}else{}}"));

    }

    private boolean parseAssignmentExpression(String code){
        Parser p = parser(code);
        p.parseAssignmentExpression();
        return p.hasErrors();
    }

    public void testParseAssignmentExpression(){
        assertFalse(parseAssignmentExpression("make = make;"));
        assertFalse(parseAssignmentExpression("this"));
        assertFalse(parseAssignmentExpression("new Dog(5, 7)"));
        assertFalse(parseAssignmentExpression("atoi(ten)"));
        assertFalse(parseAssignmentExpression("itoa(nine)"));

        //TODO add error conditions
    }

    private boolean parseNewDeclaration(String code){
        Parser p = parser(code);
        p.parseNewDeclaration();
        return p.hasErrors();
    }

    public void testParseNewDeclaration(){
        assertFalse(parseNewDeclaration("()"));
        assertFalse(parseNewDeclaration("(true, false)"));
        assertFalse(parseNewDeclaration("[null]"));
        assertTrue(parseNewDeclaration("[]"));
        assertTrue(parseNewDeclaration("({})")); //todo fix error message
    }

    private boolean parseArgumentList(String code){
        Parser p = parser(code);
        p.parseArgumentList();
        return p.hasErrors();
    }

    public void testParseArgumentList(){
        assertFalse(parseArgumentList("true, false, count"));
        assertFalse(parseArgumentList("dog"));
        assertFalse(parseArgumentList("(1+2))"));
        assertFalse(parseArgumentList("(1+2), false, null, (((test))))"));
        assertTrue(parseArgumentList("int"));
        assertTrue(parseArgumentList("1 = 3,"));
        assertTrue(parseArgumentList("yellow, class"));
    }


    private boolean parseExpressionZ(String code){
        Parser p = parser(code);
        p.parseExpressionZ();
        return p.hasErrors();
    }

    public void testParseExpressionZ(){
        assertFalse(parseExpressionZ("= this"));
        assertFalse(parseExpressionZ("&& false"));
        assertFalse(parseExpressionZ("|| null"));
        assertFalse(parseExpressionZ("== null"));
        assertFalse(parseExpressionZ("!= null"));
        assertFalse(parseExpressionZ("<= null"));
        assertFalse(parseExpressionZ(">= null"));
        assertFalse(parseExpressionZ("< 12"));
        assertFalse(parseExpressionZ("> 12"));
        assertFalse(parseExpressionZ("+ 44"));
//TODO        assertFalse(parseExpressionZ("- 'r'"));
        assertFalse(parseExpressionZ("* 3"));
        assertFalse(parseExpressionZ("/ false"));
        assertFalse(parseExpressionZ("% null"));

        assertTrue(parseExpressionZ("this"));
        assertTrue(parseExpressionZ("(=)"));
        assertTrue(parseExpressionZ(""));
    }

    private boolean parseExpression(String code){
        Parser p = parser(code);
        p.parseExpression();
        return p.hasErrors();
    }

    public void testParseExpression(){
        assertFalse(parseExpression("(true)=this"));
        assertFalse(parseExpression("(dog.ruff())"));
        assertFalse(parseExpression("true || false"));
        assertFalse(parseExpression("true"));
        assertFalse(parseExpression("false || (1 < 3)"));
        assertFalse(parseExpression("false"));
        assertFalse(parseExpression("null"));
        assertFalse(parseExpression("12"));
        assertFalse(parseExpression("12 + 13"));
//todo       assertFalse(parseExpression("'r' > 's"));
        assertFalse(parseExpression("x[3]"));
        assertFalse(parseExpression("x[3].getY()"));
        assertFalse(parseExpression("x[3].y"));
        assertFalse(parseExpression("x[3].y = z"));
        assertFalse(parseExpression("x().y().z()"));

        assertTrue(parseExpression("null && ((nu{}ll) == (false))"));
        assertTrue(parseExpression("null && ((null) == (false)"));
    }


    private boolean parseFnArrMember(String code){
        Parser p = parser(code);
        p.parseFnArrMember();
        return p.hasErrors();
    }

    public void testParseFnArrayMember(){
        assertFalse(parseFnArrMember("()"));
        assertFalse(parseFnArrMember("(cat)"));
        assertFalse(parseFnArrMember("[x]"));
        assertFalse(parseFnArrMember("[x + 3]"));

        assertTrue(parseFnArrMember("({})"));
        assertTrue(parseFnArrMember("[]"));
        assertTrue(parseFnArrMember("["));
        assertTrue(parseFnArrMember("[class]"));
    }


    private boolean parseMemberRefZ(String code){
        Parser p = parser(code);
        p.parseMemberRefZ();
        return p.hasErrors();
    }

    public void testParseMemberRefZ(){
        assertFalse(parseMemberRefZ(".elephant"));
        assertFalse(parseMemberRefZ(".elephant()"));
        assertFalse(parseMemberRefZ(".elephant(true)"));
        assertFalse(parseMemberRefZ(".elephant[x]"));
        
        assertTrue(parseMemberRefZ("elephant"));
        assertTrue(parseMemberRefZ(".true()"));
        assertTrue(parseMemberRefZ(".true()"));
    }

    private boolean parseStatement(String code){
        Parser p = parser(code);
        p.parseStatement();
        return p.hasErrors();
    }

    public void testParseStatement(){
        assertFalse(parseStatement("{if(true){}else{}}"));
        assertFalse(parseStatement("{if(true){return x;}else{return y;}}"));
        assertFalse(parseStatement("{if(true){x = 12; x + y; return next;}}"));
        assertFalse(parseStatement("while(x > 5){x = x - 1;}"));
        assertFalse(parseStatement("cout << hello;"));
        assertFalse(parseStatement("cin >> hello;"));

        assertTrue(parseStatement("int"));
        assertTrue(parseStatement("cout << hello"));
        assertTrue(parseStatement("{if(true){x = 12; x + y; return next;}"));
    }

    private boolean parseParameter(String code){
        Parser p = parser(code);
        p.parseParameter();
        return p.hasErrors();
    }

    public void testParseParameter(){
        assertFalse(parseParameter("int age"));
        assertFalse(parseParameter("int age[]"));
        assertFalse(parseParameter("Dog dog"));
        
        assertTrue(parseParameter("class num"));
        assertTrue(parseParameter("int age["));
    }

    private boolean parseParameterList(String code){
        Parser p = parser(code);
        p.parseParameterList();
        return p.hasErrors();
    }

    public void testParseParameterList(){
        assertFalse(parseParameterList("int count"));
        assertFalse(parseParameterList("int count, int next, int three, bool hasmore"));
        assertFalse(parseParameterList("Dog name"));

        assertTrue(parseParameterList("class something"));
        assertTrue(parseParameterList("\"\""));
    }

    public void testParserBird(){
        Parser p = null;//new Parser(RESOURCE_PATH + "kxisrc/parser/bird.kxi");
        p.syntaxPass();

        assertFalse(p.hasErrors());
    }

    /* public void testParserTest(){
        Parser p = new Parser("testparser.kxi");
        p.parse();

        assertFalse(p.hasErrors());
    }

    public void testParserTest2(){
        Parser p = new Parser("testsource/test2.kxi");
        p.parse();

        assertFalse(p.hasErrors());
    }*/
}

