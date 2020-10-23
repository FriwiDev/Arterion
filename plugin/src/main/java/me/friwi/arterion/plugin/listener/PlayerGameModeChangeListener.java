package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class PlayerGameModeChangeListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerGameModeChangeListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent evt) {
        //Update interfaces
        plugin.getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
            @Override
            public void run() {
                ArterionPlayerUtil.get(evt.getPlayer()).getPlayerScoreboard().updateAllPlayerRelations();
            }
        }, 1);
    }
}
