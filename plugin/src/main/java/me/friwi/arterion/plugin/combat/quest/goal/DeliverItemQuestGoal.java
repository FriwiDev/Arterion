package me.friwi.arterion.plugin.combat.quest.goal;

import me.friwi.arterion.plugin.combat.quest.QuestGoal;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.nio.ByteBuffer;

public class DeliverItemQuestGoal extends QuestGoal {
    private ItemStack deliver;

    public DeliverItemQuestGoal(ItemStack deliver) {
        this.deliver = deliver;
    }

    @Override
    public boolean isReached(ArterionPlayer p) {
        int available = 0;
        for (ItemStack s : p.getBukkitPlayer().getInventory()) {
            if (s != null && s.isSimilar(deliver)) {
                available += s.getAmount();
                if (available >= deliver.getAmount()) return true;
            }
        }
        return false;
    }

    @Override
    public void consume(ArterionPlayer p) {
        int remaining = deliver.getAmount();
        ItemStack[] stacks = p.getBukkitPlayer().getInventory().getContents();
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null && stacks[i].isSimilar(deliver)) {
                if (stacks[i].getAmount() > remaining) {
                    stacks[i].setAmount(stacks[i].getAmount() - remaining);
                    p.getBukkitPlayer().getInventory().setItem(i, stacks[i]);
                    remaining = 0;
                    break;
                } else {
                    remaining -= stacks[i].getAmount();
                    p.getBukkitPlayer().getInventory().setItem(i, null);
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }
        p.getBukkitPlayer().updateInventory();
    }

    @Override
    public String getDescription(Language lang) {
        return lang.getTranslation("quest.goal.deliveritems").translate(deliver.getAmount(), lang.translateObject(deliver)).getMessage();
    }

    @Override
    public void readFrom(ByteBuffer buffer) {

    }

    @Override
    public void writeTo(ByteBuffer buffer) {

    }

    @Override
    public void onKillEntity(ArterionPlayer p, LivingEntity entity) {

    }

    @Override
    public void onMineBlock(ArterionPlayer p, Block block) {

    }

    @Override
    public void onEarnXP(ArterionPlayer p, int xp) {

    }
}
