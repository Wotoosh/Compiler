package backend.mipsinstruction;

public class LaInstruction extends MipsInstruction {
    private String target;  // target register
    private String label;
    private String base;    //base register
    private int offset;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public LaInstruction(String t, String l) {
        target = t;
        label = l;
        base = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LaInstruction that = (LaInstruction) o;

        if (offset != that.offset) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        return base != null ? base.equals(that.base) : that.base == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (base != null ? base.hashCode() : 0);
        result = 31 * result + offset;
        return result;
    }

    public LaInstruction(String tar, String b, int o) {
        target = tar;
        base = b;
        offset = o;
    }

    @Override
    public void changeName() {
        if (target != null && target.charAt(0) == 'r') {
            target = target + "_" + counter;
        }
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void replace(String old, String newReg) {
        if (target != null && target.equals(old)) {
            target = newReg;
        }
        if (base != null && base.equals(old)) {
            base = newReg;
        }
    }

    @Override
    public String toString() {
        if (base == null) {
            return "la " + target + ", " + label.substring(1) + "\n";
        } else {
            return "la " + target + ", " + offset + "(" + base + ")\n";
        }
    }

}
