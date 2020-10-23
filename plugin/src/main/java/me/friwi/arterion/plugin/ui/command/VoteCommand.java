package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;

@CommandAlias("vote|abstimmen")
public class VoteCommand extends BaseCommand {
    private CommandManager commandManager;

    public VoteCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void vote(CommandSender sender, String[] args) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.vote.msg"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.vote.help"));
    }
}
