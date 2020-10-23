package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class WorldInitListener implements Listener {
    private ArterionPlugin plugin;

    public WorldInitListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent evt) {
        //Disable spawn loading on temporary worlds
        if (ArterionPlugin.getInstance().getTemporaryWorldManager().getWorld(evt.getWorld().getName()) != null) {
            evt.getWorld().setKeepSpawnInMemory(false);
            evt.getWorld().setAutoSave(false);
        }
    }
}
