package backend.mipsinstruction;

public class JrInstruction extends MipsInstruction {
    @Override
    public String toString() {
        return "jr $ra\n";
    }
}
