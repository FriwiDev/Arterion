package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.player.ArterionPlayer;

public class HotbarMoneyCard extends MergeableHotbarCard<HotbarMoneyCard> {
    private ArterionPlayer player;
    private long money;

    public HotbarMoneyCard(ArterionPlayer player, long money) {
        super(1500);
        this.player = player;
        this.money = money;
    }

    @Override
    public String getMessage() {
        if (money >= 0) return player.getTranslation("hotbar.moneygain", money / 100f);
        else return player.getTranslation("hotbar.moneyloose", money / 100f);
    }

    @Override
    public void mergeWithCard(HotbarMoneyCard card) {
        this.money += card.money;
    }
}
