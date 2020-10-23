package me.friwi.arterion.plugin.combat.friendlies;

import me.friwi.arterion.plugin.player.ArterionPlayer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class FriendlyPlayerList {
    protected List<ArterionPlayer> markedPlayers = new LinkedList<>();

    public void addMarkedPlayer(ArterionPlayer player) {
        this.markedPlayers.add(player);
        this.updateScoreboardName(player);
    }

    public void removeMarkedPlayer(ArterionPlayer player) {
        this.markedPlayers.remove(player);
        this.updateScoreboardName(player);
    }

    public boolean isPlayerMarked(ArterionPlayer player) {
        return markedPlayers.contains(player);
    }

    protected void updateScoreboardName(ArterionPlayer player) {
        if (player.getBukkitPlayer().isOnline()) {
            for (ArterionPlayer friendly : getFriendlies()) {
                if (friendly.getPlayerScoreboard() != null) {
                    friendly.getPlayerScoreboard().updatePlayerRelation(player);
                }
            }
        }
    }

    public abstract Collection<ArterionPlayer> getFriendlies();
}
