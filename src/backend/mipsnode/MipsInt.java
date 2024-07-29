package backend.mipsnode;

import llvm.type.ArrayType;
import llvm.type.PointerType;
import llvm.type.Type;
import llvm.value.user.constant.constantdata.ConstantDataArray;
import llvm.value.user.constant.constantdata.ConstantInt;
import llvm.value.user.constant.globalobject.GlobalVariable;
import llvm.value.user.instruction.LocalAllocaInstruction;

import java.util.ArrayList;
import java.util.List;

public class MipsInt extends MipsData {

    private boolean isZero;
    private List<MipsConst> initvalues;
    private int dim1;
    private int dim2;
    private int dim;

    public MipsData derive() {
        if (dim == 1 || dim == 0) {
            return new MipsInt();
        } else {
            return new MipsInt(dim1);
        }
    }

    public MipsInt(LocalAllocaInstruction l) {
        initvalues = new ArrayList<>();
        dim = l.getType().getDim();
        Type tmp = l.getType();
        dim1 = 0;
        isZero = false;
        dim2 = 0;
        while (tmp instanceof PointerType) {
            tmp = ((PointerType) tmp).getPointed();
        }
        if (dim >= 1) {
            dim1 = ((ArrayType) tmp).getElements();
            dim2 = 1;
        }
        if (dim == 2) {
            dim2 = ((ArrayType) ((ArrayType) tmp).getElementType()).getElements();
        }
        if (l.getConstInitVal() != null && l.getConstInitVal() instanceof ConstantDataArray && ((ConstantDataArray) l.getConstInitVal()).getValues() != null) {
            for (ConstantInt c : ((ConstantDataArray) l.getConstInitVal()).getValues()) {
                initvalues.add(new MipsConst(c.getNum()));
                size += 4;
            }
        } else {
            for (int i = 0; i < dim1 * dim2; i++) {
                initvalues.add(new MipsConst(0));
                size += 4;
            }
        }
        if (dim == 0) {
            initvalues.add(new MipsConst(0));
            size = 4;
        }
    }

    public MipsInt() {
        isZero = false;
        size = 4;
        initvalues = new ArrayList<>();
        initvalues.add(new MipsConst(0));
        dim = 0;
        dim2 = 1;
    }

    public MipsInt(int index1) {
        isZero = false;
        size = index1 * 4;
        initvalues = new ArrayList<>();
        for (int i = 0; i < index1; i++) {
            initvalues.add(new MipsConst(0));
        }
        dim = 1;
        dim1 = index1;
        dim2 = 1;
    }

    public MipsInt(int index1, int index2) {
        isZero = false;
        size = index1 * 4 * index2;
        initvalues = new ArrayList<>();
        for (int i = 0; i < index1 * index2; i++) {
            initvalues.add(new MipsConst(0));
        }
        dim = 2;
        dim1 = index1;
        dim2 = index2;
    }

    public MipsInt(GlobalVariable g) {
        name = g.getId();
        size = g.getType().getSize() * 4;
        isZero = g.getIsZero();
        initvalues = new ArrayList<>();
        if (isZero) {
            for (int i = 0; i < size; i++) {
                initvalues.add(new MipsConst(0));
            }
            dim = g.getType().getDim();
            if (dim >= 1) {
                dim1 = ((ArrayType) g.getType()).getElements();
            }
            if (dim == 2) {
                dim2 = ((ArrayType) ((ArrayType) g.getType()).getElementType()).getElements();
            }

        } else if (g.getValue() instanceof ConstantDataArray) {
            for (ConstantInt c : ((ConstantDataArray) g.getValue()).getValues()) {
                initvalues.add(new MipsConst(c.getNum()));
            }
            dim = ((ConstantDataArray) g.getValue()).getDim();
            if (dim == 2) {
                dim2 = ((ConstantDataArray) ((ConstantDataArray) g.getValue()).getIndex(0)).size();
            }
            dim1 = ((ConstantDataArray) g.getValue()).size();
        } else if (g.getValue() instanceof ConstantInt) {
            initvalues.add(new MipsConst(((ConstantInt) g.getValue()).getNum()));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isZero ) {
            return name.substring(1) + " :.space " + size + "\n";
        }
        if (name != null) {
            sb.append(name.substring(1));
        }
        sb.append(": .word ");
        for (MipsConst c : initvalues) {
            sb.append(c.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        return sb.toString();
    }

    public int getDim() {
        return dim;
    }

    public int getDim1() {
        return dim1;
    }

    public int getDim2() {
        if (dim == 2) {
            return dim2;
        } else {
            return 1;
        }
    }
}
