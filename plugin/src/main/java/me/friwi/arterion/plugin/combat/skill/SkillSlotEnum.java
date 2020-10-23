package me.friwi.arterion.plugin.combat.skill;

import org.bukkit.Material;

public enum SkillSlotEnum {
    ACTIVE1(Material.GREEN_RECORD, "\2472\247l", "RECORD_CAT", "\2472", 0xFF00AA00),
    ACTIVE2(Material.GOLD_RECORD, "\2476\247l", "RECORD_13", "\2476", 0xFFFFAA00),
    ACTIVE3(Material.RECORD_3, "\247c\247l", "RECORD_BLOCKS", "\247c", 0xFFFF5555),
    ACTIVE4(Material.RECORD_4, "\2474\247l", "RECORD_CHIRP", "\2474", 0xFFAA0000),
    ACTIVE5(Material.RECORD_6, "\2475\247l", "RECORD_MALL", "\2475", 0xFFAA00AA);

    private Material diskMaterial;
    private String color;
    private String nmsMaterial;
    private String rawColor;
    private int ARGB;

    SkillSlotEnum(Material diskMaterial, String color, String nmsMaterial, String rawColor, int ARGB) {
        this.diskMaterial = diskMaterial;
        this.color = color;
        this.nmsMaterial = nmsMaterial;
        this.rawColor = rawColor;
        this.ARGB = ARGB;
    }

    public Material getDiskMaterial() {
        return diskMaterial;
    }

    public String getColor() {
        return color;
    }

    public String getNmsMaterial() {
        return nmsMaterial;
    }

    public String getRawColor() {
        return rawColor;
    }

    public int getSlot() {
        return ordinal();
    }

    public int getARGB() {
        return ARGB;
    }
}
