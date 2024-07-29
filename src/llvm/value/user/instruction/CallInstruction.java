package llvm.value.user.instruction;

import llvm.IdName;
import llvm.type.IntegerType;
import llvm.value.user.constant.globalobject.Function;

import java.util.ArrayList;
import java.util.List;

public class CallInstruction extends Instruction {
    private Function function;
    private String targetId;
    private List<String> params;

    public int getDimOf(int i) {
        return function.getArguments().get(i).getRealDim();
    }

    public Function getFunction() {
        return function;
    }

    public List<String> getParams() {
        return params;
    }

    public CallInstruction(Function f, List<String> rParams) {
        super();
        function = f;
        if (f.getRet() instanceof IntegerType) {
            targetId = "%" + IdName.tmpVar + allocID();
        } else {
            targetId = null;
        }
        params = rParams;
    }

    @Override
    public String toString() {
        String s = "";
        if (targetId != null) {
            s += targetId + " = call i32 ";
        } else {
            s += "call void ";
        }
        s += "@" + function.getName() + "(";
        for (int i = 0; i < params.size(); i++) {
            s += function.getArguments().get(i).getType() + " " + params.get(i);
            if (i != params.size() - 1) {
                s += ", ";
            }
        }
        s += ")\n";
        return s;
    }

    public String getTargetId() {
        return this.targetId;
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        for (String s : params) {
            if (s.charAt(0) == '%') {
                using.add(s);
            }
        }
        return using;
    }

    public String getDef() {
        return targetId;
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
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).equals(old)) {
                params.set(i, s);
            }
        }
    }
}
