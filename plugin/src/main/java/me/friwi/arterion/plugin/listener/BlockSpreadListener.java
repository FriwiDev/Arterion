package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockSpreadListener implements Listener {

    private ArterionPlugin plugin;

    public BlockSpreadListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent evt) {
        if (evt.getNewState().getType() == Material.GRASS) {
            Region region = ArterionChunkUtil.getNonNull(evt.getBlock().getChunk()).getRegion();
            if (region.isStopDecay()) {
                evt.setCancelled(true);
                return;
            }
        }
    }
}
