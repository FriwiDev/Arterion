package me.friwi.arterion.plugin.combat.friendlies;

import me.friwi.arterion.plugin.combat.group.Group;
import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ArterionPlayerFriendlyPlayerList extends FriendlyPlayerList {
    private ArterionPlayer player;
    private Map<UUID, Group> markedPlayerMap = new HashMap<>();
    private Map<UUID, Team> markedPlayerMapTeam = new HashMap<>();

    public ArterionPlayerFriendlyPlayerList(ArterionPlayer player) {
        this.player = player;
    }

    @Override
    public void addMarkedPlayer(ArterionPlayer player) {
        super.addMarkedPlayer(player);
        if (this.player.getTeam() != null) {
            this.player.getTeam().getFriendlyPlayerList().addMarkedPlayer(player);
            markedPlayerMapTeam.put(player.getBukkitPlayer().getUniqueId(), this.player.getTeam());
        } else if (this.player.getGroup() != null) { //Do not add marked players for group when player is in a team
            this.player.getGroup().getFriendlyPlayerList().addMarkedPlayer(player);
            markedPlayerMap.put(player.getBukkitPlayer().getUniqueId(), this.player.getGroup());
        }
    }

    @Override
    public void removeMarkedPlayer(ArterionPlayer player) {
        super.removeMarkedPlayer(player);
        Group removeFrom = markedPlayerMap.remove(player.getBukkitPlayer().getUniqueId());
        if (removeFrom != null) removeFrom.getFriendlyPlayerList().removeMarkedPlayer(player);
        Team removeFromTeam = markedPlayerMapTeam.remove(player.getBukkitPlayer().getUniqueId());
        if (removeFromTeam != null) removeFromTeam.getFriendlyPlayerList().removeMarkedPlayer(player);
    }

    @Override
    public boolean isPlayerMarked(ArterionPlayer player) {
        return super.isPlayerMarked(player)
                || (this.player.getTeam() == null && this.player.getGroup() != null && this.player.getGroup().getFriendlyPlayerList().isPlayerMarked(player))
                || (this.player.getTeam() != null && this.player.getTeam().getFriendlyPlayerList().isPlayerMarked(player));
    }

    @Override
    public Collection<ArterionPlayer> getFriendlies() {
        if (player.getTeam() != null) return player.getTeam().getMembers();
        List<ArterionPlayer> ret = new LinkedList<>();
        if (player.getGroup() != null) {
            ret.addAll(player.getGroup().getMembers());
        } else {
            ret.add(player);
        }
        if (player.getRoomMate() != null) {
            Player x = Bukkit.getPlayer(player.getRoomMate());
            if (x != null && x.isOnline()) {
                ret.add(ArterionPlayerUtil.get(x));
            }
        }
        return ret;
    }
}
