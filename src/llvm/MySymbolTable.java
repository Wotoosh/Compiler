package llvm;

import llvm.value.Value;
import llvm.value.user.constant.globalobject.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable {
    private MySymbolTable father;
    private List<MySymbolTable> sons;
    private Map<String, Value> variables;
    private Map<String, Value> functions;

    public MySymbolTable() {
        father = null;
        sons = new ArrayList<>();
        variables = new HashMap<>();
        functions = new HashMap<>();
    }

    public Map<String, Value> getVariables() {
        return variables;
    }

    public void addGlobalVar(Value allocaInstruction) {
        variables.put(allocaInstruction.getName(), allocaInstruction);
    }

    public void addTmpVar(Value allocaInstruction) {
        variables.put(allocaInstruction.getName(), allocaInstruction);
    }

    public MySymbolTable child() {
        MySymbolTable child = new MySymbolTable();
        child.father = this;
        child.variables = new HashMap<>(this.variables);
        sons.add(child);
        return child;
    }

    public void add(Function f) {
        functions.put(f.getName(), f);
    }

    public Value get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (functions.containsKey(name)) {
            return functions.get(name);
        } else if (father != null) {
            return father.get(name);
        } else {
            return null;
        }
    }

    public MySymbolTable getFather() {
        return father;
    }
}
