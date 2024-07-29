package backend.mipsinstruction;

public class JInstruction extends MipsInstruction {
    private String label;

    @Override
    public String toString() {
        return "j " + label + '\n';
    }

    public JInstruction(String s) {
        label = s;
    }

    public String getLabel() {
        return label;
    }


}
