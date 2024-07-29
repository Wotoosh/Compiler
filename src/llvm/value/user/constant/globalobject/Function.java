package llvm.value.user.constant.globalobject;

import frontend.node.FuncDef;
import frontend.node.MainFuncDef;
import llvm.type.IntegerType;
import llvm.type.Type;
import llvm.type.VoidType;
import llvm.value.Argument;
import llvm.value.BasicBlock;
import llvm.value.user.instruction.RetInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Function extends GlobalObject {
    private List<Argument> arguments;
    private Type ret;
    private List<BasicBlock> basicBlocks;
    private Map<String, BasicBlock> labels;
    private boolean isBuiltIn;

    public void setBlocks(List<BasicBlock> basicBlocks) {
        this.basicBlocks = basicBlocks;
    }

    public void removeBlock(BasicBlock b) {
        basicBlocks.remove(b);
    }

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public BasicBlock getBlock(String label) {
        return labels.get(label);
    }

    public String getName() {
        return name;
    }

    public Function(FuncDef f) {
        this.name = f.getIdent().getName();
        this.ret = f.hasReturn() ? new IntegerType() : new VoidType();
        this.arguments = new ArrayList<>();
        this.basicBlocks = new ArrayList<>();
        this.isBuiltIn = false;
        this.labels = new HashMap<>();
    }

    public Function(MainFuncDef f) {
        this.name = "main";
        this.ret = new IntegerType();
        this.arguments = new ArrayList<>();
        this.basicBlocks = new ArrayList<>();
        this.isBuiltIn = false;
        this.labels = new HashMap<>();
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public boolean getBuiltIn() {
        return this.isBuiltIn;
    }

    public void addBasicBlock(BasicBlock b) {
        basicBlocks.add(b);
        labels.put(b.getLabel(), b);
    }

    public Type getRet() {
        return this.ret;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("define dso_local ");
        s.append(ret);
        s.append(" @");
        s.append(name);
        s.append("(");
        for (int i = 0; i < arguments.size(); i++) {
            s.append(arguments.get(i));
            if (i != arguments.size() - 1) {
                s.append(", ");
            }
        }
        s.append("){\n");
        for (BasicBlock b : basicBlocks) {
            s.append(b);
        }
        s.append("}\n");
        return s.toString();
    }

    public void checkReturn() {
        if (this.ret instanceof VoidType) {
            if (this.basicBlocks.size() == 1) {
                if (!this.basicBlocks.get(0).hasReturn()) {
                    basicBlocks.get(0).addInstruction(new RetInstruction());
                }
            } else {
                if (!this.basicBlocks.get(this.basicBlocks.size() - 2).hasReturn()) {
                    basicBlocks.get(basicBlocks.size() - 1).addInstruction(new RetInstruction());
                } else {
                    basicBlocks.remove(basicBlocks.size() - 1);
                }
            }
        } else {
            basicBlocks.remove(basicBlocks.size() - 1);
        }
    }
}
