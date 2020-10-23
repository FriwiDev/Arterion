package me.friwi.arterion.plugin.combat.friendlies;

import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;

import java.util.Collection;

public class GuildFriendlyPlayerList extends FriendlyPlayerList {
    private Guild guild;

    public GuildFriendlyPlayerList(Guild guild) {
        this.guild = guild;
    }

    @Override
    public Collection<ArterionPlayer> getFriendlies() {
        return guild.getOnlineMembers();
    }
}
