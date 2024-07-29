package frontend.errorchecker;

import frontend.node.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTableCheck {
    private Map<String, SymbolInfo> symbols;
    private ArrayList<String> orderedSymbols;

    public SymbolTableCheck() {
        orderedSymbols = new ArrayList<>();
        symbols = new HashMap<>();
    }

    public void add(ConstDef c, ErrorChecker e, boolean isErrorTesting) {
        String name = c.getIdent().getName();
        Ident i = c.getIdent();
        if (symbols.containsKey(name)) {
            if (isErrorTesting) {
                e.checkErrorB(i);
            }
        } else {
            SymbolInfo s = new SymbolInfo(c);
            symbols.put(name, s);
            orderedSymbols.add(name);
        }
    }

    public void add(FuncFParam f, ErrorChecker e, boolean errorTesting) {
        String name = f.getIdent().getName();
        Ident i = f.getIdent();
        if (symbols.containsKey(name)) {
            if (errorTesting) {
                e.checkErrorB(i);
            }
        } else {
            SymbolInfo s = new SymbolInfo(f);
            symbols.put(name, s);
            orderedSymbols.add(name);
        }
    }

    public void add(FuncDef f, ErrorChecker e, SymbolTableCheck child,boolean errorTesting) {
        if (symbols.containsKey(f.getIdent().getName())) {
            if (errorTesting) {
                e.checkErrorB(f.getIdent());
                return;
            }
        }
        ArrayList<SymbolType> params = new ArrayList<>();
        for (String item : child.orderedSymbols) {
            if (child.symbols.get(item).isParam) {
                params.add(child.symbols.get(item).symbolType);
            }
        }
        SymbolInfo symbolInfo = new SymbolInfo(f, params);
        symbols.put(f.getIdent().getName(), symbolInfo);
    }

    public void add(VarDef v, ErrorChecker e, boolean errorTesting) {
        String name = v.getIdent().getName();
        Ident i = v.getIdent();
        if (symbols.containsKey(name)) {
            if (errorTesting) {
                e.checkErrorB(i);
            }
        } else {
            SymbolInfo s = new SymbolInfo(v);
            symbols.put(name, s);
            orderedSymbols.add(name);
        }
    }

    public boolean contains(Ident ident, SymbolType s) {
        if (symbols.containsKey(ident.getName())) {
            if (!symbols.get(ident.getName()).getSymbolType().equals(SymbolType.IntFunc)
                    && !symbols.get(ident.getName()).getSymbolType().equals(SymbolType.VoidFunc)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public boolean contains(Ident ident) {
        if (symbols.containsKey(ident.getName())) {
            if (symbols.get(ident.getName()).getSymbolType().equals(SymbolType.IntFunc) ||
                    symbols.get(ident.getName()).getSymbolType().equals(SymbolType.VoidFunc)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public SymbolInfo getSymbolInfo(Ident i) {
        return symbols.get(i.getName());
    }
}
