package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.PlayerJobLevelCalculator;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;

public class HotbarJobXPCard extends MergeableHotbarCard<HotbarJobXPCard> {
    private ArterionPlayer player;
    private int xp;
    private int level;
    private int max;
    private JobEnum job;

    public HotbarJobXPCard(ArterionPlayer player, int level, int xp, JobEnum job) {
        super(1500);
        this.player = player;
        this.xp = xp;
        this.job = job;
        this.level = level;
        this.max = PlayerJobLevelCalculator.getXPNeededForLevel(level);
    }

    @Override
    public String getMessage() {
        int current = 0;
        if (player.getJobLevel(job) > level) current = max;
        else if (player.getJobLevel(job) == level)
            current = PlayerJobLevelCalculator.getCurrentOverflowFromXP(player.getJobXp(job));
        if (xp >= 0)
            return player.getTranslation("hotbar.jobxpgain", xp, ProgressBar.generate("\2472", (current + 0f) / (max + 0f), 40), current, max, job.getName(player.getLanguage()));
        else
            return player.getTranslation("hotbar.jobxploose", xp, ProgressBar.generate("\2474", (current + 0f) / (max + 0f), 40), current, max, job.getName(player.getLanguage()));
    }

    @Override
    public void mergeWithCard(HotbarJobXPCard card) {
        this.xp += card.xp;
    }

    @Override
    public boolean canBeMerged(HotbarCard other) {
        if (super.canBeMerged(other)) {
            return level == ((HotbarJobXPCard) other).level && job == ((HotbarJobXPCard) other).job;
        }
        return false;
    }
}
