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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("setpos")
public class SetPosCommand extends BaseCommand {
    private CommandManager commandManager;

    public SetPosCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void setPos(Player sender, String args[]) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length != 1) {
            help(sender);
            return;
        }
        Location loc = sender.getLocation().clone();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setZ(loc.getBlockZ() + 0.5);
        switch (args[0].toLowerCase()) {
            case "spawn":
                this.commandManager.getPlugin().getArterionConfig().spawn = loc;
                break;
            case "artefact":
                this.commandManager.getPlugin().getArterionConfig().artefact = loc;
                break;
            case "artefact_cristal_1":
                this.commandManager.getPlugin().getArterionConfig().artefact_cristal_1 = loc;
                Artefact.getCristals()[0].move(loc);
                break;
            case "artefact_cristal_2":
                this.commandManager.getPlugin().getArterionConfig().artefact_cristal_2 = loc;
                Artefact.getCristals()[1].move(loc);
                break;
            case "artefact_cristal_3":
                this.commandManager.getPlugin().getArterionConfig().artefact_cristal_3 = loc;
                Artefact.getCristals()[2].move(loc);
                break;
            case "graveruin_center":
                this.commandManager.getPlugin().getArterionConfig().graveruin_center = loc;
                CapturePoints.GRAVE_RUIN.setCapCenter(loc);
                break;
            case "graveruin_glass":
                this.commandManager.getPlugin().getArterionConfig().graveruin_glass = loc;
                CapturePoints.GRAVE_RUIN.setGlassBlock(loc);
                break;
            case "deserttemple_center":
                this.commandManager.getPlugin().getArterionConfig().deserttemple_center = loc;
                CapturePoints.DESERT_TEMPLE.setCapCenter(loc);
                break;
            case "deserttemple_glass":
                this.commandManager.getPlugin().getArterionConfig().deserttemple_glass = loc;
                CapturePoints.DESERT_TEMPLE.setGlassBlock(loc);
                break;
            case "morgoth_portal":
                this.commandManager.getPlugin().getArterionConfig().morgoth_portal = loc;
                break;
            case "wilderness_portal":
                this.commandManager.getPlugin().getArterionConfig().wilderness_portal = loc;
                break;
            default:
                sender.sendMessage(LanguageAPI.translate(sender, "command.setpos.fail"));
                return;
        }
        this.commandManager.getPlugin().saveConfig();
        sender.sendMessage(LanguageAPI.translate(sender, "command.setpos.success"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.setpos.help"));
    }
}
