package gmd.uvsc.kxi;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;

import gmd.uvsc.kxi.Scanner;
import gmd.uvsc.kxi.Token;
import gmd.uvsc.kxi.TokenType;
import gmd.uvsc.kxi.TokenUtil;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Jan 7, 2008
 * Time: 8:04:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScannerTest extends TestCase {
    public static final String RESOURCE_PATH = "src/test/resources/";

    public void testDog() {
        Scanner scanner = null;// = new Scanner(RESOURCE_PATH + "kxisrc/scanner/dog.kxi");

        Token token = scanner.getCurrentToken();
        assertNull(token);

        token = scanner.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.PUBLIC, token.getType());

        token = scanner.peekToken();
        assertEquals(TokenType.CLASS, token.getType());

        token = scanner.getCurrentToken();
        assertEquals(TokenType.PUBLIC, token.getType());

        token = scanner.nextToken();
        assertEquals(TokenType.CLASS, token.getType());
    }

    public void testCMinus() {
        Scanner tokenizer = new Scanner(new BufferedReader(new StringReader("k = (k * -1);")));
        Token token = tokenizer.nextToken();

        while (token.getType() != TokenType.EOF) {
            System.out.println(token);
            token = tokenizer.nextToken();
        }
    }

    public void testDot() {
        Scanner tokenizer = new Scanner(new BufferedReader(new StringReader("frogger.croak()")));
        Token token = tokenizer.nextToken();

        while (token.getType() != TokenType.EOF) {
            System.out.println(token);
            token = tokenizer.nextToken();
        }
    }

    public void testFullDogFile() {
        Scanner tokenizer = null; //new Scanner(RESOURCE_PATH + "kxisrc/scanner/dog.kxi");
        Token token = tokenizer.nextToken();

        while (token.getType() != TokenType.EOF) {
            System.out.println(token);
            token = tokenizer.nextToken();
        }
    }

    public void testKeywords() {
        String[] keywords = "atoi,bool,class,char,cin,cout,else,false,if,int,itoa,main,new,null,object,public,private,return,string,this,true,void,while".split(",");

        for (String keyword : keywords) {
            Scanner scanner = new Scanner(new BufferedReader(new StringReader(keyword)));
            assertEquals(TokenUtil.lookupTokenType(keyword), scanner.nextToken().getType());
        }
    }

    public void testQuotedString() {
        Scanner scanner = new Scanner(new BufferedReader(new StringReader("String test = \"Hello World!!!\"")));
        Token token = scanner.nextToken();
        assertEquals(TokenType.IDENTIFIER, token.getType());

        scanner.nextToken();
        assertEquals(TokenType.IDENTIFIER, scanner.getCurrentToken().getType());

        scanner.nextToken();
        assertEquals(TokenType.ASSIGNMENT_OPERATOR, scanner.getCurrentToken().getType());

        scanner.nextToken();
        assertEquals(TokenType.STRING_LITERAL, scanner.getCurrentToken().getType());
        assertEquals("Hello World!!!", scanner.getCurrentToken().getLexeme());
        assertEquals(1, scanner.getCurrentToken().getLineNumber());
    }

    public void testExpression() {
        Scanner scanner = new Scanner(new BufferedReader(new StringReader("x = 42+y*36/z;")));
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.ASSIGNMENT_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.NUMERIC_LITERAL, scanner.nextToken().getType());
        assertEquals(TokenType.MATH_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.MATH_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.NUMERIC_LITERAL, scanner.nextToken().getType());
        assertEquals(TokenType.MATH_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.SEMICOLON, scanner.nextToken().getType());
        assertEquals(TokenType.EOF, scanner.nextToken().getType());
    }

    public void testBooleanOperator() {
        assertTokenIs("<", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs("<=", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs(">", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs("<=", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs("==", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs("&&", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs("||", TokenType.BOOLEAN_OPERATOR);
        assertTokenIs("&", TokenType.AND);
        assertTokenIs("|", TokenType.OR);
    }

    public void testParseNumber() {
        Scanner scanner = new Scanner(new BufferedReader(new StringReader(".test 5.5")));
        assertEquals(TokenType.PERIOD, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        System.out.println("scanner.peekToken() = " + scanner.peekToken());
        assertEquals(TokenType.NUMERIC_LITERAL, scanner.nextToken().getType());
        System.out.println("scanner.peekToken() = " + scanner.peekToken());
        assertEquals(TokenType.EOF, scanner.nextToken().getType());
    }

    public void testAssignment(){
        Scanner scanner = new Scanner(new BufferedReader(new StringReader("a = b;")));
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        
        assertEquals(TokenType.ASSIGNMENT_OPERATOR,scanner.peekToken().getType());
        assertEquals(TokenType.ASSIGNMENT_OPERATOR,scanner.peekToken().getType());
        assertEquals(TokenType.ASSIGNMENT_OPERATOR,scanner.peekToken().getType());

        assertEquals(TokenType.ASSIGNMENT_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.SEMICOLON, scanner.nextToken().getType());
    }

    public void testCharacterLiteral(){
        Scanner scanner =  new Scanner(new BufferedReader(new StringReader("'b' + \"hello\"")));
        Token token = scanner.nextToken();
        System.out.println("token = " + token);
        token = scanner.nextToken();
        System.out.println("token = " + token);
        token = scanner.nextToken();
        System.out.println("token = " + token);
    }

    private void assertTokenIs(String code, TokenType tokenType) {
        Scanner scanner = new Scanner(new BufferedReader(new StringReader(code)));
        assertEquals(tokenType, scanner.nextToken().getType());
    }

    public void testLT(){
        Scanner scanner = new Scanner(new BufferedReader(new StringReader("cout << x;")));
        assertEquals(TokenType.COUT, scanner.nextToken().getType());
        assertEquals(TokenType.DOUBLE_ARROW_LEFT, scanner.peekToken().getType());
        assertEquals(TokenType.DOUBLE_ARROW_LEFT, scanner.peekToken().getType());
        assertEquals(TokenType.DOUBLE_ARROW_LEFT, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.SEMICOLON, scanner.nextToken().getType());

       /* scanner = new Scanner(new BufferedReader(new StringReader("a <= b")));
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.peekToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.peekToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());


        scanner = new Scanner(new BufferedReader(new StringReader("a < b < c;")));
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.peekToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.peekToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.IDENTIFIER, scanner.nextToken().getType());
        assertEquals(TokenType.SEMICOLON, scanner.nextToken().getType());

        *//*scanner = new Scanner(new BufferedReader(new StringReader("&& false")));
        assertEquals(TokenType.BOOLEAN_OPERATOR, scanner.nextToken().getType());
        assertEquals(TokenType.FALSE, scanner.peekToken().getType());*//*

        scanner = new Scanner(new BufferedReader(new StringReader("<< false")));
        assertEquals(TokenType.DOUBLE_ARROW_LEFT, scanner.nextToken().getType());
        assertEquals(TokenType.FALSE, scanner.nextToken().getType());
*/


    }

}
