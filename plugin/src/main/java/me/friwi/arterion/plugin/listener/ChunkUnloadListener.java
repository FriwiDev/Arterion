package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.TemporaryWorldManager;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadListener implements Listener {
    private ArterionPlugin plugin;

    public ChunkUnloadListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent evt) {
        //Temporary worlds
        TemporaryWorldManager.ChunkPreferenceEnum pref = ArterionPlugin.getInstance().getTemporaryWorldManager().isWantedChunk(evt.getChunk());
        if (pref == TemporaryWorldManager.ChunkPreferenceEnum.WANTED) {
            evt.setCancelled(true);
            return;
        }
        //Temporary blocks
        ArterionChunk chunk = ArterionChunkUtil.getNonNull(evt.getChunk());
        if (chunk.hasTemporaryBlocks()) evt.setCancelled(true);
    }
}
