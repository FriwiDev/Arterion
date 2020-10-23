package me.friwi.arterion.plugin.combat.friendlies;

import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.player.ArterionPlayer;

import java.util.Collection;

public class TeamFriendlyPlayerList extends FriendlyPlayerList {
    private Team team;

    public TeamFriendlyPlayerList(Team team) {
        this.team = team;
    }

    @Override
    public Collection<ArterionPlayer> getFriendlies() {
        return team.getMembers();
    }
}
