package gmd.uvsc.kxi.symbol;

public class Symbol {
    private String symbolId;
    private String value;
    private String type;
    private String modifier;
    private String returnType;
    private String params;
    private int sizeof;

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public String getValue() {
        return value;
        /*if(symbolId.startsWith("lit")){
            return value;
        }
        return "0";*/
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public int getSizeof() {
        return sizeof;
    }

    public void setSizeof(int sizeof) {
        this.sizeof = sizeof;
    }

    public String toString() {
        return "Symbol{" +
                "symbolId='" + symbolId + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", modifier='" + modifier + '\'' +
                ", returnType='" + returnType + '\'' +
                ", params='" + params + '\'' +
                ", sizeof='" + sizeof + '\'' +
                '}';
    }
}
