package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@CommandAlias("maintenance")
public class MaintenanceCommand extends BaseCommand {
    private CommandManager commandManager;

    public MaintenanceCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@state")
    @Syntax("<ON|OFF>")
    public void maintenance(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 1) {
            this.help(sender);
            return;
        }

        if (sender instanceof Player) {
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

            CommandManager.StateEnum state = null;
            try {
                state = CommandManager.StateEnum.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                ep.sendTranslation("command.maintenance.notfound");
                return;
            }

            this.commandManager.getPlugin().setMaintenance(state.getBooleanMapping());
            if (state.getBooleanMapping()) {
                ep.sendTranslation("command.maintenance.on");
            } else {
                ep.sendTranslation("command.maintenance.off");
            }
        } else if (sender instanceof ConsoleCommandSender) {
            CommandManager.StateEnum state = null;
            try {
                state = CommandManager.StateEnum.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("State not found.");
                return;
            }

            this.commandManager.getPlugin().setMaintenance(state.getBooleanMapping());
            if (state.getBooleanMapping()) {
                sender.sendMessage("command.maintenance.on");
            } else {
                sender.sendMessage("command.maintenance.off");
            }
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.maintenance.help"));
    }
}
