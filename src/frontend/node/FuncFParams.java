package frontend.node;

import java.util.ArrayList;

public class FuncFParams implements Node {
    private ArrayList<FuncFParam> params;

    public FuncFParams(ArrayList<FuncFParam> f) {
        params = f;
    }

    public FuncFParam get(int i) {
        return params.get(i);
    }

    public ArrayList<FuncFParam> getFuncFParam() {
        return params;
    }
}
