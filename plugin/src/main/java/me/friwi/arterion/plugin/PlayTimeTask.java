package me.friwi.arterion.plugin;

import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.entity.Player;

public class PlayTimeTask extends InternalTask {
    @Override
    public void run() {
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayerUtil.get(p).trackStatistic(StatType.ONLINE_MINUTES, 0, v -> v + 1);
        }
        for (Guild g : ArterionPlugin.getInstance().getGuildManager().getGuilds()) {
            if (g.getDeleted() == DatabaseGuild.NOT_DELETED) {
                if (g.getOnlineMemberCount() > 0) {
                    g.trackStatistic(StatType.GUILD_ONLINE_MINUTES, 0, v -> v + g.getOnlineMemberCount());
                }
            }
        }
    }
}
