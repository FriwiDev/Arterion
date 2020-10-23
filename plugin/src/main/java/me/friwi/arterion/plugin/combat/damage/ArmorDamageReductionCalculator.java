package me.friwi.arterion.plugin.combat.damage;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ArmorDamageReductionCalculator {
    private static final Random random = new Random();

    public static double calculateDamageReduction(ItemStack stack, EntityDamageEvent.DamageCause cause) {
        if (cause == EntityDamageEvent.DamageCause.STARVATION || cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.SUICIDE)
            return 0;
        if (stack == null) return 0;
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().DMG_ARMOR.get(stack.getType().toString());
        if (f == null || !f.isDeclared()) {
            f = ArterionPlugin.getInstance().getFormulaManager().DMG_ARMOR.get("other");
        }

        boolean isFire = cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.LAVA;
        boolean isExplosion = cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;

        int protection = stack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        int fire = isFire ? stack.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) : 0;
        int fall = cause == EntityDamageEvent.DamageCause.FALL ? stack.getEnchantmentLevel(Enchantment.PROTECTION_FALL) : 0;
        int explosions = isExplosion ? stack.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) : 0;
        int projectile = cause == EntityDamageEvent.DamageCause.PROJECTILE ? stack.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) : 0;

        if (cause == EntityDamageEvent.DamageCause.FALL) {
            //Return only fall damage enchantment factor (protection with fall minus protection without fall)
            double rand = random.nextDouble();
            double high = f.evaluateDouble(rand, 0, 0, fall, 0, 0);
            double low = f.evaluateDouble(rand, 0, 0, 0, 0, 0);
            return high - low;
        }

        return f.evaluateDouble(random.nextDouble(), protection, fire, fall, explosions, projectile);
    }

    public static double calculateDamageReduction(HumanEntity human, EntityDamageEvent.DamageCause cause) {
        double sum = 0;
        for (ItemStack stack : human.getInventory().getArmorContents()) sum += calculateDamageReduction(stack, cause);
        if (human instanceof Player) {
            //Apply prestige point
            sum += ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_DEFENSE.evaluateDouble(ArterionPlayerUtil.get((Player) human).getPointsDefense()) * 100;
        }
        //Notify listeners about armor calculation
        return Hooks.ARMOR_RESISTANCE_HOOK.execute(human, sum);
    }
}
