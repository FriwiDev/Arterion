package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;

@CommandAlias("help|?|h|hilfe|support")
public class HelpCommand extends BaseCommand {
    @Default
    @CommandCompletion("")
    @Syntax("")
    public void help(CommandSender sender, String[] args) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.help.msg"));
    }

    @co.aikar.commands.annotation.HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.help.msg"));
    }
}
