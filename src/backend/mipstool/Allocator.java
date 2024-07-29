package backend.mipstool;

import backend.mipsinstruction.*;
import backend.mipsnode.MipsBlock;
import backend.mipsnode.MipsData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Allocator {
    private boolean isOpt;
    private Map<Regs, String> users;      //value为当前使用该物理寄存器的虚拟寄存器
    private Map<String, String> virtual;  //key为中间变量，value为虚拟寄存器
    private Map<String, String> middle;   //key为虚拟寄存器reg+count
    private Map<String, MipsData> globalDatas;
    private Map<String, Integer> offset;
    private List<Regs> regs;
    private List<MipsInstruction> ultimate;
    private MipsBlock curBlock;
    private HashMap<String, List<MipsInstruction>> uses;
    private long count;
    private int index;
    private String last;
    private List<MipsInstruction> save_delete;
    private Map<String, String> save_global;
    private List<String> sus;

    public Allocator(boolean isOpt) {
        this.isOpt = isOpt;
        users = new HashMap<>();
        virtual = new HashMap<>();
        sus = new ArrayList<>();
        middle = new HashMap<>();
        regs = new ArrayList<>();
        uses = new HashMap<>();
        count = 0;
        save_global = new HashMap<>();
        if (!isOpt) {
            count = 0;
            users.put(Regs.$t0, "");
            regs.add(Regs.$t0);
            users.put(Regs.$t1, "");
            regs.add(Regs.$t1);
            users.put(Regs.$t2, "");
            regs.add(Regs.$t2);
            users.put(Regs.$t3, "");
            regs.add(Regs.$t3);
            users.put(Regs.$t4, "");
            regs.add(Regs.$t4);
            users.put(Regs.$t5, "");
            regs.add(Regs.$t5);
            users.put(Regs.$t6, "");
            regs.add(Regs.$t6);
            users.put(Regs.$t7, "");
            regs.add(Regs.$t7);
            users.put(Regs.$t8, "");
            regs.add(Regs.$t8);
            users.put(Regs.$t9, "");
            regs.add(Regs.$t9);
            users.put(Regs.$s0, "");
            regs.add(Regs.$s0);
            users.put(Regs.$s1, "");
            regs.add(Regs.$s1);
            users.put(Regs.$s2, "");
            regs.add(Regs.$s2);
            users.put(Regs.$s3, "");
            regs.add(Regs.$s3);
            users.put(Regs.$s4, "");
            regs.add(Regs.$s4);
            users.put(Regs.$s5, "");
            regs.add(Regs.$s5);
            users.put(Regs.$s6, "");
            regs.add(Regs.$s6);
            users.put(Regs.$s7, "");
            regs.add(Regs.$s7);
        }
    }

    private String allocVirtual() {
        last = "reg" + count;
        return "reg" + count++;
    }

    public String allocVirtual(String name) {
        if (virtual.containsKey(name)) {
            return virtual.get(name);
        } else {
            String ret = allocVirtual();
            middle.put(ret, name);
            virtual.put(name, ret);
            return ret;
        }
    }

    public void load(String id) {
        if (virtual.containsKey(id)) {
            last = virtual.get(id);
        }
    }

    public String getLast() {
        return last;
    }

    public void setGlobalDatas(Map<String, MipsData> globalDatas) {
        this.globalDatas = globalDatas;
    }

    public void setOffset(Map<String, Integer> offset) {
        this.offset = offset;
    }

    public String allocReg(String s, boolean isTarget) {
        if (s == null || s.charAt(0) == '@' || s.charAt(0) == '$') {
            return s;
        } else {
            if (users.containsValue(s)) {
                for (Map.Entry<Regs, String> entry : users.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().equals(s)) {
                        return entry.getKey().toString();
                    }
                }
            } else {
                String tar = null;
                for (Map.Entry<Regs, String> entry : users.entrySet()) {
                    if (entry.getValue().equals("")) {
                        users.put(entry.getKey(), s);
                        tar = entry.getKey().toString();
                        break;
                    }
                }
                if (tar == null) {
                    tar = regs.get(index).toString();
                    if (offset.containsKey(middle.get(users.get(regs.get(index))))) {
                        MipsStoreLocal store = new MipsStoreLocal(regs.get(index).toString(),
                                offset.get(middle.get(users.get(regs.get(index)))), Regs.$sp.toString());
                        ultimate.add(store);
                    }
                    users.put(regs.get(index), s);
                }
                if (!isTarget) {
                    if (save_global.containsKey(s)) {
                        LaInstruction load = new LaInstruction(tar, save_global.get(s));
                        ultimate.add(load);
                    } else {
                        if (offset.containsKey(s)) {
                            MipsLoadLocal load = new MipsLoadLocal(tar,
                                    offset.get(s), Regs.$sp.toString());
                            ultimate.add(load);
                        } else {
                            MipsLoadLocal load = new MipsLoadLocal(tar,
                                    offset.get(middle.get(s)), Regs.$sp.toString());
                            ultimate.add(load);
                        }
                    }
                }
                index++;
                index %= regs.size();
                return tar;
            }
        }
        return null;
    }

    public boolean contains(String s) {
        return virtual.containsKey(s);
    }

    public void clear() {
        for (Map.Entry<Regs, String> entry : users.entrySet()) {
            if (!entry.getValue().equals("")) {
                if (offset.containsKey(middle.get(entry.getValue()))) {
                    MipsStoreLocal store = new MipsStoreLocal(entry.getKey().toString(),
                            offset.get(middle.get(entry.getValue())), Regs.$sp.toString());
                    ultimate.add(store);
                } else if (offset.containsKey(entry.getValue())) {
                    MipsStoreLocal store = new MipsStoreLocal(entry.getKey().toString(),
                            offset.get(entry.getValue()), Regs.$sp.toString());
                    ultimate.add(store);
                }
            }
        }
        users.replaceAll((key, value) -> "");
    }


    public void setDefines(List<MipsInstruction> instructions) {
        users.replaceAll((key, value) -> "");
        uses.clear();
        for (MipsInstruction i : instructions) {
            if (i instanceof AdduInstruction) {
                if (i.getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
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
                save_global.put(i.getTarget(), ((LaInstruction) i).getLabel());
                if (i.getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
                }
            } else if (i instanceof LiInstruction) {
                if (i.getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
                }
            } else if (i instanceof MipsDivInstruction) {
                if (i.getTarget().charAt(0) != '$') {
                    uses.put(i.getTarget(), new ArrayList<>());
                }
                if (((MipsDivInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((MipsDivInstruction) i).getLeft()).add(i);
                }
                if (((MipsDivInstruction) i).getRight() != null) {
                    uses.get(((MipsDivInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof MipsLoadLocal) {
                if (((MipsLoadLocal) i).getTarget().charAt(0) != '$') {
                    if(i.getTarget().equals("reg398_52_53")){
                        int a=2;
                    }
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
                    uses.put(i.getTarget(), new ArrayList<>());
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
                    uses.put(((MipsSubInstruction) i).getTarget(), new ArrayList<>());
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
                if (((OrInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((OrInstruction) i).getTarget(), new ArrayList<>());
                }
                if (((OrInstruction) i).getLeft().charAt(0) != '$') {
                    uses.get(((OrInstruction) i).getLeft()).add(i);
                }
                if (((OrInstruction) i).getRight().charAt(0) != '$') {
                    uses.get(((OrInstruction) i).getRight()).add(i);
                }
            } else if (i instanceof SltInstruction) {
                if (((SltInstruction) i).getTarget().charAt(0) != '$') {
                    uses.put(((SltInstruction) i).getTarget(), new ArrayList<>());
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

    public void deleteUse(String t, MipsInstruction i) {
        if (t == null || t.charAt(0) != 'r') {
            return;
        }
        if (uses.containsKey(t)) {
            uses.get(t).remove(i);
        }
        if (uses.get(t).isEmpty()) {
            for (Map.Entry<Regs, String> entry : users.entrySet()) {
                if (!entry.getValue().equals("") && entry.getValue().equals(t)) {
                    entry.setValue("");
                }
            }
        }
    }

    public void buildSus(MipsBlock b) {
        List<MipsInstruction> inst = b.getInstructions();
        sus.clear();
        for (int i = 0; i < inst.size() - 1; i++) {
            //if (inst.get(i) instanceof MipsMulInstruction && ((MipsMulInstruction) inst.get(i)).getNum() == 2) {
            if (inst.get(i + 1) instanceof AdduInstruction && ((AdduInstruction) inst.get(i + 1)).getRight() != null) {
                sus.add(inst.get(i + 1).getTarget());
            }
            // }
        }
    }

    public void mem2regSim(MipsBlock b) {
        Map<Integer, List<MipsInstruction>> insts = new HashMap<>();
        List<MipsInstruction> inst = b.getInstructions();
        Map<String, List<MipsInstruction>> global_addrs = new HashMap<>();
        setDefines(inst);
        save_delete = new ArrayList<>();
        buildSus(b);
        for (MipsInstruction i : inst) {
            if (i instanceof MipsLoadLocal && ((MipsLoadLocal) i).getBase().equals(Regs.$sp.toString()) && ((MipsLoadLocal) i).getTarget().charAt(0) == 'r') {
                int off = ((MipsLoadLocal) i).getOffset();
                if (insts.containsKey(off)) {
                    insts.get(off).add(i);
                } else {
                    List<MipsInstruction> tmp = new ArrayList<>();
                    tmp.add(i);
                    insts.put(off, tmp);
                }
            } else if (i instanceof MipsStoreLocal && ((MipsStoreLocal) i).getBase().equals(Regs.$sp.toString()) && ((MipsStoreLocal) i).getSource().charAt(0) == 'r') {
                int off = ((MipsStoreLocal) i).getOffset();
                if (insts.containsKey(off)) {
                    insts.get(off).add(i);
                } else {
                    List<MipsInstruction> tmp = new ArrayList<>();
                    tmp.add(i);
                    insts.put(off, tmp);
                }
            } else if (i instanceof MipsStoreLocal && ((MipsStoreLocal) i).getSource().charAt(0) == 'r') {
                String base = ((MipsStoreLocal) i).getBase();
                if (sus.contains(base)) {
                    transfer(insts);
                    insts.clear();
                    transferGlobal(global_addrs);
                    global_addrs.clear();
                }
                if (global_addrs.containsKey(base)) {
                    global_addrs.get(base).add(i);
                } else {
                    List<MipsInstruction> tmp = new ArrayList<>();
                    tmp.add(i);
                    global_addrs.put(base, tmp);
                }
            } else if (i instanceof MipsLoadLocal && i.getTarget().charAt(0) == 'r') {
                String base = ((MipsLoadLocal) i).getBase();
                if (sus.contains(base)) {
                    transfer(insts);
                    insts.clear();
                    transferGlobal(global_addrs);
                    global_addrs.clear();
                }
                if (global_addrs.containsKey(base)) {
                    global_addrs.get(base).add(i);
                } else {
                    List<MipsInstruction> tmp = new ArrayList<>();
                    tmp.add(i);
                    global_addrs.put(base, tmp);
                }
            } else if (i instanceof JalInstruction) {
                transfer(insts);
                insts.clear();
                transferGlobal(global_addrs);
                global_addrs.clear();
            }
        }
        transfer(insts);
        insts.clear();
        transferGlobal(global_addrs);
        global_addrs.clear();
        b.getInstructions().removeAll(save_delete);
        save_delete.clear();
    }


    public void transferGlobal(Map<String, List<MipsInstruction>> insts) {
        for (Map.Entry<String, List<MipsInstruction>> entry : insts.entrySet()) {
            List<MipsInstruction> tmp = entry.getValue();
            int type = 0;  //0:init 1:last is loadd 2:last is store
            MipsStoreLocal last_store = null;
            MipsLoadLocal last_load = null;
            List<MipsInstruction> delete = new ArrayList<>();
            if (tmp.size() == 1) {
                continue;
            } else {
                MipsInstruction inst = tmp.get(0);
                if (inst instanceof MipsLoadLocal) {
                    type = 1;
                    last_load = (MipsLoadLocal) inst;
                } else {
                    type = 2;
                    last_store = (MipsStoreLocal) inst;
                    delete.add(inst);
                }
                for (int i = 1; i < tmp.size(); i++) {
                    inst = tmp.get(i);
                    if (type == 1) {
                        if (inst instanceof MipsLoadLocal) {
                            if (last_store != null) {
                                for (MipsInstruction m : uses.get(inst.getTarget())) {
                                    m.replace(inst.getTarget(), last_store.getSource());
                                    uses.get(last_store.getSource()).add(m);
                                }
                                delete.add(inst);
                            } else {
                                for (MipsInstruction m : uses.get(inst.getTarget())) {
                                    m.replace(inst.getTarget(), last_load.getTarget());
                                    uses.get(last_load.getTarget()).add(m);
                                }
                                delete.add(inst);
                            }
                        } else if (inst instanceof MipsStoreLocal) {
                            type = 2;
                            last_store = (MipsStoreLocal) inst;
                            delete.add(inst);
                        }
                    } else if (type == 2) {
                        if (inst instanceof MipsLoadLocal) {
                            type = 1;
                            for (MipsInstruction m : uses.get(inst.getTarget())) {
                                m.replace(inst.getTarget(), last_store.getSource());
                                uses.get(last_store.getSource()).add(m);
                            }
                            delete.add(inst);
                        } else if (inst instanceof MipsStoreLocal) {
                            last_store = (MipsStoreLocal) inst;
                            delete.add(inst);
                        }
                    }
                }
            }
            delete.remove(last_store);
            save_delete.addAll(delete);
        }
    }

    public void transfer(Map<Integer, List<MipsInstruction>> insts) {
        for (Map.Entry<Integer, List<MipsInstruction>> entry : insts.entrySet()) {
            List<MipsInstruction> tmp = entry.getValue();
            int type = 0;  //0:init 1:last is loadd 2:last is store
            MipsStoreLocal last_store = null;
            MipsLoadLocal last_load = null;
            List<MipsInstruction> delete = new ArrayList<>();
            if (tmp.size() == 1) {
                continue;
            } else {
                MipsInstruction inst = tmp.get(0);
                if (inst instanceof MipsLoadLocal) {
                    type = 1;
                    last_load = (MipsLoadLocal) inst;
                } else {
                    type = 2;
                    last_store = (MipsStoreLocal) inst;
                    delete.add(inst);
                }
                for (int i = 1; i < tmp.size(); i++) {
                    inst = tmp.get(i);
                    if (type == 1) {
                        if (inst instanceof MipsLoadLocal) {
                            if (last_store != null) {
                                for (MipsInstruction m : uses.get(((MipsLoadLocal) inst).getTarget())) {
                                    m.replace(((MipsLoadLocal) inst).getTarget(), last_store.getSource());
                                    uses.get(last_store.getSource()).add(m);
                                }
                                delete.add(inst);
                            } else {
                                for (MipsInstruction m : uses.get(((MipsLoadLocal) inst).getTarget())) {
                                    m.replace(((MipsLoadLocal) inst).getTarget(), last_load.getTarget());
                                    uses.get(last_load.getTarget()).add(m);
                                }
                                delete.add(inst);
                            }
                        } else if (inst instanceof MipsStoreLocal) {
                            type = 2;
                            last_store = (MipsStoreLocal) inst;
                            delete.add(inst);
                        }
                    } else if (type == 2) {
                        if (inst instanceof MipsLoadLocal) {
                            type = 1;
                            for (MipsInstruction m : uses.get(((MipsLoadLocal) inst).getTarget())) {
                                m.replace(((MipsLoadLocal) inst).getTarget(), last_store.getSource());
                                uses.get(last_store.getSource()).add(m);
                            }
                            delete.add(inst);
                        } else if (inst instanceof MipsStoreLocal) {
                            last_store = (MipsStoreLocal) inst;
                            delete.add(inst);
                        }
                    }
                }
            }
            delete.remove(last_store);
            save_delete.addAll(delete);
        }
    }

    public boolean isReal(String s) {
        if (offset.containsKey(s)) {
            return true;
        } else if (globalDatas.containsKey(s)) {
            return true;
        }
        return false;
    }

    public List<MipsInstruction> allocReg(MipsBlock block) {
        users.replaceAll((key, value) -> "");
        index = 0;
        String s1, s2;
        List<MipsInstruction> instructions = block.getInstructions();
        setDefines(instructions);
        ultimate = new ArrayList<>();
        curBlock = block;
        for (MipsInstruction inst : instructions) {
            if (inst instanceof AdduInstruction) {
                if (inst.getTarget().equals(Regs.$sp.toString())) {
                    if (((AdduInstruction) inst).getNum() < 0) {
                        ((AdduInstruction) inst).setNum(curBlock.getFunction().getSize() * -1);
                    } else {
                        ((AdduInstruction) inst).setNum(curBlock.getFunction().getSize());
                    }
                }
                s1 = ((AdduInstruction) inst).getLeft();
                ((AdduInstruction) inst).setLeft(allocReg(((AdduInstruction) inst).getLeft(), false));
                s2 = ((AdduInstruction) inst).getRight();
                ((AdduInstruction) inst).setRight(allocReg(((AdduInstruction) inst).getRight(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                ((AdduInstruction) inst).setTarget(allocReg(((AdduInstruction) inst).getTarget(), true));
            } else if (inst instanceof MipsSubInstruction) {
                if (inst.getTarget().equals(Regs.$sp.toString())) {
                    ((MipsSubInstruction) inst).setNum(curBlock.getFunction().getSize());
                }
                s1 = ((MipsSubInstruction) inst).getLeft();
                ((MipsSubInstruction) inst).setLeft(allocReg(((MipsSubInstruction) inst).getLeft(), false));
                s2 = ((MipsSubInstruction) inst).getRight();
                ((MipsSubInstruction) inst).setRight(allocReg(((MipsSubInstruction) inst).getRight(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                ((MipsSubInstruction) inst).setTarget(allocReg(((MipsSubInstruction) inst).getTarget(), true));
            } else if (inst instanceof MipsDivInstruction) {
                s1 = ((MipsDivInstruction) inst).getLeft();
                ((MipsDivInstruction) inst).setLeft(allocReg(((MipsDivInstruction) inst).getLeft(), false));
                s2 = ((MipsDivInstruction) inst).getRight();
                ((MipsDivInstruction) inst).setRight(allocReg(((MipsDivInstruction) inst).getRight(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                ((MipsDivInstruction) inst).setTarget(allocReg(((MipsDivInstruction) inst).getTarget(), true));
            } else if (inst instanceof MipsMulInstruction) {
                s1 = ((MipsMulInstruction) inst).getLeft();
                ((MipsMulInstruction) inst).setLeft(allocReg(((MipsMulInstruction) inst).getLeft(), false));
                s2 = ((MipsMulInstruction) inst).getRight();
                ((MipsMulInstruction) inst).setRight(allocReg(((MipsMulInstruction) inst).getRight(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                ((MipsMulInstruction) inst).setTarget(allocReg(((MipsMulInstruction) inst).getTarget(), true));
            } else if (inst instanceof BeqInstruction) {
                s1 = ((BeqInstruction) inst).getRs();
                ((BeqInstruction) inst).setRs(allocReg(((BeqInstruction) inst).getRs(), false));
                s2 = ((BeqInstruction) inst).getRt();
                ((BeqInstruction) inst).setRt(allocReg(((BeqInstruction) inst).getRt(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                clear();
            } else if (inst instanceof BneInstruction) {
                s1 = ((BneInstruction) inst).getRs();
                ((BneInstruction) inst).setRs(allocReg(((BneInstruction) inst).getRs(), false));
                s2 = ((BneInstruction) inst).getRt();
                ((BneInstruction) inst).setRt(allocReg(((BneInstruction) inst).getRt(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                clear();
            } else if (inst instanceof BgeInstruction) {
                s1 = ((BgeInstruction) inst).getRt();
                ((BgeInstruction) inst).setRt(allocReg(((BgeInstruction) inst).getRt(), false));
                s2 = ((BgeInstruction) inst).getRs();
                ((BgeInstruction) inst).setRs(allocReg(((BgeInstruction) inst).getRs(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                clear();
            } else if (inst instanceof BltInstruction) {
                s1 = ((BltInstruction) inst).getRt();
                ((BltInstruction) inst).setRt(allocReg(((BltInstruction) inst).getRt(), false));
                s2 = ((BltInstruction) inst).getRs();
                ((BltInstruction) inst).setRs(allocReg(((BltInstruction) inst).getRs(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                clear();
            } else if (inst instanceof BgtInstruction) {
                s1 = ((BgtInstruction) inst).getRt();
                ((BgtInstruction) inst).setRt(allocReg(((BgtInstruction) inst).getRt(), false));

                s2 = ((BgtInstruction) inst).getRs();
                ((BgtInstruction) inst).setRs(allocReg(((BgtInstruction) inst).getRs(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                clear();
            } else if (inst instanceof BleInstruction) {
                s1 = ((BleInstruction) inst).getRt();
                ((BleInstruction) inst).setRt(allocReg(((BleInstruction) inst).getRt(), false));

                s2 = ((BleInstruction) inst).getRs();
                ((BleInstruction) inst).setRs(allocReg(((BleInstruction) inst).getRs(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                clear();
            } else if (inst instanceof LaInstruction) {
                s1 = ((LaInstruction) inst).getBase();
                ((LaInstruction) inst).setBase(allocReg(((LaInstruction) inst).getBase(), false));
                deleteUse(s1, inst);
                ((LaInstruction) inst).setTarget(allocReg(((LaInstruction) inst).getTarget(), true));
            } else if (inst instanceof LiInstruction) {
                ((LiInstruction) inst).setTarget(allocReg(((LiInstruction) inst).getTarget(), true));
            } else if (inst instanceof MoveInstruction) {
                s1 = ((MoveInstruction) inst).getSrc();
                ((MoveInstruction) inst).setSrc(allocReg(((MoveInstruction) inst).getSrc(), false));
                deleteUse(s1, inst);
                ((MoveInstruction) inst).setDes(allocReg(((MoveInstruction) inst).getDes(), true));
            } else if (inst instanceof MipsLoadLocal) {
                // clear();
                s1 = ((MipsLoadLocal) inst).getBase();
                ((MipsLoadLocal) inst).setBase(allocReg(((MipsLoadLocal) inst).getBase(), false));
                deleteUse(s1, inst);
                ((MipsLoadLocal) inst).setTarget(allocReg(((MipsLoadLocal) inst).getTarget(), true));
            } else if (inst instanceof MipsLoadGlobal) {
                ((MipsLoadGlobal) inst).setTarget(allocReg(((MipsLoadGlobal) inst).getTarget(), true));
            } else if (inst instanceof MipsStoreLocal) {
                //clear();
                s1 = ((MipsStoreLocal) inst).getBase();
                ((MipsStoreLocal) inst).setBase(allocReg(((MipsStoreLocal) inst).getBase(), false));

                s2 = ((MipsStoreLocal) inst).getSource();
                ((MipsStoreLocal) inst).setSource(allocReg(((MipsStoreLocal) inst).getSource(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
            } else if (inst instanceof MipsStoreGlobal) {
                s1 = ((MipsStoreGlobal) inst).getSource();
                ((MipsStoreGlobal) inst).setSource(allocReg(((MipsStoreGlobal) inst).getSource(), false));
                deleteUse(s1, inst);
            } else if (inst instanceof JrInstruction) {
                MipsInstruction i1 = ultimate.get(ultimate.size() - 1);
                ultimate.remove(i1);
                clear();
                ultimate.add(i1);
                ultimate.add(new AdduInstruction(Regs.$sp.toString(),
                        block.getFunction().getSize(), Regs.$sp.toString()));
            } else if (inst instanceof JalInstruction) {
                clear();
                ultimate.add(inst);
                users.replaceAll((key, value) -> "");
            } else if (inst instanceof SltInstruction) {
                s1 = ((SltInstruction) inst).getLeft();
                ((SltInstruction) inst).setLeft(allocReg(((SltInstruction) inst).getLeft(), false));

                s2 = ((SltInstruction) inst).getRight();
                ((SltInstruction) inst).setRight(allocReg(((SltInstruction) inst).getRight(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                ((SltInstruction) inst).setTarget(allocReg(((SltInstruction) inst).getTarget(), true));
            } else if (inst instanceof OrInstruction) {
                s1 = ((OrInstruction) inst).getLeft();
                ((OrInstruction) inst).setLeft(allocReg(((OrInstruction) inst).getLeft(), false));
                s2 = ((OrInstruction) inst).getRight();
                ((OrInstruction) inst).setRight(allocReg(((OrInstruction) inst).getRight(), false));
                deleteUse(s1, inst);
                deleteUse(s2, inst);
                ((OrInstruction) inst).setTarget(allocReg(((OrInstruction) inst).getTarget(), true));
            } else if (inst instanceof JInstruction) {
                clear();
            }
            if (!(inst instanceof JalInstruction)) {
                ultimate.add(inst);
            }
        }

        return ultimate;
    }

}
