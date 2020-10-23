package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerItemConsumeListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerItemConsumeListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent evt) {
        if (evt.getItem().getType() == Material.MILK_BUCKET) {
            ArterionPlayerUtil.get(evt.getPlayer()).getPotionTracker().clearEffects();
        }
    }
}
