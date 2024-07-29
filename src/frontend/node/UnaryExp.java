package frontend.node;

import frontend.errorchecker.SymbolInfo;
import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import llvm.MySymbolTable;

import java.util.ArrayList;

public class UnaryExp implements Node {
    private PrimaryExp primaryExp;
    private Ident ident;
    private FuncRParams funcRParams;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;

    public UnaryExp(Ident i) {
        primaryExp = null;
        ident = i;
        funcRParams = null;
        unaryExp = null;
        unaryOp = null;
    }

    public UnaryExp(UnaryOp op, UnaryExp u) {
        primaryExp = null;
        ident = null;
        funcRParams = null;
        unaryExp = u;
        unaryOp = op;
//        if (u.unaryExp != null) {
//            if (op.getOp().equals("-") && u.unaryOp.getOp().equals("+")) {
//                unaryOp = new UnaryOp("-");
//                this.unaryExp = u.unaryExp;
//            } else if (op.getOp().equals("+") && u.unaryOp.getOp().equals("-")) {
//                unaryOp = new UnaryOp("-");
//                this.unaryExp = u.unaryExp;
//            } else if (unaryOp.getOp().equals(u.unaryOp.getOp())) {
//                unaryOp = new UnaryOp("+");
//                this.unaryExp = u.unaryExp;
//            }
//        }
    }

    public UnaryExp(PrimaryExp p) {
        primaryExp = p;
        ident = null;
        funcRParams = null;
        unaryExp = null;
        unaryOp = null;
    }

    public UnaryExp(Ident i, FuncRParams p) {
        primaryExp = null;
        ident = i;
        funcRParams = p;
        unaryExp = null;
        unaryOp = null;
    }

    public FuncRParams getFuncRParams() {
        return funcRParams;
    }

    @Override
    public String toString() {
        return (unaryOp != null ? unaryOp.getOp() : "") + (unaryExp == null ? "<primaryexp>" : unaryExp.toString());
    }

    public int getVal(MySymbolTable s) {
        if (primaryExp != null) {
            return primaryExp.getVal(s);
        } else if (unaryExp != null) {
            if (unaryOp.getOp().equals("-")) {
                return -unaryExp.getVal(s);
            } else if (unaryOp.getOp().equals("+")) {
                return unaryExp.getVal(s);
            } else if (unaryOp.getOp().equals("!")) {
                return unaryExp.getVal(s) > 0 ? 0 : 1;
            }
        } else {
            return -1;   //TODO函数调用
        }
        return -1;
    }

    public SymbolType getSymbolType(ArrayList<SymbolTableCheck> s) {
        if (primaryExp != null) {
            return primaryExp.getSymbolType(s);
        } else if (unaryExp != null) {
            return unaryExp.getSymbolType(s);
        } else {
            SymbolInfo symbolInfo = null;
            for (SymbolTableCheck item : s) {
                if (item.contains(ident)) {
                    symbolInfo = item.getSymbolInfo(ident);
                }
            }
            if (symbolInfo.getSymbolType() == SymbolType.IntFunc) {
                return SymbolType.NormalInt;
            }
            return SymbolType.VoidFunc;
        }
    }

    public Ident getIdent() {
        return ident;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }

    public String getUnaryOp() {
        return unaryOp.getOp();
    }
}
