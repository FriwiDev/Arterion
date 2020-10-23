package me.friwi.arterion.plugin.ui.invite;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.chat.FormattedChat;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InvitationSystem {
    private ArterionPlugin plugin;
    private Map<UUID, Invitation> invitationMap = new ConcurrentHashMap<>();

    public InvitationSystem(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void invite(ArterionPlayer inviter, Object invitedTo, ArterionPlayer player, Runnable printDescription, InvitationHandler handler) {
        for (Map.Entry<UUID, Invitation> entry : invitationMap.entrySet()) {
            if (entry.getValue().getInviter().equals(invitedTo) && entry.getValue().getPlayer().equals(player.getBukkitPlayer().getUniqueId())) {
                inviter.sendTranslation("invitation.alreadyinvited");
                return;
            }
        }
        UUID uuid = generateNewUUid();
        invitationMap.put(uuid, new Invitation(invitedTo, uuid, player.getBukkitPlayer().getUniqueId(), handler));
        player.sendTranslation("line");
        printDescription.run();
        FormattedChat.sendFormattedChat(player.getBukkitPlayer(), player.getTranslation("invitation.agreeordeny", uuid.toString()));
        player.sendTranslation("line");
        plugin.getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
            @Override
            public void run() {
                if (invitationMap.remove(uuid) != null) {
                    handler.onTimeout(player);
                }
            }
        }, plugin.getFormulaManager().PLAYER_INVITE_TIMEOUT.evaluateInt());
        inviter.sendTranslation("invitation.invited", player);
    }

    public Invitation onAction(UUID uuid, boolean accept, ArterionPlayer player) {
        Invitation inv = invitationMap.remove(uuid);
        if (inv != null) {
            if (!inv.getPlayer().equals(player.getBukkitPlayer().getUniqueId())) {
                //Someone being sneaky
                invitationMap.put(uuid, inv);
                return null;
            }
            if (accept) {
                inv.getInvitationHandler().onAccept(player);
            } else {
                inv.getInvitationHandler().onDeny(player);
            }
        }
        return inv;
    }

    private UUID generateNewUUid() {
        UUID gen;
        while (invitationMap.containsKey(gen = UUID.randomUUID())) ;
        return gen;
    }
}
