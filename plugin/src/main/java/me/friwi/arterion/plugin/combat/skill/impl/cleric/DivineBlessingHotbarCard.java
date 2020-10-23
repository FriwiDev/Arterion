package me.friwi.arterion.plugin.combat.skill.impl.cleric;

import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.NotForModUser;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;

public class DivineBlessingHotbarCard extends HotbarCard implements PriorityHotbarCard, NotForModUser {
    long expires;

    public DivineBlessingHotbarCard(long duration) {
        super(duration);
        this.expires = System.currentTimeMillis() + duration;
    }

    @Override
    public String getMessage() {
        long remaining = getExpires() - System.currentTimeMillis();
        float percentage = (remaining + 0f) / (duration + 0f);
        if (expires < System.currentTimeMillis()) {
            this.setExpires(0);
            percentage = 0;
        }
        return ProgressBar.generate("\2474", percentage, 40);
    }
}
