package frontend.errorchecker;

import frontend.node.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorChecker {

    ArrayList<String> save;


    public ErrorChecker() {
        save = new ArrayList<>();
    }

    public void print(int l, String t) {
        save.add(l + " " + t + "\n");
    }

    public void printAll() {
        for (String s : save) {
            System.out.println(s);
        }
    }

    public boolean hasError() {
        if (save.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public void print(FileOutputStream fout) {
        Collections.sort(save, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] s1 = o1.split(" ");
                String[] s2 = o2.split(" ");
                return Integer.valueOf(s1[0]) - Integer.valueOf(s2[0]);
            }

        });
        for (String item : save) {
            try {
                fout.write(item.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getLength() {
        return save.size();
    }

    public void restore(int index) {
        while (save.size() > index) {
            save.remove(index);
        }
    }

    public void checkErrorA(int line, String token) {
        Pattern regex = Pattern.compile("[^\\x20\\x21\\x28-\\x7e]");
        token = token.replaceAll("%d", "");
        if (token.charAt(0) == '"') {
            token = token.substring(1);
        }
        if (token.length() != 0 && token.charAt(token.length() - 1) == '"') {
            token = token.substring(0, token.length() - 1);
        }
        Matcher regexMatcher = regex.matcher(token);
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '\\' && (i == token.length() - 1 || token.charAt(i + 1) != 'n')) {
                print(line, "a");
                return;
            }
        }
        if (regexMatcher.find()) {
            print(line, "a");
        }
    }

    public void checkErrorB(Ident i) {
        print(i.getLine(), "b");
    }

    public boolean checkErrorC(ArrayList<SymbolTableCheck> s, Ident i, SymbolType symbolType) {
        int flag = 1;
        for (SymbolTableCheck item : s) {
            if (item.contains(i, symbolType)) {
                flag = 0;
            }
        }
        if (flag == 1) {
            print(i.getLine(), "c");
        }
        return flag == 1;
    }

    public boolean checkErrorC(ArrayList<SymbolTableCheck> s, Ident i) {
        int flag = 1;
        for (SymbolTableCheck item : s) {
            if (item.contains(i)) {
                flag = 0;
            }
        }
        if (flag == 1) {
            print(i.getLine(), "c");
        }
        return flag == 1;
    }

    public boolean checkErrorD(ArrayList<SymbolTableCheck> symbolTables, Ident i, FuncRParams f) {
        SymbolInfo symbolInfo = null;
        for (SymbolTableCheck item : symbolTables) {
            if (item.contains(i)) {
                symbolInfo = item.getSymbolInfo(i);
            }
        }
        if (symbolInfo != null) {
            boolean flag = isParamsNumEqual(symbolInfo, f);
            if (!flag) {
                print(i.getLine(), "d");
                return true;
            }
        }
        return false;
    }

    public void checkErrorE(ArrayList<SymbolTableCheck> symbolTables, Ident i, FuncRParams f) {
        SymbolInfo symbolInfo = findSymbolInfoFunc(symbolTables, i);
        ArrayList<SymbolType> symbolTypes = symbolInfo.getParams();
        int flag = 0;
        if (f == null) {
            if (symbolTypes != null && symbolTypes.size() != 0) {
                flag = 1;
            }
        } else {
            ArrayList<Exp> exps = f.getExps();
            for (int j = 0; j < exps.size(); j++) {
                if (symbolTypes.get(j) != exps.get(j).getSymbolType(symbolTables)) {
                    flag = 1;
                }
            }
        }
        if (flag == 1) {
            print(i.getLine(), "e");
        }
    }


    public SymbolInfo findSymbolInfoFunc(ArrayList<SymbolTableCheck> symbolTables, Ident i) {
        SymbolInfo symbolInfo = null;
        for (SymbolTableCheck item : symbolTables) {
            if (item.contains(i)) {
                symbolInfo = item.getSymbolInfo(i);
            }
        }
        return symbolInfo;
    }

    public SymbolInfo findSymbolInfo(ArrayList<SymbolTableCheck> symbolTables, Ident i) {
        SymbolInfo symbolInfo = null;
        for (SymbolTableCheck item : symbolTables) {
            if (item.contains(i, SymbolType.NormalInt)) {
                symbolInfo = item.getSymbolInfo(i);
            }
        }
        return symbolInfo;
    }

    public boolean isParamsNumEqual(SymbolInfo symbolInfo, FuncRParams f) {
        if (f == null) {
            if (symbolInfo.getParams().isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
        if (symbolInfo.getParams().size() != f.getExps().size()) {
            return false;
        }
        return true;
    }

    public void checkErrorF(FuncDef funcDef) {
        if (funcDef.getFuncType().getType().equals("INTTK")) {
            return;
        }
        Block b = funcDef.getBlock();
        BlockItem blockItem = b.getLastBlockItem();
        if (blockItem == null || blockItem.getStmt() == null) {
            return;
        } else {
            Stmt stmt = blockItem.getStmt();
            if (stmt.getType() == 7 && stmt.getExp() != null) {
                print(stmt.getLine(), "f");
            }
        }
    }

    public void checkErrorG(FuncDef funcDef, int line) {
        if (funcDef.getFuncType().getType().equals("VOIDTK")) {
            return;
        }
        Block b = funcDef.getBlock();
        BlockItem blockItem = b.getLastBlockItem();
        if (blockItem == null || blockItem.getStmt() == null) {
            print(line, "g");
        } else {
            Stmt stmt = blockItem.getStmt();
            if (stmt.getType() != 7 || stmt.getExp() == null) {
                print(line, "g");
            }
        }
    }

    public void checkErrorG(MainFuncDef m, int line) {
        Block b = m.getBlock();
        BlockItem blockItem = b.getLastBlockItem();
        if (blockItem == null || blockItem.getStmt() == null) {
            print(line, "g");
        } else {
            Stmt stmt = blockItem.getStmt();
            if (stmt.getType() != 7 || stmt.getExp() == null) {
                print(line, "g");
            }
        }
    }

    public void checkErrorH(ArrayList<SymbolTableCheck> symbolTables, Stmt s) {
        SymbolInfo symbolInfo = findSymbolInfo(symbolTables, s.getlVal().getIdent());
        if (symbolInfo != null && symbolInfo.getIsConst()) {
            print(s.getlVal().getIdent().getLine(), "h");
        }
    }

    public void checkErrorI(int line) {
        print(line, "i");
    }

    public void checkErrorJ(int line) {
        print(line, "j");
    }

    public void checkErrorK(int line) {
        print(line, "k");
    }

    public void checkErrorL(int line, String fs, ArrayList<Exp> exps) {
        int countfs = 0;
        int countexps = 0;
        for (int i = 0; i < fs.length() - 1; i++) {
            if (fs.charAt(i) == '%' && fs.charAt(i + 1) == 'd') {
                countfs++;
            }
        }
        if (exps != null) {
            countexps = exps.size();
        }
        if (countfs != countexps) {
            print(line, "l");
        }
    }

    public void checkErrorM(int line) {
        print(line, "m");
    }
}
