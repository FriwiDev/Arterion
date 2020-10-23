package me.friwi.arterion.plugin.combat.skill.card;

import me.friwi.arterion.plugin.combat.skill.ActiveSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;

public class CooldownCard extends HotbarCard implements PriorityHotbarCard {
    private ActiveSkill skill;
    private ArterionPlayer player;

    public CooldownCard(ArterionPlayer player, ActiveSkill skill) {
        super(2000);
        this.skill = skill;
        this.player = player;
    }

    @Override
    public String getMessage() {
        int cd = skill.getCooldown(player);
        if (cd > 0) {
            return player.getTranslation("skill.cooldown.hotbar", skill.getName(player), cd);
        } else {
            int mana = skill.getMana(player) - player.getMana();
            if (mana > 0) {
                return player.getTranslation("skill.mana.hotbar", skill.getName(player), mana);
            } else {
                return player.getTranslation("skill.mana.ready", skill.getName(player));
            }
        }
    }
}
