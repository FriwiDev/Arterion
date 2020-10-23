package me.friwi.arterion.plugin.combat.skill.card;

import me.friwi.arterion.plugin.combat.skill.ActiveSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;
import me.friwi.arterion.plugin.ui.title.TitleAPI;

public class SkillFailCard extends HotbarCard implements PriorityHotbarCard {
    private ActiveSkill skill;
    private String msg;
    private ArterionPlayer player;
    private boolean first = true;

    public SkillFailCard(ArterionPlayer player, String msg, ActiveSkill skill) {
        super(2000);
        this.skill = skill;
        this.msg = player.getTranslation(msg, skill.getName(player));
        this.player = player;
    }

    @Override
    public String getMessage() {
        if (first) {
            TitleAPI.send(player, "", msg, 0, (int) (duration / 50), 0);
            first = false;
        }
        return msg;
    }
}
