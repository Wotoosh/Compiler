package llvm.value.user.instruction;

import java.util.ArrayList;
import java.util.List;

public class BranchInstruction extends Instruction {
    private String targetId;
    private String trueLabel;
    private String falseLabel;

    public String getTrueLabel() {
        return trueLabel;
    }

    public String getFalseLabel() {
        return falseLabel;
    }

    public BranchInstruction(String targetId, String trueLabel, String falseLabel) {
        super();
        this.targetId = targetId;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public BranchInstruction(String trueLabel) {
        super();
        this.trueLabel = trueLabel;
        this.targetId = null;
        this.falseLabel = null;
    }

    public String getTargetId() {
        return targetId;
    }

    public String toString() {
        if (targetId == null) {
            return "br label %" + trueLabel + "\n";
        } else {
            return "br i1 " + targetId + ", label %" + trueLabel + ", label %" + falseLabel + "\n";
        }
    }

    @Override
    public List<String> getUsing() {
        if (targetId == null) {
            return new ArrayList<>();
        } else {
            List<String> using = new ArrayList<>();
            using.add(targetId);
            return using;
        }
    }

    @Override
    public void replaceUse(String old, String s) {
        if (targetId != null && targetId.equals(old)) {
            targetId = s;
        }
        if (trueLabel.equals(old)) {
            trueLabel = s;
        }
        if (falseLabel != null && falseLabel.equals(old)) {
            falseLabel = s;
        }
    }
}
