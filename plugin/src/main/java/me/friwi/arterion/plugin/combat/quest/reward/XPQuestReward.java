package me.friwi.arterion.plugin.combat.quest.reward;

import me.friwi.arterion.plugin.combat.quest.QuestReward;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;

public class XPQuestReward extends QuestReward {
    private int amount;

    public XPQuestReward(int amount) {
        this.amount = amount;
    }

    @Override
    public void give(ArterionPlayer p) {
        p.addXP(amount);
        p.sendTranslation("quest.reward.xp", amount);
    }

    @Override
    public String getDescription(Language lang) {
        return lang.getTranslation("quest.reward.xp").translate(amount).getMessage();
    }
}
