package llvm.type;

public class IntegerType extends Type {
    //private int value;
    private int length;

    public IntegerType() {
        length = 32;
    }

    public IntegerType(int l) {
        length = l;
    }

    @Override
    public String toString() {
        return "i" + length;
    }

    public int getSize() {
        return 1;
    }

    @Override
    public int getDim() {
        return 0;
    }
}
