package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("kill")
public class KillCommand extends BaseCommand {
    private CommandManager commandManager;

    public KillCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void kill(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).setHealth(0);
            sender.sendMessage(LanguageAPI.translate(sender, "command.kill.success"));
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.kill.help"));
    }
}
