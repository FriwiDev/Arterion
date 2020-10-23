package me.friwi.arterion.plugin.world.block;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import org.bukkit.block.Block;

public class NBTBlockUtil {
    public static CustomBlockType getType(Block block) {
        NBTCompound comp = NBTInjector.getNbtData(block.getState());
        if (comp != null && comp.hasKey("E_type")) {
            try {
                return CustomBlockType.values()[comp.getShort("E_type")];
            } catch (ArrayIndexOutOfBoundsException e) {
                return CustomBlockType.NONE;
            }
        }
        return CustomBlockType.NONE;
    }

    public static void setType(Block block, CustomBlockType customBlockType) {
        NBTCompound comp = NBTInjector.getNbtData(block.getState());
        comp.setShort("E_type", (short) customBlockType.ordinal());
    }
}
