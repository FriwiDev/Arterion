package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("invite")
public class InviteCommand extends BaseCommand {
    private CommandManager commandManager;

    public InviteCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void invite(CommandSender sender, String[] args) {
        if (args.length < 2) {
            this.help(sender);
            return;
        }

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        boolean accept = args[0].equalsIgnoreCase("a");
        try {
            UUID inv = UUID.fromString(args[1]);
            if (this.commandManager.getPlugin().getInvitationSystem().onAction(inv, accept, ep) == null) {
                ep.sendTranslation("invitation.timedout");
            }
        } catch (IllegalArgumentException e) {
            this.help(sender);
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.invite.help"));
    }
}
