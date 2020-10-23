package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockFadeListener implements Listener {

    private ArterionPlugin plugin;

    public BlockFadeListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent evt) {
        Region region = ArterionChunkUtil.getNonNull(evt.getBlock().getChunk()).getRegion();
        if (region.isStopDecay()) {
            evt.setCancelled(true);
            return;
        }
    }
}
