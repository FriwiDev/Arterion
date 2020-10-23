package me.friwi.arterion.plugin.world.item;

import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public interface DescribedItem {
    default String getName(String key) {
        return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate().getMessage();
    }

    default List<String> getDescriptionWithLimit(String key, String prefix, int limit) {
        List<String> ret = new LinkedList<>();
        String[] desc = LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate().getMessage().split(" ");
        String s = "";
        for (String x : desc) {
            if (s.length() + x.length() < limit) s += x + " ";
            else {
                ret.add(prefix + s);
                s = "";
                s += x + " ";
            }
        }
        if (s.length() > 0) ret.add(prefix + s);
        return ret;
    }

    default ItemStack toItemStack(Material mat, CustomItemType type, String key, Long displayPrice) {
        ItemStack stack = new ItemStack(mat, 1);
        stack = NBTItemUtil.setType(stack, type);
        List<String> desc = getDescriptionWithLimit(key + ".desc", "\2477", 30);
        if (displayPrice != null) {
            desc.add(0, LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("siege.price").translate((displayPrice + 0d) / 100d).getMessage());
        }
        stack = NBTItemUtil.addGlowNameLore(stack, true, getName(key + ".name"), desc);
        return stack;
    }
}
