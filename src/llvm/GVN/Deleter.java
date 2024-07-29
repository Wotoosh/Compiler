package llvm.GVN;

import llvm.Module;
import llvm.type.PointerType;
import llvm.value.BasicBlock;
import llvm.value.user.User;
import llvm.value.user.constant.globalobject.Function;
import llvm.value.user.instruction.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Deleter {
    private Module module;
    private List<Instruction> instructions;
    private List<Instruction> ultimate;
    private Map<String, User> table;

    public Deleter(Module m, Map<String, User> t) {
        module = m;
        ultimate = new ArrayList<>();
        table = t;
    }

    public void run() {
        for (Function function : module.getFunctionList()) {
            for (BasicBlock b : function.getBasicBlocks()) {
                ultimate = new ArrayList<>();
                instructions = b.getInstructions();
                deleteDeadCode();
                b.setInstructions(ultimate);
            }
        }
    }

    public void deleteDeadCode() {
        Collections.reverse(instructions);
        for (Instruction i : instructions) {
            if (!i.hasUser()) {
                if (i instanceof StoreInstruction) {
                    if (table.get(((StoreInstruction) i).getAddrId()) instanceof ElePointerInstruction) {
                        ElePointerInstruction ele = (ElePointerInstruction) table.get(i.getUsing().get(0));
                        if (table.get(ele.getAddrId()).getUsers().size() == 1
                                && !(ele.getType() instanceof PointerType) && ele.getAddrId().charAt(0) == '%') {
                            ele.deleteUser(i);
                        } else {
                            ultimate.add(i);
                        }
                    } else {
                        if (table.get(((StoreInstruction) i).getAddrId()).getUsers().size() == 1) {
                            table.get(((StoreInstruction) i).getAddrId()).deleteUser(i);
                        } else {
                            ultimate.add(i);
                        }
                    }
                } else if (i instanceof AllocaInstruction || i instanceof PutStrInstruction || i instanceof RetInstruction || i instanceof putIntInstruction) {
                    ultimate.add(i);
                } else if (i instanceof CallInstruction) {
                    ultimate.add(i);
                } else if (i instanceof BranchInstruction) {
                    ultimate.add(i);
                } else {
                    List<String> using = i.getUsing();
                    for (String s : using) {
                        if (s.charAt(0) == '%' && s.charAt(0) == 't') {
                            table.get(s).deleteUser(i);
                        }
                    }
                }
            } else {
                ultimate.add(i);
            }
        }
        Collections.reverse(ultimate);
    }

}
