package frontend.node;

import java.util.ArrayList;

public class VarDecl implements Node {
    private BType bType;
    private ArrayList<VarDef> varDefs;

    public VarDecl(BType b, ArrayList<VarDef> v) {
        bType = b;
        varDefs = v;
    }

    public ArrayList<VarDef> getVarDefs() {
        return varDefs;
    }
}
