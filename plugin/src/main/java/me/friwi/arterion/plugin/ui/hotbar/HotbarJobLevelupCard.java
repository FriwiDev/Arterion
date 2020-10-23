package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.PlayerJobLevelCalculator;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HotbarJobLevelupCard extends MergeableHotbarCard {
    private ArterionPlayer player;
    private int level;
    private JobEnum job;
    private boolean first = true;

    public HotbarJobLevelupCard(ArterionPlayer player, JobEnum job, int level) {
        super(4000);
        this.player = player;
        this.level = level;
        this.job = job;
    }

    @Override
    public String getMessage() {
        if (first) {
            first = false;
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
            TitleAPI.send(player, "", player.getTranslation("hotbar.joblevelup.title", job.getName(player.getLanguage()), level), 0, (int) duration / 50, 0);
            if (level == PlayerJobLevelCalculator.getMaxLevel()) {
                LanguageAPI.broadcastMessage("line");
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                    ap.sendTranslation("player.joblevelmax", player, job.getName(ap.getLanguage()), level);
                }
                LanguageAPI.broadcastMessage("line");
            }
        }
        return player.getTranslation("hotbar.joblevelup", job.getName(player.getLanguage()), level);
    }

    @Override
    public void mergeWithCard(MergeableHotbarCard card) {

    }
}
