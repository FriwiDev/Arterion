package me.friwi.arterion.plugin.combat.damage;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PotionDamageBoostCalculator {
    private static final Random random = new Random();

    public static double calculateDamageBoost(LivingEntity livingEntity) {
        int weakness_level = 0;
        int strength_level = 0;
        for (PotionEffect effect : livingEntity.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.WEAKNESS))
                weakness_level = Math.max(weakness_level, effect.getAmplifier() + 1);
            if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE))
                strength_level = Math.max(strength_level, effect.getAmplifier() + 1);
        }
        return ArterionPlugin.getInstance().getFormulaManager().DMG_BOOST.evaluateDouble(random.nextDouble(), strength_level, weakness_level);
    }
}
