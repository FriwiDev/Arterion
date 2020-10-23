package me.friwi.arterion.plugin.combat.classes;

import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.Material;

import static org.bukkit.Material.*;

public enum ClassEnum {
    NONE(new Material[0]),
    PALADIN(DIAMOND_SPADE, IRON_SPADE, GOLD_SPADE, STONE_SPADE, WOOD_SPADE),
    BARBAR(DIAMOND_AXE, IRON_AXE, GOLD_AXE, STONE_AXE, WOOD_AXE),
    SHADOWRUNNER(DIAMOND_SWORD, IRON_SWORD, GOLD_SWORD, STONE_SWORD, WOOD_SWORD),
    FORESTRUNNER(BOW),
    MAGE(DIAMOND_HOE, IRON_HOE, GOLD_HOE, STONE_HOE, WOOD_HOE),
    CLERIC(STICK);

    private Material[] classWeapons;

    private ClassEnum(Material... classWeapons) {
        this.classWeapons = classWeapons;
    }

    public boolean isWeaponAllowed(Material material) {
        for (Material m : classWeapons) if (m == material) return true;
        return false;
    }

    public String getName(Language lang) {
        return lang.getTranslation("class." + name().toLowerCase()).translate().getMessage();
    }
}
