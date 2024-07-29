package llvm.value.user.constant;

import llvm.value.user.User;

public abstract class Constant extends User {
    public int getDim() {
        return 0;
    }
}
