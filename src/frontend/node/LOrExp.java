package frontend.node;

public class LOrExp implements Node {
    private LAndExp landExp;
    private LOrExp lorExp;

    public LOrExp(LAndExp l) {
        landExp = l;
        lorExp = null;
    }

    public LOrExp(LOrExp lor, LAndExp l) {
        landExp = l;
        lorExp = lor;
    }

    public LOrExp getLOrExp() {
        return lorExp;
    }

    public LAndExp getLandExp() {
        return landExp;
    }
}
