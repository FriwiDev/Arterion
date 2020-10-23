package me.friwi.arterion.plugin.combat.skill.card;

import me.friwi.arterion.plugin.combat.skill.ActiveSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;

public class NotHereCard extends HotbarCard implements PriorityHotbarCard {
    private ActiveSkill skill;
    private ArterionPlayer player;

    public NotHereCard(ArterionPlayer player, ActiveSkill skill) {
        super(2000);
        this.skill = skill;
        this.player = player;
    }

    @Override
    public String getMessage() {
        return player.getTranslation("skill.nothere.hotbar", skill.getName(player));
    }
}
