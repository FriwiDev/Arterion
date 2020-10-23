package me.friwi.arterion.plugin.ui.toplist;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;

public class TopListRefresher {
    public static void beginRefreshing() {
        PlayerTopList players = new PlayerTopList(10, Bukkit.getWorlds().get(0).getBlockAt(16, 82, -11), BlockFace.SOUTH);
        GuildTopList guilds = new GuildTopList(3, Bukkit.getWorlds().get(0).getBlockAt(6, 82, -26), BlockFace.EAST);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                players.refreshTopList();
                guilds.refreshTopList();
            }
        }, 20 * 60 * 5, 20 * 60 * 5);
    }
}
