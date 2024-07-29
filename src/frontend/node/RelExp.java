package frontend.node;

public class RelExp implements Node {
    private String op;
    private AddExp addExp;
    private RelExp relExp;

    public RelExp(AddExp a) {
        addExp = a;
        relExp = null;
        op = null;
    }

    public RelExp(RelExp r, String o, AddExp a) {
        op = o;
        addExp = a;
        relExp = r;
    }

    public RelExp getRelExp() {
        return relExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public String getOp() {
        return op;
    }
}
