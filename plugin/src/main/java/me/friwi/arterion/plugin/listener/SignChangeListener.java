package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeListener implements Listener {
    private ArterionPlugin plugin;

    public SignChangeListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent evt) {
        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());

        if (ep.getRank().isHigherOrEqualThan(Rank.PREMIUM)) {
            for (int i = 0; i < evt.getLines().length; i++) {
                evt.getLines()[i] = ChatColor.translateAlternateColorCodes('&', evt.getLines()[i]).replace("&&", "&");
            }
        }
    }
}
