package backend.mipstool;

import backend.mipsinstruction.MipsInstruction;

public class MipsHashInst {
    private Class c;
    private MipsInstruction instruction;

    public MipsHashInst(Class cl, MipsInstruction i) {
        c=cl;
        instruction = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MipsHashInst that = (MipsHashInst) o;

        if (c != null ? !c.equals(that.c) : that.c != null) return false;
        return instruction != null ? instruction.equals(that.instruction) : that.instruction == null;
    }

    @Override
    public int hashCode() {
        int result = c != null ? c.hashCode() : 0;
        result = 31 * result + (instruction != null ? instruction.hashCode() : 0);
        return result;
    }
}
