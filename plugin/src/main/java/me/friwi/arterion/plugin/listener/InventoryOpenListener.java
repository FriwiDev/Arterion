package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.LockUtil;
import org.bukkit.GameMode;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryOpenListener implements Listener {
    private ArterionPlugin plugin;

    public InventoryOpenListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent evt) {
        if (evt.getPlayer() instanceof Player) {
            if (evt.getInventory().getHolder() instanceof DoubleChest || evt.getInventory().getHolder() instanceof Chest) {
                Chest bs;
                if (evt.getInventory().getHolder() instanceof Chest) {
                    bs = (Chest) evt.getInventory().getHolder();
                } else {
                    bs = (Chest) ((DoubleChest) evt.getInventory().getHolder()).getLeftSide();
                }
                Lock l = LockUtil.getLock(bs);
                if (l != null) {
                    ArterionPlayer p = ArterionPlayerUtil.get((Player) evt.getPlayer());
                    if (!l.canAccess(p) && p.getBukkitPlayer().getGameMode() != GameMode.CREATIVE && p.getBukkitPlayer().getGameMode() != GameMode.SPECTATOR) {
                        l.sendDeny(p);
                        evt.setCancelled(true);
                        return;
                    } else {
                        l.sendAllow(p);
                    }
                }
            }
        }
    }
}
