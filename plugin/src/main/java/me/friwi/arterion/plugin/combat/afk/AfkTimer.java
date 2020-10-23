package me.friwi.arterion.plugin.combat.afk;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class AfkTimer {
    public static void beginProcessAfkPlayers() {
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            boolean full = false;

            @Override
            public void run() {
                boolean nowFull = ArterionPlugin.getOnlinePlayers().size() >= (Bukkit.getMaxPlayers() * 0.95);
                if (!full && nowFull) {
                    full = true;
                    for (Player p : ArterionPlugin.getOnlinePlayers()) {
                        ArterionPlayer ap = ArterionPlayerUtil.get(p);
                        ap.resetAfkTime();
                    }
                } else if (!nowFull) {
                    full = false;
                }
                if (!full) {
                    return;
                }
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                    if (!ap.getRank().isTeam()) {
                        long afkTime = ap.getAfkTime();
                        long remaining = (5 * 60 * 1000) - afkTime;
                        if (remaining < 0) {
                            p.kickPlayer(ap.getTranslation("afktimer.kick"));
                        } else if (remaining < 60 * 1000) {
                            long seconds = remaining / 1000;
                            if (seconds == 60 || seconds == 50 || seconds == 40 || seconds == 30 || seconds == 20 || seconds == 10 || seconds < 5) {
                                ap.sendTranslation("afktimer.warning");
                                p.playSound(p.getLocation(), Sound.NOTE_PIANO, 1f, 1f);
                            }
                        }
                    }
                }
            }
        }, 20, 20);
    }
}
