package me.friwi.arterion.plugin.util.language.api;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class LanguageAPI {
    public static final String DEFAULT_LANGUAGE = "de_DE";
    private static Map<String, Language> lang;

    public static Language getLanguage(String locale) {
        return lang.get(locale);
    }

    private static void reloadLanguage(String locale) {
        System.out.println("Loading language " + locale);
        lang.put(locale, new Language(locale));
    }

    public static void reloadAllLanguages() {
        lang = new HashMap<>();
        reloadLanguage("de_DE");
    }

    public static void broadcastMessage(String key, Object... values) {
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ep = ArterionPlayerUtil.get(p);
            if (ep == null) continue;
            p.sendMessage(ep.getLanguage().getTranslation(key).translate(values).getMessage());
        }
        Bukkit.getConsoleSender().sendMessage(getLanguage(DEFAULT_LANGUAGE).getTranslation(key).translate(values).getMessage());
    }

    public static String translate(ArterionPlayer ep, String key, Object... values) {
        if (ep == null) {
            return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate(values).getMessage();
        } else {
            return ep.getLanguage().getTranslation(key).translate(values).getMessage();
        }
    }

    public static String translate(CommandSender sender, String key, Object... values) {
        if (sender instanceof Player) {
            return translate(ArterionPlayerUtil.get((Player) sender), key, values);
        } else {
            return translate((ArterionPlayer) null, key, values);
        }
    }

    public static Language getLanguage(CommandSender sender) {
        if (sender instanceof Player) {
            return ArterionPlayerUtil.get((Player) sender).getLanguage();
        } else {
            return getDefaultLanguage();
        }
    }

    public static String translate(String key, Object... values) {
        return translate((ArterionPlayer) null, key, values);
    }

    public static Language getDefaultLanguage() {
        return getLanguage(DEFAULT_LANGUAGE);
    }
}
