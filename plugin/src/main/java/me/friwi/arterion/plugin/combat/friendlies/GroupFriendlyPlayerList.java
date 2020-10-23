package me.friwi.arterion.plugin.combat.friendlies;

import me.friwi.arterion.plugin.combat.group.Group;
import me.friwi.arterion.plugin.player.ArterionPlayer;

import java.util.Collection;
import java.util.LinkedList;

public class GroupFriendlyPlayerList extends FriendlyPlayerList {
    private Group group;

    public GroupFriendlyPlayerList(Group group) {
        this.group = group;
    }

    @Override
    public Collection<ArterionPlayer> getFriendlies() {
        LinkedList<ArterionPlayer> copy = new LinkedList<>();
        copy.addAll(group.getMembers());
        copy.add(group.getLeader());
        return copy;
    }
}
