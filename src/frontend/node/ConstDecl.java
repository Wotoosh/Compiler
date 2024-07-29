package frontend.node;

import java.util.ArrayList;

public class ConstDecl implements Node {
    BType bType;
    ArrayList<ConstDef> constDefs;

    public ConstDecl(BType b, ArrayList<ConstDef> c) {
        bType = b;
        constDefs = c;
    }

    public ArrayList<ConstDef> getConstDefs() {
        return constDefs;
    }
}
