package llvm.value;

import llvm.IdName;
import llvm.type.PointerType;
import llvm.type.Type;

public class Argument extends Value {
    private String id;

    public int getBrackNum() {
        return elementType.getDim();
    }

    public int getRealDim() {
        int i = elementType.getDim();
        if (elementType instanceof PointerType) {
            i++;
        }
        return i;
    }

    public Argument(Type t, String s) {
        elementType = t;
        name = s;
        this.id = "%" + IdName.argument + allocID();
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return elementType + " " + id;
    }
}
