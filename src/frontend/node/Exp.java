package frontend.node;

import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import llvm.MySymbolTable;

import java.util.ArrayList;

public class Exp implements Node {
    private AddExp addExp;

    public Exp(AddExp a) {
        addExp = a;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public SymbolType getSymbolType(ArrayList<SymbolTableCheck> s) {
        return addExp.getSymbolType(s);
    }

    public int getVal(MySymbolTable s) {
        return addExp.getVal(s);
    }
}
