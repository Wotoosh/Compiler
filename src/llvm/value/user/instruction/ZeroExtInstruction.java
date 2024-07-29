package llvm.value.user.instruction;

import llvm.IdName;

import java.util.ArrayList;
import java.util.List;

public class ZeroExtInstruction extends Instruction {
    private String oldId;
    private int oldWidth;
    private int newWidth;
    private String targetId;

    public void replaceTar(String s) {
        for (Instruction i : users) {
            i.replaceUse(targetId, s);
        }
        targetId = s;
    }

    public void replaceUse(String old, String s) {
        if (oldId.equals(old)) {
            oldId = s;
        }
    }

    @Override
    public String getDef() {
        return targetId;
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        using.add(oldId);
        return using;
    }

    public ZeroExtInstruction(String valId) {
        this.oldId = valId;
        oldWidth = 1;
        newWidth = 32;
        targetId = "%" + IdName.tmpVar + allocID();
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return targetId + " = zext i" + oldWidth + " " + oldId + " " + "to i" + newWidth + "\n";
    }

    public String getOldId() {
        return oldId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZeroExtInstruction that = (ZeroExtInstruction) o;

        if (oldWidth != that.oldWidth) return false;
        if (newWidth != that.newWidth) return false;
        if (oldId != null ? !oldId.equals(that.oldId) : that.oldId != null) return false;
        return targetId != null ? targetId.equals(that.targetId) : that.targetId == null;
    }

    @Override
    public int hashCode() {
        int result = oldId != null ? oldId.hashCode() : 0;
        result = 31 * result + oldWidth;
        result = 31 * result + newWidth;
        return result;
    }
}
