package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("steal|gsteal|klauen|diebstahl")
public class StealCommand extends BaseCommand {
    private CommandManager commandManager;

    public StealCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void steal(CommandSender sender, String[] args) {
        if (args.length != 0) {
            this.help(sender);
            return;
        }

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        ArterionPlugin.getInstance().getGuildFightManager().steal(ep);
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.steal.help"));
    }
}
