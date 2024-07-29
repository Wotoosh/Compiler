package llvm.value.user.constant.constantdata;

import llvm.value.user.constant.Constant;

import java.util.ArrayList;
import java.util.List;

public class ConstantDataArray extends ConstantDataSequential {
    private List<Constant> elements;

    public ConstantDataArray() {
        elements = new ArrayList<>();
    }

    public int getDim() {
        return elements.get(0).getDim() + 1;
    }

    public List<ConstantInt> getValues() {
        List<ConstantInt> list = new ArrayList<>();
        for (Constant c : elements) {
            if (c instanceof ConstantDataArray) {
                list.addAll(((ConstantDataArray) c).getValues());
            } else {
                list.add((ConstantInt) c);
            }
        }
        return list;
    }

    public Constant getIndex(int i) {
        return elements.get(i);
    }

    public void add(Constant constant) {
        elements.add(constant);
    }

    public int size() {
        return elements.size();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[");

        for (int i = 0; i < elements.size(); ++i) {
            if (elements.get(i) instanceof ConstantDataArray) {
                ret.append("[" + ((ConstantDataArray) elements.get(i)).size() + " x i32] ");
            }
            ret.append(elements.get(i));
            if (i != elements.size() - 1) {
                ret.append(", ");
            }
        }
        ret.append("]");
        return ret.toString();
    }
}
