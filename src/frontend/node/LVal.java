package frontend.node;

import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import llvm.MySymbolTable;
import llvm.type.ArrayType;
import llvm.type.IntegerType;
import llvm.type.Type;
import llvm.value.Value;
import llvm.value.user.constant.globalobject.GlobalVariable;
import llvm.value.user.instruction.LocalAllocaInstruction;

import java.util.ArrayList;
import java.util.Map;

public class LVal implements Node {
    private Ident ident;
    private ArrayList<Exp> exprs;

    public LVal(Ident i, ArrayList<Exp> e) {
        ident = i;
        exprs = e;
    }

    public ArrayList<Exp> getExps() {
        return exprs;
    }

    public SymbolType getSymbolType(ArrayList<SymbolTableCheck> s) {
        SymbolType t = null;
        for (SymbolTableCheck item : s) {
            if (item.contains(ident, SymbolType.NormalInt)) {  //search var
                t = item.getSymbolInfo(ident).getSymbolType();
            }
        }
        if (exprs == null) {
            return t;
        } else {
            if (t == SymbolType.TwoDimInt) {
                if (exprs.size() == 2) {
                    return SymbolType.NormalInt;
                } else {
                    return SymbolType.OneDimInt;
                }
            } else if (t == SymbolType.OneDimInt) {
                if (exprs.size() == 1) {
                    return SymbolType.NormalInt;
                }
            }
            return SymbolType.NormalInt;
        }
    }

    public Ident getIdent() {
        return ident;
    }

    public int getDim() {
        if (exprs == null) {
            return 0;
        } else {
            return exprs.size();
        }
    }

    //仅考虑引用的变量可以是全局变量的情况
    public int getVal(MySymbolTable s) {
        Map<String, Value> map = s.getVariables();
        if (map.containsKey(ident.getName())) {
            Value v = map.get(ident.getName());
            int index1 = exprs != null && exprs.size() > 0 ? exprs.get(0).getVal(s) : 0;
            int index2 = exprs != null && exprs.size() > 1 ? exprs.get(1).getVal(s) : 0;
            if (v instanceof GlobalVariable) {
                GlobalVariable g=(GlobalVariable) v;
                return g.getConstVal( index1, index2);
            } else if (v instanceof LocalAllocaInstruction) {
                return ((LocalAllocaInstruction) v).getConstVal(index1, index2);
            }
        } else if (s.getFather() != null) {
            return getVal(s.getFather());
        }
        return 0;
    }

    public Type getType(MySymbolTable s) {
        if (getDim() == 0) {
            return new IntegerType();
        } else if (getDim() == 1) {
            return new ArrayType(exprs.get(0).getVal(s), new IntegerType());
        } else if (getDim() == 2) {
            return new ArrayType(exprs.get(0).getVal(s), new ArrayType(exprs.get(1).getVal(s), new IntegerType()));
        }
        return null;
    }
}
