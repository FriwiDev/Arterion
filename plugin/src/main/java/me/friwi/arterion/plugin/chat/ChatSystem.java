package me.friwi.arterion.plugin.chat;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.entity.Player;

public class ChatSystem {
    private ArterionPlugin plugin;

    public ChatSystem(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void dispatchMessage(ArterionPlayer player, String msg) {
        player.checkNotMuted(() -> {
            if (player.getChatChannel() == ChatChannel.GUILD) {
                if (player.getGuild() != null) {
                    String tag = player.getGuild().getClanTagColor().toString() + player.getGuild().getTag();
                    for (ArterionPlayer ep : player.getGuild().getOnlineMembers()) {
                        if (!player.getRank().isHigherTeam() && ep.getPersistenceHolder().getIgnoredPlayers().contains(player.getUUID()))
                            continue;
                        ep.sendTranslation("chat.channel." + ChatChannel.GUILD.name().toLowerCase() + ".msg", player, msg, tag);
                    }
                } else {
                    player.setChatChannel(ChatChannel.GLOBAL);
                }
                return;
            }

            if (player.getChatChannel() == ChatChannel.GROUP) {
                if (player.getGroup() != null) {
                    for (ArterionPlayer ep : player.getGroup().getMembers()) {
                        if (!player.getRank().isHigherTeam() && ep.getPersistenceHolder().getIgnoredPlayers().contains(player.getUUID()))
                            continue;
                        ep.sendTranslation("chat.channel." + ChatChannel.GROUP.name().toLowerCase() + ".msg", player, msg);
                    }
                    ArterionPlayer ep = player.getGroup().getLeader();
                    boolean ignored = !player.getRank().isHigherTeam() && ep.getPersistenceHolder().getIgnoredPlayers().contains(player.getUUID());
                    if (!ignored)
                        ep.sendTranslation("chat.channel." + ChatChannel.GROUP.name().toLowerCase() + ".msg", player, msg);
                } else {
                    player.setChatChannel(ChatChannel.GLOBAL);
                }
                return;
            }

            if (player.getChatChannel() == ChatChannel.TEAM) {
                if (!player.getRank().isTeam()) player.setChatChannel(ChatChannel.GLOBAL);
                String tag = "";
                if (player.getGuild() != null)
                    tag = player.getGuild().getClanTagColor().toString() + player.getGuild().getTag();
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    ArterionPlayer ep = ArterionPlayerUtil.get(p);
                    if (ep != null) {
                        if (!ep.getRank().isTeam()) continue;
                        ep.sendTranslation("chat.channel." + player.getChatChannel().name().toLowerCase() + ".msg", player, msg, tag);
                    }
                }
                return;
            }


            String tag = "";
            if (player.getGuild() != null)
                tag = player.getGuild().getClanTagColor().toString() + player.getGuild().getTag();
            for (Player p : ArterionPlugin.getOnlinePlayers()) {
                ArterionPlayer ep = ArterionPlayerUtil.get(p);
                if (ep != null) {
                    if (!player.getRank().isHigherTeam() && ep.getPersistenceHolder().getIgnoredPlayers().contains(player.getUUID()))
                        continue;
                    if (player.getChatChannel() == ChatChannel.SUPPORT) {
                        if (!ep.getRank().isTeam() && ep.getChatChannel() != ChatChannel.SUPPORT) continue;
                    }
                    if (player.getChatChannel() == ChatChannel.LOCAL) {
                        if (!player.getBukkitPlayer().getLocation().getWorld().equals(p.getWorld()))
                            continue;
                        if (player.getBukkitPlayer().getLocation().distance(p.getLocation()) > 120)
                            continue;
                    }
                    ep.sendTranslation("chat.channel." + player.getChatChannel().name().toLowerCase() + ".msg", player, msg, tag);
                }
            }
        });

    }
}
