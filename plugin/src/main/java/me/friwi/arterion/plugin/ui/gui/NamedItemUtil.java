package me.friwi.arterion.plugin.ui.gui;

import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class NamedItemUtil {
    public static ItemStack create(Material type, String name) {
        return create(type, 1, (byte) 0, name, (List) null);
    }

    public static ItemStack create(Material type, int amount, String name) {
        return create(type, amount, (byte) 0, name, (List) null);
    }

    public static ItemStack create(Material type, String name, List<String> lore) {
        return create(type, 1, (byte) 0, name, lore);
    }

    public static ItemStack create(Material type, String name, String... lore) {
        return create(type, 1, name, lore);
    }

    public static ItemStack create(Material type, int amount, String name, String... lore) {
        return create(type, amount, (byte) 0, name, lore);
    }

    public static ItemStack create(Material type, int amount, byte data, String name, String... lore) {
        return create(type, amount, data, name, ImmutableList.copyOf(lore));
    }

    public static ItemStack create(Material type, int amount, byte data, String name, List<String> lore) {
        ItemStack ret = new ItemStack(type, amount, data);
        ItemMeta meta = ret.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        ret.setItemMeta(meta);
        return ret;
    }

    public static ItemStack modify(ItemStack stack, String name, String... lore) {
        return modify(stack, name, ImmutableList.copyOf(lore));
    }

    public static ItemStack modify(ItemStack stack, String name, List<String> lore) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }
}
