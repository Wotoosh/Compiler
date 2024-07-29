package llvm.value.user.constant.constantdata;

import llvm.type.ArrayType;
import llvm.type.IntegerType;
import llvm.value.user.constant.Constant;

public class ConstantString extends Constant {
    private String string;

    public ConstantString(String s, int c) {
        string = s;
        this.elementType = new ArrayType(c + 1, new IntegerType(8));
    }

    public int getSize() {
        return ((ArrayType) elementType).getElements();
    }

    @Override
    public String toString() {
        return "c\"" + string + "\\00\"";
    }

    public String getString() {
        return string;
    }
}
