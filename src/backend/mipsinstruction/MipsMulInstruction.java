package backend.mipsinstruction;

public class MipsMulInstruction extends MipsInstruction {
    private String left;
    private String right;
    private String target;
    private int num;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MipsMulInstruction that = (MipsMulInstruction) o;

        if (num != that.num) return false;
        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;
        return target != null ? target.equals(that.target) : that.target == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + num;
        return result;
    }

    public void replace(String old, String n) {
        if (left != null && left.equals(old)) {
            left = n;
        }
        if (right != null && right.equals(old)) {
            right = n;
        }
        if (target != null && target.equals(old)) {
            target = n;
        }
    }

    public MipsMulInstruction(String l, String r, String t) {
        left = l;
        right = r;
        target = t;
    }

    public MipsMulInstruction(String l, String t, int n) {
        left = l;
        right = null;
        target = t;
        num = n;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getTarget() {
        return target;
    }

    public String getRight() {
        return right;
    }

    public String getLeft() {
        return left;
    }

    public int getNum() {
        return num;
    }

    @Override
    public void changeName() {
        if (left != null && left.charAt(0) == 'r') {
            left = left + "_" + counter;
        }
        if (right != null && right.charAt(0) == 'r') {
            right = right + "_" + counter;
        }
        if (target != null && target.charAt(0) == 'r') {
            target = target + "_" + counter;
        }
    }

    @Override
    public String toString() {
        if (right == null) {
            if (num == 5) {
                return "sll $at, " + left + ", 2" + "\nadd " + target + ", $at, " + left + "\n";
            } else if (num == 3) {
                return "sll $at, " + left + ", 1" + "\nadd " + target + ", $at, " + left + "\n";
            }
            int base = 1;
            for (int i = 0; i < 32; i++) {
                if (base == num) {
                    return "sll " + target + ", " + left + ", " + i + "\n";
                }
                base *= 2;
            }
            return "mul " + target + ", " + left + ", " + num + "\n";
        } else {
            return "mul " + target + ", " + left + ", " + right + "\n";
        }
    }
}
