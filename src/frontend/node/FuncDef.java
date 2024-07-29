package frontend.node;

public class FuncDef implements Node {
    private FuncType funcType;
    private Ident ident;
    private FuncFParams params;
    private Block block;

    public FuncDef(FuncType ft, Ident i, FuncFParams f, Block b) {
        funcType = ft;
        ident = i;
        params = f;
        block = b;
    }

    public FuncDef(FuncType ft, Ident i, FuncFParams f) {
        funcType = ft;
        ident = i;
        params = f;
        block = null;
    }

    public FuncFParams getFuncFParams() {
        return params;
    }

    public Ident getIdent() {
        return ident;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public Block getBlock() {
        return block;
    }

    public boolean hasReturn() {
        return funcType.hasReturn();
    }
}
