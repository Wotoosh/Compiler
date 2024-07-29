package frontend.errorchecker;

import frontend.node.*;

import java.util.ArrayList;

public class SymbolInfo {
    public SymbolType symbolType;
    public ArrayList<SymbolType> params;
    public boolean isParam;
    public boolean isConst;
    private Ident ident;

    public SymbolInfo(VarDef v) {
        ident = v.getIdent();
        int dim = v.getDim();
        if (dim == 0) {
            symbolType = SymbolType.NormalInt;
        } else if (dim == 1) {
            symbolType = SymbolType.OneDimInt;
        } else if (dim == 2) {
            symbolType = SymbolType.TwoDimInt;
        }
        isParam = false;
        isConst = false;
        params = null;
    }

    public SymbolInfo(ConstDef c) {
        ident = c.getIdent();
        int dim = c.getDim();
        if (dim == 0) {
            symbolType = SymbolType.NormalInt;
        } else if (dim == 1) {
            symbolType = SymbolType.OneDimInt;
        } else if (dim == 2) {
            symbolType = SymbolType.TwoDimInt;
        }
        isParam = false;
        isConst = true;
        params = null;
    }

    public SymbolInfo(FuncFParam f) {
        ident = f.getIdent();
        int dim = f.getDim();
        if (dim == 0) {
            symbolType = SymbolType.NormalInt;
        } else if (dim == 1) {
            symbolType = SymbolType.OneDimInt;
        } else if (dim == 2) {
            symbolType = SymbolType.TwoDimInt;
        }
        isParam = true;
        isConst = false;
        params = null;
    }

    public SymbolInfo(FuncDef f, ArrayList<SymbolType> p) {
        isParam = false;
        params = p;
        ident = f.getIdent();
        if (f.getFuncType().getType().equals("VOIDTK")) {
            symbolType = SymbolType.VoidFunc;
        } else {
            symbolType = SymbolType.IntFunc;
        }
        isConst = false;
    }

    public boolean getIsConst() {
        return isConst;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public Ident getIdent() {
        return ident;
    }

    public ArrayList<SymbolType> getParams() {
        return params;
    }
}
