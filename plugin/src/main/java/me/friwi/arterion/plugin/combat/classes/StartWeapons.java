package me.friwi.arterion.plugin.combat.classes;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class StartWeapons {
    public static void giveStartWeapons(ArterionPlayer p, ClassEnum clasz) {
        PlayerInventory i = p.getBukkitPlayer().getInventory();
        switch (clasz) {
            case PALADIN:
                i.addItem(new ItemStack(Material.IRON_SPADE));
                break;
            case BARBAR:
                i.addItem(new ItemStack(Material.IRON_AXE));
                break;
            case FORESTRUNNER:
                i.addItem(new ItemStack(Material.BOW));
                i.addItem(new ItemStack(Material.ARROW, 64));
                i.addItem(new ItemStack(Material.ARROW, 64));
                i.addItem(new ItemStack(Material.ARROW, 64));
                break;
            case SHADOWRUNNER:
                i.addItem(new ItemStack(Material.IRON_SWORD));
                break;
            case MAGE:
                i.addItem(new ItemStack(Material.IRON_HOE));
                break;
            case CLERIC:
                i.addItem(new ItemStack(Material.STICK));
                break;
        }
        i.addItem(new ItemStack(Material.BAKED_POTATO, 64));
        i.addItem(new ItemStack(Material.BED));
        if (i.getHelmet() == null) i.setHelmet(setColor(new ItemStack(Material.LEATHER_HELMET), Color.GRAY));
        else i.addItem(setColor(new ItemStack(Material.LEATHER_HELMET), Color.GRAY));
        if (i.getChestplate() == null)
            i.setChestplate(setColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.GRAY));
        else i.addItem(setColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.GRAY));
        if (i.getLeggings() == null) i.setLeggings(setColor(new ItemStack(Material.LEATHER_LEGGINGS), Color.GRAY));
        else i.addItem(setColor(new ItemStack(Material.LEATHER_LEGGINGS), Color.GRAY));
        if (i.getBoots() == null) i.setBoots(setColor(new ItemStack(Material.LEATHER_BOOTS), Color.GRAY));
        else i.addItem(setColor(new ItemStack(Material.LEATHER_BOOTS), Color.GRAY));
        p.sendTranslation("skills.startkit");
    }

    public static void giveAdditionalStartWeapons(ArterionPlayer p, ClassEnum clasz) {
        if (clasz == ClassEnum.FORESTRUNNER) {
            PlayerInventory i = p.getBukkitPlayer().getInventory();
            i.addItem(new ItemStack(Material.ARROW, 64));
        }
    }

    private static ItemStack setColor(ItemStack stack, Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(color);
        stack.setItemMeta(meta);
        return stack;
    }
}
