package frontend.node;

import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import llvm.MySymbolTable;

import java.util.ArrayList;

public class PrimaryExp implements Node {
    private Exp exp;
    private LVal lVal;
    private MyNumber number;


    public PrimaryExp(Exp e) {
        exp = e;
        lVal = null;
        number = null;
    }

    public PrimaryExp(LVal l) {
        exp = null;
        lVal = l;
        number = null;
    }

    public PrimaryExp(MyNumber n) {
        exp = null;
        lVal = null;
        number = n;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Exp getExp() {
        return exp;
    }

    public SymbolType getSymbolType(ArrayList<SymbolTableCheck> s) {
        if (number != null) {
            return SymbolType.NormalInt;
        } else if (lVal != null) {
            return lVal.getSymbolType(s);
        } else {
            return exp.getSymbolType(s);
        }
    }

    public MyNumber getNumber() {
        return number;
    }

    public int getVal(MySymbolTable s) {
        if (number != null) {
            return number.getVal();
        } else if (lVal != null) {
            return lVal.getVal(s);
        } else {
            return exp.getVal(s);
        }
    }

}
