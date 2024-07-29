package backend.mipsnode;

import llvm.type.ArrayType;
import llvm.type.PointerType;
import llvm.value.Argument;

public class MipsArgument extends MipsData {
    private MipsData data;
    private int addr;

    public MipsArgument(Argument a, int ad) {
        name = a.getId();
        if (a.getBrackNum() == 0) {
            data = new MipsInt();
        } else if (a.getBrackNum() == 1) {
            data = new MipsInt(((ArrayType) ((PointerType) a.getType()).getPointed()).getElements());
        }
        size = data.getSize();
        addr = ad;
    }

    public MipsData getData() {
        return data;
    }
}
