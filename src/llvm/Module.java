package llvm;

import frontend.node.ConstInitVal;
import frontend.node.InitVal;
import llvm.value.user.constant.globalobject.Function;
import llvm.value.user.constant.globalobject.GlobalVariable;

import java.util.ArrayList;
import java.util.List;

public class Module {
    private List<Function> functionList;
    private List<GlobalVariable> globalVariableList;
    private MySymbolTable symbolTable;

    public Module() {
        functionList = new ArrayList<>();
        globalVariableList = new ArrayList<>();
        symbolTable = new MySymbolTable();
    }

    public List<Function> getFunctionList() {
        return functionList;
    }

    public List<GlobalVariable> getGlobalVariableList() {
        return globalVariableList;
    }

    public void addGlobalVar(GlobalVariable globalVariable, InitVal initVal) {
        globalVariableList.add(globalVariable);
        symbolTable.addGlobalVar(globalVariable);
    }

    public void addGlobalVar(GlobalVariable globalVariable, ConstInitVal constInitVal) {
        globalVariableList.add(globalVariable);
        symbolTable.addGlobalVar(globalVariable);
    }

    public void addGlobalVar(GlobalVariable g) {
        globalVariableList.add(g);
        symbolTable.addGlobalVar(g);
    }

    public MySymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void addFunction(Function function) {
        functionList.add(function);
        symbolTable.add(function);
    }

    public Function getFunction(String name) {
        for (Function f : functionList) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
}
