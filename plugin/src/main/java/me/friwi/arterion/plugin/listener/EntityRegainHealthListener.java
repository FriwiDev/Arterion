package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class EntityRegainHealthListener implements Listener {
    private ArterionPlugin plugin;

    public EntityRegainHealthListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent evt) {
        //Update health info
        if (evt.getEntity() instanceof Player) {
            ArterionPlayer ap = ArterionPlayerUtil.get((Player) evt.getEntity());

            if (ap != null) {
                //Do not boost regeneration/heal potion and natural regeneration
                if (evt.getRegainReason() != EntityRegainHealthEvent.RegainReason.MAGIC_REGEN && evt.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED && evt.getRegainReason() != EntityRegainHealthEvent.RegainReason.MAGIC) {
                    evt.setAmount(evt.getAmount() / 20d * ((Player) evt.getEntity()).getMaxHealth() / ap.getMaxHealth() * ap.getRegenHealth());
                }
                if (evt.getRegainReason() != EntityRegainHealthEvent.RegainReason.MAGIC_REGEN) {
                    evt.setAmount(evt.getAmount() / 3);
                }
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        ap.getPlayerScoreboard().updateHealth();
                    }
                });
            }
        }
    }
}
