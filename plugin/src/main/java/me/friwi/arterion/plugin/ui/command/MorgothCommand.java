package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothManager;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

@CommandAlias("morgoth")
public class MorgothCommand extends BaseCommand {
    private CommandManager commandManager;

    public MorgothCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void artefact(CommandSender sender, String args[]) {
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        Location portal = ArterionPlugin.getInstance().getArterionConfig().morgoth_portal;
        if (MorgothManager.isInFight()) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.morgoth.inprogress"));
        } else if (MorgothManager.getPortalCooldown() > 0) {
            long cd = MorgothManager.getPortalCooldown() / 1000;
            sender.sendMessage(LanguageAPI.translate(sender, "command.morgoth.incd", TimeFormatUtil.formatSeconds(cd)));
        } else if (MorgothManager.isOpeningSoon()) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.morgoth.soon"));
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.morgoth.open"));
        }
        if (portal != null)
            sender.sendMessage(LanguageAPI.translate(sender, "command.morgoth.position", portal.getWorld().getName(), portal.getBlockX(), portal.getBlockY(), portal.getBlockZ()));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.morgoth.help"));
    }
}
