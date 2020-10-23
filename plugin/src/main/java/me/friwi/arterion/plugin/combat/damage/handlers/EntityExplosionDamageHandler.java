package me.friwi.arterion.plugin.combat.damage.handlers;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.hook.Tuple;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityExplosionDamageHandler implements DamageHandler {

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        double distance = damaged.getLocation().clone().add(0, 1, 0).distance(damager.getLocation());
        double dmg = Math.max(0, ArterionPlugin.getInstance().getFormulaManager().DMG_EXPLOSION.evaluateDouble(distance));
        dmg = Hooks.ENTITY_EXPLOSION_DAMAGE_DEAL_HOOK.execute(damager, new Tuple<>(damaged, dmg)).getSecondValue();
        if (dmg <= 0) {
            evt.setCancelled(true);
            return;
        }
        DamageManager.applyDamage(evt, dmg, false);
    }
}
