package llvm.value.user.instruction;

import java.util.ArrayList;
import java.util.List;

public class RetInstruction extends Instruction {
    private String ret;

    public RetInstruction(String name) {
        super();
        this.ret = name;
        this.elementType = new llvm.type.IntegerType();
    }

    public RetInstruction() {
        super();
        this.ret = null;
        this.elementType = new llvm.type.VoidType();
    }

    public String getRet() {
        return ret;
    }

    @Override
    public String toString() {
        if (ret == null) {
            return "ret void\n";
        } else {
            return "ret i32 " + ret + "\n";
        }
    }

    @Override
    public void replaceUse(String old, String s) {
        if (ret != null && ret.equals(old)) {
            ret = s;
        }
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        if (ret != null && (ret.charAt(0) == '%' || ret.charAt(0) == '@')) {
            using.add(ret);
        }
        return using;
    }
}
