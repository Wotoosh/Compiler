package frontend.node;

import java.util.ArrayList;

public class ConstDef implements Node {
    Ident ident;
    ArrayList<ConstExp> constExps;
    ConstInitVal constInitVal;

    public ConstDef(Ident i, ArrayList<ConstExp> e, ConstInitVal c) {
        ident = i;
        constInitVal = c;
        if (e.isEmpty()) {
            constExps = null;
        } else {
            constExps = e;
        }
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }

    public Ident getIdent() {
        return ident;
    }

    public int getDim() {
        if (constExps == null) {
            return 0;
        } else {
            return constExps.size();
        }
    }

    public ConstExp getConstExp(int i) {
        return constExps.get(i);
    }
}
