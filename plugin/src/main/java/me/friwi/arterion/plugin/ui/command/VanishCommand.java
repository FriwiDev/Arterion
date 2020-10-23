package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("vanish|v")
public class VanishCommand extends BaseCommand {
    private CommandManager commandManager;

    public VanishCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void vanish(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherTeam()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        ArterionPlayer ap = ArterionPlayerUtil.get((Player) sender);
        if (ap.isVanished()) {
            ap.sendTranslation("command.vanish.off");
            ap.setVanished(false);
        } else {
            ap.sendTranslation("command.vanish.on");
            ap.setVanished(true);
        }

        ap.getPlayerScoreboard().updateAllPlayerRelations();
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.vanish.help"));
    }
}
