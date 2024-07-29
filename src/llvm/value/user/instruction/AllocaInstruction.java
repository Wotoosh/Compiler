package llvm.value.user.instruction;

public class AllocaInstruction extends Instruction {
    public boolean isConst;
    protected String id;

    public String getId() {
        return id;
    }

    public boolean getIsConst() {
        return isConst;
    }

    @Override
    public String getDef() {
        return id;
    }

}
