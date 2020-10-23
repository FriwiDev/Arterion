package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeFormatter;

@CommandAlias("artefact|artefakt|arte")
public class ArtefactCommand extends BaseCommand {
    private CommandManager commandManager;

    public ArtefactCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void artefact(CommandSender sender, String args[]) {
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        Location arte = null;
        if (Artefact.getCarrier() != null) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.atcarrier", Artefact.getCarrier()));
            sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.nextreset", DateTimeFormatter.ISO_DATE_TIME.format(Artefact.getNextReset()).replaceFirst("T", " ")));
            arte = Artefact.getCarrier().getBukkitPlayer().getLocation();
        } else if (Artefact.getOwner() != null) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.atguild", Artefact.getOwner().getName()));
            sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.nextreset", DateTimeFormatter.ISO_DATE_TIME.format(Artefact.getNextReset()).replaceFirst("T", " ")));
            arte = Artefact.getOwner().getHomeLocation();
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.inruin"));
            arte = ArterionPlugin.getInstance().getArterionConfig().artefact;
        }
        if (arte != null)
            sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.position", arte.getWorld().getName(), arte.getBlockX(), arte.getBlockY(), arte.getBlockZ()));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.artefact.help"));
    }
}
