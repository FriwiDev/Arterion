package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
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
import java.util.List;

@CommandAlias("who|whois|info|player|p")
public class InfoCommand extends BaseCommand {
    private CommandManager commandManager;

    public InfoCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("[player]")
    public void info(CommandSender sender, String[] args) {
        DatabasePlayer player = null;
        if (args.length < 1 || args[0].isEmpty()) {
            if (sender instanceof Player) {
                player = ArterionPlayerUtil.get((Player) sender).getPersistenceHolder();
            }
            if (player == null) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.info.noplayer"));
                return;
            }
            sendInfo(sender, player);
        } else {
            new DatabaseTask() {

                @Override
                public boolean performTransaction(Database db) {
                    List<DatabasePlayer> players = db.findAllByColumnIgnoreCase(DatabasePlayer.class, "name", args[0]);
                    DatabasePlayer best = null;
                    long lastPlayed = 0;
                    for (DatabasePlayer p : players) {
                        if (p.getLastOnline() > lastPlayed || p.getLastOnline() == -1) {
                            best = p;
                            lastPlayed = p.getLastOnline();
                            if (p.getLastOnline() == -1) break;
                        }
                    }
                    if (best == null) {
                        sender.sendMessage(LanguageAPI.translate(sender, "command.info.noexist"));
                    } else {
                        sendInfo(sender, best);
                    }
                    return true;
                }

                @Override
                public void onTransactionCommitOrRollback(boolean committed) {
                    if (!committed) {
                        sender.sendMessage(LanguageAPI.translate(sender, "command.info.dberr"));
                    }
                }

                @Override
                public void onTransactionError() {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.info.dberr"));
                }
            }.execute();
        }
    }

    private void sendInfo(CommandSender sender, DatabasePlayer player) {
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
        sender.sendMessage(LanguageAPI.translate(sender, "command.info.title", LanguageAPI.getLanguage(sender).translateObject(player)));
        Player p = Bukkit.getPlayer(player.getUuid());
        ArterionPlayer ap = null;
        boolean vanish = false;
        if (p != null && p.isOnline()) {
            ap = ArterionPlayerUtil.get(p);
            vanish = ap.isVanished();
        }
        if ((player.getLastOnline() == -1 || (p != null && p.isOnline())) && !vanish) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.info.online"));
        } else {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(vanish ? ap.getVanishTime() : player.getLastLogin()), ArterionPlugin.SERVER_TIME_ZONE);
            String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
            sender.sendMessage(LanguageAPI.translate(sender, "command.info.last_seen", time));
        }
        Guild g = ArterionPlugin.getInstance().getGuildManager().getGuildByMemberUUID(player.getUuid());
        if (g != null) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.info.guild", g.getClanTagColor() + g.getTag(), g.getClanTagColor() + g.getName()));
        }
        if (player.getSelectedClass() != null && player.getSelectedClass() != ClassEnum.NONE) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.info.class", player.getSelectedClass().getName(LanguageAPI.getLanguage(sender))));
            if (player.getPrestigeLevel() > 0) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.info.plevel", player.getLevel(), player.getPrestigeLevel()));
            } else {
                sender.sendMessage(LanguageAPI.translate(sender, "command.info.level", player.getLevel()));
            }
        }
        sender.sendMessage(LanguageAPI.translate(sender, "command.info.gold", player.getGold() / 100d));
        double kd = player.getDeaths() == 0 ? player.getKills() : ((player.getKills() + 0f) / (player.getDeaths() + 0f));
        sender.sendMessage(LanguageAPI.translate(sender, "command.info.kills", player.getKills(), player.getDeaths(), kd));
        sender.sendMessage(LanguageAPI.translate(sender, "line"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.info.help"));
    }
}
