package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("gm|gamemode")
public class GamemodeCommand extends BaseCommand {
    private CommandManager commandManager;

    public GamemodeCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@gamemode @players")
    @Syntax("<gamemode> [player]")
    public void gamemode(Player sender, String args[]) {
        if (!Permission.getRank(sender).isHigherTeam()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length != 1 && args.length != 2) {
            help(sender);
            return;
        }
        Player affecting = sender;
        if (args.length == 2) {
            affecting = Bukkit.getPlayer(args[1]);
            if (affecting == null || !affecting.isOnline()) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.gm.error"));
                return;
            }
        }
        int i = -1;
        GameMode gameMode = null;
        try {
            i = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {

        }
        if (i == -1) {
            try {
                gameMode = GameMode.valueOf(args[0].toUpperCase());
            } catch (Exception e) {

            }
        } else if (i >= 0 && i < GameMode.values().length) {
            gameMode = GameMode.getByValue(i);
        }

        if (gameMode == null) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.gm.invalid"));
            return;
        }

        if ((gameMode == GameMode.CREATIVE || args.length > 1) && !Permission.getRank(sender).isHigherOrEqualThan(Rank.DEVELOPER)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.gm.notallowed"));
            return;
        }

        affecting.setGameMode(gameMode);
        if (affecting.equals(sender)) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.gm.updated"));
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.gm.otherupdated", ArterionPlayerUtil.get(affecting)));
            affecting.sendMessage(LanguageAPI.translate(affecting, "command.gm.updated"));
        }
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.gm.help"));
    }
}
