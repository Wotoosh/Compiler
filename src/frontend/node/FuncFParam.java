package frontend.node;

import java.util.ArrayList;

public class FuncFParam implements Node {
    private BType bType;
    private Ident ident;
    private ArrayList<ConstExp> constExps;
    private int dim;

    public FuncFParam(BType b, Ident i, ArrayList<ConstExp> c, int d) {
        bType = b;
        ident = i;
        if (!c.isEmpty()) {
            constExps = c;
        } else {
            constExps = null;
        }
        dim = d;
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public Ident getIdent() {
        return ident;
    }

    public int getDim() {
        return dim;
    }

    public ConstExp getConstExp(int i) {
        return constExps.get(i);
    }
}
