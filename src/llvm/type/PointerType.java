package llvm.type;

public class PointerType extends Type {
    private Type pointed;

    public PointerType(Type p) {
        this.pointed = p;
    }

    @Override
    public String toString() {
        if (pointed instanceof PointerType)
            return pointed + "*";
        return pointed + "*";
    }

    public Type getPointed() {
        return pointed;
    }

    @Override
    public int getDim() {
        return pointed.getDim();
    }
}
