package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;

public class ArterionPlayerStringTranslateableConverter extends TypeConverter<ArterionPlayer, StringTranslateable> {

    @Override
    public ArterionPlayer convertOne(StringTranslateable value) {
        return ArterionPlayerUtil.get(Bukkit.getPlayer(value.getString()));
    }

    @Override
    public StringTranslateable convertTwo(ArterionPlayer value) {
        if (value == null) return new StringTranslateable("\247c#ERR NULL");
        return new StringTranslateable(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(value.getRank().getRankTranslation()).translate(value.getName()).getMessage() + value.getName());
    }

}
