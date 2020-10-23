package me.friwi.arterion.plugin.listener;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.friwi.arterion.plugin.player.reward.RewardUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerVoteListener implements Listener {
    @EventHandler
    public void onPlayerVote(VotifierEvent evt) {
        RewardUtil.onVote(evt.getVote().getUsername());
    }
}
