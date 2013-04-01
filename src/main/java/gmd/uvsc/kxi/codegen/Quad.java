package gmd.uvsc.kxi.codegen;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Mar 4, 2008
 * Time: 11:39:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class Quad {
    public static final String BRANCH_FALSE = "BRANCH_FALSE";
    public static final String EQUAL = "EQUAL";
    public static final String EMPTY = "";
    public static final String WRITE = "WRITE";
    public static final String READ = "READ";
    public static final String LESS = "LESS";
    public static final String MOV = "MOV";
    public static final String MUL = "MUL";
    public static final String DIV = "DIV";
    public static final String SUB = "SUB";
    public static final String ADD = "ADD";
    public static final String GREATER = "GREATER";
    public static final String LESS_EQUALS = "LESS_EQUALS";
    public static final String GREATER_EQUALS = "GREATER_EQUALS";
    public static final String EQUALS = "EQUALS";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOT_EQUALS = "NOT_EQUALS";
    public static final String MOD = "MOD";
    public static final String JUMP = "JMP";
    public static final String FRAME = "FRAME";

    
    public static final String LABEL_SKIPIF = "SKIPIF_";
    public static final String LABEL_ENDWHILE = "ENDWHILE_";
    public static final String LABEL_SKIPELSE = "SKIPELSE_";
    public static final String LABEL_BEGINWHILE = "BEGINWHILE_";


    private String label;
    private String instruction;
    private String operand1;
    private String operand2;
    private String operand3;
    public static final String NOP = "NOP";
    public static final String EXIT = "EXIT";
    public static final String CALL = "CALL";
    public static final String RETURN = "RETURN";


    public Quad(String instruction, String operand1, String operand2, String operand3) {
        this.label = "";
        this.instruction = instruction;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
    }

    public Quad(String instruction, String operand1, String operand2) {
        this.label = "";
        this.instruction = instruction;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = EMPTY;
    }

     public Quad(String instruction, String operand1) {
        this.label = "";
        this.instruction = instruction;
        this.operand1 = operand1;
        this.operand2 = EMPTY;
        this.operand3 = EMPTY;
    }

     public Quad(String instruction) {
        this.label = "";
        this.instruction = instruction;
        this.operand1 = EMPTY;
        this.operand2 = EMPTY;
        this.operand3 = EMPTY;
    }

    public Quad(String label, String instruction, String operand1, String operand2, String operand3) {
        this.label = label;
        this.instruction = instruction;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
    }


    public String toString() {
        //return label + " " + instruction + " " + operand1 + " " + operand2 + " " + operand3;
        return String.format("%-15s %-15s %-15s %-15s %-15s", label, instruction, operand1, operand2, operand3);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getOperand1() {
        return operand1;
    }

    public void setOperand1(String operand1) {
        this.operand1 = operand1;
    }

    public String getOperand2() {
        return operand2;
    }

    public void setOperand2(String operand2) {
        this.operand2 = operand2;
    }

    public String getOperand3() {
        return operand3;
    }

    public void setOperand3(String operand3) {
        this.operand3 = operand3;
    }
}
