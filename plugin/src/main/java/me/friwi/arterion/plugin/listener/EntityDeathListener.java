package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {
    private ArterionPlugin plugin;

    public EntityDeathListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent evt) {
        //Dont run this code for players!
        if (evt.getEntity() instanceof Player) return;
        //Handle projectile despawning
        if (evt.getEntity() instanceof Projectile) {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    Hooks.PROJECTILE_HIT_TARGET_EVENT_HOOK.unsubscribeAll((Projectile) evt.getEntity());
                }
            }, 1);
        }
        //Handle combat logging villagers
        if (plugin.getCombatLoggingHandler().onEntityDeath(evt.getEntity())) return;
        //Drop gold/xp and custom drops
        if (evt.getEntity() instanceof LivingEntity) {
            plugin.getDamageManager().handleDeath(evt);
        }
    }
}
