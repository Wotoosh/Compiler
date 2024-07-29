package llvm.value;

import llvm.type.LabelType;
import llvm.value.user.constant.globalobject.Function;
import llvm.value.user.instruction.Instruction;
import llvm.value.user.instruction.RetInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

public class BasicBlock extends Value {
    private Function function;
    private List<Instruction> instructions;
    private List<BasicBlock> fathers;
    private List<BasicBlock> sons;
    private BasicBlock trueTo;
    private BasicBlock falseTo;
    private BasicBlock From;
    private HashMap<String, Instruction> defines;


    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public Instruction peek(Instruction cur, int off) {
        int index = instructions.indexOf(cur);
        return instructions.get(index + off);
    }

    public BasicBlock(Function f) {
        function = f;
        instructions = new ArrayList<>();
        elementType = new LabelType("label" + allocID());
        defines = new HashMap<>();
        fathers = new ArrayList<>();
        sons = new ArrayList<>();
    }

    public void addFatherBlock(BasicBlock b) {
        fathers.add(b);
    }

    public String getLabel() {
        return elementType.toString();
    }

    public BasicBlock getTrueTo() {
        return trueTo;
    }

    public void setTrueTo(BasicBlock b) {
        trueTo = b;
    }

    public BasicBlock getFrom() {
        return From;
    }

    public void setFrom(BasicBlock b) {
        From = b;
    }

    public BasicBlock getFalseTo() {
        return falseTo;
    }

    public void setFalseTo(BasicBlock b) {
        falseTo = b;
    }

    public void addInstruction(Instruction i) {
        instructions.add(i);
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("", getLabel() + ":\n", "");
        for (Instruction i : instructions) {
            joiner.add("\t" + i);
        }
        return joiner.toString();
    }

    public boolean hasReturn() {
        if (instructions.size() == 0) return false;
        Instruction last = instructions.get(instructions.size() - 1);
        return last instanceof RetInstruction;
    }

    public void addSon(BasicBlock b) {
        sons.add(b);
    }

    public void deleteSon(BasicBlock b) {
        sons.remove(b);
    }

    public void deleteFather(BasicBlock b) {
        fathers.remove(b);
    }

    public void addFather(BasicBlock b) {
        fathers.add(b);
    }

    public List<BasicBlock> getFathers() {
        return fathers;
    }
}
