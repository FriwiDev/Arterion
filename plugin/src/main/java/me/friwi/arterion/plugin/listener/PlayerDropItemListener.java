package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerDropItemListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent evt) {
        CustomItem item = CustomItemUtil.getCustomItem(evt.getItemDrop().getItemStack());
        if (!item.onDrop(ArterionPlayerUtil.get(evt.getPlayer()), evt.getItemDrop())) {
            evt.setCancelled(true);
        }
    }
}
