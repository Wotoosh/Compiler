package llvm.value.user.instruction;

import llvm.value.user.User;

public abstract class Instruction extends User {
    public Instruction() {
        super();
    }

    public void setConstValue(int v) {
        for (Instruction user : users) {
            user.replaceUse(this.getDef(), String.valueOf(v));
        }
    }

    public void replaceTar(String s) {
    }

    public void replaceUse(String old, String s) {
    }

    public Boolean isConst() {
        return false;
    }

    public String getConst() {
        return null;
    }
}
