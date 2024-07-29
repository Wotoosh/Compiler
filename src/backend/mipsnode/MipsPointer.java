package backend.mipsnode;

import backend.mipsinstruction.Regs;

public class MipsPointer {
    private int offset;
    private Regs reg;
    private boolean isGlobal;
    private String Global;
    private String targetId;
    private MipsData data;
    private int base;

    public int getBase() {
        return base;
    }

    public MipsData getData() {
        return data;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    public int getOffset() {
        return offset;
    }

    public void addOffset(int i) {
        offset += i;
    }

    public MipsPointer(MipsPointer f) {
        isGlobal = false;
        Global = null;
        reg = f.reg;
        if (!isGlobal) {
            base = f.base;
        }
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public String getGlobal() {
        return Global;
    }

    public void setData(MipsData data) {
        this.data = data;
    }

    public MipsPointer(String s) {
        isGlobal = true;
        Global = s;
    }

    public MipsPointer(int i, int t) {
        isGlobal = false;
        reg = Regs.$sp;
        offset = i;
        base = t;
    }
}
