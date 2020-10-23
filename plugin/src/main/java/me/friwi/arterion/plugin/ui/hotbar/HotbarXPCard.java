package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.PlayerLevelCalculator;
import me.friwi.arterion.plugin.player.PlayerPrestigeLevelCalculator;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.language.translateables.NumberTranslateable;

public class HotbarXPCard extends MergeableHotbarCard<HotbarXPCard> {
    private ArterionPlayer player;
    private int xp;
    private int level;
    private int max;
    private boolean prestige = false;
    private double alreadyAppliedBoost;

    public HotbarXPCard(ArterionPlayer player, int xp, double alreadyAppliedBoost) {
        super(1500);
        this.player = player;
        this.xp = xp;
        this.level = player.getLevel();
        if (level >= PlayerLevelCalculator.getMaxLevel()) {
            prestige = true;
            this.level = player.getPrestigeLevel();
            this.max = PlayerPrestigeLevelCalculator.getXPNeededForLevel(level);
        } else {
            this.max = PlayerLevelCalculator.getXPNeededForLevel(level);
        }
        this.alreadyAppliedBoost = alreadyAppliedBoost;
    }

    @Override
    public String getMessage() {
        int current = 0;
        if (prestige) {
            if (player.getPrestigeLevel() > level) current = max;
            else if (player.getPrestigeLevel() == level)
                current = PlayerPrestigeLevelCalculator.getCurrentOverflowFromXP(player.getPrestigeXp());
        } else {
            if (player.getLevel() > level) current = max;
            else if (player.getLevel() == level)
                current = PlayerLevelCalculator.getCurrentOverflowFromXP(player.getClassXp());
        }
        if (xp >= 0)
            return player.getTranslation("hotbar.xpgain", xp, ProgressBar.generate("\2472", (current + 0f) / (max + 0f), 40), current, max) + (alreadyAppliedBoost != 1 ? " §4[x" + NumberTranslateable.formatNumber(alreadyAppliedBoost) + "]" : "");
        else
            return player.getTranslation("hotbar.xploose", xp, ProgressBar.generate("\2474", (current + 0f) / (max + 0f), 40), current, max);
    }

    @Override
    public void mergeWithCard(HotbarXPCard card) {
        this.xp += card.xp;
    }

    @Override
    public boolean canBeMerged(HotbarCard other) {
        if (super.canBeMerged(other)) {
            return level == ((HotbarXPCard) other).level;
        }
        return false;
    }
}
