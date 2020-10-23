package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class MorgothPortal {
    public static void enable() {
        set(Material.PORTAL, (byte) 2);
    }

    public static void disable() {
        set(Material.IRON_FENCE, (byte) 0);
    }

    private static void set(Material mat, byte subid) {
        Block b = ArterionPlugin.getInstance().getArterionConfig().morgoth_portal.getBlock();
        for (int z = -2; z <= 2; z++) {
            for (int y = 0; y < 8; y++) {
                Block b2 = b.getRelative(0, y, z);
                if (b2.getType() == Material.IRON_FENCE || b2.getType() == Material.PORTAL) {
                    b2.setTypeIdAndData(mat.getId(), subid, false);
                }
            }
        }
    }
}
