package backend.mipsnode;

import backend.mipsinstruction.MipsInstruction;
import backend.mipsinstruction.MipsLoadLocal;

import java.util.ArrayList;
import java.util.List;

public class MipsBlock {
    private String label;
    private int size;
    private List<MipsInstruction> instructions;
    private MipsFunction function;
    public boolean isSilence;

    public String getLabel() {
        return label;
    }

    public void silenceRa() {
        isSilence = true;
    }

    public void setInstructions(List<MipsInstruction> instructions) {
        this.instructions = instructions;
    }

    public List<MipsInstruction> getInstructions() {
        return instructions;
    }

    public MipsFunction getFunction() {
        return function;
    }

    public MipsBlock(String s, MipsFunction f) {
        label = s;
        instructions = new ArrayList<>();
        size = 0;
        function = f;
        isSilence = false;
    }

    public void addInstruction(MipsInstruction instruction) {
        instructions.add(instruction);
    }

    public void addInstruction(int i, MipsInstruction instruction) {
        instructions.add(i, instruction);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(":\n");
        for (MipsInstruction i : instructions) {
            if (isSilence && i instanceof MipsLoadLocal && i.getTarget().equals("$ra")) {
                continue;
            }
            sb.append('\t').append(i.toString());
        }
        return sb.toString();
    }

}
