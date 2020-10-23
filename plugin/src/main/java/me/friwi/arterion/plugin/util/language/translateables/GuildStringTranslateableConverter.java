package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;

public class GuildStringTranslateableConverter extends TypeConverter<Guild, StringTranslateable> {

    @Override
    public Guild convertOne(StringTranslateable value) {
        return ArterionPlugin.getInstance().getGuildManager().getGuildByName(value.getString());
    }

    @Override
    public StringTranslateable convertTwo(Guild value) {
        if (value == null) return new StringTranslateable("\247c#ERR NULL");
        return new StringTranslateable(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("guild.name").translate(value.getName()).getMessage());
    }

}
