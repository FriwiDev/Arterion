package me.friwi.arterion.plugin.combat.damage;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.formula.ArterionFormulaArray;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class WeaponDamageCalculator {
    private static final Random random = new Random();

    public static double calculateDamage(ItemStack stack, Entity target) {
        return calculateDamage(stack, target, false);
    }

    public static double calculateDamage(ItemStack stack, Entity target, boolean isCritical) {
        ArterionFormulaArray a = isCritical ? ArterionPlugin.getInstance().getFormulaManager().DMG_CRITICAL_WEAPON : ArterionPlugin.getInstance().getFormulaManager().DMG_WEAPON;
        if (stack == null)
            return a.get("other").evaluateDouble(random.nextDouble(), 0, 0, 0);
        boolean isundead = target instanceof Zombie || target instanceof WitherSkull || target instanceof Skeleton || target instanceof Wither
                || target instanceof PigZombie
                || (target instanceof Horse && (((Horse) target).getVariant() == Horse.Variant.UNDEAD_HORSE || ((Horse) target).getVariant() == Horse.Variant.SKELETON_HORSE));
        boolean isarthropod = target instanceof Spider /*includes cavespider*/ || target instanceof Endermite || target instanceof Silverfish;
        ArterionFormula f = a.get(stack.getType().toString());
        if (f == null || !f.isDeclared()) {
            f = a.get("other");
        }
        return f.evaluateDouble(random.nextDouble(),
                stack.getEnchantmentLevel(Enchantment.DAMAGE_ALL),
                isundead ? stack.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD) : 0,
                isarthropod ? stack.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) : 0);
    }

    public static double calculateBowDamage(ItemStack stack, double force) {
        if (stack == null) return 0;
        double r = random.nextDouble();
        return ArterionPlugin.getInstance().getFormulaManager().DMG_BOW.evaluateDouble(force, r, stack.getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
    }
}
