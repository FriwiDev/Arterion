package me.friwi.arterion.plugin.combat.group;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupSystem {
    private ArterionPlugin plugin;
    private List<Group> groupList = new CopyOnWriteArrayList<>();

    public GroupSystem(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlayerQuit(ArterionPlayer player) {
        Group g = getGroup(player);
        if (g != null) g.removePlayer(player, false);
    }

    public Group getGroup(ArterionPlayer player) {
        for (Group g : groupList) {
            if (g.isMember(player)) return g;
        }
        return null;
    }

    public Group createGroup(ArterionPlayer player) {
        if (player.getGuild() != null) {
            player.sendTranslation("guild.alreadyinguild");
            return null;
        }
        Group g = new Group(plugin, player);
        groupList.add(g);
        return g;
    }

    public void disbandGroup(Group group) {
        groupList.remove(group);
        group.onDisband();
    }

    public void openGroupDialog(ArterionPlayer player) {
        if (player.getGuild() != null) {
            player.sendTranslation("guild.alreadyinguild");
            return;
        }
        Group g = getGroup(player);
        if (g == null) g = createGroup(player);
        if (g == null) return; //Failed to create group
        g.openGroupDialog(player);
    }

    public void inviteGroup(ArterionPlayer player, String name) {
        if (player.getGuild() != null) {
            player.sendTranslation("guild.alreadyinguild");
            return;
        }
        Group g = getGroup(player);
        if (g == null) g = createGroup(player);
        if (g == null) return; //Failed to create group
        g.invite(player, name);
    }
}
