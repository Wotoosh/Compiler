package llvm.type;

import java.util.List;

public class FunctionType extends Type {
    private Type returnType;
    private List<Type> parameter;
}
