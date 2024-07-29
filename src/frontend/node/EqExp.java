package frontend.node;

public class EqExp implements Node {
    private String op;
    private RelExp relExp;
    private EqExp eqExp;

    public EqExp(RelExp r) {
        relExp = r;
        eqExp = null;
        op = null;
    }

    public EqExp(EqExp e, String o, RelExp r) {
        op = o;
        relExp = r;
        eqExp = e;
    }

    public String getOp() {
        return op;
    }

    public RelExp getRelExp() {
        return relExp;
    }

    public EqExp getEqExp() {
        return eqExp;
    }
}
