package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ui.chat.FormattedChat;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("website|web")
public class WebsiteCommand extends BaseCommand {
    private CommandManager commandManager;

    public WebsiteCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void website(CommandSender sender, String[] args) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.website.msg"));
        FormattedChat.sendFormattedChat((Player) sender, LanguageAPI.translate(sender, "command.website.suggestlogin"));

    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.website.help"));
    }
}
