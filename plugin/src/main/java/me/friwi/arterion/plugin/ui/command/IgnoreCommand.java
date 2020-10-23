package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("ignore|ign|ignorieren|blockieren")
public class IgnoreCommand extends BaseCommand {
    private CommandManager commandManager;

    public IgnoreCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void ignore(CommandSender sender, String[] args) {
        if (args.length < 1) {
            this.help(sender);
            return;
        }

        String player = args[0];

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline() && !ArterionPlayerUtil.get((Player) sender).isVanished()) {
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);
            ArterionPlayer toIgnore = ArterionPlayerUtil.get(p);
            if (ep.getUUID().equals(toIgnore.getUUID())) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.self"));
                return;
            }
            if (toIgnore.getRank().isTeam()) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.team"));
                return;
            }
            ep.toggleIgnore(p.getUniqueId(), ignored -> {
                if (ignored) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.add", toIgnore));
                } else {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.del", toIgnore));
                }
            }, succ -> {
                if (!succ) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.dberr"));
                }
            });
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.error"));
            return;
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.ignore.help"));
    }
}
