package frontend.node;

public class Token {
    private String type;
    private String value;
    private int line;

    public Token(String type, String value, int l) {
        this.type = type;
        this.value = value;
        this.line = l;
    }

    public String getType() {
        return this.type;
    }

    public int getLine() {
        return line;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return this.type + " " + this.value + "\n";
    }
}
