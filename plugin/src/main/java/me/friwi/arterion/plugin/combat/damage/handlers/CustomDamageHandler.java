package me.friwi.arterion.plugin.combat.damage.handlers;

import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Handler for plugin damage - simple pass
 */
public class CustomDamageHandler implements DamageHandler {

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        DamageManager.applyDamage(evt, evt.getDamage(), true);
    }
}
