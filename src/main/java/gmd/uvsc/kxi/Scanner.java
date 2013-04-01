package gmd.uvsc.kxi;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;
import java.util.Queue;
import java.util.LinkedList;
import java.io.*;

public class Scanner {
    private static final Logger LOG = Logger.getLogger(Scanner.class);

    private BufferedReader in;
    private StreamTokenizer tokenizer;      //Used to parse the source file
    private Pattern keywordPattern;         //Regex pattern that matches keywords
    private Token currentToken;             //Holds the current token
    private String fileName;

    private Queue<Token> buffer;

    public Scanner(BufferedReader in){
        this.in = in;
        this.tokenizer = new StreamTokenizer(in);
        this.tokenizer.quoteChar('"');
        this.tokenizer.quoteChar('\'');
        this.tokenizer.ordinaryChar('-');
        this.tokenizer.ordinaryChar('/');
        this.tokenizer.ordinaryChar('.');
        this.tokenizer.slashSlashComments(true);
        //this.tokenizer.parseNumbers();

        this.buffer = new LinkedList<Token>();
        this.currentToken = null;

        //Load Patterns
        keywordPattern = Pattern.compile("atoi|bool|class|char|cin|cout|else|false|if|int|itoa|main|new|null|object|public|private|return|string|this|true|void|while");

        try {
            in.mark(50000);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

     public Scanner(String fileName){
         this.fileName = fileName;
         try {
            this.in = new BufferedReader(new FileReader(fileName));
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }

        this.tokenizer = new StreamTokenizer(in);
        this.tokenizer.quoteChar('"');
        this.tokenizer.quoteChar('\'');
        this.tokenizer.ordinaryChar('-');
        this.tokenizer.ordinaryChar('/');
        this.tokenizer.ordinaryChar('.');
        this.tokenizer.slashSlashComments(true);
         this.tokenizer.parseNumbers();

        this.buffer = new LinkedList<Token>();
        this.currentToken = null;

        //Load Patterns
        keywordPattern = Pattern.compile("atoi|bool|class|char|cin|cout|else|false|if|int|itoa|main|new|null|object|public|private|return|string|this|true|void|while");

        try {
            in.mark(50000);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset(){
       /* try {
            in.reset();
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/

        try {
            this.in = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    /**
     * Returns the current token, or in other words the last token consumed.
     *
     * @return
     */
    public Token getCurrentToken(){
        return currentToken;
    }

    /**
     * Consumes the next token and returns it.
     *
     * @return
     */
    public Token nextToken() {
//        if(bad){bad = false; return yuk;}
        if(!buffer.isEmpty()){
            return buffer.remove();
        }
        return nextToken(true);
    }

    /**
     * Reads the next token without consuming it.
     *
     * @return
     */
    public Token peekToken() {
        if(!buffer.isEmpty()){
            return buffer.peek();
        }
        return nextToken(false);
    }

    private Token nextToken(boolean consume) {
        LOG.debug("Reading next token: (consume: " + consume + ")");
        Token token = null;
        int found = 0;
        int next = 0;
        boolean wierd = false;

        try{
            found = tokenizer.nextToken();
            LOG.debug("Found: " + found);

            switch(found){
                case StreamTokenizer.TT_NUMBER: //Found a number
                    //token = new Token(Double.toString(tokenizer.nval), TokenType.NUMERIC_LITERAL, tokenizer.lineno());
                    token = new Token((int)tokenizer.nval + "", TokenType.NUMERIC_LITERAL, tokenizer.lineno());
                    break;
                case StreamTokenizer.TT_WORD:   //Found a string
                    //Is it a keyword?
                    if(keywordPattern.matcher(tokenizer.sval).matches()){
                        token = new Token(tokenizer.sval, TokenUtil.lookupTokenType(tokenizer.sval), tokenizer.lineno());
                    }
                    else{//Not a keyword, so its probably an identifier
                        token = new Token(tokenizer.sval, TokenType.IDENTIFIER, tokenizer.lineno());
                    }
                    break;
                case StreamTokenizer.TT_EOL:    //Found end of line
                    token = new Token("eol", TokenType.EOL, tokenizer.lineno());
                    break;
                case StreamTokenizer.TT_EOF:    //Found end of file
                    token = new Token("eof", TokenType.EOF, tokenizer.lineno());
                    break;
                case '=':
                    wierd = true;
                    next = tokenizer.nextToken();

                    switch(next){
                        case '=':
                            token = new Token("==", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        default:    //We got something else
                            token = new Token("=", TokenType.ASSIGNMENT_OPERATOR, tokenizer.lineno());
                            if(!consume){
                                buffer.add(token);
                            }
                            tokenizer.pushBack();   //Push the something else back on
                        }
                    break;
                case '!':
                    wierd = true;
                    next = tokenizer.nextToken();

                    switch(next){
                        case '=':
                            token = new Token("!=", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        default:    //We got something else
                            token = new Token("!", TokenType.NOT, tokenizer.lineno());
                            if(!consume){
                                buffer.add(token);
                            }
                            tokenizer.pushBack();   //Push the something else back on
                        }
                    break;
                case '&':
                    wierd = true;
                    next = tokenizer.nextToken();

                    switch(next){
                        case '&':
                            token = new Token("&&", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        default:    //We got something else
                            token = new Token("&", TokenType.AND, tokenizer.lineno());
                            if(!consume){
                                buffer.add(token);
                            }
                            tokenizer.pushBack();   //Push the something else back on
                        }
                    break;
                case '|':
                    wierd = true;
                    next = tokenizer.nextToken();

                    switch(next){
                        case '|':
                            token = new Token("||", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        default:    //We got something else
                            token = new Token("|", TokenType.OR, tokenizer.lineno());
                            if(!consume){
                                buffer.add(token);
                            }
                            tokenizer.pushBack();   //Push the something else back on
                        }
                    break;
                case '+':
                case '-':
                case '/':
                case '*':
                case '%':
                    token = new Token(String.valueOf((char)found), TokenType.MATH_OPERATOR, tokenizer.lineno());
                    break;
                case '[':
                    token = new Token(String.valueOf((char)found), TokenType.ARRAY_BEGIN, tokenizer.lineno());
                    break;
                case ']':
                    token = new Token(String.valueOf((char)found), TokenType.ARRAY_END, tokenizer.lineno());
                    break;
                case '{':
                    token = new Token(String.valueOf((char)found), TokenType.BLOCK_BEGIN, tokenizer.lineno());
                    break;
                case '}':
                    token = new Token(String.valueOf((char)found), TokenType.BLOCK_END, tokenizer.lineno());
                    break;
                case '(':
                    token = new Token(String.valueOf((char)found), TokenType.PAREN_BEGIN, tokenizer.lineno());
                    break;
                case ')':
                    token = new Token(String.valueOf((char)found), TokenType.PAREN_END, tokenizer.lineno());
                    break;
                case ';':
                    token = new Token(String.valueOf((char)found), TokenType.SEMICOLON, tokenizer.lineno());
                    break;
                case ',':
                    token = new Token(String.valueOf((char)found), TokenType.COMMA, tokenizer.lineno());
                    break;
                case '.':
                    token = new Token(".", TokenType.PERIOD, tokenizer.lineno());
                    break;
                case '"':
//                    token = new Token(tokenizer.sval, TokenType.STRING_LITERAL, tokenizer.lineno());
                    token = new Token(tokenizer.sval, TokenType.CHARACTER_LITERAL, tokenizer.lineno());
                    break;
                case '<':
                    wierd = true;
                    //We need to look ahead to see if its a << or <=
                    next = tokenizer.nextToken();

                    switch(next){
                        case '<':   //Found <<
                            token = new Token("<<", TokenType.DOUBLE_ARROW_LEFT, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        //dont pb at the end
                        case '=':   //Found <=
                            token = new Token("<=", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        default:    //We got something else
                            token = new Token("<", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){
                                buffer.add(token);
                            }
                            tokenizer.pushBack();   //Push the something else back on
                        }
                    break;
                case '>':
                    wierd = true;
                    next = tokenizer.nextToken();

                    switch(next){
                        case '>':
                            token = new Token(">>", TokenType.DOUBLE_ARROW_RIGHT, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        case '=':   //Found <=
                            token = new Token(">=", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){   //peeking
                                buffer.add(token);
                            }
                            break;
                        default:    //We got something else
                            token = new Token(">", TokenType.BOOLEAN_OPERATOR, tokenizer.lineno());
                            if(!consume){
                                buffer.add(token);
                            }
                            tokenizer.pushBack();   //Push the something else back on
                        }
                    break;
                case '\'':
                    token = new Token(tokenizer.sval, TokenType.CHARACTER_LITERAL, tokenizer.lineno());
                    break;
                default:
                    LOG.error("Found UNKNOWN token: " + token);
                    throw new CompilerException("Unknown token found:  '" + String.valueOf((char)found) + "' (line: " + tokenizer.lineno() + ")");
            }

        }
        catch(IOException ioe){
            LOG.debug("Error trying to call nextToken()", ioe);
            throw new CompilerException("Unable to read token from file", ioe);
        }

        if(consume){
            this.currentToken = token;
        }
        else{
            if(!wierd){
                tokenizer.pushBack();
            }
        }

        LOG.debug("Created token: " + token);
        return token;
    }
}
