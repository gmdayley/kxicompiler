package gmd.uvsc.kxi;

public class Operator {
    private String value;
    private int inputPrecedence;
    private int outputPrecedence;

    public Operator(String value) {
        this.value = value;
        this.inputPrecedence = 0;
        this.outputPrecedence = 0;
    }

    public Operator(String value, int precedence) {
        this.value = value;
        this.inputPrecedence = precedence;
        this.outputPrecedence = precedence;
    }

    public Operator(String value, int inputPrecedence, int outputPrecedence) {
        this.value = value;
        this.inputPrecedence = inputPrecedence;
        this.outputPrecedence = outputPrecedence;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getInputPrecedence() {
        return inputPrecedence;
    }

    public void setInputPrecedence(int inputPrecedence) {
        this.inputPrecedence = inputPrecedence;
    }

    public int getOutputPrecedence() {
        return outputPrecedence;
    }

    public void setOutputPrecedence(int outputPrecedence) {
        this.outputPrecedence = outputPrecedence;
    }

    public String toString() {
        return value + "(INPUT: " + inputPrecedence + ", OUTPUT: " + outputPrecedence + ")";
    }
}
