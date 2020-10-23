package me.friwi.arterion.plugin.combat.damage.handlers;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class FireTickDamageHandler implements DamageHandler {

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        DamageManager.applyDamage(evt, ArterionPlugin.getInstance().getFormulaManager().DMG_FIRE_TICK.evaluateDouble(), false);
    }
}
