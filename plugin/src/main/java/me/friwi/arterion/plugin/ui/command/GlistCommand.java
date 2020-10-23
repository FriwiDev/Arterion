package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.language.translateables.ArrayTranslateable;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@CommandAlias("glist|gilden|guilds")
public class GlistCommand extends BaseCommand {
    private CommandManager commandManager;

    public GlistCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void glist(CommandSender sender, String[] args) {
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        Iterator<? extends Guild> it = ArterionPlugin.getInstance().getGuildManager().getGuilds().iterator();
        List<Guild> guilds = new ArrayList<>();
        while (it.hasNext()) {
            Guild x = it.next();
            if (x.getDeleted() == DatabaseGuild.NOT_DELETED) guilds.add(x);
        }
        guilds.sort(Comparator.comparing(Guild::getName));
        String[] guildNames = new String[guilds.size()];
        for (int i = 0; i < guildNames.length; i++) {
            Guild x = guilds.get(i);
            guildNames[i] = x.getClanTagColor() + x.getName();
        }
        ArrayTranslateable translateable = new ArrayTranslateable(guildNames);
        Language lang = LanguageAPI.getLanguage(sender);
        sender.sendMessage(LanguageAPI.translate(sender, "command.glist.pre", guildNames.length));
        sender.sendMessage(translateable.getCaption(lang, "\247f", "\2477"));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.glist.help"));
    }
}
