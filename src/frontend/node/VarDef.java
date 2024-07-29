package frontend.node;

import java.util.ArrayList;

public class VarDef implements Node {
    private Ident ident;
    private ArrayList<ConstExp> constExps;
    private InitVal initVal;

    public VarDef(Ident i, ArrayList<ConstExp> c, InitVal ini) {
        ident = i;
        if (c.isEmpty()) {
            constExps = null;
        } else {
            constExps = c;
        }
        initVal = ini;
    }

    public VarDef(Ident i, ArrayList<ConstExp> c) {
        ident = i;
        if (c.isEmpty()) {
            constExps = null;
        } else {
            constExps = c;
        }
        initVal = null;
    }

    public int getDim() {
        if (constExps == null) {
            return 0;
        } else {
            return constExps.size();
        }
    }

    public Ident getIdent() {
        return ident;
    }

    public ArrayList<ConstExp> getConstExpr() {
        return constExps;
    }

    public InitVal getInitVal() {
        return initVal;
    }

    public ConstExp getConstExp(int i) {
        return constExps.get(i);
    }
}
