package me.friwi.arterion.plugin.world.block.nonbtblocks;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.block.Block;

public class GuildBlockUtil {
    public static boolean canBuildHere(ArterionPlayer ap, Block b) {
        for (SpecialBlock sb : ArterionPlugin.getInstance().getSpecialBlockManager().getAll()) {
            if (sb instanceof GuildBlock) {
                int x_z = ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FREE_SPACE_X_Z.evaluateInt(((GuildBlock) sb).getOwner());
                int y = ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FREE_SPACE_Y.evaluateInt(((GuildBlock) sb).getOwner());
                if (Math.abs(b.getX() - sb.getLocation().getBlockX()) <= x_z && Math.abs(b.getZ() - sb.getLocation().getBlockZ()) <= x_z && (b.getY() - sb.getLocation().getBlockY() >= 0 && b.getY() - sb.getLocation().getBlockY() <= y)) {
                    ap.sendTranslation("guildblock.nospace", x_z, y);
                    return false;
                }

            }
        }
        return true;
    }
}
