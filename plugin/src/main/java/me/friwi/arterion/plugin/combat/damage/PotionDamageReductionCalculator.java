package me.friwi.arterion.plugin.combat.damage;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PotionDamageReductionCalculator {
    private static final Random random = new Random();

    public static double calculateDamageReduction(LivingEntity livingEntity, EntityDamageEvent.DamageCause cause) {
        if (cause == EntityDamageEvent.DamageCause.STARVATION || cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.SUICIDE)
            return 0;
        int level = 0;
        for (PotionEffect effect : livingEntity.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE))
                level = Math.max(level, effect.getAmplifier() + 1);
        }
        return ArterionPlugin.getInstance().getFormulaManager().DMG_RESISTANCE.evaluateDouble(random.nextDouble(), level);
    }
}
