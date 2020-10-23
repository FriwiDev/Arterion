package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tp|teleport")
public class TpCommand extends BaseCommand {
    private CommandManager commandManager;

    public TpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players @players")
    @Syntax("[from] <to>")
    public void tp(Player sender, String args[]) {
        if (!Permission.getRank(sender).isHigherTeam()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length != 1 && args.length != 2) {
            help(sender);
            return;
        }
        if (args.length == 1) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p != null && p.isOnline()) {
                sender.teleport(p);
                sender.sendMessage(LanguageAPI.translate(sender, "command.tp.yousuccess", ArterionPlayerUtil.get(p)));
            } else {
                sender.sendMessage(LanguageAPI.translate(sender, "command.tp.error"));
            }
        } else {
            Player from = Bukkit.getPlayer(args[0]);
            Player to = Bukkit.getPlayer(args[1]);
            if (from != null && from.isOnline() && to != null && to.isOnline()) {
                from.teleport(to);
                from.sendMessage(LanguageAPI.translate(from, "command.tp.othertp"));
                sender.sendMessage(LanguageAPI.translate(sender, "command.tp.success", ArterionPlayerUtil.get(from), ArterionPlayerUtil.get(to)));
            } else {
                sender.sendMessage(LanguageAPI.translate(sender, "command.tp.error"));
            }
        }
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.tp.help"));
    }
}
