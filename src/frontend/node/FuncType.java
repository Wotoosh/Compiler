package frontend.node;

public class FuncType implements Node {
    private String type;
    private String value;

    public FuncType(String s) {
        type = s;
        if (type.equals("VOIDTK")) {
            value = "void";
        } else {
            value = "int";
        }
    }

    public boolean hasReturn() {
        return !type.equals("VOIDTK");
    }

    public String getType() {
        return type;
    }
}
