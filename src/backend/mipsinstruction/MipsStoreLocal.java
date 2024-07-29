package backend.mipsinstruction;

import backend.mipsnode.MipsFunction;

public class MipsStoreLocal extends SWInstruction {
    private String source;
    private int offset;
    private String base;
    private MipsFunction func;

    public String getBase() {
        return base;
    }

    @Override
    public void changeName() {
        if (source != null && source.charAt(0) == 'r') {
            source = source + "_"+counter;
        }
        if (base != null && base.charAt(0) == 'r') {
            base = base + "_"+counter;
        }
    }

    public int getOffset() {
        if (func != null) {
            return offset - func.getSize();
        } else {
            return offset;
        }
    }

    public void setOffset(int off) {
        this.offset = off;
    }

    @Override
    public void replace(String old, String newReg) {
        if (base != null && base.equals(old)) {
            base = newReg;
        }
        if (source != null && source.equals(old)) {
            source = newReg;
        }
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public MipsStoreLocal(String s, int o, String b) {
        source = s;
        offset = o;
        base = b;
    }

    public MipsStoreLocal(String s, int o, String b, MipsFunction f) {
        source = s;
        offset = o;
        base = b;
        func = f;
    }

    public String toString() {
        if (func != null) {
            int num = offset - func.getSize() + func.getArgOff((offset-8)/4) ;
            return "sw " + source + ", " + num + "(" + base + ")\n";
        }
        return "sw " + source + ", " + offset + "(" + base + ")\n";
    }
}
