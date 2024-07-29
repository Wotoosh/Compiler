package backend.mipstool;

import backend.mipsinstruction.*;
import backend.mipsnode.MipsBlock;
import backend.mipsnode.MipsData;
import backend.mipsnode.MipsFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackVirtualOptimizer {

    private HashMap<String, List<MipsInstruction>> uses;
    private HashMap<String, MipsBlock> blocks;
    private HashMap<String, List<String>> labels;
    private HashMap<String, List<String>> fromss;
    private Map<Integer, String> hashes;
    private List<MipsInstruction> ultimate;
    private List<MipsInstruction> instructions;
    private Allocator allocator;
    private Map<String, Integer> globalOffsets;

    public BackVirtualOptimizer(Allocator a) {
        uses = new HashMap<>();
        blocks = new HashMap<>();
        labels = new HashMap<>();
        fromss = new HashMap<>();
        allocator = a;
        globalOffsets = new HashMap<>();
    }

    public void setGlobalOffsets(Map<String, MipsData> globalDatas) {
//        int off = 32768;
        int off=0;
        for (Map.Entry<String, MipsData> globalData : globalDatas.entrySet()) {
            globalOffsets.put(globalData.getKey(), off);
            off += globalData.getValue().getSize();
        }
    }

    public void run(MipsFunction f) {
        String fs = "";
        int time = 0;
        while (!fs.equals(f.toString()) && time < 10000) {
            time++;
            fs = f.toString();
            removeUnusedJump(f);
            standardArray(f);
            for (MipsBlock b : f.getBlocks()) {
                String s = "";
                while (!s.equals(b.toString())) {
                    s = b.toString();
                    constSpread(b);
                    deleteSame(b);
                    deleteDead(b);
                    allocator.mem2regSim(b);
                }
            }
            CrossBlockConst(f);
        }
        // GCM(f);
    }

    public void deleteDead(MipsBlock b) {
        setDefines(b.getInstructions());
        List<MipsInstruction> inst = b.getInstructions();
        List<MipsInstruction> delete = new ArrayList<>();
        for (MipsInstruction i : inst) {
            if (i.getTarget() != null && i.getTarget().charAt(0) == 'r' && uses.get(i.getTarget()).size() == 0) {
                delete.add(i);
                if (i instanceof AdduInstruction) {
                    if (((AdduInstruction) i).getLeft().charAt(0) != '$') {
                        uses.get(((AdduInstruction) i).getLeft()).remove(i);
                    }
                    if (((AdduInstruction) i).getRight() != null) {
                        uses.get(((AdduInstruction) i).getRight()).remove(i);
                    }
                } else if (i instanceof MipsDivInstruction) {
                    if (((MipsDivInstruction) i).getLeft().charAt(0) != '$') {
                        uses.get(((MipsDivInstruction) i).getLeft()).remove(i);
                    }
                    if (((MipsDivInstruction) i).getRight() != null) {
                        uses.get(((MipsDivInstruction) i).getRight()).remove(i);
                    }
                } else if (i instanceof MipsLoadLocal) {
                    if (((MipsLoadLocal) i).getBase() != null && ((MipsLoadLocal) i).getBase().charAt(0) != '$') {
                        uses.get(((MipsLoadLocal) i).getBase()).remove(i);
                    }
                } else if (i instanceof MipsMulInstruction) {
                    if (((MipsMulInstruction) i).getLeft().charAt(0) != '$') {
                        uses.get(((MipsMulInstruction) i).getLeft()).remove(i);
                    }
                    if (((MipsMulInstruction) i).getRight() != null && ((MipsMulInstruction) i).getRight().charAt(0) != '$') {
                        uses.get(((MipsMulInstruction) i).getRight()).remove(i);
                    }
                } else if (i instanceof MipsSubInstruction) {
                    if (((MipsSubInstruction) i).getLeft().charAt(0) != '$') {
                        uses.get(((MipsSubInstruction) i).getLeft()).remove(i);
                    }
                    if (((MipsSubInstruction) i).getRight() != null) {
                        uses.get(((MipsSubInstruction) i).getRight()).remove(i);
                    }
                } else if (i instanceof MoveInstruction) {
                    if (((MoveInstruction) i).getSrc().charAt(0) != '$') {
                        uses.get(((MoveInstruction) i).getSrc()).remove(i);
                    }
                } else if (i instanceof OrInstruction) {
                    if (((OrInstruction) i).getLeft().charAt(0) != '$') {
                        uses.get(((OrInstruction) i).getLeft()).remove(i);
                    }
                    if (((OrInstruction) i).getRight().charAt(0) != '$') {
                        uses.get(((OrInstruction) i).getRight()).remove(i);
                    }
                } else if (i instanceof SltInstruction) {
                    if (((SltInstruction) i).getLeft().charAt(0) != '$') {
                        uses.get(((SltInstruction) i).getLeft()).remove(i);
                    }
                    if (((SltInstruction) i).getRight().charAt(0) != '$') {
                        uses.get(((SltInstruction) i).getRight()).remove(i);
                    }
                }
            }
        }
        for (MipsInstruction i : delete) {
            inst.remove(i);
        }
    }

    public void deleteSame(MipsBlock b) {
        instructions = b.getInstructions();
        ultimate = new ArrayList<>();
        hashes = new HashMap<>();
        setDefines(instructions);
        for (MipsInstruction inst : instructions) {
            ultimate.add(inst);
            if (inst.getTarget() != null && inst.getTarget().charAt(0) == 'r' && !(inst instanceof MipsLoadInstruction)) {
                allocHashInst(inst);
            } else if (inst instanceof JalInstruction) {
                hashes.clear();
            }
        }
        b.setInstructions(ultimate);
    }

    public void allocHashInst(MipsInstruction i) {
        if (i instanceof MoveInstruction && ((MoveInstruction) i).getSrc().charAt(0) != 'r') {
            return;
        }
        MipsHashInst hashInst = new MipsHashInst(i.getClass(), i);
        if (hashes.containsKey(hashInst.hashCode())) {
            String newTar = hashes.get(hashInst.hashCode());
            String oldTar = i.getTarget();
            if (oldTar != null) {
                i.replace(oldTar, newTar);
                for (MipsInstruction m : uses.get(oldTar)) {
                    m.replace(oldTar, newTar);
                }
                uses.get(newTar).addAll(uses.get(oldTar));
            }
            ultimate.remove(ultimate.size() - 1);
        } else {
            hashes.put(hashInst.hashCode(), i.getTarget());
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
                    labels.get(b.getLabel()).add(((JInstruction) i).getLabel());
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

    //label为被克隆的
    public void addCloneBlock(String label, MipsBlock block, MipsFunction f) {
        List<MipsInstruction> inst = blocks.get(label).getInstructions();
        for (MipsInstruction i : inst) {
            try {
                MipsInstruction tmp = (MipsInstruction) i.clone();
                block.getInstructions().add(tmp);
                if (tmp.getTarget() != null) {
                    f.addLocalInt(tmp.getTarget());
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        allocator.setOffset(f.getOffset());
        if (inst.size() > 0) {
            inst.get(0).addCounter();
        }
    }

    public void removeUnusedJump(MipsFunction f) {
        buildLabelRelations(f);
        for (MipsBlock block : f.getBlocks()) {
            while (labels.get(block.getLabel()).size() == 1 && block.getInstructions().get(block.getInstructions().size() - 1) instanceof JInstruction) {
                String label = labels.get(block.getLabel()).get(0);
                // block.getInstructions().remove(block.getInstructions().size() - 1);
                block.getInstructions().removeIf(o -> o instanceof JInstruction);
                addCloneBlock(label, block, f);
                List<String> tmp = new ArrayList<>(labels.get(label));
                labels.put(block.getLabel(), tmp);
                fromss.get(label).remove(block.getLabel());
                for (String s : tmp) {
                    if (!fromss.get(s).contains(block.getLabel())) {
                        fromss.get(s).add(block.getLabel());
                    }
                }
            }
        }
        List<MipsBlock> delete = new ArrayList<>();
        int n = 0;
        while (n != f.getBlocks().size()) {
            delete.clear();
            n = f.getBlocks().size();
            for (Map.Entry<String, List<String>> entry : fromss.entrySet()) {
                if (entry.getValue().size() == 0) {
                    delete.add(blocks.get(entry.getKey()));
                    for (String s : labels.get(entry.getKey())) {
                        fromss.get(s).remove(entry.getKey());
                    }
                }
            }
            for (MipsBlock b : delete) {
                f.getBlocks().remove(b);
                fromss.remove(b.getLabel());
            }
        }

    }

    public void constSpread(MipsBlock b) {
        Map<String, Integer> consts = new HashMap<>();
        List<MipsInstruction> ultimate = new ArrayList<>();
        setDefines(b.getInstructions());
        ultimate.clear();
        List<MipsInstruction> inst = b.getInstructions();
        Map<String, Integer> constAddr = new HashMap<>();
        for (int j = 0; j < inst.size(); j++) {
            MipsInstruction i = inst.get(j);
            if (i instanceof LiInstruction) {
                consts.put(i.getTarget(), ((LiInstruction) i).getConst());
            } else if (i instanceof AdduInstruction) {
                if (consts.containsKey(((AdduInstruction) i).getLeft())) {
                    if (((AdduInstruction) i).getRight() == null) {
                        int res = consts.get(((AdduInstruction) i).getLeft()) + ((AdduInstruction) i).getNum();
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    } else if (consts.containsKey(((AdduInstruction) i).getRight())) {
                        int res = consts.get(((AdduInstruction) i).getLeft()) + consts.get(((AdduInstruction) i).getRight());
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    }
                } else if (constAddr.containsKey(((AdduInstruction) i).getLeft()) && ((AdduInstruction) i).getRight() == null) {
                    int res = constAddr.get(((AdduInstruction) i).getLeft()) + ((AdduInstruction) i).getNum();
                    ultimate.add(new AdduInstruction(Regs.$gp.toString(), res, i.getTarget()));
                    constAddr.put(i.getTarget(), res);
                    continue;
                }
            } else if (i instanceof MipsSubInstruction) {
                if (consts.containsKey(((MipsSubInstruction) i).getLeft())) {
                    if (((MipsSubInstruction) i).getRight() == null) {
                        int res = consts.get(((MipsSubInstruction) i).getLeft()) - ((MipsSubInstruction) i).getNum();
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    } else if (consts.containsKey(((MipsSubInstruction) i).getRight())) {
                        int res = consts.get(((MipsSubInstruction) i).getLeft()) - consts.get(((MipsSubInstruction) i).getRight());
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    }
                }
            } else if (i instanceof MipsMulInstruction) {
                if (consts.containsKey(((MipsMulInstruction) i).getLeft())) {
                    if (((MipsMulInstruction) i).getRight() == null) {
                        int res = consts.get(((MipsMulInstruction) i).getLeft()) * ((MipsMulInstruction) i).getNum();
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    } else if (consts.containsKey(((MipsMulInstruction) i).getRight())) {
                        int res = consts.get(((MipsMulInstruction) i).getLeft()) * consts.get(((MipsMulInstruction) i).getRight());
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    }
                } else if (((MipsMulInstruction) i).getRight() == null && ((MipsMulInstruction) i).getNum() == 0) {
                    int res = 0;
                    LiInstruction li = new LiInstruction(i.getTarget(), res);
                    consts.put(i.getTarget(), res);
                    ultimate.add(li);
                    continue;
                }
            } else if (i instanceof MipsDivInstruction) {
                if (consts.containsKey(((MipsDivInstruction) i).getLeft())) {
                    if (((MipsDivInstruction) i).getRight() == null) {
                        int res = consts.get(((MipsDivInstruction) i).getLeft()) / ((MipsDivInstruction) i).getNum();
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    } else if (consts.containsKey(((MipsDivInstruction) i).getRight())) {
                        int res = consts.get(((MipsDivInstruction) i).getLeft()) / consts.get(((MipsDivInstruction) i).getRight());
                        LiInstruction li = new LiInstruction(i.getTarget(), res);
                        consts.put(i.getTarget(), res);
                        ultimate.add(li);
                        continue;
                    }
                }
            } else if (i instanceof BeqInstruction) {
                if (consts.containsKey(((BeqInstruction) i).getRs())) {
                    if (((BeqInstruction) i).getRt() == null) {
                        if (consts.get(((BeqInstruction) i).getRs()) != ((BeqInstruction) i).getNum()) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BeqInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    } else if (consts.containsKey(((BeqInstruction) i).getRt())) {
                        if (consts.get(((BeqInstruction) i).getRs()) == consts.get(((BeqInstruction) i).getRt())) {
                            ultimate.add(new JInstruction(((BeqInstruction) i).getLabel()));
                            j++;
                            break;
                        } else {
                            continue;
                        }
                    }
                } else if (((BeqInstruction) i).getRt() != null && ((BeqInstruction) i).getRs().equals(((BeqInstruction) i).getRt())) {
                    ultimate.add(new JInstruction(((BeqInstruction) i).getLabel()));
                    j++;
                    break;
                }
            } else if (i instanceof BneInstruction) {
                if (consts.containsKey(((BneInstruction) i).getRs())) {
                    if (((BneInstruction) i).getRt() == null) {
                        if (consts.get(((BneInstruction) i).getRs()) != ((BneInstruction) i).getNum()) {
                            ultimate.add(new JInstruction(((BneInstruction) i).getLabel()));
                            j++;
                            break;
                        } else {
                            continue;
                        }
                    } else if (consts.containsKey(((BneInstruction) i).getRt())) {
                        if (consts.get(((BneInstruction) i).getRs()) != consts.get(((BneInstruction) i).getRt())) {
                            ultimate.add(new JInstruction(((BneInstruction) i).getLabel()));
                            j++;
                            break;
                        } else {
                            continue;
                        }
                    }
                } else if (((BneInstruction) i).getRt() != null && ((BneInstruction) i).getRs().equals(((BneInstruction) i).getRt())) {
                    continue;
                }
            } else if (i instanceof BgeInstruction) {
                if (consts.containsKey(((BgeInstruction) i).getRs())) {
                    if (((BgeInstruction) i).getRt() == null) {
                        if (consts.get(((BgeInstruction) i).getRs()) < ((BgeInstruction) i).getNum()) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BgeInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    } else if (consts.containsKey(((BgeInstruction) i).getRt())) {
                        if (consts.get(((BgeInstruction) i).getRs()) < consts.get(((BgeInstruction) i).getRt())) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BgeInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    }
                } else if (((BgeInstruction) i).getRt() != null && ((BgeInstruction) i).getRs().equals(((BgeInstruction) i).getRt())) {
                    ultimate.add(new JInstruction(((BgeInstruction) i).getLabel()));
                    j++;
                    break;
                }
            } else if (i instanceof BgtInstruction) {
                if (consts.containsKey(((BgtInstruction) i).getRs())) {
                    if (((BgtInstruction) i).getRt() == null) {
                        if (consts.get(((BgtInstruction) i).getRs()) <= ((BgtInstruction) i).getNum()) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BgtInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    } else if (consts.containsKey(((BgtInstruction) i).getRt())) {
                        if (consts.get(((BgtInstruction) i).getRs()) <= consts.get(((BgtInstruction) i).getRt())) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BgtInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    }
                } else if (((BgtInstruction) i).getRt() != null && ((BgtInstruction) i).getRs().equals(((BgtInstruction) i).getRt())) {
                    continue;
                }
            } else if (i instanceof BleInstruction) {
                if (consts.containsKey(((BleInstruction) i).getRs())) {
                    if (((BleInstruction) i).getRt() == null) {
                        if (consts.get(((BleInstruction) i).getRs()) > ((BleInstruction) i).getNum()) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BleInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    } else if (consts.containsKey(((BleInstruction) i).getRt())) {
                        if (consts.get(((BleInstruction) i).getRs()) > consts.get(((BleInstruction) i).getRt())) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BleInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    }
                } else if (((BleInstruction) i).getRt() != null && ((BleInstruction) i).getRs().equals(((BleInstruction) i).getRt())) {
                    ultimate.add(new JInstruction(((BleInstruction) i).getLabel()));
                    j++;
                    break;
                }
            } else if (i instanceof BltInstruction) {
                if (consts.containsKey(((BltInstruction) i).getRs())) {
                    if (((BltInstruction) i).getRt() == null) {
                        if (consts.get(((BltInstruction) i).getRs()) >= ((BltInstruction) i).getNum()) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BltInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    } else if (consts.containsKey(((BltInstruction) i).getRt())) {
                        if (consts.get(((BltInstruction) i).getRs()) >= consts.get(((BltInstruction) i).getRt())) {
                            continue;
                        } else {
                            ultimate.add(new JInstruction(((BltInstruction) i).getLabel()));
                            j++;
                            break;
                        }
                    }
                } else if (((BltInstruction) i).getRt() != null && ((BltInstruction) i).getRs().equals(((BltInstruction) i).getRt())) {
                    continue;
                }
            } else if (i instanceof SltInstruction) {
                if (consts.containsKey(((SltInstruction) i).getLeft()) && consts.containsKey(((SltInstruction) i).getRight())) {
                    if (consts.get(((SltInstruction) i).getLeft()) < consts.get(((SltInstruction) i).getRight())) {
                        ultimate.add(new LiInstruction(i.getTarget(), 1));
                        consts.put(i.getTarget(), 1);
                        continue;
                    } else {
                        ultimate.add(new LiInstruction(i.getTarget(), 0));
                        consts.put(i.getTarget(), 0);
                        continue;
                    }
                } else if (((SltInstruction) i).getRight() != null && ((SltInstruction) i).getLeft().equals(((SltInstruction) i).getRight())) {
                    ultimate.add(new LiInstruction(i.getTarget(), 0));
                    consts.put(i.getTarget(), 0);
                    continue;
                }
            } else if (i instanceof LaInstruction) {
                if (globalOffsets.containsKey(i.getLabel())) {
                    ultimate.add(new AdduInstruction(Regs.$gp.toString(), globalOffsets.get(i.getLabel()), i.getTarget()));
                    constAddr.put(i.getTarget(), globalOffsets.get(i.getLabel()));
                    continue;
                }
            }
            ultimate.add(i);
        }
        b.setInstructions(ultimate);
    }


    public void standardArrayStoreLi(MipsFunction f) {
        HashMap<String, Integer> maps = new HashMap<>();
        for (MipsBlock b : f.getBlocks()) {
            List<MipsInstruction> inst = b.getInstructions();
            setDefines(inst);
            List<MipsInstruction> delete = new ArrayList<>();
            List<MipsInstruction> tmp = new ArrayList<>();
            for (int i = 0; i < inst.size(); i++) {
                if (i < inst.size() - 3) {
                    if (inst.get(i) instanceof AdduInstruction &&
                            ((AdduInstruction) inst.get(i)).getLeft().equals(Regs.$sp.toString())) {
                        if (inst.get(i + 1) instanceof AdduInstruction &&
                                inst.get(i).getTarget().equals(((AdduInstruction) inst.get(i + 1)).getLeft())) {
                            if (inst.get(i + 2) instanceof LiInstruction && inst.get(i + 3)
                                    instanceof MipsStoreLocal && inst.get(i + 2).
                                    getTarget().equals(((MipsStoreLocal) inst.get(i + 3)).getSource()) && ((MipsStoreLocal) inst.get(i + 3)).getBase().equals(inst.get(i + 1).getTarget())) {
                                int off = ((AdduInstruction) inst.get(i + 1)).getNum() + ((AdduInstruction) inst.get(i)).getNum();
                                ((AdduInstruction) inst.get(i + 1)).setLeft(Regs.$sp.toString());
                                ((AdduInstruction) inst.get(i + 1)).setNum(off);
                                delete.add(inst.get(i));
                                delete.add(inst.get(i + 1));
                                tmp.add(inst.get(i));
                                tmp.add(inst.get(i + 1));
                                tmp.add(inst.get(i + 2));
                                MipsStoreLocal storeLocal = new MipsStoreLocal(((MipsStoreLocal) inst.get(i + 3)).getSource(), off, Regs.$sp.toString());
                                tmp.add(storeLocal);
                                maps.put(inst.get(i + 1).getTarget(), off);
                                i += 3;
                                continue;
                            }
                        }
                    }
                    tmp.add(inst.get(i));
                } else {
                    tmp.add(inst.get(i));
                }
            }
            setDefines(tmp);
            for (MipsInstruction i : tmp) {
                if (i instanceof MipsLoadLocal) {
                    if (maps.containsKey(((MipsLoadLocal) i).getBase())) {
                        ((MipsLoadLocal) i).setOffset(maps.get(((MipsLoadLocal) i).getBase()));
                        ((MipsLoadLocal) i).setBase(Regs.$sp.toString());
                    }
                } else if (i instanceof MipsStoreLocal) {
                    if (maps.containsKey(((MipsStoreLocal) i).getBase())) {
                        ((MipsStoreLocal) i).setOffset(maps.get(((MipsStoreLocal) i).getBase()));
                        ((MipsStoreLocal) i).setBase(Regs.$sp.toString());
                    }
                }
                if (i.getTarget() != null && i.getTarget().charAt(0) == 'r') {
                    List<MipsInstruction> use = uses.get(i.getTarget());
                    Boolean flag = true;
                    for (MipsInstruction u : use) {
                        if (u instanceof MipsLoadLocal || u instanceof MipsLoadGlobal) {
                            continue;
                        } else if (u instanceof MipsStoreLocal) {
                            if (((MipsStoreLocal) u).getSource().equals(i.getTarget())) {
                                flag = false;
                                break;
                            }
                        } else {
                            flag = false;
                            break;
                        }

                    }
                    if (!flag) {
                        delete.remove(i);
                    } else if (uses.get(i.getTarget()).size() == 0) {
                        delete.add(i);
                    }
                }
            }
            tmp.removeAll(delete);
            b.setInstructions(tmp);
        }
    }

    public void standardArrayStore(MipsFunction f) {
        HashMap<String, Integer> maps = new HashMap<>();
        for (MipsBlock b : f.getBlocks()) {
            List<MipsInstruction> inst = b.getInstructions();
            setDefines(inst);
            List<MipsInstruction> delete = new ArrayList<>();
            List<MipsInstruction> tmp = new ArrayList<>();
            for (int i = 0; i < inst.size(); i++) {
                if (i < inst.size() - 2) {
                    if (inst.get(i) instanceof AdduInstruction && ((AdduInstruction) inst.get(i)).getLeft().equals(Regs.$sp.toString())) {
                        if (inst.get(i + 1) instanceof AdduInstruction && inst.get(i).getTarget()
                                .equals(((AdduInstruction) inst.get(i + 1)).getLeft())) {
                            if (inst.get(i + 2) instanceof MipsStoreLocal && ((MipsStoreLocal) inst.get(i + 2)).getBase().equals(inst.get(i + 1).getTarget())) {
                                int off = ((AdduInstruction) inst.get(i + 1)).getNum() + ((AdduInstruction) inst.get(i)).getNum();
                                ((AdduInstruction) inst.get(i + 1)).setLeft(Regs.$sp.toString());
                                ((AdduInstruction) inst.get(i + 1)).setNum(off);
                                delete.add(inst.get(i));
                                delete.add(inst.get(i + 1));
                                tmp.add(inst.get(i));
                                tmp.add(inst.get(i + 1));
                                MipsStoreLocal storeLocal = new MipsStoreLocal(((MipsStoreLocal) inst.get(i + 2)).getSource(), off, Regs.$sp.toString());
                                tmp.add(storeLocal);
                                maps.put((inst.get(i + 1)).getTarget(), off);
                                i += 2;
                                continue;
                            }
                        }
                    }
                    tmp.add(inst.get(i));
                } else {
                    tmp.add(inst.get(i));
                }
            }

            setDefines(tmp);
            for (MipsInstruction i : tmp) {
                if (i instanceof MipsLoadLocal) {
                    if (maps.containsKey(((MipsLoadLocal) i).getBase())) {
                        ((MipsLoadLocal) i).setOffset(maps.get(((MipsLoadLocal) i).getBase()));
                        ((MipsLoadLocal) i).setBase(Regs.$sp.toString());
                    }
                } else if (i instanceof MipsStoreLocal) {
                    if (maps.containsKey(((MipsStoreLocal) i).getBase())) {
                        ((MipsStoreLocal) i).setOffset(maps.get(((MipsStoreLocal) i).getBase()));
                        ((MipsStoreLocal) i).setBase(Regs.$sp.toString());
                    }
                }
                if (i.getTarget() != null && i.getTarget().charAt(0) == 'r') {
                    List<MipsInstruction> use = uses.get(i.getTarget());
                    Boolean flag = true;
                    for (MipsInstruction u : use) {
                        if (u instanceof MipsLoadLocal || u instanceof MipsLoadGlobal) {
                            continue;
                        } else if (u instanceof MipsStoreLocal) {
                            if (((MipsStoreLocal) u).getSource().equals(i.getTarget())) {
                                flag = false;
                                break;
                            }
                        } else {
                            flag = false;
                            break;
                        }

                    }
                    if (!flag && delete.contains(i)) {
                        delete.remove(i);
                    } else if (uses.get(i.getTarget()).size() == 0) {
                        delete.add(i);
                    }
                }
            }
            tmp.removeAll(delete);
            b.setInstructions(tmp);
        }
    }

    public void standardArray(MipsFunction f) {
        standardArrayStoreLi(f);
        standardArrayStore(f);
        standardArrayLoad(f);
    }

    public void standardArrayLoad(MipsFunction f) {
        HashMap<String, Integer> maps = new HashMap<>();
        for (MipsBlock b : f.getBlocks()) {
            List<MipsInstruction> inst = b.getInstructions();
            setDefines(inst);
            List<MipsInstruction> tmp = new ArrayList<>();
            List<MipsInstruction> delete = new ArrayList<>();
            for (int i = 0; i < inst.size(); i++) {
                if (i < inst.size() - 2) {
                    if (inst.get(i) instanceof AdduInstruction && ((AdduInstruction) inst.get(i)).getLeft().equals(Regs.$sp.toString())) {
                        if (inst.get(i + 1) instanceof AdduInstruction && ((AdduInstruction) inst.get(i)).getTarget()
                                .equals(((AdduInstruction) inst.get(i + 1)).getLeft())) {
                            if (inst.get(i + 2) instanceof MipsLoadLocal && ((MipsLoadLocal) inst.get(i + 2)).getBase().equals(inst.get(i + 1).getTarget())) {
                                int off = ((AdduInstruction) inst.get(i + 1)).getNum() + ((AdduInstruction) inst.get(i)).getNum();
                                ((AdduInstruction) inst.get(i + 1)).setLeft(Regs.$sp.toString());
                                ((AdduInstruction) inst.get(i + 1)).setNum(off);
                                delete.add(inst.get(i));
                                delete.add(inst.get(i + 1));
                                tmp.add(inst.get(i));
                                tmp.add(inst.get(i + 1));
                                MipsLoadLocal loadLocal = new MipsLoadLocal((inst.get(i + 2)).getTarget(), off, Regs.$sp.toString());
                                tmp.add(loadLocal);
                                maps.put((inst.get(i + 1)).getTarget(), off);
                                i += 2;
                                continue;
                            }
                        }
                    }
                    tmp.add(inst.get(i));
                } else {
                    tmp.add(inst.get(i));
                }
            }
            setDefines(tmp);
            for (MipsInstruction i : tmp) {
                if (i instanceof MipsLoadLocal) {
                    if (maps.containsKey(((MipsLoadLocal) i).getBase())) {
                        ((MipsLoadLocal) i).setOffset(maps.get(((MipsLoadLocal) i).getBase()));
                        ((MipsLoadLocal) i).setBase(Regs.$sp.toString());
                    }
                } else if (i instanceof MipsStoreLocal) {
                    if (maps.containsKey(((MipsStoreLocal) i).getBase())) {
                        ((MipsStoreLocal) i).setOffset(maps.get(((MipsStoreLocal) i).getBase()));
                        ((MipsStoreLocal) i).setBase(Regs.$sp.toString());
                    }
                }
                if (i.getTarget() != null && i.getTarget().charAt(0) == 'r') {
                    List<MipsInstruction> use = uses.get(i.getTarget());
                    Boolean flag = true;
                    for (MipsInstruction u : use) {
                        if (u instanceof MipsLoadLocal || u instanceof MipsLoadGlobal) {
                            continue;
                        } else if (u instanceof MipsStoreLocal) {
                            if (((MipsStoreLocal) u).getSource().equals(i.getTarget())) {
                                flag = false;
                                break;
                            }
                        } else {
                            flag = false;
                            break;
                        }

                    }
                    if (!flag && delete.contains(i)) {
                        delete.remove(i);
                    } else if (uses.get(i.getTarget()).size() == 0) {
                        delete.add(i);
                    }
                }
            }
            tmp.removeAll(delete);
            b.setInstructions(tmp);
        }
    }

    public void setDefines(List<MipsInstruction> instructions) {
//        if (uses.size() == 10 && instructions.size() == 21) {
//            System.out.println("debug");
//        }
        uses.clear();
        for (MipsInstruction i : instructions) {
            if (i instanceof AdduInstruction) {
                if (((AdduInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((AdduInstruction) i).getTarget(), new ArrayList<>());
                }
                if (((AdduInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((AdduInstruction) i).getLeft()).add(i);
                }
                if (((AdduInstruction) i).getRight() != null) {
                    uses.get(((AdduInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof BeqInstruction) {
                if (((BeqInstruction) i).getRs().charAt(0) != '$') {
                    uses.get(((BeqInstruction) i).getRs()).add(i);
                }
                if (((BeqInstruction) i).getRt() != null) {
                    uses.get(((BeqInstruction) i).getRt()).add(i);
                }
            } else if (i instanceof BgeInstruction) {
                if (((BgeInstruction) i).getRs().charAt(0) != '$') {
                    uses.get(((BgeInstruction) i).getRs()).add(i);
                }
                if (((BgeInstruction) i).getRt() != null) {
                    uses.get(((BgeInstruction) i).getRt()).add(i);
                }
            } else if (i instanceof BgtInstruction) {
                if (((BgtInstruction) i).getRs().charAt(0) != '$') {
                    uses.get(((BgtInstruction) i).getRs()).add(i);
                }
                if (((BgtInstruction) i).getRt() != null) {
                    uses.get(((BgtInstruction) i).getRt()).add(i);
                }
            } else if (i instanceof BleInstruction) {
                if (((BleInstruction) i).getRs().charAt(0) != '$') {
                    uses.get(((BleInstruction) i).getRs()).add(i);
                }
                if (((BleInstruction) i).getRt() != null) {
                    uses.get(((BleInstruction) i).getRt()).add(i);
                }
            } else if (i instanceof BltInstruction) {
                if (((BltInstruction) i).getRs().charAt(0) != '$') {
                    uses.get(((BltInstruction) i).getRs()).add(i);
                }
                if (((BltInstruction) i).getRt() != null) {
                    uses.get(((BltInstruction) i).getRt()).add(i);
                }
            } else if (i instanceof BneInstruction) {
                if (((BneInstruction) i).getRs().charAt(0) != '$') {
                    uses.get(((BneInstruction) i).getRs()).add(i);
                }

                if (((BneInstruction) i).getRt() != null) {
                    uses.get(((BneInstruction) i).getRt()).add(i);
                }
            } else if (i instanceof LaInstruction) {
                if (((LaInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((LaInstruction) i).getTarget(), new ArrayList<>());
                }
            } else if (i instanceof LiInstruction) {
                if (((LiInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((LiInstruction) i).getTarget(), new ArrayList<>());
                }
            } else if (i instanceof MipsDivInstruction) {
                if (((MipsDivInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((MipsDivInstruction) i).getTarget(), new ArrayList<>());
                }
                if (((MipsDivInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((MipsDivInstruction) i).getLeft()).add(i);
                }
                if (((MipsDivInstruction) i).getRight() != null) {
                    uses.get(((MipsDivInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof MipsLoadLocal) {
                if (((MipsLoadLocal) i).getTarget().charAt(0) != '$') {
                    uses.put(((MipsLoadLocal) i).getTarget(), new ArrayList<>());
                }
                if (((MipsLoadLocal) i).getBase() != null && ((MipsLoadLocal) i).getBase().charAt(0) != '$') {
                    uses.get(((MipsLoadLocal) i).getBase()).add(i);
                }
            } else if (i instanceof MipsLoadGlobal) {
                if (((MipsLoadGlobal) i).getTarget().charAt(0) != '$') {
                    uses.put(((MipsLoadGlobal) i).getTarget(), new ArrayList<>());
                }
            } else if (i instanceof MipsMulInstruction) {
                if (((MipsMulInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((MipsMulInstruction) i).getTarget(), new ArrayList<>());
                }
                if (((MipsMulInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((MipsMulInstruction) i).getLeft()).add(i);
                }
                if (((MipsMulInstruction) i).getRight() != null && ((MipsMulInstruction) i).getRight().charAt(0) != '$') {
                    uses.get(((MipsMulInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof MipsStoreLocal) {
                if (((MipsStoreLocal) i).getBase() != null && ((MipsStoreLocal) i).getBase().charAt(0) != '$') {
                    uses.get(((MipsStoreLocal) i).getBase()).add(i);
                }
                if (((MipsStoreLocal) i).getSource().charAt(0) != '$') {
                    uses.get(((MipsStoreLocal) i).getSource()).add(i);
                }
            } else if (i instanceof MipsStoreGlobal) {
                if (((MipsStoreGlobal) i).getSource().charAt(0) != '$') {
                    uses.get(((MipsStoreGlobal) i).getSource()).add(i);
                }
            } else if (i instanceof MipsSubInstruction) {
                if (((MipsSubInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
                }
                if (((MipsSubInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((MipsSubInstruction) i).getLeft()).add(i);
                }
                if (((MipsSubInstruction) i).getRight() != null) {
                    uses.get(((MipsSubInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof MoveInstruction) {
                if (((MoveInstruction) i).getDes().charAt(0) != '$') {
                    uses.put(((MoveInstruction) i).getDes(), new ArrayList<>());
                }
                if (((MoveInstruction) i).getSrc().charAt(0) != '$') {
                    uses.get(((MoveInstruction) i).getSrc()).add(i);
                }
            } else if (i instanceof OrInstruction) {
                if (i.getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
                }
                if (((OrInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((OrInstruction) i).getLeft()).add(i);
                }
                if (((OrInstruction) i).getRight().charAt(0) != '$') {
                    uses.get(((OrInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof SltInstruction) {
                if (i.getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
                }
                if (((SltInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((SltInstruction) i).getLeft()).add(i);
                }
                if (((SltInstruction) i).getRight().charAt(0) != '$') {
                    uses.get(((SltInstruction) i).getRight()).add(i);
                }
            }
        }
    }

    public Map<Integer, Integer> buildConstCur(MipsBlock b) {
        HashMap<Integer, Integer> curOffConst = new HashMap<>();
        List<String> suspicious = new ArrayList<>();
        List<MipsInstruction> insts = b.getInstructions();
        for (int k = 0; k < insts.size(); k++) {
            if (insts.get(k) instanceof MipsStoreLocal && ((MipsStoreLocal) insts.get(k)).getBase().equals(Regs.$sp.toString()) && ((MipsStoreLocal) insts.get(k)).getOffset() < 0) {
                if (curOffConst.containsKey(((MipsStoreLocal) insts.get(k)).getOffset())) {
                    curOffConst.remove(((MipsStoreLocal) insts.get(k)).getOffset());
                }
            } else if (insts.get(k) instanceof LiInstruction && insts.get(k + 1) instanceof MipsStoreLocal && ((MipsStoreLocal) insts.get(k + 1)).getBase().equals(Regs.$sp.toString())) {
                curOffConst.put(((MipsStoreLocal) insts.get(k + 1)).getOffset(), ((LiInstruction) insts.get(k)).getConst());
                k++;
            } else if (insts.get(k) instanceof AdduInstruction && ((AdduInstruction) insts.get(k)).getRight() != null && ((AdduInstruction) insts.get(k)).getLeft().equals(Regs.$sp.toString())) {
                suspicious.add(insts.get(k).getTarget());
            } else if (insts.get(k) instanceof MipsStoreLocal && ((MipsStoreLocal) insts.get(k)).getBase().equals(Regs.$sp.toString())) {
                if (curOffConst.containsKey(((MipsStoreLocal) insts.get(k)).getOffset())) {
                    curOffConst.remove(((MipsStoreLocal) insts.get(k)).getOffset());
                }
            } else if (insts.get(k) instanceof MipsStoreLocal && suspicious.contains(((MipsStoreLocal) insts.get(k)).getBase())) {
                curOffConst.remove(((MipsStoreLocal) insts.get(k)).getOffset());
            }
        }
        return curOffConst;
    }

    public void CrossBlockConst(MipsFunction f) {
        Map<Integer, Integer> consts1 = new HashMap<>();//key为相对于$sp的偏移量，value为常量值
        buildLabelRelations(f);
        for (MipsBlock b : f.getBlocks()) {
            if (!labels.containsKey(b.getLabel())) {
                continue;
            }
            consts1 = buildConstCur(b);
            for (String s : labels.get(b.getLabel())) {
                int flag = 2;
                for (String s1 : fromss.get(s)) {
                    if (s1.equals(s)) {
                        flag = 1;    //自己到自己
                    } else if (!s1.equals(b.getLabel())) {
                        flag = 0;
                    }
                }
                if (flag == 1) {
                    List<MipsInstruction> insts = blocks.get(s).getInstructions();
                    List<String> suspicious = new ArrayList<>();
                    for (int k = 0; k < insts.size(); k++) {
                        if (insts.get(k) instanceof MipsStoreLocal && ((MipsStoreLocal) insts.get(k)).getBase().equals(Regs.$sp.toString()) && ((MipsStoreLocal) insts.get(k)).getOffset() < 0) {
                            flag = 0;  //存参数，要调用含参函数
                            break;
                        } else if (insts.get(k) instanceof LiInstruction && insts.get(k + 1) instanceof MipsStoreLocal && ((MipsStoreLocal) insts.get(k + 1)).getBase().equals(Regs.$sp.toString())) {
                            if (consts1.containsKey(((MipsStoreLocal) insts.get(k + 1)).getOffset())) {
                                if (consts1.get(((MipsStoreLocal) insts.get(k + 1)).getOffset()) != ((LiInstruction) insts.get(k)).getConst()) {
                                    consts1.remove(((MipsStoreLocal) insts.get(k + 1)).getOffset());
                                }
                            }
                        } else if (insts.get(k) instanceof AdduInstruction && ((AdduInstruction) insts.get(k)).getRight() != null && ((AdduInstruction) insts.get(k)).getLeft().equals(Regs.$sp.toString())) {
                            suspicious.add(insts.get(k).getTarget());
                        } else if (insts.get(k) instanceof MipsStoreLocal && ((MipsStoreLocal) insts.get(k)).getBase().equals(Regs.$sp.toString())) {
                            if (consts1.containsKey(((MipsStoreLocal) insts.get(k)).getOffset())) {
                                consts1.remove(((MipsStoreLocal) insts.get(k)).getOffset());
                            }
                        } else if (insts.get(k) instanceof MipsStoreLocal && suspicious.contains(((MipsStoreLocal) insts.get(k)).getBase())) {
                            flag = 0;
                            break;
                        }
                    }
                }
                List<String> suspicious = new ArrayList<>();
                if (flag != 0) {
                    List<MipsInstruction> instructions = blocks.get(s).getInstructions();
                    for (int i = 0; i < instructions.size(); i++) {
                        if (instructions.get(i) instanceof MipsLoadLocal && ((MipsLoadLocal) instructions.get(i)).getBase().equals(Regs.$sp.toString())) {
                            if (consts1.containsKey(((MipsLoadLocal) instructions.get(i)).getOffset())) {
                                LiInstruction liInstruction = new LiInstruction((instructions.get(i)).getTarget(), consts1.get(((MipsLoadLocal) instructions.get(i)).getOffset()));
                                instructions.remove(i);
                                instructions.add(i, liInstruction);
                            }
                        } else if (instructions.get(i) instanceof MipsStoreLocal && ((MipsStoreLocal) instructions.get(i)).getBase().equals(Regs.$sp.toString())) {
                            if (consts1.containsKey(((MipsStoreLocal) instructions.get(i)).getOffset())) {
                                consts1.remove(((MipsStoreLocal) instructions.get(i)).getOffset());
                            }
                        } else if (instructions.get(i) instanceof AdduInstruction && ((AdduInstruction) instructions.get(i)).getRight() != null && ((AdduInstruction) instructions.get(i)).getLeft().equals(Regs.$sp.toString())) {
                            suspicious.add(instructions.get(i).getTarget());
                        } else if (instructions.get(i) instanceof MipsStoreLocal && suspicious.contains(((MipsStoreLocal) instructions.get(i)).getBase())) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void GCM(MipsFunction f) {
        List<MipsBlock> blocks = f.getBlocks();
        Map<String, MipsBlock> blockmap = new HashMap<>();
        for (MipsBlock b : blocks) {
            blockmap.put(b.getLabel(), b);
        }
        List<MipsBlock> trues = new ArrayList<>();
        trues.add(blocks.get(0));
        MipsBlock start = blocks.get(0);
        while (start.getInstructions().size() > 0) {
            MipsInstruction j = start.getInstructions().get(start.getInstructions().size() - 1);
            MipsInstruction br = start.getInstructions().get(start.getInstructions().size() - 2);
            if (j instanceof JInstruction) {
                start = blockmap.get(br.getLabel());
                while (trues.contains(start)) {
                    List<MipsBlock> circle = trues.subList(trues.indexOf(start), trues.size());
                    int index = trues.indexOf(start);
                    moveInst(circle, trues.get(index - 1));
                    start = blockmap.get(j.getLabel());
                }
                trues.add(start);
            } else {
                break;
            }
        }
    }

    public void moveInst(List<MipsBlock> blocks, MipsBlock head) {
        List<String> noChange = new ArrayList<>();
        List<MipsInstruction> potential = new ArrayList<>();
        List<Integer> noWrite = new ArrayList<>();
        List<String> consts = new ArrayList<>();
        HashMap<Integer, List<String>> writeContents = new HashMap<>();
        List<Integer> write = new ArrayList<>();
        for (MipsBlock b : blocks) {
            for (MipsInstruction i : b.getInstructions()) {
                if (i instanceof MipsStoreLocal) {
                    if (((MipsStoreLocal) i).getBase().equals(Regs.$sp.toString())) {
                        write.add(((MipsStoreLocal) i).getOffset());
                        if (writeContents.containsKey(((MipsStoreLocal) i).getOffset())) {
                            writeContents.get(((MipsStoreLocal) i).getOffset()).add(i.getTarget());
                        } else {
                            List<String> temp = new ArrayList<>();
                            temp.add(i.getTarget());
                            writeContents.put(((MipsStoreLocal) i).getOffset(), temp);
                        }
                    }
                } else if (i instanceof LiInstruction) {
                    consts.add(i.getTarget());
                    noChange.add(i.getTarget());
                    potential.add(i);
                }
            }
        }
        for (MipsBlock b : blocks) {
            for (MipsInstruction i : b.getInstructions()) {
                if (i instanceof MipsLoadLocal) {
                    if (((MipsLoadLocal) i).getBase().equals(Regs.$sp.toString())) {
                        if (!write.contains(((MipsLoadLocal) i).getOffset())) {
                            noChange.add(i.getTarget());
                            noWrite.add(((MipsLoadLocal) i).getOffset());
                            potential.add(i);
                        }
                    }
                } else if (i instanceof AdduInstruction) {
                    if (noChange.contains(((AdduInstruction) i).getLeft())) {
                        if (((AdduInstruction) i).getRight() == null || noChange.contains(((AdduInstruction) i).getRight())) {
                            noChange.add(i.getTarget());
                            potential.add(i);
                        }
                    }
                } else if (i instanceof MipsSubInstruction) {
                    if (noChange.contains(((MipsSubInstruction) i).getLeft())) {
                        if (((MipsSubInstruction) i).getRight() == null || noChange.contains(((MipsSubInstruction) i).getRight())) {
                            noChange.add(i.getTarget());
                            potential.add(i);
                        }
                    }
                } else if (i instanceof MipsMulInstruction) {
                    if (noChange.contains(((MipsMulInstruction) i).getLeft())) {
                        if (((MipsMulInstruction) i).getRight() == null || noChange.contains(((MipsMulInstruction) i).getRight())) {
                            noChange.add(i.getTarget());
                            potential.add(i);
                        }
                    }
                } else if (i instanceof MipsDivInstruction) {
                    if (noChange.contains(((MipsDivInstruction) i).getLeft())) {
                        if (((MipsDivInstruction) i).getRight() == null || noChange.contains(((MipsDivInstruction) i).getRight())) {
                            noChange.add(i.getTarget());
                            potential.add(i);
                        }
                    }
                } else if (i instanceof OrInstruction) {
                    if (noChange.contains(((OrInstruction) i).getLeft())) {
                        if (((OrInstruction) i).getRight() == null || noChange.contains(((OrInstruction) i).getRight())) {
                            noChange.add(i.getTarget());
                            potential.add(i);
                        }
                    }
                } else if (i instanceof MipsStoreLocal) {
                    if (((MipsStoreLocal) i).getBase().equals(Regs.$sp.toString())) {
                        if (noChange.contains(((MipsStoreLocal) i).getSource())) {
                            potential.add(i);
                        }
                    }
                }
            }
        }
        if (head.getLabel().equals("label208")) {
            int k = 0;
        }
        for (MipsBlock b : blocks) {
            setDefines(b.getInstructions());
            int size = -1;
            List<MipsInstruction> deletes = new ArrayList<>();
            while (deletes.size() != size) {
                size = deletes.size();
                for (MipsInstruction i : potential) {
                    if (i instanceof MipsStoreLocal) {
                        if (b.getInstructions().contains(i)) {
                            deletes.add(i);
                            b.getInstructions().remove(i);
                        }
                    } else if (uses.containsKey(i.getTarget())) {
                        if (deletes.containsAll(uses.get(i.getTarget()))) {
                            if (b.getInstructions().contains(i)) {
                                b.getInstructions().remove(i);
                                deletes.add(i);
                            }
                        }
                    }
                }
            }
        }
        for (MipsInstruction i : potential) {
            int size = head.getInstructions().size();
            try {
                head.getInstructions().add(size - 1, (MipsInstruction) i.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
}
