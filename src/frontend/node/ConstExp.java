package frontend.node;

import llvm.MySymbolTable;

public class ConstExp {
    private AddExp addExp;

    public ConstExp(AddExp a) {
        addExp = a;
    }

    public int getVal(MySymbolTable s) {
        return addExp.getVal(s);
    }
}
