package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.language.translateables.ArrayTranslateable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;

@CommandAlias("ginfo|gildeninfo|g|guild")
public class GinfoCommand extends BaseCommand {
    private CommandManager commandManager;

    public GinfoCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@guild")
    @Syntax("[guild]")
    public void ginfo(CommandSender sender, String[] args) {
        Guild g = null;
        if (args.length < 1 || args[0].isEmpty()) {
            if (sender instanceof Player) {
                g = ArterionPlayerUtil.get((Player) sender).getGuild();
            }
            if (g == null) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.notinguild"));
                return;
            }
        } else {
            g = ArterionPlugin.getInstance().getGuildManager().getGuildByName(args[0]);
            if (g == null) g = ArterionPlugin.getInstance().getGuildManager().getGuildByTag(args[0]);
            if (g == null) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.notfound"));
                return;
            }
        }

        String[] officers = new String[g.getOfficers().size()];
        Iterator<DatabasePlayer> it = g.getOfficers().iterator();
        int i = 0;
        while (it.hasNext()) {
            officers[i] = it.next().getName();
            i++;
        }
        ArrayTranslateable translateable = new ArrayTranslateable(officers);
        Language lang = LanguageAPI.getLanguage(sender);

        String[] members = new String[g.getOfficers().size() + g.getMembers().size() + 1];
        it = g.getOfficers().iterator();
        members[0] = g.getLeader().getName();
        i = 1;
        while (it.hasNext()) {
            members[i] = it.next().getName();
            i++;
        }
        it = g.getMembers().iterator();
        while (it.hasNext()) {
            members[i] = it.next().getName();
            i++;
        }
        Arrays.sort(members);
        for (int j = 0; j < members.length; j++) {
            Player p = Bukkit.getPlayer(members[j]);
            if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
                members[j] = "\247a" + members[j];
            } else {
                members[j] = "\2478" + members[j];
            }
        }
        ArrayTranslateable translateable2 = new ArrayTranslateable(members);

        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.title", g.getClanTagColor() + g.getTag(), g.getClanTagColor() + g.getName()));
        if (g.isProtected() && !g.hasArtefact()) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(g.getProtection()), ArterionPlugin.SERVER_TIME_ZONE);
            String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
            sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.protected", time));
        }
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.leader", g.getLeader().getName()));
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.officers", translateable.getCaption(lang, "\247f", "\247e")));
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.members", translateable2.getCaption(lang, "\247f", "")));
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.vault", g.getMoneyBearer().getCachedMoney() / 100d));
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.area", g.getRegionDistance() * 2 + 1));
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.kills", g.getClanKills()));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.ginfo.help"));
    }
}
