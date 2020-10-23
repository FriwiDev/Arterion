package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
    private ArterionPlugin plugin;

    public InventoryCloseListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getPlayer() instanceof Player) {
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) evt.getPlayer());
            if (ep.getOpenGui() != null && ep.getOpenGui() instanceof ItemGUI) {
                ep.getOpenGui().onClose();
            }
        }
        //PvP Chests
        if (evt.getInventory().getHolder() instanceof Chest) {
            plugin.getPvpChestManager().onChestClose(((Chest) evt.getInventory().getHolder()).getBlock());
        }
    }
}
