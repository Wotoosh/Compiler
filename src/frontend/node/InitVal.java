package frontend.node;

import java.util.ArrayList;

public class InitVal implements Node {
    private Exp exp;
    private ArrayList<InitVal> initVals;

    public InitVal(ArrayList<InitVal> ini) {
        if (ini.isEmpty()) {
            initVals = null;
        } else {
            initVals = ini;
        }
        exp = null;
    }

    public InitVal(Exp e) {
        exp = e;
        initVals = null;
    }

    public Exp getExp() {
        return exp;
    }

    public ArrayList<InitVal> getInitVals() {
        return initVals;
    }
}
