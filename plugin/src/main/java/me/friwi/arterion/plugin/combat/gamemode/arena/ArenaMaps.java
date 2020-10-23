package me.friwi.arterion.plugin.combat.gamemode.arena;

import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum ArenaMaps {
    STANDARD("standard", "standard", false, new ItemStack(Material.CLAY)),
    CASTLE("castle", "castle", true, new ItemStack(Material.COBBLE_WALL)),
    WINDMILL("windmill", "windmill", true, new ItemStack(Material.WHEAT)),
    ORK_ARENA("ork", "ork", true, new ItemStack(Material.SLIME_BALL));

    private static final Random RANDOM = new SecureRandom();
    private static final List<ArenaMaps> NON_PREMIUM_MAPS = new ArrayList<>();

    static {
        for (ArenaMaps map : values()) {
            if (!map.isPremiumOnly()) NON_PREMIUM_MAPS.add(map);
        }
    }

    private String name;
    private String templateDir;
    private boolean premiumOnly;
    private ItemStack previewStack;

    ArenaMaps(String name, String templateDir, boolean premiumOnly, ItemStack previewStack) {
        this.name = name;
        this.templateDir = templateDir;
        this.premiumOnly = premiumOnly;
        this.previewStack = previewStack;
    }

    public static ArenaMaps randomNonPremium() {
        return NON_PREMIUM_MAPS.get(RANDOM.nextInt(NON_PREMIUM_MAPS.size()));
    }

    public String getName(Language lang) {
        return lang.getTranslation(getTranslationKey()).translate().getMessage();
    }

    public String getBy(Language lang) {
        return lang.getTranslation(getTranslationKey() + ".by").translate().getMessage();
    }

    public String getTranslationKey() {
        return "arena.map." + name.toLowerCase();
    }

    public String getTemplateDir() {
        return templateDir;
    }

    public boolean isPremiumOnly() {
        return premiumOnly;
    }

    public ItemStack getPreviewStack() {
        return previewStack.clone();
    }
}
