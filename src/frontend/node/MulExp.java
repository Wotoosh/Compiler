package frontend.node;

import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import llvm.MySymbolTable;

import java.util.ArrayList;

public class MulExp implements Node {
    private String op;
    private UnaryExp unaryExp;
    private MulExp mulExp;

    public MulExp(MulExp m, String s, UnaryExp u) {
        op = s;
        unaryExp = u;
        mulExp = m;
    }

    public MulExp(UnaryExp u) {
        op = null;
        unaryExp = u;
        mulExp = null;
    }

    public SymbolType getSymbolType(ArrayList<SymbolTableCheck> s) {
        if (mulExp == null) {
            return unaryExp.getSymbolType(s);
        } else {
            return mulExp.getSymbolType(s).compareTo(unaryExp.getSymbolType(s)) >= 0 ? mulExp.getSymbolType(s) : unaryExp.getSymbolType(s);
        }
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }

    public MulExp getMulExp() {
        return mulExp;
    }

    public String getOp() {
        return op;
    }

    public int getVal(MySymbolTable s) {
        if (mulExp == null) {
            return unaryExp.getVal(s);
        } else {
            if (op.equals("*")) {
                return mulExp.getVal(s) * unaryExp.getVal(s);
            } else if (op.equals("/")) {
                return mulExp.getVal(s) / unaryExp.getVal(s);
            } else if (op.equals("%")) {
                return mulExp.getVal(s) % unaryExp.getVal(s);
            }
        }
        return -1;
    }

}
