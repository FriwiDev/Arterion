package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LegendaryUnlockCard extends HotbarCard {
    private ArterionPlayer player;
    private boolean first = true;

    public LegendaryUnlockCard(ArterionPlayer player) {
        super(4000);
        this.player = player;
    }

    @Override
    public String getMessage() {
        if (first) {
            first = false;
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
            TitleAPI.send(player, "", player.getTranslation("hotbar.legiunlock.title"), 0, (int) duration / 50, 0);
            LanguageAPI.broadcastMessage("line");
            for (Player p : ArterionPlugin.getOnlinePlayers()) {
                ArterionPlayer ap = ArterionPlayerUtil.get(p);
                ap.sendTranslation("player.legiunlock", player);
            }
            LanguageAPI.broadcastMessage("line");
        }
        return player.getTranslation("hotbar.legiunlock");
    }
}
