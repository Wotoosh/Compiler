package llvm.type;

public abstract class Type {
    public int getSize() {
        return 1;
    }

    public Type getPointer() {
        return new PointerType(this);
    }

    public int getDim() {
        return 0;
    }

}
