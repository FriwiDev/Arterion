package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.permissions.TimeUnit;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("stop")
public class StopCommand extends BaseCommand {
    @Default
    @CommandCompletion("@nothing @timeunit")
    @Syntax("<amount> <MINUTES|HOURS|DAYS|YEARS> [reason]")
    public void setPlayerRank(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.DEVELOPER)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 2) {
            this.help(sender);
            return;
        }

        String amount = args[0];
        String unit = args[1];

        TimeUnit r = null;
        try {
            r = TimeUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.stop.notfound"));
            return;
        }
        int time = 0;
        try {
            time = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.stop.nonumber"));
            return;
        }

        if (time <= 0) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.stop.nonumber"));
            return;
        }

        long expires = System.currentTimeMillis() + time * r.getSingleDuration();
        String reason = "";
        if (args.length == 2) {
            reason = "Scheduled restart";
        } else {
            for (int i = 2; i < args.length; i++) {
                reason += args[i] + (i < args.length - 1 ? " " : "");
            }
        }

        ArterionPlugin.getInstance().shutdownReason = reason;
        ArterionPlugin.getInstance().shutdownTime = expires;
        long ticks = time * r.getSingleDuration() / 50;

        String finalReason = reason;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            long tick = 0;

            @Override
            public void run() {
                long remaining = ticks - tick;
                if (remaining > 0) {
                    if (remaining % (5 * 60 * 20) == 0 || remaining == (3 * 60 * 20) || remaining == (2 * 60 * 20)) {
                        LanguageAPI.broadcastMessage("line");
                        LanguageAPI.broadcastMessage("command.stop.warn_minutes", remaining / 60 / 20, finalReason);
                        LanguageAPI.broadcastMessage("line");
                    } else if (remaining <= (60 * 20) && remaining % (10 * 20) == 0) {
                        LanguageAPI.broadcastMessage("line");
                        LanguageAPI.broadcastMessage("command.stop.warn_seconds", remaining / 20, finalReason);
                        if (remaining == 30 * 20) {
                            ArterionPlugin.getInstance().getCombatLoggingHandler().setDisableNPCSpawning(true);
                            LanguageAPI.broadcastMessage("command.stop.combatlog_disable");
                        }
                        LanguageAPI.broadcastMessage("line");
                    }
                } else {
                    if (remaining == 0) {
                        for (Player p : ArterionPlugin.getOnlinePlayers()) {
                            ArterionPlayer lp = ArterionPlayerUtil.get(p);
                            p.kickPlayer(lp.getTranslation("command.stop.kick", finalReason));
                        }
                    } else if (remaining == -5 * 20) {
                        cancel();
                        Bukkit.shutdown();
                    }
                }
                tick += 20;
            }
        }, 20, 20);

        sender.sendMessage(LanguageAPI.translate(sender, "command.stop.success"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.stop.help"));
    }
}
