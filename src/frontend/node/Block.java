package frontend.node;

import java.util.ArrayList;

public class Block implements Node {
    private ArrayList<BlockItem> blockItems;

    public Block(ArrayList<BlockItem> b) {
        blockItems = b;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public BlockItem getLastBlockItem() {
        if (blockItems.isEmpty())
            return null;
        return blockItems.get(blockItems.size() - 1);
    }
}
