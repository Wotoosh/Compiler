package backend.mipsinstruction;

public class MipsStoreGlobal extends SWInstruction {
    private String source;
    private String global;
    private int offset;

    public String getGlobal() {
        return global;
    }

    @Override
    public void changeName() {
        if (source != null && source.charAt(0) == 'r') {
            source = source + "_"+counter;
        }
        if (global != null && global.charAt(0) == 'r') {
            global = global + "_"+counter;
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public void replace(String old, String newReg) {
        if (source != null && source.equals(old)) {
            source = newReg;
        }
        if (global != null && global.equals(old)) {
            global = newReg;
        }
    }

    public String getSource() {
        return source;
    }

    public MipsStoreGlobal(String s, String g, int o) {
        source = s;
        global = g;
        offset = o;
    }

    @Override
    public String toString() {
        return "sw " + source + ", " + global.substring(1) + "\n";
    }
}
