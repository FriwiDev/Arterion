package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.TimeUnit;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CommandAlias("ban")
public class BanCommand extends BaseCommand {
    private CommandManager commandManager;

    public BanCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players @nothing @timeunit")
    @Syntax("<player> <amount> <MINUTES|HOURS|DAYS|YEARS> [reason]")
    public void setPlayerRank(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).canBan()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 3) {
            this.help(sender);
            return;
        }

        String player = args[0];
        String amount = args[1];
        String unit = args[2];

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);
        TimeUnit r = null;
        try {
            r = TimeUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LanguageAPI.translate(ep, "command.ban.notfound"));
            return;
        }
        int time = 0;
        try {
            time = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            sender.sendMessage(LanguageAPI.translate(ep, "command.ban.nonumber"));
            return;
        }

        long expires = System.currentTimeMillis() + time * r.getSingleDuration();
        long issued = System.currentTimeMillis();
        String reason = "";
        if (args.length == 3) {
            reason = "You have been banned";
        } else {
            for (int i = 3; i < args.length; i++) {
                reason += args[i] + (i < args.length - 1 ? " " : "");
            }
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(expires), ArterionPlugin.SERVER_TIME_ZONE);
            String timestr = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
            p.kickPlayer("\247cYou are banned until \2477" + timestr + "\247c!\n" +
                    "\247cReason: \2477" + reason);
        }

        String finalReason = reason;
        new DatabaseTask() {
            boolean fail = false;

            @Override
            public boolean performTransaction(Database db) {
                DatabasePlayer affected = db.findOneByColumn(DatabasePlayer.class, "name", player);
                DatabasePlayer issuer = db.find(DatabasePlayer.class, ep.getBukkitPlayer().getUniqueId());
                if (affected == null) {
                    fail = true;
                } else {
                    commandManager.getPlugin().getSanctionManager().sanction(affected, issuer, SanctionType.BAN, issued, expires, finalReason);
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (fail) sender.sendMessage(LanguageAPI.translate(sender, "command.ban.error"));
                else sender.sendMessage(LanguageAPI.translate(sender, "command.ban.completed", player));
            }

            @Override
            public void onTransactionError() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.ban.error"));
            }
        }.execute();
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.ban.help"));
    }
}
