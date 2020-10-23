package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

import java.security.SecureRandom;
import java.util.Random;

public class EntityPortalListener implements Listener {
    private final Random RANDOM = new SecureRandom();
    private ArterionPlugin plugin;

    public EntityPortalListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent evt) {
        //Handle combat logging villagers
        if (plugin.getCombatLoggingHandler().isCombatLoggingVillager(evt.getEntity())) {
            evt.setCancelled(true);
            return;
        }

        //Handle projectiles
        if (evt.getEntity() instanceof Projectile) {
            evt.setCancelled(true);
            return;
        }
    }
}
