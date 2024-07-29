package llvm.type;

public class LabelType extends Type {
    private String label;

    public LabelType(String l) {
        this.label = l;
    }

    @Override
    public String toString() {
        return label;
    }
}
