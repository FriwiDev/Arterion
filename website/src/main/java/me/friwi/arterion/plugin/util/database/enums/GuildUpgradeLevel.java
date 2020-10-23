package me.friwi.arterion.plugin.util.database.enums;

public enum GuildUpgradeLevel {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5,
    LEVEL_6;

    public GuildUpgradeLevel next() {
        return values()[ordinal() + 1];
    }
}
