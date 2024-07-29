package llvm.value.user.instruction;

import llvm.IdName;
import llvm.type.IntegerType;
import llvm.type.Type;

import java.util.ArrayList;
import java.util.List;

public class LoadInstruction extends Instruction {
    private String addrId;
    private String targetId;
    private Boolean isConst;
    private int ConstValue;

    public LoadInstruction(String pointerTo, Boolean isC) {
        super();
        addrId = pointerTo;
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = new IntegerType();
        isConst = isC;
    }

    public void setConstValue(int value) {
        super.setConstValue(value);
        ConstValue = value;
    }

    public LoadInstruction(String pointerTo, Type t, Boolean isC) {
        super();
        this.elementType = t;
        addrId = pointerTo;
        targetId = "%" + IdName.tmpVar + allocID();
        isConst = isC;
    }

    public String getAddrId() {
        return addrId;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return targetId + " = load " + elementType + ", " + elementType.getPointer() + " " + addrId + "\n";
    }

    @Override
    public String getDef() {
        return targetId;
    }

    @Override
    public void replaceUse(String old, String s) {
        if (addrId.equals(old)) {
            addrId = s;
        }
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        if (addrId != null && (addrId.charAt(0) == '%' || addrId.charAt(0) == '@')) {
            using.add(addrId);
        }
        return using;
    }

    @Override
    public void replaceTar(String s) {
        for (Instruction i : users) {
            i.replaceUse(targetId, s);
        }
        targetId = s;
    }

    public Boolean isConst() {
        return isConst;
    }

    public String getConst() {
        return String.valueOf(ConstValue);
    }
}
