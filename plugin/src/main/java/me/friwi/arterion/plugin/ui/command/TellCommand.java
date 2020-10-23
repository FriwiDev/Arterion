package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tell|t|m|msg|message|whisper")
public class TellCommand extends BaseCommand {
    private CommandManager commandManager;

    public TellCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> <message>")
    public void tell(CommandSender sender, String[] args) {
        if (args.length < 2) {
            this.help(sender);
            return;
        }

        String player = args[0];
        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        String reason = "";
        for (int i = 1; i < args.length; i++) {
            reason += args[i] + (i < args.length - 1 ? " " : "");
        }

        String finalReason = reason;
        ep.checkNotMuted(() -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
                ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
                if (onlinePlayer.equals(ep)) {
                    ep.sendTranslation("command.tell.self");
                } else {
                    boolean ignored = !ep.getRank().isHigherTeam() && onlinePlayer.getPersistenceHolder().getIgnoredPlayers().contains(ep.getUUID());
                    if (!ignored)
                        onlinePlayer.sendTranslation("command.tell.msgformat", ep, onlinePlayer, finalReason);
                    ep.sendTranslation("command.tell.msgformat", ep, onlinePlayer, finalReason);
                    if (!ignored) onlinePlayer.setLastWhisperer(ep.getName());
                    ep.setLastWhisperer(onlinePlayer.getName());
                }
            } else {
                sender.sendMessage(LanguageAPI.translate(sender, "command.tell.error"));
                return;
            }
        });
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.tell.help"));
    }
}
