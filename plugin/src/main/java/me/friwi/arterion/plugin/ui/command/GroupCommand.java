package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("group|party")
public class GroupCommand extends BaseCommand {
    private CommandManager commandManager;

    public GroupCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void group(Player sender, String args[]) {
        if (args.length != 0) {
            if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
                ArterionPlayer ep = ArterionPlayerUtil.get(sender);
                commandManager.getPlugin().getGroupSystem().inviteGroup(ep, args[1]);
                return;
            }
            this.help(sender);
            return;
        }
        ArterionPlayer ep = ArterionPlayerUtil.get(sender);
        commandManager.getPlugin().getGroupSystem().openGroupDialog(ep);
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.group.help"));
    }
}
