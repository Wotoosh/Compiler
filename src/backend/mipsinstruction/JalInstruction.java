package backend.mipsinstruction;

public class JalInstruction extends MipsInstruction {
    private String label;

    @Override
    public void replaceLabel(String old, String newLabel) {
        if (label != null && label.equals(old)) {
            label = newLabel;
        }
    }

    @Override
    public String toString() {
        return "jal " + label + '\n';
    }

    public JalInstruction(String s) {
        label = s;
    }
}
