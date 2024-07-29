package frontend.node;

public class MainFuncDef implements Node {
    Block block;

    public MainFuncDef(Block b) {
        block = b;
    }

    public Block getBlock() {
        return block;
    }
}
