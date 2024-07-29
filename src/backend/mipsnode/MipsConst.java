package backend.mipsnode;

public class MipsConst extends MipsData {
    private int value;

    public int getValue() {
        return value;
    }

    public MipsConst(int i) {
        value = i;
        size=4;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
