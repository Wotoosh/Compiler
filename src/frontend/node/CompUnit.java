package frontend.node;

import java.util.ArrayList;

public class CompUnit implements Node {
    private ArrayList<Decl> decls;
    private ArrayList<FuncDef> defs;
    private MainFuncDef mainFuncDef;

    public CompUnit(ArrayList<Decl> d, ArrayList<FuncDef> f, MainFuncDef m) {
        if (!d.isEmpty()) {
            decls = d;
        } else {
            decls = null;
        }
        if (!f.isEmpty()) {
            defs = f;
        } else {
            defs = null;
        }
        mainFuncDef = m;
        //symbolTable=new SymbolTable(d.getSymbolTable(),f.getSymbolTable());
    }

    public ArrayList<Decl> getDecls() {
        return decls;
    }

    public ArrayList<FuncDef> getFuncs() {
        return defs;
    }

    public MainFuncDef getMainFunc() {
        return mainFuncDef;
    }
}
