package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.banneditem.BannedItems;
import me.friwi.recordable.FinishBrewEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public class BrewListener implements Listener {
    private ArterionPlugin plugin;

    public BrewListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBrew(FinishBrewEvent evt) {
        final BrewerInventory inv = evt.getContents();
        for (int i = 0; i < 3; i++) {
            final ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (BannedItems.isBanned(item)) {
                inv.setItem(i, new ItemStack(Material.POTION, item.getAmount()));
            }
        }
    }
}
