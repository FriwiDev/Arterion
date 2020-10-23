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

@CommandAlias("war|krieg|gfight")
public class WarCommand extends BaseCommand {
    private CommandManager commandManager;

    public WarCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@guild")
    @Syntax("<guild>")
    public void war(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.help(sender);
            return;
        }

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        ArterionPlugin.getInstance().getGuildFightManager().attack(ep, commandManager.getPlugin().getGuildManager().getGuildByName(args[0]));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.war.help"));
    }
}
