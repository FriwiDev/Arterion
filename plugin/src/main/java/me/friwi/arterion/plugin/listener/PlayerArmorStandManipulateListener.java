package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class PlayerArmorStandManipulateListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerArmorStandManipulateListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent evt) {
        //Prevent playing of items on holograms
        if (!evt.getRightClicked().isVisible()) evt.setCancelled(true);
    }
}
