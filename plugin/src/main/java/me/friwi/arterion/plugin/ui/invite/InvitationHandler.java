package me.friwi.arterion.plugin.ui.invite;

import me.friwi.arterion.plugin.player.ArterionPlayer;

public interface InvitationHandler {
    void onAccept(ArterionPlayer player);

    void onTimeout(ArterionPlayer player);

    void onDeny(ArterionPlayer player);
}
