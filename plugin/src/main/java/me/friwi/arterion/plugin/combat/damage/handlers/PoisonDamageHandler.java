package me.friwi.arterion.plugin.combat.damage.handlers;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public class PoisonDamageHandler implements DamageHandler {

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        DamageManager.applyDamage(evt, ArterionPlugin.getInstance().getFormulaManager().DMG_POISON.evaluateDouble(), false);
        //Don't kill an entity with poison!
        if (evt.getEntity() instanceof LivingEntity) {
            if (((LivingEntity) evt.getEntity()).getHealth() <= evt.getDamage()) evt.setCancelled(true);
        }
    }
}
