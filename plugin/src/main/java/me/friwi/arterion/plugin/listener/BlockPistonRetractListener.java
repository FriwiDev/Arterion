package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class BlockPistonRetractListener implements Listener {
    private ArterionPlugin plugin;

    public BlockPistonRetractListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRetract(BlockPistonRetractEvent evt) {
        for (Block b : evt.getBlocks()) {
            ArterionChunk sourceChunk = ArterionChunkUtil.getNonNull(b.getChunk());
            ArterionChunk targetChunk = ArterionChunkUtil.getNonNull(b.getRelative(evt.getDirection()).getChunk());
            if (!sourceChunk.getRegion().equals(targetChunk.getRegion())) {
                evt.setCancelled(true);
                return;
            }
        }
    }
}
