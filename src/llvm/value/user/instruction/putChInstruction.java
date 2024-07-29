package llvm.value.user.instruction;

public class putChInstruction extends Instruction {
    private int ch;

    public putChInstruction(char c) {
        super();
        ch = c;
    }

    public putChInstruction() {
        super();
        ch = '\n';
    }

    @Override
    public String toString() {
        return "call void @putch(i32 " + ch + ")\n";
    }
}
