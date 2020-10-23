package me.friwi.arterion.plugin.combat.team;

import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerList;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerListProvider;
import me.friwi.arterion.plugin.combat.friendlies.TeamFriendlyPlayerList;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Team implements FriendlyPlayerListProvider, Iterable<ArterionPlayer> {
    private FriendlyPlayerList friendlyPlayerList;
    private List<ArterionPlayer> members = new CopyOnWriteArrayList<>();
    private Set<ArterionPlayer> allFormerMembers = new HashSet<>();
    private ChatColor color;

    public Team(ChatColor color) {
        this.color = color;
        friendlyPlayerList = new TeamFriendlyPlayerList(this);
    }

    public void addMember(ArterionPlayer player) {
        if (player == null) return;
        if (!members.contains(player)) members.add(player);
        allFormerMembers.add(player);
        player.setTeam(this);
        updatePlayerTeamAffiliation(player);
    }

    public void removeMember(ArterionPlayer player) {
        if (player == null) return;
        members.remove(player);
        player.setTeam(null);
        updatePlayerTeamAffiliation(player);
    }

    public void disband() {
        Iterator<ArterionPlayer> it = members.iterator();
        while (it.hasNext()) {
            ArterionPlayer p = it.next();
            p.setTeam(null);
            updatePlayerTeamAffiliation(p);
        }
        members.clear();
    }

    public void updatePlayerTeamAffiliation(ArterionPlayer p) {
        p.getPlayerScoreboard().updateAllPlayerRelations();
    }

    @Override
    public FriendlyPlayerList getFriendlyPlayerList() {
        return friendlyPlayerList;
    }

    public List<ArterionPlayer> getMembers() {
        return members;
    }

    public void sendTranslation(String key, Object... values) {
        for (ArterionPlayer member : members) member.sendTranslation(key, values);
    }

    public void sendMessage(String msg) {
        for (ArterionPlayer member : members) member.sendMessage(msg);
    }

    public ChatColor getColor() {
        return color;
    }

    public ArterionPlayer getMember(UUID uuid) {
        for (ArterionPlayer member : members) {
            if (member.getBukkitPlayer().getUniqueId().equals(uuid)) return member;
        }
        return null;
    }

    public void setObjective(Objective objective) {
        for (ArterionPlayer player : getMembers()) {
            player.getSkillSlots().setObjective(objective, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        }
    }

    public boolean hasMember(ArterionPlayer player) {
        return getMember(player.getBukkitPlayer().getUniqueId()) != null;
    }

    public Set<ArterionPlayer> getAllFormerMembers() {
        return allFormerMembers;
    }

    public ArterionPlayer getFormerMember(UUID uuid) {
        for (ArterionPlayer member : allFormerMembers) {
            if (member.getBukkitPlayer().getUniqueId().equals(uuid)) return member;
        }
        return null;
    }

    public boolean hasFormerMember(ArterionPlayer player) {
        return getFormerMember(player.getBukkitPlayer().getUniqueId()) != null;
    }

    @NotNull
    @Override
    public Iterator<ArterionPlayer> iterator() {
        return members.iterator();
    }

    @Override
    public void forEach(Consumer<? super ArterionPlayer> consumer) {
        members.forEach(consumer);
    }

    @Override
    public Spliterator<ArterionPlayer> spliterator() {
        return members.spliterator();
    }
}
