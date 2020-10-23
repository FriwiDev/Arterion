package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class InventoryPickupItemListener implements Listener {
    private ArterionPlugin plugin;

    public InventoryPickupItemListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent evt) {
        //Custom items
        if (evt.getItem().getItemStack() != null) {
            CustomItem item = CustomItemUtil.getCustomItem(evt.getItem().getItemStack());
            if (!item.onInventoryPickup(evt.getInventory())) {
                evt.setCancelled(true);
                return;
            }
        }
    }
}
