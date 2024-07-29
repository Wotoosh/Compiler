package llvm.value.user.instruction;

import llvm.MySymbolTable;
import llvm.type.Type;
import llvm.value.Value;

import java.util.ArrayList;
import java.util.List;

public class StoreInstruction extends Instruction {
    private String addrId;
    private String varId;

    @Override
    public String getDef() {
        return null;
    }

    @Override
    public List<String> getUsing() {
        List<String> using = new ArrayList<>();
        using.add(addrId);
        using.add(varId);
        return using;
    }

    public StoreInstruction(String name, MySymbolTable s, Type t, String id_now) {
        super();
        elementType = t;
        this.name = name;
        Value value = s.get(name);
        addrId = (value).getId();
        varId = id_now;
    }

    public String getAddrId() {
        return addrId;
    }

    public String getVarId() {
        return varId;
    }

    public StoreInstruction(String name, String addr, MySymbolTable s, Type t, String id_now) {
        super();
        elementType = t;
        this.name = name;
        Value value = s.get(name);
        addrId = addr;
        varId = id_now;
    }

    public StoreInstruction(String name, String addr, Type t, String id_now) {
        super();
        elementType = t;
        this.name = name;
        addrId = addr;
        varId = id_now;
    }

    @Override
    public String toString() {
        return "store " + elementType + " " + varId + ", " + elementType + "* " + addrId + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoreInstruction that = (StoreInstruction) o;

        if (addrId != null ? !addrId.equals(that.addrId) : that.addrId != null) return false;
        return varId != null ? varId.equals(that.varId) : that.varId == null;
    }

    @Override
    public int hashCode() {
        int result = addrId != null ? addrId.hashCode() : 0;
        result = 31 * result + (varId != null ? varId.hashCode() : 0);
        return result;
    }

    @Override
    public void replaceUse(String old, String s) {
        if (addrId.equals(old)) {
            addrId = s;
        }
        if (varId.equals(old)) {
            varId = s;
        }
    }

    public boolean isLocal() {
        ElePointerInstruction i = (ElePointerInstruction) users.get(0);
        return false;
    }
}
