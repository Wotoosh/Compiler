package llvm.value.user.constant.constantdata;

import llvm.value.user.constant.Constant;

public class ConstantInt extends Constant {
    private int num;

    public ConstantInt(int i) {
        num = i;
    }

    @Override
    public String toString() {
        return "i32 " + num;
    }

    public int getNum() {
        return num;
    }

    public int getDim() {
        return 0;
    }
}
