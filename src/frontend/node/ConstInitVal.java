package frontend.node;

import java.util.ArrayList;

public class ConstInitVal implements Node {
    ConstExp constExp;
    ArrayList<ConstInitVal> constInitVals;

    public ConstInitVal(ArrayList<ConstInitVal> c) {
        if (c.isEmpty()) {
            constInitVals = null;
        } else {
            constInitVals = c;
        }
    }

    public ConstInitVal(ConstExp e) {
        constExp = e;
        constInitVals = null;
    }

    public ArrayList<ConstInitVal> getConstInitVals() {
        return constInitVals;
    }

    public ConstExp getConstExp() {
        return constExp;
    }
}
