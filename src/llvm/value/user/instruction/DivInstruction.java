package llvm.value.user.instruction;

import llvm.IdName;

import java.util.ArrayList;
import java.util.List;

public class DivInstruction extends Instruction {
    private String value1;
    private String value2;
    private String targetId;

    @Override
    public String getDef() {
        return targetId;
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        using.add(value1);
        using.add(value2);
        return using;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public DivInstruction(String s1, String s2) {
        super();
        value1 = s1;
        value2 = s2;
        targetId = "%" + IdName.tmpVar + allocID();
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return targetId + " = sdiv i32 " + value1 + ", " + value2 + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DivInstruction that = (DivInstruction) o;

        if (!value1.equals(that.value1)) return false;
        if (!value2.equals(that.value2)) return false;
        return targetId.equals(that.targetId);
    }

    @Override
    public int hashCode() {
        int result = value1.hashCode();
        result = 31 * result + value2.hashCode();
        return result;
    }

    @Override
    public void replaceUse(String old, String s) {
        if (value1.equals(old)) {
            value1 = s;
        }
        if (value2.equals(old)) {
            value2 = s;
        }
    }

    @Override
    public void replaceTar(String s) {
        for (Instruction i : users) {
            i.replaceUse(targetId, s);
        }
        targetId = s;
    }
}
