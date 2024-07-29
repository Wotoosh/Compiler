package llvm.value.user.instruction;

import llvm.IdName;

public class GetIntInstruction extends Instruction {
    private String targetId;

    public GetIntInstruction() {
        super();
        targetId = "%" + IdName.getInt + allocID();
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return targetId + " = call i32 @getint()\n";
    }

    @Override
    public String getDef() {
        return targetId;
    }
}
