package llvm;

import llvm.GVN.Deleter;
import llvm.GVN.HashInst;
import llvm.value.BasicBlock;
import llvm.value.user.User;
import llvm.value.user.constant.constantdata.ConstantString;
import llvm.value.user.constant.globalobject.Function;
import llvm.value.user.constant.globalobject.GlobalVariable;
import llvm.value.user.instruction.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidOptimizer {
    private Module module;
    private Map<String, User> table;
    private Map<String, BasicBlock> blocks; //key为label,value为对应基本块
    private Map<Integer, String> hashes;
    private List<Instruction> instructions;
    private List<Instruction> ultimate;
    private Map<String, User> consts;
    List<Instruction> save_delete;

    public void buildUsers(Module top) {
        table = new HashMap<>();
        consts = new HashMap<>();
        blocks = new HashMap<>();
        for (GlobalVariable g : top.getGlobalVariableList()) {
            table.put(g.getDef(), g);
            if (g.getIsConst()) {
                consts.put(g.getDef(), g);
            }
        }
        for (Function f : top.getFunctionList()) {
            for (BasicBlock b : f.getBasicBlocks()) {
                blocks.put(b.getLabel(), b);
            }
        }
        for (Function f : top.getFunctionList()) {
            for (BasicBlock b : f.getBasicBlocks()) {
                //   blocks.put(b.getLabel(), b);
                for (Instruction i : b.getInstructions()) {
                    if (i.getDef() != null) {
                        table.put(i.getDef(), i);
                    }
                    if (i instanceof LocalAllocaInstruction && ((LocalAllocaInstruction) i).getIsConst()) {
                        consts.put(i.getDef(), i);
                    }
                    if (i instanceof BranchInstruction) {
                        blocks.get(((BranchInstruction) i).getTrueLabel()).addFatherBlock(b);
                        b.addSon(blocks.get(((BranchInstruction) i).getTrueLabel()));
                        if (((BranchInstruction) i).getFalseLabel() != null) {
                            blocks.get(((BranchInstruction) i).getFalseLabel()).addFatherBlock(b);
                            b.addSon(blocks.get(((BranchInstruction) i).getFalseLabel()));
                        }
                    }
                    List<String> strings = i.getUsing();
                    for (String s : strings) {
                        if ((s.charAt(0) == '%' || s.charAt(0) == '@') && table.containsKey(s)) {
                            table.get(s).addUser(i);
                        }
                    }
                }
            }
        }
        deleteSameGlobal();

    }

    public void spreadConst() {
        for (Function f : module.getFunctionList()) {
            for (BasicBlock b : f.getBasicBlocks()) {
                List<Instruction> tmp = new ArrayList<>();
                for (Instruction i : b.getInstructions()) {
                    if (i instanceof LoadInstruction) {
                        String addrId = ((LoadInstruction) i).getAddrId();
                        User user = table.get(addrId);
                        if (user instanceof Instruction) {
                            Instruction addr = (Instruction) user;
                            if (addr instanceof LocalAllocaInstruction) {
                                if (consts.containsKey(addrId)) {
                                    ((LoadInstruction) i).setConstValue(((LocalAllocaInstruction) addr).getConstVal(0, 0));
                                } else {
                                    tmp.add(i);
                                }
                            } else if (addr instanceof ElePointerInstruction) {
                                String addrId1 = ((ElePointerInstruction) addr).getAddrId();
                                User user1 = table.get(addrId1);
                                if (user1 instanceof Instruction) {
                                    Instruction addr1 = (Instruction) table.get(addrId1);
                                    if (addr1 instanceof LocalAllocaInstruction) {
                                        String index0 = ((ElePointerInstruction) addr).getRealIndex0();
                                        String index1 = ((ElePointerInstruction) addr).getRealIndex1();
                                        if (consts.containsKey(addrId1) && isConst(index0) && isConst(index1)) {
                                            ((LoadInstruction) i).setConstValue(((LocalAllocaInstruction) addr1).
                                                    getConstVal(Integer.parseInt(index0), Integer.parseInt(index1)));
                                        } else {
                                            tmp.add(i);
                                        }
                                    } else {
                                        tmp.add(i);
                                    }
                                } else if (user1 instanceof GlobalVariable) {
                                    GlobalVariable g = (GlobalVariable) user1;
                                    String index0 = ((ElePointerInstruction) addr).getRealIndex0();
                                    String index1 = ((ElePointerInstruction) addr).getRealIndex1();
                                    if (g.getIsConst() && isConst(index0) && isConst(index1)) {
                                        i.setConstValue(g.getConstVal(Integer.parseInt(index0), Integer.parseInt(index1)));
                                    } else {
                                        tmp.add(i);
                                    }
                                } else {
                                    tmp.add(i);
                                }
                            } else {
                                tmp.add(i);
                            }
                        } else if (user instanceof GlobalVariable) {
                            GlobalVariable g = (GlobalVariable) user;
                            if (g.getIsConst()) {
                                i.setConstValue(g.getConstVal(0, 0));
                            } else {
                                tmp.add(i);
                            }
                        } else {
                            tmp.add(i);
                        }
                    } else if (i instanceof AddInstruction) {
                        String id1 = ((AddInstruction) i).getValue1();
                        String id2 = ((AddInstruction) i).getValue2();
                        if (isConst(id1) && isConst(id2)) {
                            int value1 = getConst(id1) + getConst(id2);
                            i.setConstValue(value1);
                        } else {
                            tmp.add(i);
                        }
                    } else if (i instanceof DivInstruction) {
                        String id1 = ((DivInstruction) i).getValue1();
                        String id2 = ((DivInstruction) i).getValue2();
                        if (isConst(id1) && isConst(id2)) {
                            int value1 = getConst(id1) / getConst(id2);
                            i.setConstValue(value1);
                        } else {
                            tmp.add(i);
                        }
                    } else if (i instanceof SubInstruction) {
                        String id1 = ((SubInstruction) i).getValue1();
                        String id2 = ((SubInstruction) i).getValue2();
                        if (isConst(id1) && isConst(id2)) {
                            int value1 = getConst(id1) - getConst(id2);
                            i.setConstValue(value1);
                        } else {
                            tmp.add(i);
                        }
                    } else if (i instanceof MulInstruction) {
                        String id1 = ((MulInstruction) i).getValue1();
                        String id2 = ((MulInstruction) i).getValue2();
                        if (isConst(id1) && isConst(id2)) {
                            int value1 = getConst(id1) * getConst(id2);
                            i.setConstValue(value1);
                        } else {
                            tmp.add(i);
                        }
                    } else {
                        tmp.add(i);
                    }
                }
                b.setInstructions(tmp);
            }
        }
    }

    public void deleteSameGlobal() {
        Map<String, String> save = new HashMap<>();
        for (Map.Entry<String, User> entry : consts.entrySet()) {
            if (entry.getValue() instanceof GlobalVariable) {
                if (((GlobalVariable) entry.getValue()).getValue() instanceof ConstantString) {
                    String old = ((ConstantString) ((GlobalVariable) entry.getValue()).getValue()).getString();
                    if (save.containsKey(old)) {
                        entry.getValue().replaceTar(save.get(old));
                        table.remove(entry.getKey());
                    } else {
                        save.put(((ConstantString) ((GlobalVariable) entry.getValue()).getValue()).getString(), entry.getKey());
                    }
                }
            }
        }
        for (String s : save.values()) {
            if (consts.containsKey(s)) {
                consts.remove(s);
            }
        }
    }


    public MidOptimizer(Module module) {
        this.module = module;
        hashes = new HashMap<>();
        table = new HashMap<>();
        instructions = new ArrayList<>();
        buildUsers(module);
    }

    public void start() {
        String original = "";
        while (!original.equals(module.toString())) {
            original = module.toString();
            for (Function f : module.getFunctionList()) {
                //   hashes = new HashMap<>();
                for (BasicBlock b : f.getBasicBlocks()) {
                    ultimate = new ArrayList<>();
                    hashes = new HashMap<>();
                    instructions = b.getInstructions();
                    optimize();
                    b.setInstructions(ultimate);
                }
            }
            Deleter deleter = new Deleter(module, table);
            deleter.run();
            spreadConst();
        }

    }

    public void optimize() {
        for (Instruction inst : instructions) {
            ultimate.add(inst);
            if (!(inst instanceof LoadInstruction) && !(inst instanceof CallInstruction) &&
                    !(inst instanceof StoreInstruction) && !(inst instanceof PutStrInstruction)
                    && !(inst instanceof putIntInstruction)) {
                allocHashInst(inst);
            }
        }
    }

    public void allocHashInst(Instruction i) {
        HashInst hashInst = new HashInst(i.getClass(), i);
        if (hashes.containsKey(hashInst.hashCode())) {
            String newTar = hashes.get(hashInst.hashCode());
            String oldTar = i.getDef();
            if (oldTar != null) {
                i.replaceTar(newTar);
                table.get(newTar).inheritUser(i);
            }
            ultimate.remove(ultimate.size() - 1);
        } else {
            hashes.put(hashInst.hashCode(), i.getDef());
        }
    }

    public boolean isConst(String s) {
        if (s.charAt(0) != '@' && s.charAt(0) != '%') {
            return true;
        } else if (consts.containsKey(s)) {
            return true;
        } else {
            return false;
        }
    }

    public int getConst(String s) {
        if (s.charAt(0) != '%' && s.charAt(0) != '@') {
            return Integer.valueOf(s);
        }
        return 0;
    }

}
