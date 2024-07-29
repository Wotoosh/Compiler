package llvm.GVN;

import llvm.value.user.instruction.Instruction;

public class HashInst {
    private Class c;
    private Instruction instruction;

    public HashInst(Class cl, Instruction i) {
        c=cl;
        instruction = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashInst hashInst = (HashInst) o;

        if (c != hashInst.c) return false;
        return instruction != null ? instruction.equals(hashInst.instruction) : hashInst.instruction == null;
    }

    @Override
    public int hashCode() {
        int result = c != null ? c.hashCode() : 0;
        result = 31 * result + (instruction != null ? instruction.hashCode() : 0);
        return result;
    }
}
