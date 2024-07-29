package backend.mipsinstruction;

public class SltInstruction extends MipsInstruction {
    private String left;
    private String right;
    private String target;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SltInstruction that = (SltInstruction) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;
        return target != null ? target.equals(that.target) : that.target == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public String getTarget() {
        return target;
    }

    public SltInstruction(String l, String r, String t) {
        left = l;
        right = r;
        target = t;
    }

    @Override
    public String toString() {
        return "slt " + target + ", " + left + ", " + right + "\n";
    }
}
