package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {
    private ArterionPlugin plugin;

    public ChunkLoadListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent evt) {
        //Regions
        plugin.getRegionManager().onChunkLoad(evt.getChunk());
    }
}
