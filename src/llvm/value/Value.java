package llvm.value;

import llvm.type.Type;

public class Value {
    private static long  counter;
    protected String name;
    protected Type elementType;

    public Value() {

    }

    public Value(String s, Type t) {
        name = s;
        elementType = t;
    }

    public long allocID() {
        return counter++;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return elementType;
    }

    public String getId() {
        return null;
    }

    public Boolean isConst() {
        return false;
    }

}
