package me.friwi.arterion.plugin.combat.damage;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public interface DamageHandler {
    void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt);
}
