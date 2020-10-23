package me.friwi.arterion.plugin.combat.skill.impl.mage;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;

public class ChainLightningHitCard extends HotbarCard implements PriorityHotbarCard {
    private String msg;
    private ArterionPlayer player;

    public ChainLightningHitCard(ArterionPlayer player, int amount, int possible) {
        super(2000);
        float fraction = (amount + 0f) / (possible + 0f);
        String color = "\247a";
        if (fraction > 0.25) {
            color = "\247e";
        }
        if (fraction > 0.5) {
            color = "\247c";
        }
        if (fraction > 0.75) {
            color = "\2474";
        }
        this.msg = player.getTranslation("skill.chain_lightning.hit.hotbar", color + amount);
        this.player = player;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
