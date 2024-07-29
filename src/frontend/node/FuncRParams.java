package frontend.node;

import java.util.ArrayList;

public class FuncRParams {
    private ArrayList<Exp> exps;

    public FuncRParams(ArrayList<Exp> e) {
        if (e.isEmpty()) {
            exps = null;
        } else {
            exps = e;
        }
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }
}
