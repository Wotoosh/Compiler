package frontend;

import frontend.errorchecker.ErrorChecker;
import frontend.errorchecker.SymbolTableCheck;
import frontend.errorchecker.SymbolType;
import frontend.node.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int curIndex;
    private Token curToken;
    private ArrayList<String> save;
    private CompUnit compUnit;
    private ErrorChecker errorChecker;
    private ArrayList<SymbolTableCheck> symbolTables;
    private int avail;
    private boolean errorTesting;

    public Parser(ArrayList<Token> tokens, ErrorChecker e, boolean testerror) {
        this.tokens = tokens;
        curIndex = 0;
        errorChecker = e;
        symbolTables = new ArrayList<>();
        curToken = tokens.get(curIndex);
        save = new ArrayList<>();
        avail = 0;
        errorTesting = testerror;
        compUnit = parseCompUnit();

    }

    public void print(FileOutputStream fout) {
        for (String item : save) {
            try {
                fout.write(item.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CompUnit getCompUnit() {
        return compUnit;
    }

    public void print() {
        for (String s : save) {
            System.out.print(s);
        }
    }

    public void nextToken() {
        curIndex++;
        save.add(curToken.toString());
        if (curIndex >= tokens.size())
            curToken = null;
        else
            curToken = tokens.get(curIndex);
    }

    public Token peekToken(int i) {
        if (curIndex + i >= tokens.size())
            return null;
        else
            return tokens.get(curIndex + i);
    }

    public void removeSymbolTabelTail() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    public MainFuncDef parseMainFuncDef() {
        nextToken(); //jump int
        nextToken(); //jump main
        nextToken(); //jump (
        if (!curToken.getValue().equals(")")) {
            if (errorTesting) {
                errorChecker.checkErrorJ(peekToken(-1).getLine());
            }
        } else {
            nextToken(); // jump )
        }
        Block block = parseBlock();
        MainFuncDef m = new MainFuncDef(block);
        if (errorTesting) {
            errorChecker.checkErrorG(m, peekToken(-1).getLine());
        }
        save.add("<MainFuncDef>\n");
        return m;
    }

    public CompUnit parseCompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        SymbolTableCheck s = new SymbolTableCheck();
        symbolTables.add(s);
        while (!peekToken(1).getType().equals("MAINTK")) {
            if (peekToken(2).getType().equals("LPARENT")) {
                FuncDef funcDef = parseFuncDef();
                funcDefs.add(funcDef);
            } else {
                Decl decl = parseDecl();
                decls.add(decl);
            }
        }
        MainFuncDef mainFuncDef = parseMainFuncDef();
        save.add("<CompUnit>\n");
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }
//
//    public boolean isForStmt() {
//        if (!curToken.getType().equals("IDENFR")) {
//            return false;
//        }
//        int length = save.size();
//        int pos = curIndex;
//        int error_pos = 0;
//        if (errorTesting) {
//            error_pos = errorChecker.getLength();
//        }
//        parseLVal();
//        boolean flag;
//        if (curToken.getValue().equals("=")) {
//            flag = true;
//        } else {
//            flag = false;
//        }
//        curIndex = pos;
//        curToken = tokens.get(curIndex);
//        while (save.size() > length) {
//            save.remove(length);
//        }
//        if (errorTesting) {
//            errorChecker.restore(error_pos);
//        }
//        return flag;
//    }

    public boolean isExpIndent() {
        if (!curToken.getType().equals("IDENFR")) {
            return false;
        }
        int length = save.size();
        int pos = curIndex;
        int error_pos = 0;
        if (errorTesting) {
            error_pos = errorChecker.getLength();
        }
        parseLVal();
        boolean flag;
        if (curToken.getValue().equals("=")) {
            flag = false;
        } else {
            flag = true;
        }
        curIndex = pos;
        curToken = tokens.get(curIndex);
        while (save.size() > length) {
            save.remove(length);
        }
        if (errorTesting) {
            errorChecker.restore(error_pos);
        }
        return flag;
    }

    public Stmt parseStmt2() {
        Block block = parseBlock();
        Stmt stmt = new Stmt(2, block);
        return stmt;
    }

    public Stmt parseStmt3() {
        nextToken();
        nextToken();  //jump "("
        Cond cond = parseCond();
        if (!curToken.getValue().equals(")")) {
            if (errorTesting) {
                errorChecker.checkErrorJ(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        Stmt s1 = parseStmt();
        Stmt s2 = null;
        if (curToken.getValue().equals("else")) {
            nextToken();
            s2 = parseStmt();
        }
        return new Stmt(3, cond, s1, s2);
    }

    public Cond parseCond() {
        LOrExp lOrExp = parseLOrExp();
        Cond cond = new Cond(lOrExp);
        save.add("<Cond>\n");
        return cond;
    }

    public LOrExp parseLOrExp() {
        LAndExp lAndExp = parseLAndExp();
        LOrExp lOrExp = new LOrExp(lAndExp);
        save.add("<LOrExp>\n");
        while (curToken.getValue().equals("||")) {
            nextToken();
            lAndExp = parseLAndExp();
            lOrExp = new LOrExp(lOrExp, lAndExp);
            save.add("<LOrExp>\n");
        }
        return lOrExp;
    }

    public LAndExp parseLAndExp() {
        EqExp e = parseEqExp();
        LAndExp lAndExp = new LAndExp(e);
        save.add("<LAndExp>\n");
        while (curToken.getValue().equals("&&")) {
            nextToken();
            e = parseEqExp();
            lAndExp = new LAndExp(lAndExp, e);
            save.add("<LAndExp>\n");
        }
        return lAndExp;
    }

    public EqExp parseEqExp() {
        RelExp r = parseRelExp();
        EqExp eqExp = new EqExp(r);
        save.add("<EqExp>\n");
        while (curToken.getValue().equals("==") || curToken.getValue().equals("!=")) {
            String op = curToken.getValue();
            nextToken();
            r = parseRelExp();
            eqExp = new EqExp(eqExp, op, r);
            save.add("<EqExp>\n");
        }
        return eqExp;
    }

    public RelExp parseRelExp() {
        AddExp a = parseAddExp();
        RelExp relExp = new RelExp(a);
        save.add("<RelExp>\n");
        while (curToken.getValue().equals(">") || curToken.getValue().equals("<") || curToken.getValue().equals(">=") || curToken.getValue().equals("<=")) {
            String op = curToken.getValue();
            nextToken();
            a = parseAddExp();
            relExp = new RelExp(relExp, op, a);
            save.add("<RelExp>\n");
        }
        return relExp;
    }

    public ForStmt parseForStmt() {
        LVal lVal = parseLVal();
        nextToken();//jump =
        Exp exp1 = parseExp();
        ForStmt forStmt = new ForStmt(lVal, exp1);
        save.add("<ForStmt>\n");
        return forStmt;
    }

    public Stmt parseStmt4() {
        nextToken();
        nextToken();//jump "("
        ForStmt forStmt1 = null;
        ForStmt forStmt2 = null;
        Cond cond = null;
        Stmt s;
        if (!curToken.getValue().equals(";")) {
            forStmt1 = parseForStmt();
            nextToken();
        } else {
            nextToken();
        }
        if (!curToken.getValue().equals(";")) {
            cond = parseCond();
            nextToken();
        } else {
            nextToken();
        }
        if (!curToken.getValue().equals(")")) {
            forStmt2 = parseForStmt();
            nextToken();
        } else {
            nextToken();
        }
        s = parseStmt();
        return new Stmt(4, forStmt1, cond, forStmt2, s);
    }

    public Stmt parseStmt5() {
        Stmt stmt = new Stmt(5);   //break;
        nextToken();
        if (!curToken.getValue().equals(";")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        return stmt;
    }

    public Stmt parseStmt9() {
        nextToken();  //jump printf
        nextToken();  //jump "("
        FormatString formatString = new FormatString(curToken.getValue());
        if (errorTesting) {
            errorChecker.checkErrorA(curToken.getLine(), curToken.getValue());
        }
        int line = peekToken(-2).getLine();  //printf的行号
        nextToken();
        ArrayList<Exp> exps = new ArrayList<>();
        while (curToken.getValue().equals(",")) {
            nextToken();
            exps.add(parseExp());
        }
        if (!curToken.getValue().equals(")")) {
            if (errorTesting) {
                errorChecker.checkErrorJ(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        if (!curToken.getValue().equals(";")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        if (errorTesting) {
            errorChecker.checkErrorL(peekToken(-2).getLine(), formatString.getFormatString(), exps);
        }
        return new Stmt(9, formatString, exps);
    }

    public Stmt parseStmt7() {
        int line = curToken.getLine();
        nextToken();
        Exp exp = null;
        if (isExpFirst()) {
            exp = parseExp();
        }
        if (!curToken.getValue().equals(";")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        return new Stmt(7, exp, line);
    }

    public Stmt parseStmt6() {
        Stmt stmt = new Stmt(6);   //continue;
        nextToken();
        if (!curToken.getValue().equals(";")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        return stmt;
    }

    public Stmt parseStmt1() {
        if (curToken.getValue().equals(";")) {
            nextToken();
            return new Stmt(1);
        } else {
            Exp e = parseExp();
            if (!curToken.getValue().equals(";")) {
                if (errorTesting) {
                    errorChecker.checkErrorI(peekToken(-1).getLine());
                }
            } else {
                nextToken();
            }
            return new Stmt(1, e);
        }
    }

    public Stmt parseStmt() {
        Stmt stmt = null;
        if (curToken.getValue().equals("{")) {
            stmt = parseStmt2();
        } else if (curToken.getValue().equals("}")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else if (curToken.getType().equals("IFTK")) {
            stmt = parseStmt3();
        } else if (curToken.getType().equals("FORTK")) {
            avail++;
            stmt = parseStmt4();
            avail--;
        } else if (curToken.getType().equals("BREAKTK")) {
            if (avail == 0) {
                if (errorTesting) {
                    errorChecker.checkErrorM(curToken.getLine());
                }
            }
            stmt = parseStmt5();
        } else if (curToken.getType().equals("CONTINUETK")) {
            if (avail == 0) {
                if (errorTesting) {
                    errorChecker.checkErrorM(curToken.getLine());
                }
            }
            stmt = parseStmt6();
        } else if (curToken.getType().equals("RETURNTK")) {
            stmt = parseStmt7();
        } else if (curToken.getType().equals("PRINTFTK")) {
            stmt = parseStmt9();
        } else if (curToken.getValue().equals(";") || !curToken.getType().equals("IDENFR")) {
            stmt = parseStmt1();
        } else {
            if (!isExpIndent()) {
                LVal lVal = parseLVal();
                nextToken();
                if (peekToken(0).getType().equals("GETINTTK")) {
                    nextToken(); // jump getint
                    nextToken(); // jump (
                    if (!curToken.getValue().equals(")")) {
                        if (errorTesting) {
                            errorChecker.checkErrorJ(peekToken(-1).getLine());
                        }
                    } else {
                        nextToken();
                    }
                    if (!curToken.getValue().equals(";")) {
                        if (errorTesting) {
                            errorChecker.checkErrorI(peekToken(-1).getLine());
                        }
                    } else {
                        nextToken();
                    }
                    stmt = new Stmt(8, lVal);
                } else {
                    Exp exp = parseExp();
                    stmt = new Stmt(0, lVal, exp);
                    if (!curToken.getValue().equals(";")) {
                        if (errorTesting) {
                            errorChecker.checkErrorI(peekToken(-1).getLine());
                        }
                    } else {
                        nextToken();
                    }
                }
            } else {
                Exp exp = parseExp();
                stmt = new Stmt(1, exp);
                if (!curToken.getValue().equals(";")) {
                    if (errorTesting) {
                        errorChecker.checkErrorI(peekToken(-1).getLine());
                    }
                } else {
                    nextToken();
                }
            }
        }
        save.add("<Stmt>\n");
        if (stmt != null) {
            if (stmt.getType() == 0 || stmt.getType() == 8) {
                if (errorTesting) {
                    errorChecker.checkErrorH(symbolTables, stmt);
                }
            }
        }
        return stmt;
    }

    public boolean isExpFirst() {
        if (curToken.getValue().equals("(")) {
            return true;
        } else if (isUnaryOp()) {
            return true;
        } else if (curToken.getType().equals("IDENFR")) {
            return true;
        } else if (curToken.getType().equals("INTCON")) {
            return true;
        }
        return false;
    }


    public FuncFParams parseFuncFParams() {
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        SymbolTableCheck s = getSymbolTableTail();
        FuncFParam f = parseFuncFParam();
        funcFParams.add(f);
        s.add(f, errorChecker, errorTesting);
        while (curToken.getType().equals("COMMA")) {
            nextToken();
            f = parseFuncFParam();
            funcFParams.add(f);
            s.add(f, errorChecker, errorTesting);
        }
        save.add("<FuncFParams>\n");
        return new FuncFParams(funcFParams);
    }

    public SymbolTableCheck getSymbolTableTail() {
        return symbolTables.get(symbolTables.size() - 1);
    }

    public FuncFParam parseFuncFParam() {
        BType bType = parseBType();
        Ident ident = parseIdent();
        ArrayList<ConstExp> constExps = new ArrayList<>();
        int dim = 0;
        if (curToken.getValue().equals("[")) {
            nextToken();
            dim++;
            if (!curToken.getValue().equals("]")) {
                if (errorTesting) {
                    errorChecker.checkErrorK(peekToken(-1).getLine());
                }
            } else {
                nextToken();
            }
            while (curToken.getValue().equals("[")) {
                nextToken();
                dim++;
                constExps.add(parseConstExp());
                if (!curToken.getValue().equals("]")) {
                    if (errorTesting) {
                        errorChecker.checkErrorK(peekToken(-1).getLine());
                    }
                } else {
                    nextToken();
                }
            }
        }
        save.add("<FuncFParam>\n");
        return new FuncFParam(bType, ident, constExps, dim);
    }

    public boolean isMulToAdd() {
        if (curToken.getValue().equals("+") || curToken.getValue().equals("-")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isUnaryToMul() {
        if ("*/%".contains(curToken.getValue())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFuncRParamsFirst() {
        return isExpFirst();
    }


    public UnaryOp parseUnaryOp() {
        UnaryOp op = new UnaryOp(curToken.getValue());
        nextToken();
        save.add("<UnaryOp>\n");
        return op;
    }

    public Exp parseExp() {
        AddExp a = parseAddExp();
        Exp e = new Exp(a);
        save.add("<Exp>\n");
        return e;
    }

    public LVal parseLVal() {
        Ident ident = parseIdent();
        ArrayList<Exp> exps = new ArrayList<>();
        while (curToken.getValue().equals("[")) {
            nextToken();  //jump "["
            exps.add(parseExp());
            if (!curToken.getValue().equals("]")) {
                if (errorTesting) {
                    errorChecker.checkErrorK(peekToken(-1).getLine());
                }
            } else {
                nextToken();  //jump"]"
            }
        }
        save.add("<LVal>\n");
        LVal lVal = null;
        if (!exps.isEmpty()) {
            lVal = new LVal(ident, exps);
            if (exps.size() == 1) {
                if (errorTesting) {
                    errorChecker.checkErrorC(symbolTables, ident, SymbolType.OneDimInt);
                }
            } else {
                if (errorTesting) {
                    errorChecker.checkErrorC(symbolTables, ident, SymbolType.TwoDimInt);
                }
            }
        } else {
            lVal = new LVal(ident, null);
            if (errorTesting) {
                errorChecker.checkErrorC(symbolTables, ident, SymbolType.NormalInt);
            }
        }
        return lVal;
    }

    public MyNumber parseNumber() {
        MyNumber n = new MyNumber(curToken.getValue());
        nextToken();
        save.add("<Number>\n");
        return n;
    }

    public PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp = null;
        if (curToken.getValue().equals("(")) {
            nextToken();
            Exp e = parseExp();
            primaryExp = new PrimaryExp(e);
            if (!curToken.getValue().equals(")")) {
                if (errorTesting) {
                    errorChecker.checkErrorJ(peekToken(-1).getLine());
                }
            } else {
                nextToken();
            }
        } else if (curToken.getType().equals("IDENFR")) {
            LVal lVal = parseLVal();
            primaryExp = new PrimaryExp(lVal);
        } else if (curToken.getType().equals("INTCON")) {
            MyNumber number = parseNumber();
            primaryExp = new PrimaryExp(number);
        }
        save.add("<PrimaryExp>\n");
        return primaryExp;
    }


    public UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = null;
        if (isUnaryOp()) {
            UnaryOp op = parseUnaryOp();
            UnaryExp exp = parseUnaryExp();
            unaryExp = new UnaryExp(op, exp);
        } else if (!curToken.getValue().equals("(") && peekToken(1).getValue().equals("(")) {
            Ident ident = parseIdent();
            boolean hasC = false;
            if (errorTesting) {
                hasC = errorChecker.checkErrorC(symbolTables, ident);
            }
            nextToken();//jump"("
            FuncRParams params = null;
            if (isFuncRParamsFirst()) {
                params = parseFuncRParams();
                if (!curToken.getValue().equals(")")) {
                    if (errorTesting) {
                        errorChecker.checkErrorJ(peekToken(-1).getLine());
                    }
                } else {
                    nextToken();
                }
                unaryExp = new UnaryExp(ident, params);
            } else {
                unaryExp = new UnaryExp(ident);
                if (!curToken.getValue().equals(")")) {
                    if (errorTesting) {
                        errorChecker.checkErrorJ(peekToken(-1).getLine());
                    }
                } else {
                    nextToken();
                }
            }
            if (errorTesting) {
                boolean hasD = errorChecker.checkErrorD(symbolTables, ident, params);
                if (!hasD && !hasC) {
                    errorChecker.checkErrorE(symbolTables, ident, params);
                }
            }
        } else {
            PrimaryExp p = parsePrimaryExp();
            unaryExp = new UnaryExp(p);
        }
        save.add("<UnaryExp>\n");
        return unaryExp;
    }

    public boolean isUnaryOp() {
        if ("+-!".contains(curToken.getValue())) {
            return true;
        } else {
            return false;
        }
    }

    public MulExp parseMulExp() {
        UnaryExp u = parseUnaryExp();
        MulExp m = new MulExp(u);
        save.add("<MulExp>\n");
        while (isUnaryToMul()) {
            String op = curToken.getValue();
            nextToken();
            u = parseUnaryExp();
            m = new MulExp(m, op, u);
            save.add("<MulExp>\n");
        }
        return m;
    }

    public AddExp parseAddExp() {
        MulExp mul = parseMulExp();
        AddExp a = new AddExp(mul);
        save.add("<AddExp>\n");
        while (isMulToAdd()) {
            String op = curToken.getValue();
            nextToken();
            mul = parseMulExp();
            a = new AddExp(a, op, mul);
            save.add("<AddExp>\n");
        }
        return a;
    }

    public ConstExp parseConstExp() {
        AddExp addExp = parseAddExp();
        ConstExp c = new ConstExp(addExp);
        save.add("<ConstExp>\n");
        return c;
    }

    public BType parseBType() {
        BType bType = new BType(curToken.getValue());
        nextToken();
        return bType;
    }

    public BlockItem parseBlockItem() {
        BlockItem blockItem = null;
        if (isDeclFirst()) {
            Decl d = parseDecl();
            blockItem = new BlockItem(d);
        } else {
            Stmt stmt = parseStmt();
            blockItem = new BlockItem(stmt);
        }
        return blockItem;
    }

    public boolean isDeclFirst() {
        if (curToken.getType().equals("INTTK") || curToken.getType().equals("CONSTTK")) {
            return true;
        } else {
            return false;
        }
    }

    public Block parseBlock() {
        nextToken();// jump "{"
        symbolTables.add(new SymbolTableCheck());
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        while (!curToken.getValue().equals("}")) {
            blockItems.add(parseBlockItem());
        }
        nextToken(); //jump "}"
        Block b = new Block(blockItems);
        save.add("<Block>\n");
        removeSymbolTabelTail();
        return b;
    }

    public Block parseBlockNoNewTable() {
        nextToken();// jump "{"
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        while (!curToken.getValue().equals("}")) {
            blockItems.add(parseBlockItem());
        }
        nextToken(); //jump "}"
        Block b = new Block(blockItems);
        save.add("<Block>\n");
        return b;
    }

    public FuncDef parseFuncDef() {
        FuncType funcType = parseFuncType();
        Ident ident = parseIdent();
        nextToken();  // jump "("
        SymbolTableCheck s = new SymbolTableCheck();
        symbolTables.add(s);
        FuncFParams funcFParams = null;
        if (curToken.getType().equals("INTTK")) {
            funcFParams = parseFuncFParams();
        }
        if (!curToken.getValue().equals(")")) {
            if (errorTesting) {
                errorChecker.checkErrorJ(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        FuncDef f = new FuncDef(funcType, ident, funcFParams);
        SymbolTableCheck cur = symbolTables.get(symbolTables.size() - 2);
        cur.add(f, errorChecker, s,errorTesting);
        Block b = parseBlockNoNewTable();
        int line = peekToken(-1).getLine();  // line suggests the line of "}"
        f = new FuncDef(funcType, ident, funcFParams, b);
        removeSymbolTabelTail();
        if (errorTesting) {
            errorChecker.checkErrorF(f);
            errorChecker.checkErrorG(f, line);
        }
        save.add("<FuncDef>\n");
        return f;
    }

    public FuncType parseFuncType() {
        FuncType funcType = new FuncType(curToken.getType());
        nextToken();
        save.add("<FuncType>\n");
        return funcType;
    }

    public Ident parseIdent() {
        Ident ident = new Ident(curToken.getValue(), curToken.getLine());
        nextToken();
        return ident;
    }

    public Decl parseDecl() {
        Decl d = null;
        if (curToken.getType().equals("CONSTTK")) {
            d = new Decl(parseConstDecl());
        } else {
            d = new Decl(parseVarDecl());
        }
        //save.add("<Decl>\n");
        return d;
    }

    public VarDef parseVarDef() {
        VarDef varDef = null;
        Ident ident = parseIdent();
        ArrayList<ConstExp> constExps = new ArrayList<>();
        while (curToken.getValue().equals("[")) {
            nextToken();
            constExps.add(parseConstExp());
            if (!curToken.getValue().equals("]")) {
                if (errorTesting) {
                    errorChecker.checkErrorK(peekToken(-1).getLine());
                }
            } else {
                nextToken();
            }
        }
        if (curToken.getValue().equals("=")) {
            nextToken();
            InitVal initVal = parseInitVal();
            varDef = new VarDef(ident, constExps, initVal);
        } else {
            varDef = new VarDef(ident, constExps);
        }
        save.add("<VarDef>\n");
        return varDef;
    }

    public InitVal parseInitVal() {
        InitVal initVal = null;
        if (curToken.getValue().equals("{")) {
            ArrayList<InitVal> initVals = new ArrayList<>();
            nextToken();
            while (!curToken.getValue().equals("}")) {
                if (curToken.getValue().equals(",")) {
                    nextToken();
                }
                initVals.add(parseInitVal());
            }
            nextToken();
            initVal = new InitVal(initVals);
        } else {
            Exp e = parseExp();
            initVal = new InitVal(e);
        }
        save.add("<InitVal>\n");
        return initVal;
    }

    public VarDecl parseVarDecl() {
        BType b = parseBType();
        ArrayList<VarDef> varDefs = new ArrayList<>();
        VarDef v = parseVarDef();
        SymbolTableCheck cur = getSymbolTableTail();
        cur.add(v, errorChecker, errorTesting);
        varDefs.add(v);
        while (curToken.getValue().equals(",")) {
            nextToken();
            v = parseVarDef();
            cur.add(v, errorChecker, errorTesting);
            varDefs.add(v);
        }
        if (!curToken.getValue().equals(";")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        save.add("<VarDecl>\n");
        return new VarDecl(b, varDefs);
    }

    public ConstInitVal parseConstInitVal() {
        ArrayList<ConstInitVal> constInitVals = new ArrayList<>();
        ConstInitVal constInitVal = null;
        if (curToken.getValue().equals("{")) {
            nextToken();
            while (!curToken.getValue().equals("}")) {
                if (curToken.getValue().equals(",")) {
                    nextToken();
                }
                constInitVals.add(parseConstInitVal());
            }
            constInitVal = new ConstInitVal(constInitVals);
            nextToken();
        } else {
            ConstExp e = parseConstExp();
            constInitVal = new ConstInitVal(e);
        }
        save.add("<ConstInitVal>\n");
        return constInitVal;
    }

    public ConstDef parseConstDef() {
        Ident ident = parseIdent();
        ConstInitVal constInitVal = null;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        while (curToken.getValue().equals("[")) {
            nextToken();
            constExps.add(parseConstExp());
            if (!curToken.getValue().equals("]")) {
                if (errorTesting) {
                    errorChecker.checkErrorK(peekToken(-1).getLine());
                }
            } else {
                nextToken();
            }
        }
        if (curToken.getValue().equals("=")) {
            nextToken();
            constInitVal = parseConstInitVal();
        }
        save.add("<ConstDef>\n");
        return new ConstDef(ident, constExps, constInitVal);
    }

    public ConstDecl parseConstDecl() {
        nextToken();  //jump const
        BType b = parseBType();
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        ConstDef d = parseConstDef();
        SymbolTableCheck cur = getSymbolTableTail();
        cur.add(d, errorChecker, errorTesting);
        constDefs.add(d);
        while (curToken.getValue().equals(",")) {
            nextToken();
            d = parseConstDef();
            cur.add(d, errorChecker, errorTesting);
            constDefs.add(d);
        }
        if (!curToken.getValue().equals(";")) {
            if (errorTesting) {
                errorChecker.checkErrorI(peekToken(-1).getLine());
            }
        } else {
            nextToken();
        }
        save.add("<ConstDecl>\n");
        return new ConstDecl(b, constDefs);
    }

    public FuncRParams parseFuncRParams() {
        ArrayList<Exp> exps = new ArrayList<>();
        exps.add(parseExp());
        while (curToken.getType().equals("COMMA")) {
            nextToken();
            exps.add(parseExp());
        }
        save.add("<FuncRParams>\n");
        return new FuncRParams(exps);
    }

    public void error(String s) {
        if (s.equals("j")) {
            System.out.println("lack )");
        } else if (s.equals("k")) {
            System.out.println("lack ]");
        } else if (s.equals("i")) {
            System.out.println("lack ;");
        }
    }
}
