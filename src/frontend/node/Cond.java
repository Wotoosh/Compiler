package frontend.node;

public class Cond implements Node {
    private LOrExp lOrExp;

    public Cond(LOrExp l) {
        lOrExp = l;
    }

    public LOrExp getLOrExp() {
        return lOrExp;
    }
}
