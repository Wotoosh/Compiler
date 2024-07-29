package llvm.value.user.instruction;

import llvm.IdName;

import java.util.ArrayList;
import java.util.List;

public class IcmpInstruction extends Instruction {

    private String value1;
    private String value2;
    private String targetId;
    private IcmpType icmpType;

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

    public IcmpType getIcmpType() {
        return icmpType;
    }

    public IcmpInstruction(String s1, String s2, IcmpType i) {
        super();
        value1 = s1;
        value2 = s2;
        targetId = "%" + IdName.tmpVar + allocID();
        icmpType = i;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return targetId + " = icmp " + icmpType.toString().toLowerCase()
                + " i32 " + value1 + ", " + value2 + "\n";
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IcmpInstruction that = (IcmpInstruction) o;

        if (value1 != null ? !value1.equals(that.value1) : that.value1 != null) return false;
        if (value2 != null ? !value2.equals(that.value2) : that.value2 != null) return false;
        if (targetId != null ? !targetId.equals(that.targetId) : that.targetId != null) return false;
        return icmpType == that.icmpType;
    }

    @Override
    public int hashCode() {
        int result = value1 != null ? value1.hashCode() : 0;
        result = 31 * result + (value2 != null ? value2.hashCode() : 0);
        result = 31 * result + (icmpType != null ? icmpType.hashCode() : 0);
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
