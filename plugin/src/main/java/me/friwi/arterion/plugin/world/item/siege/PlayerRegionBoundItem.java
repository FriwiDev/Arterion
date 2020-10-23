package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import me.friwi.arterion.plugin.world.region.PlayerClaimRegion;
import org.bukkit.block.Block;

public interface PlayerRegionBoundItem {
    default boolean checkUseOn(ArterionPlayer ap, Block b) {
        ArterionChunk chunk = ArterionChunkUtil.getNonNull(b.getChunk());
        boolean ret = chunk.getRegion() instanceof PlayerClaimRegion || chunk.getRegion() instanceof GuildRegion;
        if (!ret) {
            ap.sendTranslation("siege.wrongregion");
        }
        if (chunk.getRegion() instanceof GuildRegion) {
            if (((GuildRegion) chunk.getRegion()).getGuild().getLocalFight() != null) {
                if (ap.getGuild().equals(((GuildRegion) chunk.getRegion()).getGuild())) {
                    ap.sendTranslation("siege.notattacking");
                    return false;
                }
            }
        }
        return ret;
    }
}
