package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent evt) {
        ArterionPlayer player = ArterionPlayerUtil.get(evt.getPlayer());
        if (player != null) player.getBAC().disableMovementChecksFor(500);
    }
}
