package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeBlockListener implements Listener {
    private ArterionPlugin plugin;

    public EntityChangeBlockListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent evt) {
        if (evt.getEntityType() == EntityType.WITHER) {
            evt.setCancelled(true);
        } else if (evt.getEntityType() == EntityType.FALLING_BLOCK) {
            if (this.plugin.getExplosionHandler().handleFallingBlockImpact((FallingBlock) evt.getEntity())) {
                evt.setCancelled(true);
            } else {
                Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.execute((FallingBlock) evt.getEntity(), evt);
            }
        }
        if (evt.getEntityType() == EntityType.ENDERMAN) {
            evt.setCancelled(true);
        }
    }
}
