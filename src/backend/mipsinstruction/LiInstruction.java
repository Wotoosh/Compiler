package backend.mipsinstruction;

import backend.mipsnode.MipsConst;

public class LiInstruction extends MipsInstruction {
    private String reg;
    private MipsConst mipsConst;

    public String getTarget() {
        return reg;
    }

    public void setTarget(String t) {
        this.reg = t;
    }

    public int getConst() {
        return mipsConst.getValue();
    }

    public LiInstruction(String r, int i) {
        reg = r;
        mipsConst = new MipsConst(i);
    }

    @Override
    public void changeName() {
        if (reg != null && reg.charAt(0) == 'r') {
            reg = reg + "_"+counter;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiInstruction that = (LiInstruction) o;

        if (reg != null ? !reg.equals(that.reg) : that.reg != null) return false;
        return mipsConst != null ? mipsConst.equals(that.mipsConst) : that.mipsConst == null;
    }

    @Override
    public int hashCode() {
        int result = reg != null ? reg.hashCode() : 0;
        result = 31 * result + (mipsConst != null ? mipsConst.hashCode() : 0);
        return result;
    }

    @Override
    public void replace(String old, String newReg) {
        if (reg != null && reg.equals(old)) {
            reg = newReg;
        }
    }

    public String toString() {
        return "li " + reg.toString() + ", " + mipsConst.toString() + "\n";
    }
}
