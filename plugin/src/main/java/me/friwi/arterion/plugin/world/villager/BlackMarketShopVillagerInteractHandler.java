package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.BlackMarketItem;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import org.bukkit.inventory.ItemStack;

public class BlackMarketShopVillagerInteractHandler implements VillagerInteractHandler {
    @Override
    public void handleInteract(ArterionPlayer ep) {
        boolean[] inBuy = new boolean[]{false};
        ep.openGui(new ItemGUI(ep, ep.getTranslation("black_market.shop.name"), () -> {
            ItemStack[] stacks = new ItemStack[9];
            stacks[4] = new MorgothDungeonKeyItem().setShowPrice(true).toItemStack();
            return stacks;
        }, ((clickType, in) -> {
            if (inBuy[0]) return;
            inBuy[0] = true;
            BlackMarketItem clickedItem = null;
            switch (in) {
                case 4:
                    clickedItem = new MorgothDungeonKeyItem();
                    break;
            }
            if (clickedItem == null) {
                inBuy[0] = false;
                return;
            }
            ItemStack give = clickedItem.toItemStack();
            int slot = -1;
            ItemStack[] contents = ep.getBukkitPlayer().getInventory().getContents();
            for (int i = 0; i < 36; i++) {
                if (contents[i] != null && contents[i].isSimilar(give) && contents[i].getAmount() < contents[i].getMaxStackSize()) {
                    slot = i;
                    break;
                }
            }
            if (slot == -1) {
                slot = ep.getBukkitPlayer().getInventory().firstEmpty();
            }
            if (slot == -1) {
                ep.sendTranslation("black_market.shop.nospace");
                inBuy[0] = false;
                return;
            }
            BlackMarketItem finalClickedItem = clickedItem.setShowPrice(true);
            ep.getBagMoneyBearer().addMoney(-clickedItem.getPrice(), success -> {
                if (!success) {
                    ep.sendTranslation("black_market.shop.nomoney");
                    inBuy[0] = false;
                    return;
                }
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        int slot = -1;
                        ItemStack[] contents = ep.getBukkitPlayer().getInventory().getContents();
                        for (int i = 0; i < 36; i++) {
                            if (contents[i] != null && contents[i].isSimilar(give) && contents[i].getAmount() < contents[i].getMaxStackSize()) {
                                slot = i;
                                give.setAmount(contents[i].getAmount() + 1);
                                ep.getBukkitPlayer().getInventory().setItem(slot, give);
                                break;
                            }
                        }
                        if (slot == -1) {
                            slot = ep.getBukkitPlayer().getInventory().firstEmpty();
                            if (slot == -1) {
                                ep.getBukkitPlayer().getWorld().dropItem(ep.getBukkitPlayer().getLocation(), give);
                            } else {
                                ep.getBukkitPlayer().getInventory().setItem(slot, give);
                            }
                        }
                        if (slot == -1) {
                            ep.sendTranslation("black_market.shop.nospace");
                            inBuy[0] = false;
                            return;
                        }
                        ep.getBukkitPlayer().updateInventory();
                        ep.sendTranslation("black_market.shop.success");
                        inBuy[0] = false;
                    }
                });
            });
        })));
    }
}
