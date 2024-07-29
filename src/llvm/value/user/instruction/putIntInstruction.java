package llvm.value.user.instruction;

import java.util.List;

public class putIntInstruction extends Instruction {
    private String id;

    public putIntInstruction(String s) {
        super();
        this.id = s;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "call void @putint(i32 " + id + ")\n";
    }

    @Override
    public List<String> getUsing() {
        List<String> using = super.getUsing();
        if (id.charAt(0) == '%') {
            using.add(id);
        }
        return using;
    }

    @Override
    public void replaceUse(String old, String s) {
        if (id.equals(old)) {
            id = s;
        }
    }
}
