package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.quest.Quest;
import me.friwi.arterion.plugin.combat.quest.QuestEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QuestVillagerInteractHandler implements VillagerInteractHandler {
    @Override
    public void handleInteract(ArterionPlayer ep) {
        ep.openGui(new ItemGUI(ep, ep.getTranslation("quest.gui.name"), () -> {
            ItemStack[] stacks = new ItemStack[9];
            if (ep.getQuest() == null) {
                stacks[4] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, ep.getTranslation("quest.gui.newquest"));
            } else {
                int price = ArterionPlugin.getInstance().getFormulaManager().QUEST_RETURN_PRICE.evaluateInt();
                stacks[2] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, ep.getTranslation("quest.gui.redeem"));
                stacks[6] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, ep.getTranslation("quest.gui.returnquest"), ep.getTranslation("quest.gui.returnquestprice", price / 100d));
            }
            return stacks;
        }, ((clickType, in) -> {
            ep.closeGui();
            if (ep.getQuest() == null) {
                Quest quest = QuestEnum.getRandomQuest();
                ep.setQuest(quest, succ -> {
                });
                quest.print(ep);
                ep.sendTranslation("quest.gui.commandsuggest");
            } else {
                if (in == 2) {
                    ep.getQuest().checkFinished(ep);
                } else {
                    int price = ArterionPlugin.getInstance().getFormulaManager().QUEST_RETURN_PRICE.evaluateInt();
                    ep.getBagMoneyBearer().transferMoney(price, null, succ -> {
                        if (!succ) {
                            ep.sendTranslation("quest.gui.money", price / 100d);
                            return;
                        }
                        ep.sendTranslation("quest.gui.returned");
                        ep.setQuest(null, succ1 -> {
                        });
                    });
                }
            }
        })));
    }
}
