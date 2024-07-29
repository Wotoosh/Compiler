package llvm;

import frontend.node.*;
import llvm.type.*;
import llvm.value.Argument;
import llvm.value.BasicBlock;
import llvm.value.Value;
import llvm.value.user.constant.globalobject.Function;
import llvm.value.user.constant.globalobject.GlobalVariable;
import llvm.value.user.instruction.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IRBuilder {
    private CompUnit compUnit;
    private Module topModule;
    private Object curId;  //以%开头为局部变量，以@为全局变量，无前缀为常数
    private BasicBlock curBlock;
    private boolean hasPrint;
    private boolean hasGetInt;
    private Stack<BasicBlock> continueStack;
    private Stack<BasicBlock> breakStack;

    public Module getModule() {
        return topModule;
    }

    public IRBuilder(CompUnit c) {
        this.compUnit = c;
        topModule = new Module();
        hasPrint = false;
        hasGetInt = false;
        continueStack = new Stack<>();
        breakStack = new Stack<>();
    }

    public void print(FileOutputStream fout) {
        if (hasGetInt) {
            try {
                fout.write("declare i32 @getint()\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (hasPrint) {
            try {
                fout.write(("declare dso_local void" +
                        " @putint(i32)\n" + "declare void @putstr(i8*)\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Value item : topModule.getGlobalVariableList()) {
            try {
                fout.write(item.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Function item : topModule.getFunctionList()) {
            try {
                if (!item.getBuiltIn()) {
                    fout.write(item.toString().getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void buildLlvm() {

        if (compUnit.getDecls() != null) {
            for (Decl d : compUnit.getDecls()) {
                buildGlobalVarDecl(d);
            }
        }
        if (compUnit.getFuncs() != null) {
            for (FuncDef f : compUnit.getFuncs()) {
                buildFuncDef(f);
            }
        }
        buildMainFuncDef(compUnit.getMainFunc());
    }

    public void buildMainFuncDef(MainFuncDef f) {
        Function function = new Function(f);
        BasicBlock firstBlock = new BasicBlock(function);
        curBlock = firstBlock;
        function.addBasicBlock(firstBlock);
        MySymbolTable child = topModule.getSymbolTable().child();
        List<Argument> arguments = new ArrayList<>();
        function.setArguments(arguments);
        buildBlock(child, f.getBlock());
        topModule.addFunction(function);
        function.checkReturn();
    }

    public void buildGlobalVarDecl(Decl d) {
        if (d.getVarDecl() != null) {
            List<VarDef> varDefs = d.getVarDecl().getVarDefs();
            for (VarDef v : varDefs) {
                buildGlobalVarDef(v);
            }
        }
        if (d.getConstDecl() != null) {
            List<ConstDef> constDefs = d.getConstDecl().getConstDefs();
            for (ConstDef c : constDefs) {
                buildGlobalVarDef(c);
            }
        }
    }

    public void buildGlobalVarDef(VarDef v) {
        String name = v.getIdent().getName();
        Type t = new IntegerType();
        if (v.getConstExpr() != null) {
            List<ConstExp> constExps = v.getConstExpr();
            for (int i = constExps.size() - 1; i >= 0; i--) {
                t = new ArrayType(constExps.get(i).getVal(topModule.getSymbolTable()), t);
            }
        }
        GlobalVariable g = new GlobalVariable(name, t, v.getInitVal(), topModule.getSymbolTable());
        //GlobalAllocaInstruction allocaInstruction = new GlobalAllocaInstruction(v, topModule.getSymbolTable());
        topModule.addGlobalVar(g, v.getInitVal());
    }

    public void buildGlobalVarDef(ConstDef c) {
        String name = c.getIdent().getName();
        Type t = new IntegerType();
        if (c.getConstExps() != null) {
            List<ConstExp> constExps = c.getConstExps();
            for (int i = constExps.size() - 1; i >= 0; i--) {
                t = new ArrayType(constExps.get(i).getVal(topModule.getSymbolTable()), t);
            }
        }
        GlobalVariable g = new GlobalVariable(name, t, c.getConstInitVal(), topModule.getSymbolTable());
        topModule.addGlobalVar(g, c.getConstInitVal());
    }

    public void buildTmpVarDef(ConstDef c, BasicBlock b, MySymbolTable s) {
        Type tmpType;
        if (c.getDim() == 0) {
            tmpType = new IntegerType();
        } else if (c.getDim() == 1) {
            tmpType = new ArrayType(c.getConstExp(0).getVal(s),
                    new IntegerType());    //一维数组
        } else {
            tmpType = new ArrayType(c.getConstExp(0).getVal(s),
                    new ArrayType(c.getConstExp(1).getVal(s), new IntegerType()));  //二维数组
        }
        LocalAllocaInstruction alloca = new LocalAllocaInstruction(c.getIdent().getName(), tmpType,
                true, c.getConstInitVal(), s);
        s.addTmpVar(alloca);
        b.addInstruction(alloca);
        int count0 = 0;
        int count1 = 0;
        if (c.getConstInitVal() != null) {
            if (c.getConstInitVal().getConstInitVals() != null) {
                for (ConstInitVal i : c.getConstInitVal().getConstInitVals()) {
                    if (i.getConstInitVals() != null) {
                        for (ConstInitVal j : i.getConstInitVals()) {
                            initArray(c.getIdent().getName(), 2, count0,
                                    count1, b, s, String.valueOf(j.getConstExp().getVal(s)));
                            count1++;
                        }
                    } else {
                        initArray(c.getIdent().getName(), 1, count0, count1,
                                b, s, String.valueOf(i.getConstExp().getVal(s)));
                    }
                    count0++;
                    count1 = 0;
                }
            } else {
                initArray(c.getIdent().getName(), 0, count0, count1, b, s,
                        String.valueOf(c.getConstInitVal().getConstExp().getVal(s)));
            }
        }
    }

    public void buildTmpVarDef(VarDef v, BasicBlock b, MySymbolTable s) {
        Type tmpType;
        if (v.getDim() == 0) {
            tmpType = new IntegerType();
        } else if (v.getDim() == 1) {
            tmpType = new ArrayType(v.getConstExp(0).getVal(s),
                    new IntegerType());    //一维数组时
        } else {
            tmpType = new ArrayType(v.getConstExp(0).getVal(s),
                    new ArrayType(v.getConstExp(1).getVal(s), new IntegerType()));    //二维数组
        }
        LocalAllocaInstruction alloca = new LocalAllocaInstruction(v.getIdent().getName(), tmpType, false);
        s.addTmpVar(alloca);
        b.addInstruction(alloca);
        int count0 = 0;
        int count1 = 0;
        if (v.getInitVal() != null) {
            if (v.getInitVal().getInitVals() != null) {
                for (InitVal i : v.getInitVal().getInitVals()) {
                    if (i.getInitVals() != null) {
                        for (InitVal j : i.getInitVals()) {
                            buildExp(j.getExp(), b, s);
                            initArray(v.getIdent().getName(), 2, count0, count1, b, s, curId.toString());
                            count1++;
                        }
                    } else {
                        buildExp(i.getExp(), b, s);
                        initArray(v.getIdent().getName(), 1, count0, count1, b, s, curId.toString());
                    }
                    count0++;
                    count1 = 0;
                }
            } else {
                buildExp(v.getInitVal().getExp(), b, s);
                initArray(v.getIdent().getName(), 0, count0, count1, b, s, curId.toString());
            }
        }
    }

    public void buildFuncDef(FuncDef f) {
        Function function = new Function(f);
        BasicBlock firstBlock = new BasicBlock(function);
        topModule.addFunction(function);
        curBlock = firstBlock;
        function.addBasicBlock(firstBlock);
        MySymbolTable child = topModule.getSymbolTable().child();
        List<Argument> arguments = buildArguments(f, firstBlock, child);
        function.setArguments(arguments);
        buildBlock(child, f.getBlock());
        function.checkReturn();
    }

    public List<Argument> buildArguments(FuncDef f, BasicBlock firstBlock, MySymbolTable s) {
        FuncFParams funcFParams = f.getFuncFParams();
        List<Argument> arguments = new ArrayList<>();
        if (funcFParams != null) {
            List<FuncFParam> params = funcFParams.getFuncFParam();
            if (params != null) {
                for (FuncFParam param : params) {
                    Type argType;
                    if (param.getDim() == 0) {
                        argType = new IntegerType();
                    } else if (param.getDim() == 1) {
                        argType = new IntegerType().getPointer();    //参数为一维数组时，指针指向一维
                    } else {
                        argType = new ArrayType(param.getConstExp(0).getVal(s),
                                new IntegerType()).getPointer();    //参数为二维数组时，指针指向一维
                    }
                    Argument argument = new Argument(argType, param.getIdent().getName());
                    arguments.add(argument);
                    LocalAllocaInstruction alloca = new LocalAllocaInstruction(param.getIdent().getName(),
                            argType, false);
                    s.addTmpVar(alloca);
                    StoreInstruction store = new StoreInstruction(param.getIdent().getName(),
                            s, argType, argument.getId());
                    firstBlock.addInstruction(alloca);
                    firstBlock.addInstruction(store);
                }
            }
        }
        return arguments;
    }

    public void buildBlock(MySymbolTable symbolTable, Block block) {
        List<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            if (blockItem.getStmt() != null) {
                buildStmt(blockItem.getStmt(), symbolTable);
            } else {
                buildDecl(blockItem.getDecl(), curBlock, symbolTable);
            }
        }
    }

    public void buildStmt(Stmt stmt, MySymbolTable s) {
        if (stmt.getType() == 0) {
            buildStmt0(stmt, curBlock, s);
        } else if (stmt.getType() == 1) {
            buildStmt1(stmt, curBlock, s);
        } else if (stmt.getType() == 2) {
            buildStmt2(stmt, s);
        } else if (stmt.getType() == 3) {
            buildStmt3(stmt, curBlock, s);
        } else if (stmt.getType() == 4) {
            buildStmt4(stmt, curBlock, s);
        } else if (stmt.getType() == 5) {
            buildStmt5(curBlock);
        } else if (stmt.getType() == 6) {
            buildStmt6(curBlock);
        } else if (stmt.getType() == 7) {
            buildStmt7(stmt, curBlock, s);
        } else if (stmt.getType() == 8) {
            buildStmt8(stmt, curBlock, s);
        } else if (stmt.getType() == 9) {
            buildStmt9(stmt, curBlock, s);
        }

    }

    public void buildStmt0(Stmt stmt, BasicBlock b, MySymbolTable s) {
        buildExp(stmt.getExp(), b, s);
        String rightId = curId.toString();
        writeLval(stmt.getlVal(), b, s, rightId);
    }

    public void buildStmt1(Stmt stmt, BasicBlock b, MySymbolTable s) {
        if (stmt.getExp() != null) {
            buildExp(stmt.getExp(), b, s);
        }
    }

    public void buildStmt2(Stmt stmt, MySymbolTable s) {
        buildBlock(s.child(), stmt.getBlock());
    }

    public void buildStmt3(Stmt stmt, BasicBlock b, MySymbolTable s) {
        BasicBlock trueBlock = new BasicBlock(b.getFunction());
        BasicBlock falseBlock = new BasicBlock(b.getFunction());
        trueBlock.setFrom(b);
        falseBlock.setFrom(b);
        buildCond(stmt.getCond(), b, s, trueBlock, falseBlock);
        curBlock = trueBlock;
        b.getFunction().addBasicBlock(trueBlock);
        buildStmt(stmt.getIfStmt(), s);
        if (stmt.getElseStmt() != null) {
            BasicBlock after = new BasicBlock(b.getFunction());
            BranchInstruction br = new BranchInstruction(after.getLabel());
            curBlock.addInstruction(br);
            curBlock = falseBlock;
            b.getFunction().addBasicBlock(falseBlock);
            buildStmt(stmt.getElseStmt(), s);
            curBlock.addInstruction(br);
            curBlock = after;
            b.getFunction().addBasicBlock(curBlock);
        } else {
            BranchInstruction br = new BranchInstruction(falseBlock.getLabel());
            curBlock.addInstruction(br);
            curBlock = falseBlock;
            b.getFunction().addBasicBlock(falseBlock);
        }
    }

    public void buildStmt4(Stmt stmt, BasicBlock b, MySymbolTable s) {
        if (stmt.getForStmt1() != null) {
            buildForStmt(stmt.getForStmt1(), b, s);
        }
        BasicBlock cond = new BasicBlock(b.getFunction());  //条件判断
        BranchInstruction br = new BranchInstruction(cond.getLabel());
        b.addInstruction(br);
        BasicBlock body = new BasicBlock(b.getFunction());
        body.setFrom(cond);
        BasicBlock sideEffect = new BasicBlock(b.getFunction());
        continueStack.push(sideEffect);
        BasicBlock after = new BasicBlock(b.getFunction());
        breakStack.push(after);
        curBlock = cond;
        if (stmt.getCond() != null) {
            buildCond(stmt.getCond(), cond, s, body, after);
        } else {
            BranchInstruction br1 = new BranchInstruction(body.getLabel());
            cond.addInstruction(br1);
        }
        b.getFunction().addBasicBlock(cond);
        b.getFunction().addBasicBlock(sideEffect);
        curBlock = sideEffect;
        if (stmt.getForStmt2() != null) {
            buildForStmt(stmt.getForStmt2(), sideEffect, s);
        }
        br = new BranchInstruction(cond.getLabel());
        sideEffect.addInstruction(br);
        curBlock = body;
        b.getFunction().addBasicBlock(body);
        buildStmt(stmt.getForStmt(), s);
        br = new BranchInstruction(sideEffect.getLabel());
        curBlock.addInstruction(br);
        continueStack.pop();
        breakStack.pop();
        curBlock = after;
        b.getFunction().addBasicBlock(after);
    }

    public void buildStmt5(BasicBlock b) {
        BranchInstruction br = new BranchInstruction(breakStack.peek().getLabel());
        curBlock.addInstruction(br);
        BasicBlock newBlock = new BasicBlock(b.getFunction());
        b.getFunction().addBasicBlock(newBlock);
        curBlock = newBlock;
    }

    public void buildStmt6(BasicBlock b) {
        BranchInstruction br = new BranchInstruction(continueStack.peek().getLabel());
        curBlock.addInstruction(br);
        BasicBlock newBlock = new BasicBlock(b.getFunction());
        b.getFunction().addBasicBlock(newBlock);
        curBlock = newBlock;
    }

    public void buildStmt7(Stmt stmt, BasicBlock b, MySymbolTable s) {
        if (stmt.getExp() == null) {
            RetInstruction ret = new RetInstruction();
            b.addInstruction(ret);
        } else {
            buildExp(stmt.getExp(), b, s);
            if (b.getFunction().getType() instanceof VoidType) {
                RetInstruction ret = new RetInstruction();
                b.addInstruction(ret);
            } else {
                RetInstruction ret = new RetInstruction(curId.toString());
                b.addInstruction(ret);
            }
        }
        BasicBlock next = new BasicBlock(b.getFunction());
        curBlock = next;
        b.getFunction().addBasicBlock(next);
    }

    public void buildStmt8(Stmt s, BasicBlock b, MySymbolTable symbolTable) {
        if (!hasGetInt) {
            hasGetInt = true;
        }
        GetIntInstruction getint = new GetIntInstruction();
        b.addInstruction(getint);
        curId = getint.getTargetId();
        writeLval(s.getlVal(), b, symbolTable, curId.toString());
    }

    public void buildStmt9(Stmt s, BasicBlock b, MySymbolTable symbolTable) {
        if (!hasPrint) {
            hasPrint = true;
        }
        String str = s.getFormatString().getFormatString();
        int len = str.length();
        int count = 0;
        int tmplen = 0;
        StringBuilder sb = new StringBuilder();
        List<String> ids = new ArrayList<>();
        if (s.getExps() != null) {
            for (int i = 0; i < s.getExps().size(); i++) {
                buildExp(s.getExps().get(i), b, symbolTable);
                ids.add(curId.toString());
            }
        }
        for (int i = 1; i < len - 1; i++) {
            if (str.charAt(i) != '%' && str.charAt(i) != '\\') {
                sb.append(str.charAt(i));
                tmplen++;
            } else if (str.charAt(i) == '%') {
                String str1 = sb.toString();
                sb.setLength(0);
                if (str1.length() > 0) {
                    GlobalVariable g = new GlobalVariable(str1, tmplen);
                    topModule.addGlobalVar(g);
                    ElePointerInstruction e = new ElePointerInstruction(g);
                    b.addInstruction(e);
                    curId = e.getTargetId();
                    PutStrInstruction p = new PutStrInstruction(curId.toString());
                    b.addInstruction(p);
                }
                tmplen = 0;
                putIntInstruction p = new putIntInstruction(ids.get(count));
                b.addInstruction(p);
                i++;
                count++;
            } else if (str.charAt(i) == '\\') {
                tmplen++;
                sb.append("\\0A");
                i++;
            }
        }
        String str1 = sb.toString();
        sb.setLength(0);
        if (str1.length() > 0) {
            GlobalVariable g = new GlobalVariable(str1, tmplen);
            topModule.addGlobalVar(g);
            ElePointerInstruction e = new ElePointerInstruction(g);
            b.addInstruction(e);
            curId = e.getTargetId();
            PutStrInstruction p = new PutStrInstruction(curId.toString());
            b.addInstruction(p);
        }
        tmplen = 0;
    }

    public void buildForStmt(ForStmt forStmt, BasicBlock b, MySymbolTable s) {
        buildExp(forStmt.getExp(), b, s);
        String newValue = curId.toString();
        writeLval(forStmt.getLVal(), b, s, newValue);
    }

    public void buildCond(Cond cond, BasicBlock b, MySymbolTable s,
                          BasicBlock trueBlock, BasicBlock falseBlock) {
        buildLOrExp(cond.getLOrExp(), b, s, trueBlock, falseBlock);
        // b.getFunction().addBasicBlock(trueBlock);
    }

    public void buildLOrExp(LOrExp lOrExp, BasicBlock b, MySymbolTable s,
                            BasicBlock trueBlock, BasicBlock falseBlock) {
        LOrExp tmp = lOrExp;
        List<LAndExp> lAndExps = new ArrayList<>();
        lAndExps.add(tmp.getLandExp());
        while (tmp.getLOrExp() != null) {
            tmp = tmp.getLOrExp();
            lAndExps.add(0, tmp.getLandExp());
        }
        for (int i = 0; i < lAndExps.size() - 1; i++) {
            BasicBlock nextBlock = new BasicBlock(b.getFunction());
            buildLandExp(lAndExps.get(i), curBlock, s, trueBlock, nextBlock);
            curBlock = nextBlock;
            b.getFunction().addBasicBlock(nextBlock);
        }
        buildLandExp(lAndExps.get(lAndExps.size() - 1), curBlock, s, trueBlock, falseBlock);
        curBlock.setTrueTo(trueBlock);
        curBlock.setFalseTo(falseBlock);
    }

    public void buildLandExp(LAndExp lAndExp, BasicBlock b, MySymbolTable s,
                             BasicBlock trueBlock, BasicBlock falseBlock) {
        LAndExp tmp = lAndExp;
        List<EqExp> eqExps = new ArrayList<>();
        eqExps.add(tmp.getEqExp());
        while (tmp.getLAndExp() != null) {
            tmp = tmp.getLAndExp();
            eqExps.add(0, tmp.getEqExp());
        }
        for (int i = 0; i < eqExps.size() - 1; i++) {
            BasicBlock nextBlock = new BasicBlock(b.getFunction());
            buildEqExp(eqExps.get(i), curBlock, s);
            IcmpInstruction icm = new IcmpInstruction(curId.toString(), "0", IcmpType.NE);
            curBlock.addInstruction(icm);
            curId = icm.getTargetId();
            BranchInstruction br = new BranchInstruction(curId.toString(),
                    nextBlock.getType().toString(), falseBlock.getType().toString());
            curBlock.addInstruction(br);
            curBlock.setTrueTo(nextBlock);
            curBlock.setFalseTo(falseBlock);
            nextBlock.setFrom(curBlock);
            curBlock = nextBlock;
            b.getFunction().addBasicBlock(nextBlock);
        }
        buildEqExp(eqExps.get(eqExps.size() - 1), curBlock, s);
        IcmpInstruction icm = new IcmpInstruction(curId.toString(), "0", IcmpType.NE);
        curBlock.addInstruction(icm);
        curId = icm.getTargetId();
        BranchInstruction br = new BranchInstruction(curId.toString(),
                trueBlock.getType().toString(), falseBlock.getType().toString());
        curBlock.addInstruction(br);
        curBlock.setTrueTo(trueBlock);
        curBlock.setFalseTo(falseBlock);
        //b.getFunction().addBasicBlock(nextBlock);
    }

    public void buildEqExp(EqExp eqExp, BasicBlock b, MySymbolTable s) {
        if (eqExp.getEqExp() == null) {
            buildRelExp(eqExp.getRelExp(), b, s);
        } else {
            buildEqExp(eqExp.getEqExp(), b, s);
            String leftId = curId.toString();
            buildRelExp(eqExp.getRelExp(), b, s);
            String rightId = curId.toString();
            IcmpInstruction icmp = null;
            if (eqExp.getOp().equals("==")) {
                icmp = new IcmpInstruction(leftId, rightId, IcmpType.EQ);
            } else if (eqExp.getOp().equals("!=")) {
                icmp = new IcmpInstruction(leftId, rightId, IcmpType.NE);
            }
            curId = icmp.getTargetId();
            b.addInstruction(icmp);
            ZeroExtInstruction zeroExt = new ZeroExtInstruction(curId.toString());
            curBlock.addInstruction(zeroExt);
            curId = zeroExt.getTargetId();
        }
    }

    public void buildRelExp(RelExp relExp, BasicBlock b, MySymbolTable s) {
        if (relExp.getRelExp() == null) {
            buildAddExp(relExp.getAddExp(), b, s);
        } else {
            buildRelExp(relExp.getRelExp(), b, s);
            String leftId = curId.toString();
            buildAddExp(relExp.getAddExp(), b, s);
            String rightId = curId.toString();
            IcmpInstruction icmp = null;
            if (relExp.getOp().equals(">")) {
                icmp = new IcmpInstruction(leftId, rightId, IcmpType.SGT);
            } else if (relExp.getOp().equals(">=")) {
                icmp = new IcmpInstruction(leftId, rightId, IcmpType.SGE);
            } else if (relExp.getOp().equals("<")) {
                icmp = new IcmpInstruction(leftId, rightId, IcmpType.SLT);
            } else if (relExp.getOp().equals("<=")) {
                icmp = new IcmpInstruction(leftId, rightId, IcmpType.SLE);
            }
            curId = icmp.getTargetId();
            ZeroExtInstruction zeroExt = new ZeroExtInstruction(curId.toString());
            curId = zeroExt.getTargetId();
            b.addInstruction(icmp);
            b.addInstruction(zeroExt);
        }
    }

    public void buildExp(Exp e, BasicBlock b, MySymbolTable s) {
        buildAddExp(e.getAddExp(), b, s);
    }

    public void buildAddExp(AddExp addExp, BasicBlock b, MySymbolTable s) {
        if (addExp.getAddExp() != null) {
            buildAddExp(addExp.getAddExp(), b, s);
            String leftId = curId.toString();
            buildMulExp(addExp.getMulExp(), b, s);
            String rightId = curId.toString();
            if (addExp.getOp().equals("+")) {
                AddInstruction addInstruction = new AddInstruction(leftId, rightId);
                b.addInstruction(addInstruction);
                curId = addInstruction.getTargetId();
            } else if (addExp.getOp().equals("-")) {
                SubInstruction subInstruction = new SubInstruction(leftId, rightId);
                b.addInstruction(subInstruction);
                curId = subInstruction.getTargetId();
            }
        } else {
            buildMulExp(addExp.getMulExp(), b, s);
        }
    }

    public void buildMulExp(MulExp mulExp, BasicBlock b, MySymbolTable s) {
        if (mulExp.getMulExp() != null) {
            buildMulExp(mulExp.getMulExp(), b, s);
            String leftId = curId.toString();
            buildUnaryExp(mulExp.getUnaryExp(), b, s);
            String rightId = curId.toString();
            if (mulExp.getOp().equals("*")) {
                MulInstruction mulInstruction = new MulInstruction(leftId, rightId);
                b.addInstruction(mulInstruction);
                curId = mulInstruction.getTargetId();
            } else if (mulExp.getOp().equals("/")) {
                DivInstruction divInstruction = new DivInstruction(leftId, rightId);
                b.addInstruction(divInstruction);
                curId = divInstruction.getTargetId();
            } else if (mulExp.getOp().equals("%")) {
                DivInstruction inst1 = new DivInstruction(leftId, rightId);
                String res1 = inst1.getTargetId();
                MulInstruction inst2 = new MulInstruction(res1, rightId);
                String res2 = inst2.getTargetId();
                SubInstruction inst3 = new SubInstruction(leftId, res2);
                curId = inst3.getTargetId();
                b.addInstruction(inst1);
                b.addInstruction(inst2);
                b.addInstruction(inst3);
            }
        } else {
            buildUnaryExp(mulExp.getUnaryExp(), b, s);
        }
    }

    public void buildUnaryExp(UnaryExp unaryExp, BasicBlock b, MySymbolTable s) {
        UnaryExp unaryExp1 = unaryExp;
        Stack<String> ops = new Stack<>();
        while (unaryExp1.getUnaryExp() != null) {
            if (!unaryExp1.getUnaryOp().equals("+")) {
                if (unaryExp1.getUnaryOp().equals("-") && !ops.isEmpty() && ops.peek().equals("-")) {
                    ops.pop();
                } else {
                    ops.push(unaryExp1.getUnaryOp());
                }
                unaryExp1 = unaryExp1.getUnaryExp();
            } else {
                unaryExp1 = unaryExp1.getUnaryExp();
            }
        }
        if (unaryExp1.getPrimaryExp() != null) {
            buildPrimaryExp(unaryExp1.getPrimaryExp(), b, s);
        } else if (unaryExp1.getIdent() != null) {
            Function f = topModule.getFunction(unaryExp1.getIdent().getName());
            List<String> params = new ArrayList<>();
            if (unaryExp1.getFuncRParams() != null) {
                for (Exp exp : unaryExp1.getFuncRParams().getExps()) {
                    buildExp(exp, b, s);
                    params.add(curId.toString());
                }
            }
            CallInstruction callInstruction = new CallInstruction(f, params);
            b.addInstruction(callInstruction);
            if (f.getRet() instanceof IntegerType) {
                curId = callInstruction.getTargetId();
            }
        } else {
            buildUnaryExp(unaryExp1.getUnaryExp(), b, s);
        }
        while (!ops.isEmpty()) {
            String op = ops.pop();
            if (op.equals("!")) {
                IcmpInstruction icmp = new IcmpInstruction("0", curId.toString(), IcmpType.EQ);
                curId = icmp.getTargetId();
                curBlock.addInstruction(icmp);
                ZeroExtInstruction zex = new ZeroExtInstruction(curId.toString());
                curId = zex.getTargetId();
                curBlock.addInstruction(zex);
            } else if (op.equals("-")) {
                SubInstruction subInstruction = new SubInstruction("0", curId.toString());
                b.addInstruction(subInstruction);
                curId = subInstruction.getTargetId();
            }
        }
    }

    public void buildPrimaryExp(PrimaryExp primaryExp, BasicBlock b, MySymbolTable s) {
        if (primaryExp.getExp() != null) {
            buildExp(primaryExp.getExp(), b, s);
        } else if (primaryExp.getNumber() != null) {
            curId = primaryExp.getNumber().getVal();
        } else if (primaryExp.getlVal() != null) {
            buildLValRead(primaryExp.getlVal(), b, s);
        }
    }

    public void buildLValRead(LVal lval, BasicBlock b, MySymbolTable s) {
        Value allocaInstruction = s.get(lval.getIdent().getName());
        curId = allocaInstruction.getId();
        Type type = allocaInstruction.getType();
        String name = allocaInstruction.getId();
        int count = type.getDim();
        Boolean isConst = allocaInstruction.isConst();
        if (type instanceof PointerType) {
            //读入param，不可能为常量
            LoadInstruction loadInstruction = new LoadInstruction
                    (allocaInstruction.getId(), allocaInstruction.getType(), isConst);
            curId = loadInstruction.getTargetId();
            b.addInstruction(loadInstruction);
            name = curId.toString();
            count++;
        }
        if (lval.getDim() == 1) {
            count--;
            buildExp(lval.getExps().get(0), curBlock, s);
            String index0 = curId.toString();
            ElePointerInstruction elePointerInstruction = new
                    ElePointerInstruction(1, index0, type, name);
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
            if (type.getDim() == 2) {
                ElePointerInstruction e = new
                        ElePointerInstruction(1, "0", ((ArrayType) type).getElementType(), curId.toString());
                curId = e.getTargetId();
                b.addInstruction(e);
            } else if (type.getDim() == 1 && type instanceof PointerType) {
                ElePointerInstruction e = new
                        ElePointerInstruction(2, "0", type, curId.toString());
                curId = e.getTargetId();
                b.addInstruction(e);
            }

        } else if (lval.getDim() == 2) {
            count -= 2;
            buildExp(lval.getExps().get(0), curBlock, s);
            String index0 = curId.toString();
            buildExp(lval.getExps().get(1), curBlock, s);
            String index1 = curId.toString();
            ElePointerInstruction elePointerInstruction = new
                    ElePointerInstruction(2, index0, index1, type, name);
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
        } else if (!(type instanceof IntegerType)) {
            ElePointerInstruction elePointerInstruction = new
                    ElePointerInstruction(type, curId.toString());
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
        }
        if (count == 0) {
            LoadInstruction loadInstruction = new LoadInstruction(curId.toString(), isConst);
            curId = loadInstruction.getTargetId();
            b.addInstruction(loadInstruction);
        }
    }

    public void buildDecl(Decl d, BasicBlock b, MySymbolTable symbolTable) {
        if (d.getVarDecl() != null) {
            List<VarDef> varDefs = d.getVarDecl().getVarDefs();
            for (VarDef v : varDefs) {
                buildTmpVarDef(v, b, symbolTable);
            }
        }
        if (d.getConstDecl() != null) {
            List<ConstDef> constDefs = d.getConstDecl().getConstDefs();
            for (ConstDef c : constDefs) {
                buildTmpVarDef(c, b, symbolTable);
            }
        }
    }

    public void writeLval(LVal lval, BasicBlock b, MySymbolTable s, String newValue) {
        Value allocaInstruction = s.get(lval.getIdent().getName());
        Type type = allocaInstruction.getType();
        String newAddr = null;
        Boolean isConst = allocaInstruction.isConst();
        if (type instanceof PointerType) {
            LoadInstruction loadInstruction = new LoadInstruction(
                    allocaInstruction.getId(), allocaInstruction.getType(), isConst);
            // 写入参数，不可能为常量
            curId = loadInstruction.getTargetId();
            b.addInstruction(loadInstruction);
            newAddr = curId.toString();
        }
        if (lval.getDim() == 0) {
            if (newAddr == null) {
                StoreInstruction storeInstruction = new StoreInstruction(lval.getIdent().getName(),
                        s, new IntegerType(), newValue);
                b.addInstruction(storeInstruction);
            } else {
                StoreInstruction storeInstruction = new StoreInstruction(lval.getIdent().getName(),
                        newAddr, s, new IntegerType(), newValue);
                b.addInstruction(storeInstruction);
            }
        } else if (lval.getDim() == 1) {
            buildExp(lval.getExps().get(0), curBlock, s);
            String index0 = curId.toString();
            ElePointerInstruction elePointerInstruction;
            if (newAddr == null) {
                elePointerInstruction = new ElePointerInstruction(lval, s, index0, "0");
            } else {
                elePointerInstruction = new ElePointerInstruction(lval, newAddr, s, index0);
            }
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
            StoreInstruction storeInstruction = new StoreInstruction(lval.getIdent().getName(),
                    curId.toString(), new IntegerType(), newValue);
            b.addInstruction(storeInstruction);
        } else if (lval.getDim() == 2) {
            buildExp(lval.getExps().get(0), curBlock, s);
            String index0 = curId.toString();
            buildExp(lval.getExps().get(1), curBlock, s);
            String index1 = curId.toString();
            ElePointerInstruction elePointerInstruction;
            if (newAddr == null) {
                elePointerInstruction = new ElePointerInstruction(lval, s, index0, index1);
            } else {
                elePointerInstruction = new ElePointerInstruction(s, lval, newAddr, index0, index1);
            }
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
            StoreInstruction storeInstruction = new StoreInstruction(lval.getIdent().getName(),
                    curId.toString(), new IntegerType(), newValue);
            b.addInstruction(storeInstruction);
        }
    }

    public void initArray(String name, int dim, int index0, int index1,
                          BasicBlock b, MySymbolTable s, String newValue) {
        if (dim == 0) {
            StoreInstruction storeInstruction = new StoreInstruction(name,
                    s, new IntegerType(), newValue);
            b.addInstruction(storeInstruction);
        } else if (dim == 1) {
            ElePointerInstruction elePointerInstruction = new ElePointerInstruction(
                    1, String.valueOf(index0), "0", name, s);
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
            StoreInstruction storeInstruction = new StoreInstruction(name,
                    curId.toString(), new IntegerType(), newValue);
            b.addInstruction(storeInstruction);
        } else if (dim == 2) {
            ElePointerInstruction elePointerInstruction = new ElePointerInstruction(
                    2, String.valueOf(index0), String.valueOf(index1), name, s);
            curId = elePointerInstruction.getTargetId();
            b.addInstruction(elePointerInstruction);
            StoreInstruction storeInstruction = new StoreInstruction(name,
                    curId.toString(), new IntegerType(), newValue);
            b.addInstruction(storeInstruction);
        }
    }

}
