package backend.mipsinstruction;

public class MipsSubInstruction extends MipsInstruction {
    private String left;
    private String right;
    private String target;
    private int num;

    @Override
    public void changeName() {
        if (left != null && left.charAt(0) == 'r') {
            left = left + "_"+counter;
        }
        if (right != null && right.charAt(0) == 'r') {
            right = right + "_"+counter;
        }
        if (target != null && target.charAt(0) == 'r') {
            target = target + "_"+counter;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MipsSubInstruction that = (MipsSubInstruction) o;

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

    public void setTarget(String target) {
        this.target = target;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public int getNum() {
        return num;
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

    public void setNum(int num) {
        this.num = num;
    }

    public MipsSubInstruction(String l, String t, int i) {
        left = l;
        right = null;
        num = i;
        target = t;
    }

    public MipsSubInstruction(String l, String r, String t) {
        left = l;
        right = r;
        target = t;
    }


    @Override
    public String toString() {
        if (right != null) {
            return "subu " + target + ", " + left + ", " + right + "\n";
        } else {
            return "subiu " + target + ", " + left + ", " + num + "\n";
        }
    }
}
