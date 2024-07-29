package frontend.node;

public class MyNumber implements Node {
    private String IntConst;

    public MyNumber(String s) {
        IntConst = s;
    }

    public String getNumber() {
        return IntConst;
    }

    public int getVal() {
        return Integer.parseInt(IntConst);
    }
}