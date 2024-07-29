package backend.mipsinstruction;

public class BeqInstruction extends MipsInstruction {
    private String rs;
    private String rt;
    private String label;
    private int num;

    public void changeName() {
        if (rs != null && rs.charAt(0) == 'r') {
            rs = rs + "_"+counter;
        }
        if(rt != null && rt.charAt(0) == 'r') {
            rt = rt + "_"+counter;
        }
    }

    public void setRs(String rs) {
        this.rs = rs;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getRs() {
        return rs;
    }

    public String getRt() {
        return rt;
    }

    public int getNum() {
        return num;
    }

    public BeqInstruction(String rs, String rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    public BeqInstruction(String rs, int num,String l) {
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

    public String getLabel() {
        return label;
    }


    @Override
    public String toString() {
        if (rt == null)
            return "beq " + rs + ", " + num + ", " + label + "\n";
        else
            return "beq " + rs + ", " + rt + ", " + label + "\n";
    }
}
