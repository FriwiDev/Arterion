package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItemListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerPickupItemListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent evt) {
        CustomItem item = CustomItemUtil.getCustomItem(evt.getItem().getItemStack());
        if (!item.onPickup(ArterionPlayerUtil.get(evt.getPlayer()), evt.getItem())) {
            evt.setCancelled(true);
        }
    }
}
