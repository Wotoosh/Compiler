package backend.mipstool;

import backend.mipsinstruction.*;
import backend.mipsnode.MipsBlock;
import backend.mipsnode.MipsFunction;
import llvm.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackOptimizer {
    private Module module;
    private Map<String, MipsFunction> topFunctions;
    private HashMap<String, List<MipsInstruction>> uses;
    private Map<Regs, String> users;      //value为当前使用该物理寄存器的虚拟寄存器
    private Map<String, MipsBlock> blocks;
    private Map<String, List<String>> labels;
    private Map<String, List<String>> fromss;
    private Map<String, Integer> used;

    public BackOptimizer(Module m, Map<String, MipsFunction> tops) {
        module = m;
        topFunctions = tops;
        uses = new HashMap<>();
        users = new HashMap<>();
        blocks = new HashMap<>();
        labels = new HashMap<>();
        fromss = new HashMap<>();
        used = new HashMap<>();
        used.put("$t0", 0);
        used.put("$t1", 0);
        used.put("$t2", 0);
        used.put("$t3", 0);
        used.put("$t4", 0);
        used.put("$t5", 0);
        used.put("$t6", 0);
        used.put("$t7", 0);
        used.put("$t8", 0);
        used.put("$t9", 0);
        used.put("$s0", 0);
        used.put("$s1", 0);
        used.put("$s2", 0);
        used.put("$s3", 0);
        used.put("$s4", 0);
        used.put("$s5", 0);
        used.put("$s6", 0);
        used.put("$s7", 0);
    }

    public void run() {
        for (MipsFunction f : topFunctions.values()) {
            for (MipsBlock b : f.getBlocks()) {
                optimize(b);
            }
            buildLabelRelations(f);
            for (MipsBlock b : f.getBlocks()) {
                Map<Integer, String> tmpOffset = new HashMap<>();
                Map<Integer, MipsInstruction> save = new HashMap<>();
                List<MipsInstruction> insts = new ArrayList<>();
                used.replaceAll((key, value) -> 0);
                if (fromss.containsKey(b.getLabel()) && fromss.get(b.getLabel()).size() == 1) {
                    MipsBlock from = blocks.get(fromss.get(b.getLabel()).get(0));
                    insts = from.getInstructions();
                    for (int i = 0; i < insts.size(); i++) {
                        if (from.getInstructions().get(i).getTarget() != null) {
                            if (used.containsKey(from.getInstructions().get(i).getTarget())) {
                                used.put(from.getInstructions().get(i).getTarget(), 1);
                            }
                        }
                        if (from.getInstructions().get(i) instanceof MipsLoadLocal && ((MipsLoadLocal) from.getInstructions().get(i)).getBase().equals("$sp")) {
                            tmpOffset.put(((MipsLoadLocal) from.getInstructions().get(i)).getOffset(), from.getInstructions().get(i).getTarget());
                            save.put(((MipsLoadLocal) from.getInstructions().get(i)).getOffset(), from.getInstructions().get(i));
                        } else if (from.getInstructions().get(i) instanceof MipsStoreLocal && ((MipsStoreLocal) from.getInstructions().get(i)).getBase().equals("$sp")) {
                            tmpOffset.put(((MipsStoreLocal) from.getInstructions().get(i)).getOffset(), ((MipsStoreLocal) from.getInstructions().get(i)).getSource());
                            save.put(((MipsStoreLocal) from.getInstructions().get(i)).getOffset(), from.getInstructions().get(i));
                        }
                        if (from.getInstructions().get(i) instanceof JalInstruction) {
                            used.replaceAll((key, value) -> 0);
                            save.clear();
                            tmpOffset.clear();
                        }
                    }
                }
                List<MipsInstruction> ultimate = new ArrayList<>(b.getInstructions());
                for (int i = 0; i < b.getInstructions().size(); i++) {
                    if (b.getInstructions().get(i).getTarget() != null) {
                        if (used.containsKey(b.getInstructions().get(i).getTarget())) {
                            used.put(b.getInstructions().get(i).getTarget(), 1);
                        }
                    }
                }
                for (int i = 0; i < b.getInstructions().size(); i++) {
                    if (b.getInstructions().get(i) instanceof MipsLoadLocal && ((MipsLoadLocal) b.getInstructions().get(i)).getBase().equals("$sp") && used.containsKey(b.getInstructions().get(i).getTarget())) {
                        if (tmpOffset.containsKey(((MipsLoadLocal) b.getInstructions().get(i)).getOffset())) {
                            if (used.containsValue(0)) {
                                String key = null;
                                for (Map.Entry<String, Integer> s : used.entrySet()) {
                                    if (s.getValue() == 0) {
                                        key = s.getKey();
                                        int off = ((MipsLoadLocal) b.getInstructions().get(i)).getOffset();
                                        int index = insts.indexOf(save.get(((MipsLoadLocal) b.getInstructions().get(i)).getOffset()));
                                        if (save.get(off) instanceof MipsLoadLocal)
                                            insts.add(index + 1, new MoveInstruction(key, save.get(off).getTarget()));
                                        else if (save.get(off) instanceof MipsStoreLocal) {
                                            insts.add(index + 1, new MoveInstruction(key, ((MipsStoreLocal) save.get(off)).getSource()));
                                        }
                                        ultimate.add(ultimate.indexOf(b.getInstructions().get(i)), new MoveInstruction(b.getInstructions().get(i).getTarget(), key));
                                       MipsInstruction ints=b.getInstructions().get(i);
                                        ultimate.remove(b.getInstructions().get(i));
                                        break;
                                    }
                                }
                                used.put(key, 1);
                            }
                        }
                    }else if (b.getInstructions().get(i) instanceof JalInstruction) {
                        break;
                    }
                }
                b.setInstructions(ultimate);
            }
        }
    }


    public void optimize(MipsBlock b) {
        List<MipsInstruction> inst = b.getInstructions();
        List<MipsInstruction> newinst = new ArrayList<>();
        for (int i = 0; i < inst.size(); i++) {
            if (inst.get(i) instanceof AdduInstruction) {
                if (((AdduInstruction) inst.get(i)).getRight() == null
                        && ((AdduInstruction) inst.get(i)).getNum() == 0
                        && (inst.get(i)).getTarget().equals(((AdduInstruction) inst.get(i)).getLeft())) {
                    continue;
                } else {
                    newinst.add(inst.get(i));
                }
            } else if (inst.get(i) instanceof MoveInstruction && ((MoveInstruction) inst.get(i)).getSrc().equals(((MoveInstruction) inst.get(i)).getDes())) {
                continue;
            } else if (inst.get(i) instanceof MipsSubInstruction && ((MipsSubInstruction) inst.get(i)).getRight() == null) {
                AdduInstruction addu = new AdduInstruction(((MipsSubInstruction) inst.get(i)).getLeft(),
                        ((MipsSubInstruction) inst.get(i)).getNum() * -1, ((MipsSubInstruction) inst.get(i)).getTarget());
                newinst.add(addu);
            } else {
                newinst.add(inst.get(i));
            }
            b.setInstructions(newinst);
        }
    }

    public void buildLabelRelations(MipsFunction f) {
        blocks.clear();
        labels.clear();
        fromss.clear();
        for (MipsBlock b : f.getBlocks()) {
            blocks.put(b.getLabel(), b);
            labels.putIfAbsent(b.getLabel(), new ArrayList<>());
        }
        for (MipsBlock b : f.getBlocks()) {
            List<MipsInstruction> inst = b.getInstructions();
            for (MipsInstruction i : inst) {
                if (i instanceof JInstruction) {
                    labels.get(b.getLabel()).add(i.getLabel());
                    fromss.putIfAbsent(((JInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((JInstruction) i).getLabel()).add(b.getLabel());
                } else if (i instanceof BeqInstruction) {
                    fromss.putIfAbsent(((BeqInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((BeqInstruction) i).getLabel()).add(b.getLabel());
                    labels.get(b.getLabel()).add(((BeqInstruction) i).getLabel());
                } else if (i instanceof BgeInstruction) {
                    labels.get(b.getLabel()).add(((BgeInstruction) i).getLabel());
                    fromss.putIfAbsent(((BgeInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((BgeInstruction) i).getLabel()).add(b.getLabel());
                } else if (i instanceof BgtInstruction) {
                    labels.get(b.getLabel()).add(((BgtInstruction) i).getLabel());
                    fromss.putIfAbsent(((BgtInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((BgtInstruction) i).getLabel()).add(b.getLabel());
                } else if (i instanceof BleInstruction) {
                    labels.get(b.getLabel()).add(((BleInstruction) i).getLabel());
                    fromss.putIfAbsent(((BleInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((BleInstruction) i).getLabel()).add(b.getLabel());
                } else if (i instanceof BltInstruction) {
                    labels.get(b.getLabel()).add(((BltInstruction) i).getLabel());
                    fromss.putIfAbsent(((BltInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((BltInstruction) i).getLabel()).add(b.getLabel());
                } else if (i instanceof BneInstruction) {
                    labels.get(b.getLabel()).add(((BneInstruction) i).getLabel());
                    fromss.putIfAbsent(((BneInstruction) i).getLabel(), new ArrayList<>());
                    fromss.get(((BneInstruction) i).getLabel()).add(b.getLabel());
                }
            }
        }
    }

}
