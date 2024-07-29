package llvm.value.user.instruction;

import frontend.node.ConstDef;
import frontend.node.ConstExp;
import frontend.node.VarDef;
import llvm.MySymbolTable;
import llvm.type.ArrayType;
import llvm.type.IntegerType;
import llvm.type.Type;
import llvm.value.user.constant.globalobject.GlobalVariable;

import java.util.List;

public class GlobalAllocaInstruction extends AllocaInstruction {
    private GlobalVariable globalVariable;

    public GlobalAllocaInstruction(GlobalVariable globalVariable) {
        super();
        this.name = globalVariable.getName();
        this.elementType = globalVariable.getType();
        this.globalVariable = globalVariable;
        this.id = globalVariable.getId();
        this.isConst = globalVariable.getIsConst();
    }

    public GlobalAllocaInstruction(VarDef varDef, MySymbolTable s) {
        super();
        String name = varDef.getIdent().getName();
        Type t = new IntegerType();
        if (varDef.getConstExpr() != null) {
            List<ConstExp> constExps = varDef.getConstExpr();
            for (int i = constExps.size() - 1; i >= 0; i--) {
                t = new ArrayType(constExps.get(i).getVal(s), t);
            }
        }
        this.name = name;
        this.elementType = t;
        this.globalVariable = new GlobalVariable(name, t, varDef.getInitVal(), s);
        this.id = globalVariable.getId();
        this.isConst = globalVariable.getIsConst();
    }

    public GlobalAllocaInstruction(ConstDef constDef, MySymbolTable s) {
        super();
        String name = constDef.getIdent().getName();
        Type t = new IntegerType();
        if (constDef.getConstExps() != null) {
            List<ConstExp> constExps = constDef.getConstExps();
            for (int i = constExps.size() - 1; i >= 0; i--) {
                t = new ArrayType(constExps.get(i).getVal(s), t);
            }
        }
        this.name = name;
        this.elementType = t;
        this.globalVariable = new GlobalVariable(name, t, constDef.getConstInitVal(), s);
        this.id = globalVariable.getId();
        this.isConst = true;
    }

    public GlobalVariable getGlobalVariable() {
        return globalVariable;
    }

}
