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

@CommandAlias("r|reply")
public class ReplyCommand extends BaseCommand {
    private CommandManager commandManager;

    public ReplyCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("<message>")
    public void reply(CommandSender sender, String[] args) {
        if (args.length < 1) {
            this.help(sender);
            return;
        }

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        ep.checkNotMuted(() -> {
            String player = ep.getLastWhisperer();

            String reason = "";
            for (int i = 0; i < args.length; i++) {
                reason += args[i] + (i < args.length - 1 ? " " : "");
            }

            Player p = Bukkit.getPlayer(player);
            if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
                ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
                if (onlinePlayer.equals(ep)) {
                    ep.sendTranslation("command.tell.self");
                } else {
                    String finalReason = reason;

                    boolean ignored = !ep.getRank().isHigherTeam() && onlinePlayer.getPersistenceHolder().getIgnoredPlayers().contains(ep.getUUID());
                    if (!ignored)
                        onlinePlayer.sendTranslation("command.tell.msgformat", ep, onlinePlayer, finalReason);
                    ep.sendTranslation("command.tell.msgformat", ep, onlinePlayer, finalReason);
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
        sender.sendMessage(LanguageAPI.translate(sender, "command.reply.help"));
    }
}
