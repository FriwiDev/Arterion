package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.combat.gamemode.capturepoint.CapturePoints;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("reset")
public class ResetCommand extends BaseCommand {
    private CommandManager commandManager;

    public ResetCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void reset(Player sender, String args[]) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length != 1) {
            help(sender);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "artefact":
                Artefact.reset();
                break;
            case "graveruin":
                CapturePoints.GRAVE_RUIN.reset();
                break;
            case "deserttemple":
                CapturePoints.DESERT_TEMPLE.reset();
                break;
            default:
                sender.sendMessage(LanguageAPI.translate(sender, "command.reset.fail"));
                return;
        }
        this.commandManager.getPlugin().saveConfig();
        sender.sendMessage(LanguageAPI.translate(sender, "command.reset.success"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.reset.help"));
    }
}
