package backend.mipsinstruction;

public class MipsLoadLocal extends MipsLoadInstruction {
    private String target;
    private int offset;
    private String base;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MipsLoadLocal that = (MipsLoadLocal) o;

        if (offset != that.offset) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        return base != null ? base.equals(that.base) : that.base == null;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + (base != null ? base.hashCode() : 0);
        return result;
    }

    @Override
    public void replace(String old, String newReg) {
        if (base != null && base.equals(old)) {
            base = newReg;
        }
        if (target != null && target.equals(old)) {
            target = newReg;
        }
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getBase() {
        return base;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public void changeName() {
        if (target != null && target.charAt(0) == 'r') {
            target = target + "_"+counter;
        }
        if (base != null && base.charAt(0) == 'r') {
            base = base + "_"+counter;
        }
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public MipsLoadLocal(String t, int o, String b) {
        target = t;
        offset = o;
        base = b;
    }

    @Override
    public String toString() {
        return "lw " + target + ", " + offset + "(" + base + ")\n";
    }
}
