package frontend.node;

public class ForStmt implements Node {
    private LVal lVal;
    private Exp expr;

    public ForStmt(LVal l, Exp e) {
        lVal = l;
        expr = e;
    }

    public LVal getLVal() {
        return lVal;
    }

    public Exp getExp() {
        return expr;
    }
}
