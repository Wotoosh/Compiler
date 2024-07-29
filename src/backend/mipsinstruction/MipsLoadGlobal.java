package backend.mipsinstruction;

public class MipsLoadGlobal extends MipsLoadInstruction {
    private String target;
    private String global;
    private int offset;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public void replace(String old, String newReg) {
        if (target != null && target.equals(old)) {
            target = newReg;
        }
        if (global != null && global.equals(old)) {
            global = newReg;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MipsLoadGlobal that = (MipsLoadGlobal) o;

        if (offset != that.offset) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        return global != null ? global.equals(that.global) : that.global == null;
    }

    @Override
    public void changeName() {
        if (target != null && target.charAt(0) == 'r') {
            target = target + "_"+counter;
        }
        if (global != null && global.charAt(0) == 'r') {
            global = global + "_"+counter;
        }
    }

    @Override
    public int hashCode() {
        int result = global != null ? global.hashCode() : 0;
        result = 31 * result + offset;
        return result;
    }

    public MipsLoadGlobal(String t, String g, int o) {
        target = t;
        global = g;
        offset = o;
    }

    public String getGlobal() {
        return global;
    }

    @Override
    public String toString() {
        return "lw " + target + ", " + global.substring(1) + " + " + offset + "\n";
    }
}
