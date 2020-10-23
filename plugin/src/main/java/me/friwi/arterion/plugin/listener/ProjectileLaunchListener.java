package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.hotbar.InstantHotbarMessageCard;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class ProjectileLaunchListener implements Listener {
    private ArterionPlugin plugin;

    public ProjectileLaunchListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLaunchProjectile(ProjectileLaunchEvent evt) {
        //Enderpearl mana cost
        if (evt.getEntity() instanceof EnderPearl && evt.getEntity().getShooter() instanceof Player) {
            ArterionPlayer launcher = ArterionPlayerUtil.get((Player) evt.getEntity().getShooter());
            if (ArterionPlugin.getInstance().getTemporaryWorldManager().isTemporaryWorld(evt.getEntity().getWorld())) {
                evt.setCancelled(true);
                launcher.scheduleHotbarCard(new InstantHotbarMessageCard(1500, launcher, "enderpearl.noextworld"));
                return;
            }
            if (launcher.getMana() < launcher.getMaxMana()) {
                launcher.scheduleHotbarCard(new InstantHotbarMessageCard(1500, launcher, "enderpearl.needsfullmana"));
                evt.setCancelled(true);
                //Give back enderpearl
                ItemStack stack = launcher.getBukkitPlayer().getItemInHand();
                if (stack == null || stack.getType() == Material.AIR) {
                    stack = new ItemStack(Material.ENDER_PEARL, 1);
                } else if (stack.getType() == Material.ENDER_PEARL) {
                    stack.setAmount(stack.getAmount() + 1);
                }
                launcher.getBukkitPlayer().setItemInHand(stack);
                launcher.getBukkitPlayer().updateInventory();
            } else {
                launcher.useMana(launcher.getMaxMana());
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        evt.getEntity().setVelocity(evt.getEntity().getVelocity().multiply(0.5));
                    }
                });
            }
        }
    }
}
