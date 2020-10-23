package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;

@CommandAlias("recipes|rezepte")
public class RecipesCommand extends BaseCommand {
    private CommandManager commandManager;

    public RecipesCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void recipes(CommandSender sender, String[] args) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.recipes.msg"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.recipes.help"));
    }
}
