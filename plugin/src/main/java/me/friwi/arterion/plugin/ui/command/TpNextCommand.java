package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tpnext")
public class TpNextCommand extends BaseCommand {
    private CommandManager commandManager;

    public TpNextCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void tpnext(Player sender, String args[]) {
        if (!Permission.getRank(sender).isHigherTeam()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length != 0) {
            help(sender);
            return;
        }
        ArterionPlayer ap = ArterionPlayerUtil.get(sender);
        ap.teleportIndex++;
        if (ap.teleportIndex >= ArterionPlugin.getOnlinePlayers().size()) {
            ap.teleportIndex = 0;
        } else if (ap.teleportIndex < 0) {
            ap.teleportIndex = ArterionPlugin.getOnlinePlayers().size() - 1;
        }
        Player tpTo = ArterionPlugin.getOnlinePlayers().get(ap.teleportIndex);
        if (tpTo.getUniqueId().equals(ap.getUUID())) {
            ap.teleportIndex++;
            if (ap.teleportIndex >= ArterionPlugin.getOnlinePlayers().size()) {
                ap.teleportIndex = 0;
            } else if (ap.teleportIndex < 0) {
                ap.teleportIndex = ArterionPlugin.getOnlinePlayers().size() - 1;
            }
            tpTo = ArterionPlugin.getOnlinePlayers().get(ap.teleportIndex);
        }
        ap.getBukkitPlayer().teleport(tpTo);
        sender.sendMessage(LanguageAPI.translate(sender, "command.tpnext.success", ArterionPlayerUtil.get(tpTo)));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.tpnext.help"));
    }
}
