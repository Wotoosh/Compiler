package frontend.node;

public class BType implements Node {
    private String type;

    public BType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }

    public void setType(String s) {
        type = s;
    }
}
