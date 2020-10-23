package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
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

@CommandAlias("mute|silence")
public class MuteCommand extends BaseCommand {
    private CommandManager commandManager;

    public MuteCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players @nothing @timeunit")
    @Syntax("<player> <amount> <MINUTES|HOURS|DAYS|YEARS> [reason]")
    public void mute(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).canMute()) {
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
            sender.sendMessage(LanguageAPI.translate(ep, "command.mute.notfound"));
            return;
        }
        int time = 0;
        try {
            time = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            sender.sendMessage(LanguageAPI.translate(ep, "command.mute.nonumber"));
            return;
        }

        long expires = System.currentTimeMillis() + time * r.getSingleDuration();
        long issued = System.currentTimeMillis();
        String reason = "";
        if (args.length == 3) {
            reason = "You have been muted";
        } else {
            for (int i = 3; i < args.length; i++) {
                reason += args[i] + (i < args.length - 1 ? " " : "");
            }
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            onlinePlayer.sendTranslation("chat.mute");
            onlinePlayer.sendTranslation("chat.reason", reason);
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
                    commandManager.getPlugin().getSanctionManager().sanction(affected, issuer, SanctionType.MUTE, issued, expires, finalReason);
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (fail) sender.sendMessage(LanguageAPI.translate(sender, "command.mute.error"));
                else sender.sendMessage(LanguageAPI.translate(sender, "command.mute.completed", player));
            }

            @Override
            public void onTransactionError() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.mute.error"));
            }
        }.execute();
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.mute.help"));
    }
}
