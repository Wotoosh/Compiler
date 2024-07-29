package backend.mipsinstruction;

public class BleInstruction extends MipsInstruction{
    private String rs;
    private String rt;
    private String label;
    private int num;

    @Override
    public void changeName() {
        if (rs != null && rs.charAt(0) == 'r') {
            rs = rs + "_"+counter;
        }
        if(rt != null && rt.charAt(0) == 'r') {
            rt = rt + "_"+counter;
        }
    }

    public int getNum() {
        return num;
    }

    public String getRs() {
        return rs;
    }

    public String getRt() {
        return rt;
    }

    public void setRs(String rs) {
        this.rs = rs;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public BleInstruction(String rs, String rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public BleInstruction(String rs, int num, String l) {
        this.rs = rs;
        label = l;
        this.num = num;
        rt = null;
    }

    @Override
    public void replace(String old, String newReg) {
        if (rs != null && rs.equals(old)) {
            rs = newReg;
        }
        if (rt != null && rt.equals(old)) {
            rt = newReg;
        }
    }

    @Override
    public String toString() {
        if (rt == null)
            return "ble " + rs + ", " + num + ", " + label + "\n";
        else
            return "ble " + rs + ", " + rt + ", " + label + "\n";
    }
}
