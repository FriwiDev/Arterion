package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.PlayerLevelCalculator;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HotbarLevelupCard extends MergeableHotbarCard {
    private ArterionPlayer player;
    private int level;
    private boolean first = true;

    public HotbarLevelupCard(ArterionPlayer player, int level) {
        super(4000);
        this.player = player;
        this.level = level;
    }

    @Override
    public String getMessage() {
        if (first) {
            first = false;
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
            TitleAPI.send(player, "", player.getTranslation("hotbar.levelup.title", level), 0, (int) duration / 50, 0);
            if (level == PlayerLevelCalculator.getMaxLevel()) {
                LanguageAPI.broadcastMessage("line");
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                    ap.sendTranslation("player.levelmax", player, ap.getTranslation("class." + player.getSelectedClass().name().toLowerCase()), level);
                }
                LanguageAPI.broadcastMessage("line");
            }
        }
        return player.getTranslation("hotbar.levelup", level);
    }

    @Override
    public void mergeWithCard(MergeableHotbarCard card) {

    }
}
