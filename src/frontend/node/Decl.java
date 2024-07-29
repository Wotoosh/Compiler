package frontend.node;

public class Decl implements Node {
    private ConstDecl constDecl;
    private VarDecl varDecl;

    public Decl(ConstDecl c) {
        constDecl = c;
        varDecl = null;
    }

    public Decl(VarDecl v) {
        constDecl = null;
        varDecl = v;
    }

    public ConstDecl getConstDecl() {
        return constDecl;
    }

    public VarDecl getVarDecl() {
        return varDecl;
    }

}
