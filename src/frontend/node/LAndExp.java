package frontend.node;

public class LAndExp implements Node {
    private EqExp eqExp;
    private LAndExp landExp;

    public LAndExp(EqExp e) {
        eqExp = e;
        landExp = null;
    }

    public LAndExp(LAndExp l, EqExp e) {
        eqExp = e;
        landExp = l;
    }

    public LAndExp getLAndExp() {
        return landExp;
    }

    public EqExp getEqExp() {
        return eqExp;
    }
}
