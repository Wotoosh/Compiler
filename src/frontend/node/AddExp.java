package frontend.node;

import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import llvm.MySymbolTable;

import java.util.ArrayList;

public class AddExp implements Node {
    private AddExp addExp;
    private String op;
    private MulExp mulExp;

    public AddExp(AddExp a, String s, MulExp m) {
        addExp = a;
        op = s;
        mulExp = m;
    }

    public AddExp(MulExp m) {
        addExp = null;
        op = null;
        mulExp = m;
    }

    public SymbolType getSymbolType(ArrayList<SymbolTableCheck> s) {
        if (addExp == null) {
            return mulExp.getSymbolType(s);
        } else {
            return addExp.getSymbolType(s).compareTo(mulExp.getSymbolType(s)) >= 0 ?
                    addExp.getSymbolType(s) : mulExp.getSymbolType(s);
        }
    }

    public MulExp getMulExp() {
        return mulExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public String getOp() {
        return op;
    }

    public int getVal(MySymbolTable s) {
        if (addExp == null) {
            return mulExp.getVal(s);
        } else {
            if (op.equals("+")) {
                return addExp.getVal(s) + mulExp.getVal(s);
            } else {
                return addExp.getVal(s) - mulExp.getVal(s);
            }
        }
    }
}
