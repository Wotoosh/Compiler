package frontend.node;

import java.util.ArrayList;

public class Stmt implements Node {
    private LVal lVal;
    private Exp exp;
    private Block block;
    private Cond cond;
    private Stmt ifStmt;
    private Stmt elseStmt;
    private ForStmt forStmt1;
    private ForStmt forStmt2;
    private Stmt forStmt;
    private FormatString formatString;
    private ArrayList<Exp> exps;
    private int type;
    private int line;

    public Stmt(int t, FormatString f, ArrayList<Exp> e) {
        type = t;
        if (e.isEmpty()) {
            exps = null;
        } else {
            exps = e;
        }
        formatString = f;
        lVal = null;
        exp = null;
        block = null;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public Stmt(int t, Exp e, int l) {
        type = t;
        exps = null;
        formatString = null;
        lVal = null;
        exp = e;
        block = null;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
        line = l;
    }

    public Stmt(int t, Exp e) {
        type = t;
        exps = null;
        formatString = null;
        lVal = null;
        exp = e;
        block = null;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public Stmt(int t) {
        type = t;
        exps = null;
        formatString = null;
        lVal = null;
        exp = null;
        block = null;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public Stmt(int t, ForStmt s1, Cond c, ForStmt s2, Stmt s3) {
        type = t;
        exps = null;
        formatString = null;
        lVal = null;
        exp = null;
        block = null;
        cond = c;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = s1;
        forStmt2 = s2;
        forStmt = s3;
    }

    public Stmt(int t, Cond c, Stmt s1, Stmt s2) {
        type = t;
        exps = null;
        formatString = null;
        lVal = null;
        exp = null;
        block = null;
        cond = c;
        ifStmt = s1;
        elseStmt = s2;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public Stmt(int t, Block b) {
        type = t;
        exps = null;
        formatString = null;
        lVal = null;
        exp = null;
        block = b;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public Stmt(int t, LVal l, Exp e) {
        type = t;
        exps = null;
        formatString = null;
        lVal = l;
        exp = e;
        block = null;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public Stmt(int t, LVal l) {
        type = t;
        exps = null;
        formatString = null;
        lVal = l;
        exp = null;
        block = null;
        cond = null;
        ifStmt = null;
        elseStmt = null;
        forStmt1 = null;
        forStmt2 = null;
        forStmt = null;
    }

    public FormatString getFormatString() {
        return formatString;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public ForStmt getForStmt1() {
        return forStmt1;
    }

    public ForStmt getForStmt2() {
        return forStmt2;
    }

    public Stmt getForStmt() {
        return forStmt;
    }

    public Stmt getIfStmt() {
        return ifStmt;
    }

    public Stmt getElseStmt() {
        return elseStmt;
    }

    public Cond getCond() {
        return cond;
    }

    public int getType() {
        return type;
    }

    public Exp getExp() {
        return exp;
    }

    public int getLine() {
        return line;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Block getBlock() {
        return block;
    }
}
