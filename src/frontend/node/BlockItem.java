package frontend.node;

public class BlockItem implements Node {
    private Decl decl;
    private Stmt stmt;

    public BlockItem(Decl d) {
        decl = d;
        stmt = null;
    }

    public BlockItem(Stmt s) {
        decl = null;
        stmt = s;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public Decl getDecl() {
        return decl;
    }
}
