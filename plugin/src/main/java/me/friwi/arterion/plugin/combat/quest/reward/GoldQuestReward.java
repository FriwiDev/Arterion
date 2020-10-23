package me.friwi.arterion.plugin.combat.quest.reward;

import me.friwi.arterion.plugin.combat.quest.QuestReward;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;

public class GoldQuestReward extends QuestReward {
    private int amount;

    public GoldQuestReward(int amount) {
        this.amount = amount;
    }

    @Override
    public void give(ArterionPlayer p) {
        p.getBagMoneyBearer().addMoney(amount, succ -> {
        });
        p.sendTranslation("quest.reward.gold", amount / 100d);
    }

    @Override
    public String getDescription(Language lang) {
        return lang.getTranslation("quest.reward.gold").translate(amount / 100d).getMessage();
    }
}
