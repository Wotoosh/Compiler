package backend;

import backend.mipsinstruction.*;
import backend.mipsnode.*;
import backend.mipstool.Allocator;
import backend.mipstool.BackVirtualOptimizer;
import llvm.Module;
import llvm.type.ArrayType;
import llvm.type.PointerType;
import llvm.type.Type;
import llvm.value.BasicBlock;
import llvm.value.user.constant.constantdata.ConstantString;
import llvm.value.user.constant.globalobject.Function;
import llvm.value.user.constant.globalobject.GlobalVariable;
import llvm.value.user.instruction.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackEnd {
    private Module module;
    private Map<String, MipsData> globalDatas;
    private Map<String, MipsFunction> topFunctions;
    private MipsBlock curBlock;
    private Allocator allocator;
    private BasicBlock curBasicBlock;
    private BackVirtualOptimizer backVirtualOptimizer;
    private int continueTime;
    public static boolean isOpt = true;
    private static int cnt = 0;

    public String allocID() {
        curBlock.getFunction().addLocalInt("%myvar" + cnt);
        return "%myvar" + cnt++;
    }

    public BackEnd(Module m) {
        module = m;
        globalDatas = new HashMap<>();
        topFunctions = new HashMap<>();
        allocator = new Allocator(false);
        continueTime = 0;
        backVirtualOptimizer = new BackVirtualOptimizer(allocator);
    }

    public void print(FileOutputStream fout) {
        try {
            fout.write(".data:\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (MipsData m : globalDatas.values()) {
            try {
                fout.write(m.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if(globalDatas.isEmpty()) {
                fout.write(".text:\n\tjal main\n\t li $v0,10\n\tsyscall\n".getBytes(StandardCharsets.UTF_8));
            }else{
                fout.write(".text:\n\taddiu $gp,$gp,32768\n\tjal main\n\t li $v0,10\n\tsyscall\n".getBytes(StandardCharsets.UTF_8));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        for (MipsFunction item : topFunctions.values()) {
            try {
                fout.write(item.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Module getModule() {
        return module;
    }

    public Map<String, MipsFunction> getTopFunctions() {
        return topFunctions;
    }

    public void build() {
        List<GlobalVariable> globalVariables = module.getGlobalVariableList();
        for (GlobalVariable g : globalVariables) {
            if (g.getValue() instanceof ConstantString) {
                MipsString mipsString = new MipsString(g.getId(),
                        (((ConstantString) g.getValue()).getString()),
                        ((ConstantString) g.getValue()).getSize());
                globalDatas.put(g.getId(), mipsString);
            } else {
                MipsInt mipsInt = new MipsInt(g);
                globalDatas.put(g.getId(), mipsInt);
            }
        }
        allocator.setGlobalDatas(globalDatas);
        List<Function> functions = module.getFunctionList();
        for (Function f : functions) {
            MipsFunction mipsFunction = new MipsFunction(f.getName(), f.getArguments());
            topFunctions.put(mipsFunction.getName(), mipsFunction);
            List<BasicBlock> basicBlocks = f.getBasicBlocks();
            for (BasicBlock b : basicBlocks) {
                curBasicBlock = b;
                buildBasicBlock(b, mipsFunction);
            }

            MipsBlock first = mipsFunction.getBlock(0);
            if (mipsFunction.toString().contains("jal")) {
                first.addInstruction(0, new MipsStoreLocal(Regs.$ra.toString(), 0, Regs.$sp.toString()));
            } else {
                mipsFunction.silenceRa();
            }
            first.addInstruction(0, new MipsSubInstruction(Regs.$sp.toString(), Regs.$sp.toString(), mipsFunction.getSize()));
            allocator.setOffset(mipsFunction.getOffset());
            backVirtualOptimizer.setGlobalOffsets(globalDatas);
            backVirtualOptimizer.run(mipsFunction);
            File f1 = new File("virtue.txt");
            File f2 = new File("virtue1.txt");
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(f1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fout.write(mipsFunction.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (MipsBlock m : mipsFunction.getBlocks()) {
                m.setInstructions(allocator.allocReg(m));
            }
            try {
                fout = new FileOutputStream(f2);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fout.write(mipsFunction.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void buildBasicBlock(BasicBlock b, MipsFunction f) {
        MipsBlock mipsBlock = new MipsBlock(b.getLabel(), f);
        f.addBlock(mipsBlock);
        curBlock = mipsBlock;
        List<Instruction> instructions = b.getInstructions();
        int num = instructions.size();
        List<Instruction> noalloca = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            if (instructions.get(i) instanceof LocalAllocaInstruction) {
                MipsInt mipsInt = new MipsInt((LocalAllocaInstruction) instructions.get(i));
                f.addLocalVar(((LocalAllocaInstruction) instructions.get(i)).getId(), mipsInt);

            } else {
                noalloca.add(instructions.get(i));
            }
        }
        for (Instruction i : noalloca) {
            if (continueTime > 0) {
                continueTime--;
                continue;
            }
            if (i instanceof AddInstruction) {
                buildAddInstruction((AddInstruction) i);
            } else if (i instanceof DivInstruction) {
                buildDivInstruction((DivInstruction) i);
            } else if (i instanceof MulInstruction) {
                buildMulInstruction((MulInstruction) i);
            } else if (i instanceof SubInstruction) {
                buildSubInstruction((SubInstruction) i);
            } else if (i instanceof StoreInstruction) {
                buildStoreInstruction((StoreInstruction) i);
            } else if (i instanceof ElePointerInstruction) {
                buildElementPointer((ElePointerInstruction) i);
            } else if (i instanceof RetInstruction) {
                buildRetInstruction((RetInstruction) i);
            } else if (i instanceof GetIntInstruction) {
                buildCallGetInt((GetIntInstruction) i);
            } else if (i instanceof putIntInstruction) {
                buildCallPutInt((putIntInstruction) i);
            } else if (i instanceof PutStrInstruction) {
                buildCallPutStr((PutStrInstruction) i);
            } else if (i instanceof LoadInstruction) {
                buildLoadInstruction((LoadInstruction) i);
            } else if (i instanceof CallInstruction) {
                buildCallInstruction((CallInstruction) i);
            } else if (i instanceof IcmpInstruction) {
                buildIcmpInstruction((IcmpInstruction) i);
            } else if (i instanceof BranchInstruction) {
                buildBranchInstruction((BranchInstruction) i);
            }
        }
    }

    public MipsData transType(Type t) {
        if (t.getDim() == 0) {
            return new MipsInt();
        } else if (t.getDim() == 1) {
            Type tmp = t;
            if (t instanceof PointerType) {
                while (tmp instanceof PointerType) {
                    tmp = ((PointerType) t).getPointed();
                }
                return new MipsInt(((ArrayType) tmp).getElements());
            }
            return new MipsInt(((ArrayType) tmp).getElements());
        } else if (t.getDim() == 2) {
            ArrayType tmp = (ArrayType) t;
            if (t instanceof PointerType) {
                tmp = (ArrayType) ((PointerType) t).getPointed();
            }
            int index1 = tmp.getElements();
            int index2 = ((ArrayType) tmp.getElementType()).getElements();
            return new MipsInt(index1, index2);
        }
        return null;
    }

    public void buildLoadInstruction(LoadInstruction l) {
        String addr = l.getAddrId();
        String target = l.getTargetId();
        MipsFunction f = curBlock.getFunction();
        if (f.getPointers().containsKey(addr)) {
            MipsLoadLocal loadLocal = new MipsLoadLocal(allocator.allocVirtual(target), 0
                    , allocator.allocVirtual(addr));
            curBlock.addInstruction(loadLocal);
        } else if (globalDatas.containsKey(addr)) {
            MipsLoadGlobal load = new MipsLoadGlobal(allocator.allocVirtual(target), addr, 0);
            curBlock.addInstruction(load);
        } else {
            MipsLoadLocal loadLocal = new MipsLoadLocal(allocator.allocVirtual(target),
                    curBlock.getFunction().getOffset(addr)
                    , Regs.$sp.toString());
            curBlock.addInstruction(loadLocal);
        }
        curBlock.getFunction().addLocalVar(target, transType(l.getType()));
    }

    public void buildCallGetInt(GetIntInstruction g) {
        AdduInstruction a = new AdduInstruction(Regs.$zero.toString(), 5, Regs.$v0.toString());
        curBlock.addInstruction(a);
        curBlock.addInstruction(new SyscallInstruction());
        curBlock.getFunction().addLocalVar(g.getTargetId(), new MipsInt());
        store(Regs.$v0.toString(), g.getTargetId(), curBlock.getFunction());
    }

    public void buildCallPutInt(putIntInstruction p) {
        AdduInstruction a = new AdduInstruction(Regs.$zero.toString(), 1, Regs.$v0.toString());
        load(Regs.$a0, p.getId(), curBlock.getFunction());
        curBlock.addInstruction(a);
        curBlock.addInstruction(new SyscallInstruction());
    }

    public void buildCallPutStr(PutStrInstruction p) {
        AdduInstruction a = new AdduInstruction(Regs.$zero.toString(), 4, Regs.$v0.toString());
        if (p.getId().charAt(0) == '@') {
            LaInstruction la = new LaInstruction(Regs.$a0.toString(), p.getId());
            curBlock.addInstruction(la);
        } else {
            MoveInstruction move = new MoveInstruction(Regs.$a0.toString(), allocator.allocVirtual(p.getId()));
            curBlock.addInstruction(move);
        }
        curBlock.addInstruction(a);
        curBlock.addInstruction(new SyscallInstruction());
    }

    public void buildEQIcm(IcmpInstruction icm, String left, String right) {
        Instruction inst = curBasicBlock.peek(icm, 3);
        if (inst instanceof BranchInstruction) {
            continueTime += 3;
            String trueLabel = ((BranchInstruction) inst).getTrueLabel();
            if (left.charAt(0) == '%' && right.charAt(0) == '%') {   //左右均非立即数
                BeqInstruction beq = new BeqInstruction(allocator.allocVirtual(left),
                        allocator.allocVirtual(right), trueLabel);
                curBlock.addInstruction(beq);
            } else if (right.charAt(0) == '%') {
                BeqInstruction beq = new BeqInstruction(allocator.allocVirtual(right),
                        Integer.parseInt(left), trueLabel);
                curBlock.addInstruction(beq);
            } else if (left.charAt(0) == '%') {
                BeqInstruction beq = new BeqInstruction(allocator.allocVirtual(left),
                        Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(beq);
            } else {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(li);
                BeqInstruction beq = new BeqInstruction(allocator.getLast(),
                        Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(beq);
            }
            JInstruction j = new JInstruction(((BranchInstruction) inst).getFalseLabel());
            curBlock.addInstruction(j);
        } else {
            continueTime += 1;
            ZeroExtInstruction z = ((ZeroExtInstruction) curBasicBlock.peek(icm, 1));
            String tar = allocator.allocVirtual(z.getTargetId());
            String outcome1 = buildSlt(left, right, allocator.allocVirtual(allocID()));
            String outcome2 = buildSlt(right, left, allocator.allocVirtual(allocID()));
            OrInstruction or = new OrInstruction(outcome1, outcome2, allocator.allocVirtual(allocID()));
            String orOut = allocator.getLast();
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), 1);
            curBlock.addInstruction(or);
            curBlock.addInstruction(li);
            buildSlt(orOut, allocator.getLast(), tar);
            curBlock.getFunction().addLocalVar(z.getTargetId(), new MipsInt());
        }
    }

    public String buildSlt(String l, String r, String target) {
        if (l.charAt(0) != '%' && l.charAt(0) != 'r') {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(l));
            curBlock.addInstruction(li);
            l = allocator.getLast();
        } else if (l.charAt(0) != 'r') {
            l = allocator.allocVirtual(l);
        }
        if (r.charAt(0) != '%' && r.charAt(0) != 'r') {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(r));
            curBlock.addInstruction(li);
            r = allocator.getLast();
        } else if (r.charAt(0) != 'r') {
            r = allocator.allocVirtual(r);
        }
        SltInstruction slt = new SltInstruction(l, r, target);
        curBlock.addInstruction(slt);
        return target;
    }

    public void buildNeIcm(IcmpInstruction icm, String left, String right) {
        Instruction inst = curBasicBlock.peek(icm, 1);
        String trueLabel;
        String falseLabel;
        if (inst instanceof BranchInstruction) {
            continueTime += 1;
            trueLabel = ((BranchInstruction) inst).getTrueLabel();
            falseLabel = ((BranchInstruction) inst).getFalseLabel();
        } else if (curBasicBlock.peek(icm, 3) instanceof BranchInstruction) {
            continueTime += 3;
            BranchInstruction br = (BranchInstruction) curBasicBlock.peek(icm, 3);
            trueLabel = br.getTrueLabel();
            falseLabel = br.getFalseLabel();
        } else {
            continueTime += 1;
            ZeroExtInstruction z = ((ZeroExtInstruction) curBasicBlock.peek(icm, 1));
            String tar = allocator.allocVirtual(z.getTargetId());
            String outcome1 = buildSlt(left, right, allocator.allocVirtual(allocID()));
            String outcome2 = buildSlt(right, left, allocator.allocVirtual(allocID()));
            OrInstruction or = new OrInstruction(outcome1, outcome2, tar);
            curBlock.addInstruction(or);
            curBlock.getFunction().addLocalVar(z.getTargetId(), new MipsInt());
            return;
        }
        if (left.charAt(0) == '%' && right.charAt(0) == '%') {
            BneInstruction bne = new BneInstruction(allocator.allocVirtual(left),
                    allocator.allocVirtual(right), trueLabel);
            curBlock.addInstruction(bne);
        } else if (right.charAt(0) == '%') {
            BneInstruction bne = new BneInstruction(allocator.allocVirtual(right),
                    Integer.parseInt(left), trueLabel);
            curBlock.addInstruction(bne);
        } else if (left.charAt(0) == '%') {
            BneInstruction bne = new BneInstruction(allocator.allocVirtual(left),
                    Integer.parseInt(right), trueLabel);
            curBlock.addInstruction(bne);
        } else {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
            curBlock.addInstruction(li);
            BneInstruction bne = new BneInstruction(allocator.getLast(),
                    Integer.parseInt(right), trueLabel);
            curBlock.addInstruction(bne);
        }
        JInstruction j = new JInstruction(falseLabel);
        curBlock.addInstruction(j);
    }

    public void buildSltIcm(IcmpInstruction icm, String left, String right) {
        Instruction inst = curBasicBlock.peek(icm, 3);
        if (inst instanceof BranchInstruction) {
            continueTime += 3;
            String trueLabel = ((BranchInstruction) inst).getTrueLabel();
            if (left.charAt(0) == '%' && right.charAt(0) == '%') {
                BltInstruction blt = new BltInstruction(allocator.allocVirtual(left),
                        allocator.allocVirtual(right), trueLabel);
                curBlock.addInstruction(blt);
            } else if (left.charAt(0) == '%') {
                BltInstruction blt = new BltInstruction(allocator.allocVirtual(left),
                        Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(blt);
            } else if (right.charAt(0) == '%') {  //  left<right   ==   right>left
                BgtInstruction bgt = new BgtInstruction(allocator.allocVirtual(right),
                        Integer.parseInt(left), trueLabel);
                curBlock.addInstruction(bgt);
            } else {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(li);
                BltInstruction blt = new BltInstruction(allocator.getLast(),
                        Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(blt);
            }
            JInstruction j = new JInstruction(((BranchInstruction) inst).getFalseLabel());
            curBlock.addInstruction(j);
        } else {
            ZeroExtInstruction zxt = (ZeroExtInstruction) curBasicBlock.peek(icm, 1);
            continueTime += 1;
            String l = left;
            String r = right;
            String target = allocator.allocVirtual(zxt.getTargetId());
            if (l.charAt(0) != '%') {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(l));
                curBlock.addInstruction(li);
                l = allocator.getLast();
            } else {
                l = allocator.allocVirtual(l);
            }
            if (r.charAt(0) != '%') {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(r));
                curBlock.addInstruction(li);
                r = allocator.getLast();
            } else {
                r = allocator.allocVirtual(r);
            }
            SltInstruction slt = new SltInstruction(l, r, target);
            curBlock.addInstruction(slt);
            curBlock.getFunction().addLocalVar(zxt.getTargetId(), new MipsInt());
        }
    }

    public void buildSleIcm(IcmpInstruction icm, String left, String right) {
        Instruction inst = curBasicBlock.peek(icm, 3);
        if (inst instanceof BranchInstruction) {
            continueTime += 3;
            String trueLabel = ((BranchInstruction) inst).getTrueLabel();
            String falseLabel = ((BranchInstruction) inst).getFalseLabel();
            if (left.charAt(0) == '%' && right.charAt(0) == '%') {
                BleInstruction ble = new BleInstruction(allocator.allocVirtual(left),
                        allocator.allocVirtual(right), trueLabel);
                curBlock.addInstruction(ble);
            } else if (left.charAt(0) == '%') {
                BleInstruction ble = new BleInstruction(allocator.allocVirtual(left),
                        Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(ble);
            } else if (right.charAt(0) == '%') {  //  left<=right   ==   right>=left
                BgeInstruction bge = new BgeInstruction(allocator.allocVirtual(right),
                        Integer.parseInt(left), trueLabel);
                curBlock.addInstruction(bge);
            } else {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(li);
                BleInstruction ble = new BleInstruction(allocator.getLast(),
                        Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(ble);
            }
            JInstruction j = new JInstruction(falseLabel);
            curBlock.addInstruction(j);
        } else {  //left-right<=0
            ZeroExtInstruction z = (ZeroExtInstruction) curBasicBlock.peek(icm, 1);
            continueTime += 1;
            String l;
            if (left.charAt(0) != '%') {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(li);
                l = allocator.getLast();
            } else {
                l = allocator.allocVirtual(left);
            }
            String out;
            if (right.charAt(0) != '%') {
                MipsSubInstruction sub = new MipsSubInstruction(l,
                        allocator.allocVirtual(allocID()), Integer.parseInt(right));
                curBlock.addInstruction(sub);
            } else {
                MipsSubInstruction sub = new MipsSubInstruction(l,
                        allocator.allocVirtual(right), allocator.allocVirtual(allocID()));
                curBlock.addInstruction(sub);
            }
            out = allocator.getLast();
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), 1);
            curBlock.addInstruction(li);
            SltInstruction slt = new SltInstruction(out, allocator.getLast(),
                    allocator.allocVirtual(z.getTargetId()));
            curBlock.addInstruction(slt);
            curBlock.getFunction().addLocalVar(z.getTargetId(), new MipsInt());
        }
    }

    public void buildSgtIcm(IcmpInstruction icm, String left, String right) {
        Instruction inst = curBasicBlock.peek(icm, 3);
        if (inst instanceof BranchInstruction) {
            continueTime += 3;
            String trueLabel = ((BranchInstruction) inst).getTrueLabel();
            String falseLabel = ((BranchInstruction) inst).getFalseLabel();
            if (left.charAt(0) == '%' && right.charAt(0) == '%') {
                BgtInstruction bgt = new BgtInstruction(allocator.allocVirtual(left), allocator.allocVirtual(right), trueLabel);
                curBlock.addInstruction(bgt);
            } else if (left.charAt(0) == '%') {
                BgtInstruction bgt = new BgtInstruction(allocator.allocVirtual(left), Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(bgt);
            } else if (right.charAt(0) == '%') {  //left > right  == right<left
                BltInstruction blt = new BltInstruction(allocator.allocVirtual(right), Integer.parseInt(left), trueLabel);
                curBlock.addInstruction(blt);
            } else {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(li);
                BgtInstruction bgt = new BgtInstruction(allocator.getLast(), Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(bgt);
            }
            JInstruction j = new JInstruction(falseLabel);
            curBlock.addInstruction(j);
        } else {  //right -left<0
            continueTime += 1;
            ZeroExtInstruction z = (ZeroExtInstruction) curBasicBlock.peek(icm, 1);
            String r;
            if (right.charAt(0) != '%') {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(right));
                curBlock.addInstruction(li);
                r = allocator.getLast();
            } else {
                r = allocator.allocVirtual(right);
            }
            String out;
            if (left.charAt(0) != '%') {
                MipsSubInstruction sub = new MipsSubInstruction(r,
                        allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(sub);
            } else {
                MipsSubInstruction sub = new MipsSubInstruction(r,
                        allocator.allocVirtual(left), allocator.allocVirtual(allocID()));
                curBlock.addInstruction(sub);
            }
            out = allocator.getLast();
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), 0);
            curBlock.addInstruction(li);
            SltInstruction slt = new SltInstruction(out, allocator.getLast(),
                    allocator.allocVirtual(z.getTargetId()));
            curBlock.addInstruction(slt);
            curBlock.getFunction().addLocalVar(z.getTargetId(), new MipsInt());
        }
    }

    public void buildSgeIcm(IcmpInstruction icm, String left, String right) { //left>=right  right-left<=0
        Instruction inst = curBasicBlock.peek(icm, 3);
        if (inst instanceof BranchInstruction) {
            continueTime += 3;
            String trueLabel = ((BranchInstruction) inst).getTrueLabel();
            String falseLabel = ((BranchInstruction) inst).getFalseLabel();
            if (left.charAt(0) == '%' && right.charAt(0) == '%') {
                BgeInstruction bge = new BgeInstruction(allocator.allocVirtual(left), allocator.allocVirtual(right), trueLabel);
                curBlock.addInstruction(bge);
            } else if (left.charAt(0) == '%') {
                BgeInstruction bge = new BgeInstruction(allocator.allocVirtual(left), Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(bge);
            } else if (right.charAt(0) == '%') {  //left>=right ==  right<=left
                BleInstruction ble = new BleInstruction(allocator.allocVirtual(right), Integer.parseInt(left), trueLabel);
                curBlock.addInstruction(ble);
            } else {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(li);
                BgeInstruction bge = new BgeInstruction(allocator.getLast(), Integer.parseInt(right), trueLabel);
                curBlock.addInstruction(bge);
            }
            JInstruction j = new JInstruction(falseLabel);
            curBlock.addInstruction(j);
        } else { //right-left<=0
            ZeroExtInstruction zxt = (ZeroExtInstruction) curBasicBlock.peek(icm, 1);
            continueTime += 1;
            String l = left;
            String r = right;
            String target = allocator.allocVirtual(zxt.getTargetId());
            if (r.charAt(0) != '%') {
                LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(r));
                curBlock.addInstruction(li);
                r = allocator.getLast();
            } else {
                r = allocator.allocVirtual(r);
            }
            if (left.charAt(0) != '%') {
                MipsSubInstruction sub = new MipsSubInstruction(r,
                        allocator.allocVirtual(allocID()), Integer.parseInt(left));
                curBlock.addInstruction(sub);
            } else {
                MipsSubInstruction sub = new MipsSubInstruction(r,
                        allocator.allocVirtual(left), allocator.allocVirtual(allocID()));
                curBlock.addInstruction(sub);
            }
            String out = allocator.getLast();
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), 1);
            curBlock.addInstruction(li);
            SltInstruction slt = new SltInstruction(out, allocator.getLast(), target);
            curBlock.addInstruction(slt);
            curBlock.getFunction().addLocalVar(zxt.getTargetId(), new MipsInt());
        }
    }

    public void buildIcmpInstruction(IcmpInstruction icm) {
        String left = icm.getValue1();
        String right = icm.getValue2();
        if (icm.getIcmpType().equals(IcmpType.EQ)) {
            buildEQIcm(icm, left, right);
        } else if (icm.getIcmpType().equals(IcmpType.NE)) {
            buildNeIcm(icm, left, right);
        } else if (icm.getIcmpType().equals(IcmpType.SLT)) {
            buildSltIcm(icm, left, right);
        } else if (icm.getIcmpType().equals(IcmpType.SLE)) {
            buildSleIcm(icm, left, right);
        } else if (icm.getIcmpType().equals(IcmpType.SGT)) {
            buildSgtIcm(icm, left, right);
        } else if (icm.getIcmpType().equals(IcmpType.SGE)) {
            buildSgeIcm(icm, left, right);
        }

    }

    public void buildBranchInstruction(BranchInstruction br) {
        String label = br.getTrueLabel();
        JInstruction j = new JInstruction(label);
        curBlock.addInstruction(j);
    }

    public void buildElementPointer(ElePointerInstruction e) {
        String source = e.getAddrId();
        MipsFunction f = curBlock.getFunction();
        MipsData mipsData = null;
        MipsPointer pointer = null;
        if (f.getTable().containsKey(source)) {
            mipsData = f.getTable().get(source);
            pointer = new MipsPointer(f.getOffset(source), f.getOffset(source));
        } else if (globalDatas.containsKey(source)) {
            mipsData = globalDatas.get(source);
            pointer = new MipsPointer(source);
        } else if (f.getPointers().containsKey(source)) {
            pointer = new MipsPointer(f.getPointers().get(source));
            mipsData = f.getPointers().get(source).getData();
        }
        int offset = 0;
        boolean isNum = true;
        if (e.getRealUltra().charAt(0) == '%') {
            isNum = false;
            MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(e.getRealUltra()),
                    allocator.allocVirtual(allocID()), mipsData.getSize() / 4);
            curBlock.addInstruction(mul);
        } else {
            offset += Integer.valueOf(e.getRealUltra()) * mipsData.getSize() / 4;
        }
        MipsData subdata = mipsData;
        if (mipsData instanceof MipsArgument) {
            mipsData = ((MipsArgument) mipsData).getData();
        }
        if (e.getDim() == 2) {
            if (isNum) {
                if (e.getRealIndex0().charAt(0) == '%') {
                    isNum = false;
                    MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(e.getRealIndex0()),
                            allocator.allocVirtual(allocID()), ((MipsInt) mipsData).getDim2());
                    curBlock.addInstruction(mul);
                } else {
                    offset += Integer.parseInt(e.getRealIndex0()) * ((MipsInt) mipsData).getDim2();
                }
            } else {
                if (e.getRealIndex0().charAt(0) == '%') {
                    String ultraId = allocator.getLast();
                    MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(e.getRealIndex0()),
                            allocator.allocVirtual(allocID()), ((MipsInt) mipsData).getDim2());
                    curBlock.addInstruction(mul);
                    String index0Id = allocator.getLast();
                    AdduInstruction add = new AdduInstruction(index0Id, ultraId, allocator.allocVirtual(allocID()));
                    curBlock.addInstruction(add);
                } else {
                    offset += Integer.parseInt(e.getRealIndex0()) * ((MipsInt) mipsData).getDim2();
                }
            }
            if (isNum) {
                if (e.getRealIndex1().charAt(0) == '%') {
                    isNum = false;
                    allocator.load(e.getRealIndex1());
                } else {
                    offset += Integer.parseInt(e.getRealIndex1());
                }
            } else {
                String last = allocator.getLast();
                if (e.getRealIndex1().charAt(0) == '%') {
                    AdduInstruction add = new AdduInstruction(last, allocator.allocVirtual(e.getRealIndex1()), allocator.allocVirtual(allocID()));
                    curBlock.addInstruction(add);
                } else {
                    AdduInstruction add = new AdduInstruction(last, Integer.parseInt(e.getRealIndex1()),
                            allocator.allocVirtual(allocID()));
                    curBlock.addInstruction(add);
                }
            }
            subdata = ((MipsInt) mipsData).derive();
            subdata = ((MipsInt) subdata).derive();
        } else if (e.getDim() == 1 && mipsData instanceof MipsInt) {
            if (isNum) {
                if (e.getRealIndex0().charAt(0) == '%') {
                    isNum = false;
                    MipsMulInstruction mul = new MipsMulInstruction(
                            allocator.allocVirtual(e.getRealIndex0()), allocator.allocVirtual(allocID()), ((MipsInt) mipsData).getDim2());
                    curBlock.addInstruction(mul);
                } else {
                    offset += Integer.parseInt(e.getRealIndex0()) * ((MipsInt) mipsData).getDim2();
                }
            } else {
                if (e.getRealIndex0().charAt(0) == '%') {
                    String ultraId = allocator.getLast();
                    MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(e.getRealIndex0()),
                            allocator.allocVirtual(allocID()), ((MipsInt) mipsData).getDim2());
                    curBlock.addInstruction(mul);
                    String index0Id = allocator.getLast();
                    AdduInstruction add = new AdduInstruction(index0Id, allocator.allocVirtual(allocID()), ultraId);
                    curBlock.addInstruction(add);
                } else {
                    offset += Integer.parseInt(e.getRealIndex0()) * ((MipsInt) mipsData).getDim2();
                }
            }
            if (!(e.getType() instanceof PointerType)) {
                subdata = ((MipsInt) mipsData).derive();
            }
        } else if (mipsData instanceof MipsString) {
            subdata = new MipsInt();
        }
        offset *= 4;
        pointer.addOffset(offset);
        pointer.setData(subdata);
        pointer.setTargetId(e.getTargetId());
        f.addPointer(pointer);
        curBlock.getFunction().addLocalVar(e.getTargetId(), subdata);
        String addrUltimate = allocator.getLast();
        if (f.getPointers().containsKey(source) || e.getType() instanceof PointerType) {
            allocator.load(source);
        } else if (f.getTable().containsKey(source)) {
            AdduInstruction add = new AdduInstruction(Regs.$sp.toString(),
                    f.getOffset(source), allocator.allocVirtual(allocID()));
            curBlock.addInstruction(add);
        } else if (globalDatas.containsKey(source)) {
            LaInstruction la = new LaInstruction(allocator.allocVirtual(allocID()), source);
            curBlock.addInstruction(la);
        }
        String dataId = allocator.getLast();
        if (isNum) {
            AdduInstruction add = new AdduInstruction(dataId, offset, allocator.allocVirtual(e.getTargetId()));
            curBlock.addInstruction(add);
        } else {
            MipsMulInstruction mul = new MipsMulInstruction(addrUltimate, allocator.allocVirtual(allocID()), 4);
            curBlock.addInstruction(mul);
            if (offset != 0) {
                AdduInstruction add = new AdduInstruction(allocator.getLast(), offset, allocator.allocVirtual(allocID()));
                curBlock.addInstruction(add);
            }
            AdduInstruction add1 = new AdduInstruction(dataId, allocator.getLast(), allocator.allocVirtual(e.getTargetId()));
            curBlock.addInstruction(add1);
        }

    }

    public void buildRetInstruction(RetInstruction r) {
        if (r.getRet() != null) {
            if (r.getRet().charAt(0) == '@') {
                MipsLoadGlobal mipsLoadGlobal = new MipsLoadGlobal(Regs.$v0.toString(), r.getRet(), 0);
                curBlock.addInstruction(mipsLoadGlobal);
            } else if (r.getRet().charAt(0) == '%') {
                MoveInstruction move = new MoveInstruction(Regs.$v0.toString(),
                        allocator.allocVirtual(r.getRet()));
                curBlock.addInstruction(move);
            } else {
                LiInstruction li = new LiInstruction(Regs.$v0.toString(), Integer.parseInt(r.getRet()));
                curBlock.addInstruction(li);
            }
        }
        MipsLoadLocal l = new MipsLoadLocal(Regs.$ra.toString(), 0, Regs.$sp.toString());
        curBlock.addInstruction(l);
        JrInstruction jrInstruction = new JrInstruction();
        curBlock.addInstruction(jrInstruction);
    }


    public void buildCallInstruction(CallInstruction c) {
        String target = c.getTargetId();
        List<String> params = c.getParams();
        int size = 8;
        int i = 0;
        MipsFunction m = topFunctions.get(c.getFunction().getName());
        for (String param : params) {
            if (c.getDimOf(i) > 0) {
                if (globalDatas.containsKey(param)) {
                    LaInstruction la = new LaInstruction(allocator.allocVirtual(allocID()), param);
                    curBlock.addInstruction(la);
                    MipsStoreLocal storeLocal = new MipsStoreLocal(allocator.getLast(), size, Regs.$sp.toString(), m);
                    curBlock.addInstruction(storeLocal);
                } else if (curBlock.getFunction().getPointers().containsKey(param)) {
                    MipsStoreLocal storeLocal = new MipsStoreLocal(allocator.allocVirtual(param), size, Regs.$sp.toString(), m);
                    curBlock.addInstruction(storeLocal);
                } else if (curBlock.getFunction().getTable().containsKey(param)) {
                    allocator.load(param);
                    MipsStoreLocal storeLocal = new MipsStoreLocal(allocator.getLast(), size, Regs.$sp.toString(), m);
                    curBlock.addInstruction(storeLocal);
                }
            } else {
                load(allocator.allocVirtual(allocID()), param, curBlock.getFunction());
                MipsStoreLocal storeLocal = new MipsStoreLocal(allocator.getLast(), size, Regs.$sp.toString(), m);
                curBlock.addInstruction(storeLocal);
            }
            size += 4;
            i++;
        }
        JalInstruction jal = new JalInstruction(c.getFunction().getName());
        curBlock.addInstruction(jal);
        if (target != null) {
            curBlock.getFunction().addLocalVar(c.getTargetId(), new MipsInt());
            MoveInstruction move = new MoveInstruction(allocator.allocVirtual(c.getTargetId())
                    , Regs.$v0.toString());
            curBlock.addInstruction(move);
            // store(Regs.$v0.toString(), c.getTargetId(), curBlock.getFunction());
        }
    }

    public void load(Regs reg, String id, MipsFunction f) {
        if (globalDatas.containsKey(id)) {
            MipsLoadGlobal mipsLoadGlobal = new MipsLoadGlobal(reg.toString(), id, 0);
            curBlock.addInstruction(mipsLoadGlobal);
        } else if (f.getPointers().containsKey(id)) {
            MoveInstruction move = new MoveInstruction(reg.toString(), allocator.allocVirtual(id));
            curBlock.addInstruction(move);
        } else if (f.getTable().containsKey(id)) {
            MoveInstruction move = new MoveInstruction(reg.toString(), allocator.allocVirtual(id));
            curBlock.addInstruction(move);
        } else {
            LiInstruction li = new LiInstruction(reg.toString(), Integer.parseInt(id));
            curBlock.addInstruction(li);
        }
    }

    public void load(String reg, String id, MipsFunction f) {
        if (globalDatas.containsKey(id)) {
            MipsLoadGlobal mipsLoadGlobal = new MipsLoadGlobal(reg, id, 0);
            curBlock.addInstruction(mipsLoadGlobal);
        } else if (f.getPointers().containsKey(id)) {
            MoveInstruction move = new MoveInstruction(reg, allocator.allocVirtual(id));
            curBlock.addInstruction(move);
        } else if (f.getTable().containsKey(id)) {
            MoveInstruction move = new MoveInstruction(reg, allocator.allocVirtual(id));
            curBlock.addInstruction(move);
        } else {
            LiInstruction li = new LiInstruction(reg, Integer.parseInt(id));
            curBlock.addInstruction(li);
        }
    }

    public void store(String reg, String id, MipsFunction f) {
        if (globalDatas.containsKey(id)) {
            MipsStoreGlobal mipsStoreGlobal = new MipsStoreGlobal(reg, id, 0);
            curBlock.addInstruction(mipsStoreGlobal);
        } else if (f.getTable().containsKey(id)) {
            MipsStoreLocal mipsStoreLocal = new MipsStoreLocal(reg, f.getOffset(id), Regs.$sp.toString());
            curBlock.addInstruction(mipsStoreLocal);
        } else {
            MipsStoreLocal mipsStoreLocal = new MipsStoreLocal(reg, f.getPointers().get(id).getOffset(),
                    Regs.$sp.toString());
            curBlock.addInstruction(mipsStoreLocal);
        }
    }

    public void buildStoreInstruction(StoreInstruction storeInstruction) {
        String id = storeInstruction.getAddrId();
        String source = storeInstruction.getVarId();
        MipsFunction f = curBlock.getFunction();
        if (source.length() >= 2 && source.charAt(1) == 'a') {
            return;
        }
        if (source.charAt(0) == '@') {
            load(allocator.allocVirtual(allocID()), source, f);
        } else if (source.charAt(0) == '%' && !allocator.contains(source)) {
            MipsLoadLocal load = new MipsLoadLocal(allocator.allocVirtual(source),
                    curBlock.getFunction().getOffset(source), Regs.$sp.toString());
            curBlock.addInstruction(load);
        } else if (source.charAt(0) == '%') {
            allocator.load(source);
        } else {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()), Integer.parseInt(source));
            curBlock.addInstruction(li);
        }
        if (globalDatas.containsKey(id)) {
            MipsStoreGlobal mipsStoreGlobal = new MipsStoreGlobal(allocator.getLast(),
                    id, 0);
            curBlock.addInstruction(mipsStoreGlobal);
        } else if (f.getPointers().containsKey(id)) {
            MipsStoreLocal storeLocal = new MipsStoreLocal(allocator.getLast(), 0,
                    allocator.allocVirtual(id));
            curBlock.addInstruction(storeLocal);
        } else {
            MipsStoreLocal storeLocal = new MipsStoreLocal(allocator.getLast(),
                    curBlock.getFunction().getOffset(id), Regs.$sp.toString());
            curBlock.addInstruction(storeLocal);
        }
    }


    public void buildAddInstruction(AddInstruction a) {
        String right = a.getValue2();
        String left = a.getValue1();
        curBlock.getFunction().addLocalInt(a.getTargetId());
        if (right.charAt(0) == '%' && left.charAt(0) == '%') {
            AdduInstruction addu = new AdduInstruction(allocator.allocVirtual(left),
                    allocator.allocVirtual(right), allocator.allocVirtual(a.getTargetId()));
            curBlock.addInstruction(addu);
        } else if (right.charAt(0) == '%') {
            AdduInstruction addi = new AdduInstruction(allocator.allocVirtual(right),
                    Integer.parseInt(left), allocator.allocVirtual(a.getTargetId()));
            curBlock.addInstruction(addi);
        } else if (left.charAt(0) == '%') {
            AdduInstruction addi = new AdduInstruction(allocator.allocVirtual(left),
                    Integer.parseInt(right), allocator.allocVirtual(a.getTargetId()));
            curBlock.addInstruction(addi);
        } else {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()),
                    Integer.parseInt(left));
            curBlock.addInstruction(li);
            AdduInstruction addi = new AdduInstruction(allocator.getLast(),
                    Integer.parseInt(right), allocator.allocVirtual(a.getTargetId()));
            curBlock.addInstruction(addi);
        }
    }

    public void buildDivInstruction(DivInstruction d) {
        String right = d.getValue2();
        String left = d.getValue1();
        if (left.charAt(0) != '%' && right.charAt(0) != '%') {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()),
                    Integer.parseInt(left));
            curBlock.addInstruction(li);
            MipsDivInstruction div = new MipsDivInstruction(allocator.getLast(),
                    allocator.allocVirtual(d.getTargetId()), Integer.parseInt(right));
            curBlock.addInstruction(div);
        } else if (left.charAt(0) != '%') {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()),
                    Integer.parseInt(left));
            curBlock.addInstruction(li);
            MipsDivInstruction div = new MipsDivInstruction(allocator.getLast(),
                    allocator.allocVirtual(right), allocator.allocVirtual(d.getTargetId()));
            curBlock.addInstruction(div);
        } else if (right.charAt(0) != '%') {
            MipsDivInstruction div = new MipsDivInstruction(allocator.allocVirtual(left),
                    allocator.allocVirtual(d.getTargetId()), Integer.parseInt(right));
            curBlock.addInstruction(div);
        } else {
            MipsDivInstruction div = new MipsDivInstruction(allocator.allocVirtual(left),
                    allocator.allocVirtual(right), allocator.allocVirtual(d.getTargetId()));
            curBlock.addInstruction(div);
        }
        curBlock.getFunction().addLocalInt(d.getTargetId());
    }

    public void buildMulInstruction(MulInstruction m) {
        String right = m.getValue2();
        String left = m.getValue1();
        if (right.charAt(0) == '%' && left.charAt(0) == '%') {
            MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(left),
                    allocator.allocVirtual(right), allocator.allocVirtual(m.getTargetId()));
            curBlock.addInstruction(mul);
        } else if (right.charAt(0) == '%') {
            MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(right),
                    allocator.allocVirtual(m.getTargetId()), Integer.parseInt(left));
            curBlock.addInstruction(mul);
        } else if (left.charAt(0) == '%') {
            MipsMulInstruction mul = new MipsMulInstruction(allocator.allocVirtual(left),
                    allocator.allocVirtual(m.getTargetId()), Integer.parseInt(right));
            curBlock.addInstruction(mul);
        } else {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()),
                    Integer.parseInt(left));
            curBlock.addInstruction(li);
            MipsMulInstruction mul = new MipsMulInstruction(allocator.getLast(),
                    allocator.allocVirtual(m.getTargetId()), Integer.parseInt(right));
            curBlock.addInstruction(mul);
        }
        curBlock.getFunction().addLocalInt(m.getTargetId());
    }

    public void buildSubInstruction(SubInstruction s) {
        String right = s.getValue2();
        String left = s.getValue1();
        if (left.charAt(0) != '%') {
            LiInstruction li = new LiInstruction(allocator.allocVirtual(allocID()),
                    Integer.parseInt(left));
            curBlock.addInstruction(li);
            left = allocator.getLast();
        } else {
            left = allocator.allocVirtual(left);
        }
        if (right.charAt(0) != '%') {
            MipsSubInstruction sub = new MipsSubInstruction(left, allocator.allocVirtual(s.getTargetId()), Integer.parseInt(right));
            curBlock.addInstruction(sub);
        } else {
            right = allocator.allocVirtual(right);
            MipsSubInstruction sub = new MipsSubInstruction(left, right,
                    allocator.allocVirtual(s.getTargetId()));
            curBlock.addInstruction(sub);
        }

        curBlock.getFunction().addLocalInt(s.getTargetId());
    }


}
