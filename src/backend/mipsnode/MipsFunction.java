package backend.mipsnode;

import llvm.value.Argument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MipsFunction {
    private String name;
    private Map<String, MipsArgument> argumentList;
    private List<String> arguNames;
    private Map<String, MipsData> table;
    private Map<String, Integer> offset;
    private Map<String, MipsPointer> pointers;
    private List<MipsBlock> blocks;
    private int size;
    private boolean isSilence;

    public int getArgNum() {
        return argumentList.size();
    }

    public int getArgOff(int index) {
        int off = 0;
        Boolean flag = false;
        for (int i = 0; i < arguNames.size(); i++) {
            if (i == index) {
                flag = true;
                off += 4;
            } else if (flag == false) {
                off += argumentList.get(arguNames.get(i)).getSize();
            } else {
                off += 4;
            }
        }
        return off;
    }

    public Map<String, Integer> getOffset() {
        return offset;
    }

    public List<MipsBlock> getBlocks() {
        return blocks;
    }

    public MipsBlock getBlock(int i) {
        return blocks.get(i);
    }

    public boolean isArgument(String id) {
        return argumentList.containsKey(id);
    }

    public void silenceRa() {
        isSilence = true;
    }

    public String getName() {
        return name;
    }

    public Map<String, MipsData> getTable() {
        return table;
    }

    public int getSize() {
        return size;
    }

    public void addLocalInt(String id) {
        table.put(id, new MipsConst(0));
        offset.put(id, size);
        size += 4;
    }

    public void addLocalVar(String id, MipsData data) {
        table.put(id, data);
        offset.put(id, size);
        size += data.getSize();
    }

    public void addBlock(MipsBlock b) {
        blocks.add(b);
    }

    public Map<String, MipsPointer> getPointers() {
        return pointers;
    }

    public void addPointer(MipsPointer p) {
        pointers.put(p.getTargetId(), p);
        offset.put(p.getTargetId(), p.getOffset());
    }

    public int getOffset(String id) {
        if (!offset.containsKey(id)) {
            return -1;
        }
        return offset.get(id);
    }

    public MipsFunction(String s, List<Argument> arguments) {
        name = s;
        argumentList = new HashMap<>();
        size = 8;
        arguNames = new ArrayList<>();
        offset = new HashMap<>();
        table = new HashMap<>();
        for (Argument a : arguments) {
            MipsArgument argu = new MipsArgument(a, size);
            argumentList.put(argu.name, argu);
            arguNames.add(argu.name);
            offset.put(a.getId(), size);
            table.put(a.getId(), argu);
            size += 4;
        }
        pointers = new HashMap<>();
        blocks = new ArrayList<>();
        isSilence = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (MipsBlock b : blocks) {
            if (isSilence) {
                b.silenceRa();
            }
            sb.append(b.toString());
        }
        return sb.toString();
    }

}
