package me.friwi.arterion.plugin.world.banneditem;

import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class BannedItems {
    private static List<Material> bannedMaterials = ImmutableList.of(Material.GOLDEN_APPLE);
    private static List<PotionEffectType> bannedPotions = ImmutableList.of(
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.HEAL,
            PotionEffectType.HARM,
            PotionEffectType.CONFUSION,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.ABSORPTION
    );

    public static boolean isBanned(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getType() == Material.POTION) {
            Potion potion = Potion.fromItemStack(stack);
            if (potion == null || potion.getType() == null || potion.getType().getEffectType() == null) return false;
            return bannedPotions.contains(potion.getType().getEffectType());
        }
        return bannedMaterials.contains(stack.getType());
    }
}
