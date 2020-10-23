package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {
    private ArterionPlugin plugin;

    public BlockFromToListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent evt) {
        Block b = evt.getToBlock();

        ArterionChunk ec = ArterionChunkUtil.getNonNull(evt.getBlock().getChunk());
        ArterionChunk toChunk = ArterionChunkUtil.getNonNull(evt.getToBlock().getChunk());

        if (!ec.getRegion().equals(toChunk.getRegion())) {
            evt.setCancelled(true);
            return;
        }

        if (ec.isTemporaryBlock(b)) {
            evt.setCancelled(true);
            return;
        }

        if (Hooks.BLOCK_FLOW_TO_HOOK.execute(b.getLocation(), evt) == null) {
            return;
        }
    }
}
