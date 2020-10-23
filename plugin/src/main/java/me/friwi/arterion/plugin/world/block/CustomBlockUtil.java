package me.friwi.arterion.plugin.world.block;

import org.bukkit.block.Block;

public class CustomBlockUtil {
    private static final CustomBlock DEFAULT_BLOCK = new DefaultBlock();

    public static CustomBlock getCustomBlock(Block block) {
        CustomBlockType type = NBTBlockUtil.getType(block);
        switch (type) {
            case NONE:
                return DEFAULT_BLOCK;
            default:
                return DEFAULT_BLOCK;
        }
    }
}
