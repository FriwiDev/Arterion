package me.friwi.arterion.plugin.combat.skill.impl.barbar;

import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.NotForModUser;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;

public class EnforcedArmorHotbarCard extends HotbarCard implements PriorityHotbarCard, NotForModUser {
    EnforcedArmorSkill.EnforcedArmorSkillContainterData data;

    public EnforcedArmorHotbarCard(long duration, EnforcedArmorSkill.EnforcedArmorSkillContainterData data) {
        super(duration);
        this.data = data;
    }

    @Override
    public String getMessage() {
        long remaining = getExpires() - System.currentTimeMillis();
        float percentage = (remaining + 0f) / (duration + 0f);
        if (data.expires < System.currentTimeMillis()) {
            this.setExpires(0);
            percentage = 0;
        }
        return ProgressBar.generate("\2474", percentage, 40);
    }
}
