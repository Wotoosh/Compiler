package frontend.node;

public class Ident implements Node {
    private String name;
    private int line;

    public Ident(String s, int l) {
        name = s;
        line = l;
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return line;
    }
}
