package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.language.translateables.ArrayTranslateable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("list|l|liste|spieler")
public class ListCommand extends BaseCommand {
    private CommandManager commandManager;

    public ListCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void list(CommandSender sender, String[] args) {
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        List<ArterionPlayer> list = new ArrayList<>(ArterionPlugin.getOnlinePlayers().size());
        int vanished = 0;
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            if (!ap.isVanished()) list.add(ap);
            else vanished++;
        }
        list.sort((a, b) -> {
            if (a.getRank() != b.getRank()) {
                return Integer.valueOf(b.getRank().ordinal()).compareTo(a.getRank().ordinal());
            } else {
                return a.getName().compareTo(b.getName());
            }
        });
        ArrayTranslateable translateable = new ArrayTranslateable(list.toArray());
        Language lang = LanguageAPI.getLanguage(sender);
        sender.sendMessage(LanguageAPI.translate(sender, "command.list.pre", Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers()));
        sender.sendMessage(translateable.getCaption(lang, "\247f", "\2477"));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.list.help"));
    }
}
