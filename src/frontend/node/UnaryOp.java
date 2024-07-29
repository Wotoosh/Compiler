package frontend.node;

public class UnaryOp implements Node {
    private String op;

    public UnaryOp(String s) {
        op = s;
    }

    public String getOp() {
        return op;
    }
}
