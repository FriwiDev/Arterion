package me.friwi.arterion.plugin.combat.quest.reward;

import me.friwi.arterion.plugin.combat.quest.QuestReward;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.inventory.ItemStack;

public class ItemQuestReward extends QuestReward {
    private ItemStack stack;

    public ItemQuestReward(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void give(ArterionPlayer p) {
        ItemStack give = stack.clone();
        int maxStack = give.getMaxStackSize();
        if (maxStack < 0) maxStack = 64;
        while (give.getAmount() > maxStack) {
            give.setAmount(give.getAmount() - maxStack);
            ItemStack add = give.clone();
            add.setAmount(maxStack);
            p.getBukkitPlayer().getInventory().addItem(add);
        }
        p.getBukkitPlayer().getInventory().addItem(give);
        p.getBukkitPlayer().updateInventory();
        p.sendTranslation("quest.reward.item", stack.getAmount(), p.getLanguage().translateObject(stack));
    }

    @Override
    public String getDescription(Language lang) {
        return lang.getTranslation("quest.reward.item").translate(stack.getAmount(), lang.translateObject(stack)).getMessage();
    }
}
