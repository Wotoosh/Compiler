package backend.mipsinstruction;

public class BneInstruction extends MipsInstruction {
    private String rs;
    private String rt;
    private String label;
    private int num;

    public int getNum() {
        return num;
    }

    @Override
    public void changeName() {
        if (rs != null && rs.charAt(0) == 'r') {
            rs = rs + "_"+counter;
        }
        if(rt != null && rt.charAt(0) == 'r') {
            rt = rt + "_"+counter;
        }
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

    public BneInstruction(String rs, String rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


    public BneInstruction(String rs, int n, String label) {
        this.rs = rs;
        num = n;
        this.label = label;
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
        if (rt != null)
            return "bne " + rs + ", " + rt + ", " + label + "\n";
        else
            return "bne " + rs + ", " + num + ", " + label + "\n";
    }
}
