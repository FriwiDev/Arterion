package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.banneditem.BannedItems;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

public class FurnaceSmeltListener implements Listener {
    private ArterionPlugin plugin;

    public FurnaceSmeltListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent evt) {
        final ItemStack item = evt.getResult();
        if (item == null || item.getType() == Material.AIR) return;
        if (BannedItems.isBanned(item)) {
            evt.setCancelled(true);
        }
    }
}
