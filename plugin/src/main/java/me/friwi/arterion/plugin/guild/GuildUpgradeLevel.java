package me.friwi.arterion.plugin.guild;

import me.friwi.arterion.plugin.util.language.api.Language;

public enum GuildUpgradeLevel {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5,
    LEVEL_6;

    public String getName(Language lang) {
        return lang.getTranslation("guild.upgrade.level").translate(ordinal()).getMessage();
    }

    public GuildUpgradeLevel next() {
        return values()[ordinal() + 1];
    }
}
