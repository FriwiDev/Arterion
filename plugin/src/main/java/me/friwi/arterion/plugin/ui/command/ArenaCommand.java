package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.combat.gamemode.arena.ArenaInitializer;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("arena|duel|duell")
public class ArenaCommand extends BaseCommand {
    private CommandManager commandManager;

    public ArenaCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void arena(Player sender, String args[]) {
        if (args.length != 0) {
            this.help(sender);
            return;
        }
        ArterionPlayer ep = ArterionPlayerUtil.get(sender);
        if (ep.getArenaInitializer() != null) {
            ep.getArenaInitializer().openInterface(ep);
        } else {
            String join = ArenaInitializer.canJoinArenaInitializer(ep);
            if (join != null) {
                ep.sendTranslation("arena.younojoin." + join);
                return;
            }
            ArenaInitializer init = new ArenaInitializer(ep);
            init.openInterface(ep);
        }
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.arena.help"));
    }
}
