package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.chat.FormattedChat;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.entity.Player;

public class HotbarMessageUI {
    private ArterionPlugin plugin;

    public HotbarMessageUI(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.plugin.getSchedulers().getMainScheduler().executeInMyCircleTimer(new InternalTask() {
            @Override
            public void run() {
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    ArterionPlayer ep = ArterionPlayerUtil.get(p);
                    if (ep != null) updatePlayer(ep);
                }
            }
        }, 40, 40);
    }

    private void sendActionBar(Player player, String message) {
        FormattedChat.sendActionBar(player, message);
    }

    public void updatePlayer(ArterionPlayer arterionPlayer) {
        this.sendActionBar(arterionPlayer.getBukkitPlayer(), arterionPlayer.getHotbarMessage());
    }
}
