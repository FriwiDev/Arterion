package me.friwi.arterion.plugin.combat.damage.handlers;

import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * This damage handler just deals the normal environmental damage to all entities
 * It acts as a proxy to inflict normal minecraft damage
 */
public class DefaultDamageHandler implements DamageHandler {

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        DamageManager.applyDefaultDamage(evt);
    }
}
