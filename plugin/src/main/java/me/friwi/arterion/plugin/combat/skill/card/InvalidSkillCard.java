package me.friwi.arterion.plugin.combat.skill.card;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;
import me.friwi.arterion.plugin.ui.title.TitleAPI;

public class InvalidSkillCard extends HotbarCard implements PriorityHotbarCard {
    private ArterionPlayer player;
    private boolean first = true;

    public InvalidSkillCard(ArterionPlayer player) {
        super(4000);
        this.player = player;
    }

    @Override
    public String getMessage() {
        if (first) {
            TitleAPI.send(player, "", player.getTranslation("skill.noexist.title"), 0, (int) (duration / 50), 0);
            first = false;
        }
        return player.getTranslation("skill.noexist.hotbar");
    }
}
