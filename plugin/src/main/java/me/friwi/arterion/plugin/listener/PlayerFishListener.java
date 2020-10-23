package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.jobs.JobActivityHandler;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFishListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerFishListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent evt) {
        //Jobs
        if (evt.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            JobActivityHandler.onCatchFish(ArterionPlayerUtil.get(evt.getPlayer()));
        }
    }
}
