package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.banneditem.BannedItems;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class PrepareItemCraftListener implements Listener {
    private ArterionPlugin plugin;

    public PrepareItemCraftListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent evt) {
        final ItemStack item = evt.getInventory().getResult();
        if (item == null || item.getType() == Material.AIR) return;
        if (BannedItems.isBanned(item)) {
            evt.getInventory().setResult(null);
        }
    }
}
