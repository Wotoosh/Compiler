package llvm.value.user;

import llvm.value.Value;
import llvm.value.user.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;

public class User extends Value {
    protected List<Instruction> users;  //谁用了该指令的Target


    public User() {
        users = new ArrayList<>();
    }

    public List<Instruction> getUsers() {
        return users;
    }

    public String getDef() {
        return null;
    }

    public List<String> getUsing() {
        return new ArrayList<>();
    }

    public void clear() {
        users = new ArrayList<>();
    }

    public void addUser(Instruction i) {
        users.add(i);
    }

    public void inheritUser(Instruction i) {
        users.addAll(i.getUsers());
    }

    public void deleteUser(Instruction i) {
        users.remove(i);
    }

    public boolean hasUser() {
        return !users.isEmpty();
    }

    public void replaceTar(String s) {
        for (Instruction i : users) {
            i.replaceUse(getDef(), s);
        }
    }
}
