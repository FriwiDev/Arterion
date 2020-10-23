package me.friwi.arterion.plugin.ui.invite;

import java.util.UUID;

public class Invitation {
    private UUID code;
    private Object inviter;
    private UUID player;
    private InvitationHandler invitationHandler;

    public Invitation(Object inviter, UUID code, UUID player, InvitationHandler invitationHandler) {
        this.inviter = inviter;
        this.code = code;
        this.player = player;
        this.invitationHandler = invitationHandler;
    }

    public Object getInviter() {
        return inviter;
    }

    public UUID getCode() {
        return code;
    }

    public UUID getPlayer() {
        return player;
    }

    public InvitationHandler getInvitationHandler() {
        return invitationHandler;
    }
}
