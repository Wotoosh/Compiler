package llvm.value.user.instruction;

import frontend.node.LVal;
import llvm.IdName;
import llvm.MySymbolTable;
import llvm.type.PointerType;
import llvm.type.Type;
import llvm.value.Value;
import llvm.value.user.constant.globalobject.GlobalVariable;

import java.util.ArrayList;
import java.util.List;

public class ElePointerInstruction extends Instruction {

    private int dim;
    private String addrId;
    private String index0;
    private String index1;
    private String targetId;
    private String ultra;


    public String getRealUltra() {
        if (elementType instanceof PointerType) {
            return index0;
        } else {
            return ultra;
        }
    }

    public String getRealIndex0() {
        if (elementType instanceof PointerType) {
            return index1;
        } else {
            return index0;
        }
    }

    public String getRealIndex1() {
        if (elementType instanceof PointerType) {
            return "0";
        }
        return index1;
    }

    public ElePointerInstruction(GlobalVariable g) {
        super();
        dim = 1;
        addrId = g.getId();
        ultra = "0";
        index0 = "0";
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = g.getType();
    }

    public ElePointerInstruction(LVal lVal, MySymbolTable s, String i0, String i1) {
        super();
        dim = lVal.getDim();
        Value v = s.get(lVal.getIdent().getName());
        //AllocaInstruction alloca = (AllocaInstruction) v;
        addrId = v.getId();
        index0 = i0;
        index1 = i1;
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = v.getType();
        ultra = "0";
    }

    //
    public ElePointerInstruction(Type t, String name) {
        super();
        dim = 1;
        addrId = name;
        index0 = "0";
        index1 = "0";
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = t;
        ultra = "0";
    }

    public ElePointerInstruction(LVal lVal, String addr, MySymbolTable s, String i0) {
        super();
        dim = 1;
        Value v = s.get(lVal.getIdent().getName());
        //AllocaInstruction alloca = (AllocaInstruction) v;
        addrId = addr;
        index0 = i0;
        index1 = "0";
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = v.getType();
        ultra = "0";
    }

    public ElePointerInstruction(MySymbolTable s, LVal lVal, String addr, String i0, String i1) {
        super();
        dim = 2;
        Value v = s.get(lVal.getIdent().getName());
        // AllocaInstruction alloca = (AllocaInstruction) v;
        addrId = addr;
        index0 = i0;
        index1 = i1;
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = v.getType();
        ultra = "0";
    }

    public ElePointerInstruction(int d, String i0, String i1, String name, MySymbolTable s) {
        super();
        Value v = s.get(name);
        // AllocaInstruction alloca = (AllocaInstruction) v;
        addrId = v.getId();
        index0 = i0;
        index1 = i1;
        dim = d;
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = v.getType();
        ultra = "0";
    }

    public ElePointerInstruction(int d, String i0, String i1, Type t, String s) {
        super();
        addrId = s;
        index0 = i0;
        index1 = i1;
        dim = d;
        ultra = "0";
        targetId = "%" + IdName.tmpVar + allocID();
        this.elementType = t;

    }

    public ElePointerInstruction(int d, String i0, Type t, String s) {
        super();
        addrId = s;
        index0 = i0;
        index1 = "0";
        dim = d;
        targetId = "%" + IdName.tmpVar + allocID();
        ultra = "0";
        this.elementType = t;
    }

    public String toString() {
        if (dim == 1) {
            if (elementType instanceof PointerType) {
                return targetId + " = getelementptr " +
                        ((PointerType) elementType).getPointed() + " , " +
                        elementType + " " + addrId +
                        ", i32 " + index0 + "\n";
            }
            return targetId + " = getelementptr " + elementType + " , " +
                    elementType.getPointer() + " " + addrId +
                    ", i32 " + ultra + ", i32 " + index0 + "\n";
        } else if (dim == 2) {
            if (elementType instanceof PointerType) {
                return targetId + " = getelementptr " +
                        ((PointerType) elementType).getPointed() + " , " +
                        elementType + " " + addrId +
                        ", i32 " + index0 + ", i32 " + index1 + "\n";
            }
            return targetId + " = getelementptr " + elementType + " , " +
                    elementType.getPointer() + " " + addrId +
                    ", i32 " + ultra + ", i32 " + index0 + ", i32 " + index1 + "\n";
        } else {
            return targetId + " = getelementptr " + ((PointerType) elementType).getPointed() + " , " +
                    elementType + " " + addrId + ", i32 " + index0 + "\n";
        }
    }

    public int getDim() {
        return dim;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getAddrId() {
        return addrId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ElePointerInstruction that = (ElePointerInstruction) o;

        if (dim != that.dim) return false;
        if (addrId != null ? !addrId.equals(that.addrId) : that.addrId != null) return false;
        if (index0 != null ? !index0.equals(that.index0) : that.index0 != null) return false;
        if (index1 != null ? !index1.equals(that.index1) : that.index1 != null) return false;
        if (targetId != null ? !targetId.equals(that.targetId) : that.targetId != null) return false;
        return ultra != null ? ultra.equals(that.ultra) : that.ultra == null;
    }

    @Override
    public int hashCode() {
        int result = dim;
        result = 31 * result + (addrId != null ? addrId.hashCode() : 0);
        result = 31 * result + (index0 != null ? index0.hashCode() : 0);
        result = 31 * result + (index1 != null ? index1.hashCode() : 0);
        result = 31 * result + (ultra != null ? ultra.hashCode() : 0);
        result = 31 * result + elementType.hashCode();
        return result;
    }

    @Override
    public void replaceTar(String s) {
        for (Instruction i : users) {
            i.replaceUse(targetId, s);
        }
        targetId = s;
    }

    @Override
    public void replaceUse(String old, String s) {
        if (addrId.equals(old)) {
            addrId = s;
        }
        if (index0.equals(old)) {
            index0 = s;
        }
        if (index1 != null && index1.equals(old)) {
            index1 = s;
        }
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        using.add(addrId);
        if (index0.charAt(0) == '%' || index0.charAt(0) == '@') {
            using.add(index0);
        }
        if (index1 != null && (index1.charAt(0) == '%' || index1.charAt(0) == '@')) {
            using.add(index1);
        }
        return using;
    }

    @Override
    public String getDef() {
        return targetId;
    }
}
