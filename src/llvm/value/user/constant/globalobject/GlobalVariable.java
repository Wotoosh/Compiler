package llvm.value.user.constant.globalobject;

import frontend.node.ConstInitVal;
import frontend.node.InitVal;
import llvm.IdName;
import llvm.MySymbolTable;
import llvm.type.ArrayType;
import llvm.type.IntegerType;
import llvm.type.Type;
import llvm.value.Value;
import llvm.value.user.constant.Constant;
import llvm.value.user.constant.constantdata.ConstantDataArray;
import llvm.value.user.constant.constantdata.ConstantInt;
import llvm.value.user.constant.constantdata.ConstantString;

public class GlobalVariable extends GlobalObject {
    private Value value;
    private String id;
    private boolean isConst;
    private boolean isZero;


    @Override
    public String getDef() {
        return id;
    }

    public Value getValue() {
        return value;
    }

    public boolean getIsZero() {
        return isZero;
    }

    public GlobalVariable(String s, int len) {
        ConstantString c = new ConstantString(s, len);
        this.value = c;
        this.elementType = c.getType();
        this.id = "@" + IdName.globalVar + allocID();
        isConst = true;
        isZero = false;
        name = s;
    }

    public GlobalVariable(String n, Type t, InitVal initVal, MySymbolTable s) {
        this.name = n;
        this.elementType = t;
        isZero = false;
        if (initVal != null) {    //有初始化值
            if (initVal.getInitVals() != null) {
                ConstantDataArray c = new ConstantDataArray();
                for (InitVal i : initVal.getInitVals()) {
                    if (i.getInitVals() != null) {
                        ConstantDataArray tmp = new ConstantDataArray();
                        for (InitVal j : i.getInitVals()) {
                            Constant meta = new ConstantInt(j.getExp().getVal(s));//二维数组
                            tmp.add(meta);
                        }
                        c.add(tmp);
                    } else {
                        Constant meta = new ConstantInt(i.getExp().getVal(s));//二维数组
                        c.add(meta);  //一维数组
                    }
                }
                value = c;
            } else {
                value = new ConstantInt(initVal.getExp().getVal(s));    //integer 常量
            }
        } else {
            if (elementType instanceof IntegerType) {
                value = new ConstantInt(0);    //integer 常量
            } else {
                isZero = true;
                value = zeroInitialize(elementType);
            }
        }
        this.id = "@" + IdName.globalVar + allocID();
        this.isConst = false;
    }

    public GlobalVariable(String n, Type t, ConstInitVal initVal, MySymbolTable s) {
        this.name = n;
        this.elementType = t;
        if (initVal != null) {    //有初始化值
            if (initVal.getConstInitVals() != null) {
                ConstantDataArray c = new ConstantDataArray();
                for (ConstInitVal i : initVal.getConstInitVals()) {
                    if (i.getConstInitVals() != null) {
                        ConstantDataArray tmp = new ConstantDataArray();
                        for (ConstInitVal j : i.getConstInitVals()) {
                            Constant meta = new ConstantInt(j.getConstExp().getVal(s));//二维数组
                            tmp.add(meta);
                        }
                        c.add(tmp);
                    } else {
                        Constant meta = new ConstantInt(i.getConstExp().getVal(s));//二维数组
                        c.add(meta);  //一维数组
                    }
                }
                value = c;
            } else {
                value = new ConstantInt(initVal.getConstExp().getVal(s));    //integer 常量
            }
        } else {
            if (elementType instanceof IntegerType) {
                value = new ConstantInt(0);    //integer 常量
            } else {
                value = zeroInitialize(elementType);
            }
        }
        this.id = "@" + IdName.globalVar + allocID();
        this.isConst = true;
    }

    private Value zeroInitialize(Type t) {
        ConstantDataArray c = new ConstantDataArray();
        Type tmp = ((ArrayType) t).getElementType();
        if (tmp instanceof ArrayType) {
            for (int i = 0; i < ((ArrayType) t).getElements(); i++) {
                Value v = zeroInitialize(tmp);
                if (v instanceof Constant) {
                    c.add((Constant) v);
                }
            }
        } else if (tmp instanceof IntegerType) {
            for (int i = 0; i < ((ArrayType) t).getElements(); i++) {
                c.add(new ConstantInt(0));
            }
        }
        return c;
    }

    @Override
    public String toString() {
        if (!isConst) {
            if (elementType instanceof IntegerType) {
                return id + " = dso_local global " + value + "\n";
            } else {
                if (isZero) {
                    return id + " = dso_local global " + elementType + " " + "zeroinitializer" + "\n";
                }
                return id + " = dso_local global " + elementType + " " + value + "\n";
            }
        } else {
            if (elementType instanceof IntegerType) {
                return id + " = dso_local constant " + value + "\n";
            } else {
                if (isZero) {
                    return id + " = dso_local global " + elementType + " " + "zeroinitializer" + "\n";
                }
                return id + " = dso_local constant " + elementType + " " + value + "\n";
            }
        }
    }

    public int getConstVal( int index1, int index2) {
        if (elementType instanceof IntegerType) {      //为常数
            return ((ConstantInt) value).getNum();
        } else {
            Type tmp = ((ArrayType) elementType).getElementType();
            Constant constant1 = ((ConstantDataArray) value).getIndex(index1);
            if (tmp instanceof IntegerType) {         //一维数组求值
                return ((ConstantInt) constant1).getNum();
            } else {                                  //二维数组求值
                Constant constant2 = ((ConstantDataArray) constant1).getIndex(index2);
                return ((ConstantInt) constant2).getNum();
            }
        }
    }

    public String getId() {
        return id;
    }

    public boolean getIsConst() {
        return isConst;
    }

}
