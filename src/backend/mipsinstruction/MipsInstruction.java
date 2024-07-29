package backend.mipsinstruction;

public abstract class MipsInstruction implements Cloneable {
    protected static int counter = 0;

    public void replace(String old, String newReg) {
    }

    public void replaceLabel(String old, String newLabel) {
    }

    public String getLabel() {
        return null;
    }

    public String getTarget() {
        return null;
    }

    public void changeName() {
    }

    public void addCounter() {
        counter++;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MipsInstruction m = (MipsInstruction) super.clone();
        m.changeName();
        return m;
    }
}
