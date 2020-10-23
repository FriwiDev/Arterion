package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {
    private ArterionPlugin plugin;

    public AsyncPlayerChatListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent evt) {
        evt.setCancelled(true);

        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());
        if (ep == null) {
            evt.getPlayer().sendMessage("\247cError while processing your chat message. Please leave and log in again!");
            return;
        }
        if (ep.getOpenGui() != null) {
            if (ep.getOpenGui().onChat(evt.getMessage())) {
                return;
            }
        }
        plugin.getChatSystem().dispatchMessage(ep, evt.getMessage());
    }
}
