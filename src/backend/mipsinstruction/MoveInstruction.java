package backend.mipsinstruction;

public class MoveInstruction extends MipsInstruction {
    private String des;
    private String src;

    public String getDes() {
        return des;
    }

    public String getSrc() {
        return src;
    }

    public String getTarget() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public MoveInstruction(String d, String s) {
        this.des = d;
        this.src = s;
    }

    @Override
    public void changeName() {
        if (des != null && des.charAt(0) == 'r') {
            des = des + "_" + counter;
        }
        if (src != null && src.charAt(0) == 'r') {
            src = src + "_" + counter;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoveInstruction that = (MoveInstruction) o;

        if (des != null ? !des.equals(that.des) : that.des != null) return false;
        return src != null ? src.equals(that.src) : that.src == null;
    }

    @Override
    public int hashCode() {
        return src != null ? src.hashCode() : 0;
    }

    @Override
    public void replace(String old, String newReg) {
        if (des != null && des.equals(old)) {
            des = newReg;
        }
        if (src != null && src.equals(old)) {
            src = newReg;
        }
    }

    public String toString() {
        if (!des.equals(src)) {
            return "move " + des + ", " + src + "\n";
        } else {
            return "";
        }
    }
}
