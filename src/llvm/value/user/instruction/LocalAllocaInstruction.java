package llvm.value.user.instruction;

import frontend.node.ConstInitVal;
import llvm.IdName;
import llvm.MySymbolTable;
import llvm.type.ArrayType;
import llvm.type.IntegerType;
import llvm.type.Type;
import llvm.value.Value;
import llvm.value.user.constant.Constant;
import llvm.value.user.constant.constantdata.ConstantDataArray;
import llvm.value.user.constant.constantdata.ConstantInt;

public class LocalAllocaInstruction extends AllocaInstruction {

    private Value constInitVal;

    public Value getConstInitVal() {
        return constInitVal;
    }

    public LocalAllocaInstruction(String s, Type t, boolean b, ConstInitVal initVal, MySymbolTable symbolTable) {
        super();
        this.elementType = t;
        this.name = s;
        this.id = "%" + IdName.tmpVar + allocID();
        this.isConst = b;
        constInitVal = null;
        if (initVal != null) {    //有初始化值
            if (initVal.getConstInitVals() != null) {
                ConstantDataArray c = new ConstantDataArray();
                for (ConstInitVal i : initVal.getConstInitVals()) {
                    if (i.getConstInitVals() != null) {
                        ConstantDataArray tmp = new ConstantDataArray();
                        for (ConstInitVal j : i.getConstInitVals()) {
                            Constant meta = new ConstantInt(j.getConstExp().getVal(symbolTable));//二维数组
                            tmp.add(meta);
                        }
                        c.add(tmp);
                    } else {
                        Constant meta = new ConstantInt(i.getConstExp().getVal(symbolTable));//二维数组
                        c.add(meta);  //一维数组
                    }
                }
                constInitVal = c;
            } else {
                constInitVal = new ConstantInt(initVal.getConstExp().getVal(symbolTable));    //integer 常量
            }
        }
    }

    public LocalAllocaInstruction(String s, Type t, boolean b) {
        super();
        this.elementType = t;
        this.name = s;
        this.id = "%" + IdName.tmpVar + allocID();
        this.isConst = b;
        constInitVal = null;
    }

    @Override
    public String toString() {
        return id + " = alloca " + elementType + "\n";
    }

    public int getConstVal(int index1, int index2) {
        if (elementType instanceof IntegerType) {      //为常数
            return ((ConstantInt) constInitVal).getNum();
        } else {
            Type tmp = ((ArrayType) elementType).getElementType();
            Constant constant1 = ((ConstantDataArray) constInitVal).getIndex(index1);
            if (tmp instanceof IntegerType) {         //一维数组求值
                return ((ConstantInt) constant1).getNum();
            } else {                                  //二维数组求值
                Constant constant2 = ((ConstantDataArray) constant1).getIndex(index2);
                return ((ConstantInt) constant2).getNum();
            }
        }
    }

}
