package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.sanctions.SanctionType;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("kick")
public class KickCommand extends BaseCommand {
    private CommandManager commandManager;

    public KickCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> [reason]")
    public void kick(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).canKick()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 1) {
            this.help(sender);
            return;
        }

        String player = args[0];


        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        String reason = "";
        if (args.length == 1) {
            reason = "You have been kicked";
        } else {
            for (int i = 1; i < args.length; i++) {
                reason += args[i] + (i < args.length - 1 ? " " : "");
            }
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            p.kickPlayer("\247cYou have been kicked from the server!\n" +
                    "\247cReason: \2477" + reason);
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.kick.error"));
            return;
        }

        long issued = System.currentTimeMillis();
        long expires = System.currentTimeMillis();

        String finalReason = reason;
        new DatabaseTask() {
            boolean fail = false;

            @Override
            public boolean performTransaction(Database db) {
                DatabasePlayer affected = db.find(DatabasePlayer.class, p.getUniqueId());
                DatabasePlayer issuer = db.find(DatabasePlayer.class, ep.getBukkitPlayer().getUniqueId());
                if (affected == null) {
                    fail = true;
                } else {
                    commandManager.getPlugin().getSanctionManager().sanction(affected, issuer, SanctionType.KICK, issued, expires, finalReason);
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (fail) sender.sendMessage(LanguageAPI.translate(sender, "command.kick.error"));
                else sender.sendMessage(LanguageAPI.translate(sender, "command.kick.completed", player));
            }

            @Override
            public void onTransactionError() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.kick.error"));
            }
        }.execute();
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.kick.help"));
    }
}
