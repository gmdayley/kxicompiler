package gmd.uvsc.kxi.codegen;

import gmd.uvsc.kxi.symbol.SymbolTable;
import gmd.uvsc.kxi.symbol.Symbol;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Mar 13, 2008
 * Time: 6:29:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class CodeGenerator {
    //public List<Quad> targetCode;

    private String fileName;

    public CodeGenerator(String fileName) {
        this.fileName = fileName;
    }

    public void generateTargetCode(List<Quad> quads, SymbolTable symbolTable){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(this.fileName));

            for(Symbol s : symbolTable.getAllSymbols()){
                writeInstruction(writer, s.getSymbolId(), ".INT", s.getSymbolId().startsWith("lit") ? s.getValue() : "0", "");
            }

            for(Quad quad : quads){
                if(Quad.MOV.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDA", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "STR", "R5", "<R6>");
                }
                else if(Quad.OR.equals(quad.getInstruction())){
                    //0 is true
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "MUL", "R5", "R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                    
                }
                else if(Quad.EQUALS.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "CMP", "R5", "R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.NOT_EQUALS.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "CMP", "R5", "R6");

                    writeInstruction(writer, "", "MUL", "R5", "R5");  //should give me either 1 or 0
                    writeInstruction(writer, "", "ADI", "R5", "#-1"); //shoulg give me either 0 or -1

                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.LESS.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "CMP", "R5", "R6");    //If this is less than it should return -1
                    writeInstruction(writer, "", "ADI", "R5", "#1");    //Add one to make it zero
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.LESS_EQUALS.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "ADI", "R6", "#1");    //This is to adjust for the equals
                    writeInstruction(writer, "", "CMP", "R5", "R6");    //If this is less than it should return -1
                    writeInstruction(writer, "", "ADI", "R5", "#1");    //Add one to make it zero
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.GREATER.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "CMP", "R5", "R6");        //If this is more than it should return 1
                    writeInstruction(writer, "", "ADI", "R5", "#-1");       //Subtract one to make it zero
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.GREATER_EQUALS.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "ADI", "R6", "#-1");   //This is to adjust for the equals
                    writeInstruction(writer, "", "CMP", "R5", "R6");    //If this is more than it should return 1
                    writeInstruction(writer, "", "ADI", "R5", "#-1");   //Subtract one to make it zero
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.BRANCH_FALSE.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, quad.getLabel(), "BNZ", "R5", quad.getOperand2());
                }
                else if(Quad.JUMP.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "JMP", quad.getOperand1(), "");
                }
                else if(Quad.ADD.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "ADD", "R5","R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.SUB.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "SUB", "R5","R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.MUL.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "MUL", "R5","R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.DIV.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "DIV", "R5","R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.MOD.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "LDR", "R5", quad.getOperand1());
                    writeInstruction(writer, "", "LDR", "R6", quad.getOperand2());
                    writeInstruction(writer, "", "MOD", "R5","R6");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand3());
                    writeInstruction(writer, "", "STR", "R5", "<R7>");
                }
                else if(Quad.WRITE.equals(quad.getInstruction())){
                    if(quad.getOperand1().equalsIgnoreCase("char")){
                        writeInstruction(writer, quad.getLabel(), "LDR", "R0", quad.getOperand2());
                        writeInstruction(writer, "", "TRP", "3", "");
                    }
                    else{//int
                        writeInstruction(writer, quad.getLabel(), "LDR", "R0", quad.getOperand2());
                        writeInstruction(writer, "", "TRP", "1", "");
                    }
                }
                else if(Quad.READ.equals(quad.getInstruction())){
                    //2 int
                    //4 char
                    writeInstruction(writer, quad.getLabel(), "TRP", "5", "");
                    writeInstruction(writer, "", "LDA", "R7", quad.getOperand2());
                    writeInstruction(writer, "", "STR", "R0", "<R7>");

                }
                else if(Quad.EXIT.equals(quad.getInstruction())){
                    writeInstruction(writer, quad.getLabel(), "TRP", "0", "");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            writer.close();
        }
    }

    private void writeInstruction(PrintWriter out, String label, String instr, String op1, String op2){
         out.printf("%-15s %-15s %-15s %-15s\n", label, instr, op1, op2);
    }

}
