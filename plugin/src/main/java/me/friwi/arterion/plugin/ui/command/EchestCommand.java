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

@CommandAlias("echest|enderchest")
public class EchestCommand extends BaseCommand {
    private CommandManager commandManager;

    public EchestCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void echest(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherTeam()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 1) {
            this.help(sender);
            return;
        }

        String player = args[0];

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ((Player) sender).openInventory(p.getEnderChest());
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.echest.error"));
            return;
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.echest.help"));
    }
}
