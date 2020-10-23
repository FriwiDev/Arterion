package me.friwi.arterion.plugin.util.database.enums;

public enum GuildUpgradeEnum {
    VAULT(GuildUpgradeLevel.LEVEL_6),
    REGION(GuildUpgradeLevel.LEVEL_6),
    OFFICER(GuildUpgradeLevel.LEVEL_3);

    private GuildUpgradeLevel maxLevel;

    GuildUpgradeEnum(GuildUpgradeLevel maxLevel) {
        this.maxLevel = maxLevel;
    }

    public GuildUpgradeLevel getMaxLevel() {
        return maxLevel;
    }
}
