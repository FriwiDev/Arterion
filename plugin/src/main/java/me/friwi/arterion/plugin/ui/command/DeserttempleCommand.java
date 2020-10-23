package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.combat.gamemode.capturepoint.CapturePoint;
import me.friwi.arterion.plugin.combat.gamemode.capturepoint.CapturePoints;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

@CommandAlias("deserttemple|wüstentempel")
public class DeserttempleCommand extends BaseCommand {
    private CommandManager commandManager;

    public DeserttempleCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void deserttemple(CommandSender sender, String args[]) {
        CapturePoint capturePoint = CapturePoints.DESERT_TEMPLE;
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        Location loc = capturePoint.getCapCenter();
        if (capturePoint.getClaimedBy() != null) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.deserttemple.claimed", capturePoint.getClaimedBy().getName()));
            sender.sendMessage(LanguageAPI.translate(sender, "command.deserttemple.claimed_until", TimeFormatUtil.formatSeconds(capturePoint.getClaimedTicks() / 20)));
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.deserttemple.available"));
        }
        sender.sendMessage(LanguageAPI.translate(sender, "command.deserttemple.position", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.deserttemple.help"));
    }
}
