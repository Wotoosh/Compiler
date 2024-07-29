package llvm.value.user.instruction;

import java.util.ArrayList;
import java.util.List;

public class PutStrInstruction extends Instruction {
    private String targetId;

    public PutStrInstruction(String id) {
        super();
        targetId = id;
    }

    public String getId() {
        return targetId;
    }

    @Override
    public String toString() {
        return "call void @putstr(i8* " + targetId + ")\n";
    }

    @Override
    public void replaceUse(String old, String s) {
        if (targetId.equals(old)) {
            targetId = s;
        }
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        using.add(targetId);
        return using;
    }
}
