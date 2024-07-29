package llvm.value.user.instruction;

import llvm.IdName;

import java.util.List;

public class SubInstruction extends Instruction {
    private String targetId;
    private String value1;
    private String value2;

    @Override
    public List<String> getUsing() {
        List<String> using = super.getUsing();
        using.add(value1);
        using.add(value2);
        return using;
    }

    @Override
    public String getDef() {
        return targetId;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public SubInstruction(String s1, String s2) {
        super();
        value1 = s1;
        value2 = s2;
        targetId = "%" + IdName.tmpVar + allocID();
    }

    @Override
    public String toString() {
        return targetId + " = sub i32 " + value1 + ", " + value2 + "\n";
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubInstruction that = (SubInstruction) o;

        if (targetId != null ? !targetId.equals(that.targetId) : that.targetId != null) return false;
        if (value1 != null ? !value1.equals(that.value1) : that.value1 != null) return false;
        return value2 != null ? value2.equals(that.value2) : that.value2 == null;
    }

    @Override
    public int hashCode() {
        int result = value1 != null ? value1.hashCode() : 0;
        result = 31 * result + (value2 != null ? value2.hashCode() : 0);
        return result;
    }

    @Override
    public void replaceTar(String s) {
        for (Instruction i : users) {
            i.replaceUse(targetId, s);
        }
        targetId = s;
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
}
