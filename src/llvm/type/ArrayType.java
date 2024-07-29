package llvm.type;

public class ArrayType extends Type {
    private int elements;
    private Type elementType;

    public ArrayType(int elements, Type elementType) {
        this.elements = elements;
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        s.append(elements);
        s.append(" x ");
        s.append(elementType);
        s.append("]");
        return s.toString();
    }

    public int getSize() {
        return elements * elementType.getSize();
    }

    public Type getElementType() {
        return elementType;
    }

    public int getElements() {
        return elements;
    }

    @Override
    public int getDim() {
        return elementType.getDim() + 1;
    }
}
