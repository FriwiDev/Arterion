package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.siege.*;
import org.bukkit.inventory.ItemStack;

public class SiegeItemShopVillagerInteractHandler implements VillagerInteractHandler {
    @Override
    public void handleInteract(ArterionPlayer ep) {
        boolean[] inBuy = new boolean[]{false};
        ep.openGui(new ItemGUI(ep, ep.getTranslation("siege.shop.name"), () -> {
            ItemStack[] stacks = new ItemStack[18];
            stacks[0] = new TowerItem().setShowPrice(true).toItemStack();
            stacks[2] = new BridgeItem().setShowPrice(true).toItemStack();
            stacks[4] = new LadderItem().setShowPrice(true).toItemStack();
            stacks[6] = new ObsidianTntItem().setShowPrice(true).toItemStack();
            stacks[8] = new NormalTntItem().setShowPrice(true).toItemStack();
            stacks[10] = new SolidifyItem().setShowPrice(true).toItemStack();
            stacks[12] = new FreezeItem().setShowPrice(true).toItemStack();
            stacks[14] = new BatteringRamItem().setShowPrice(true).toItemStack();
            stacks[16] = new LockPickItem().setShowPrice(true).toItemStack();
            return stacks;
        }, ((clickType, in) -> {
            if (inBuy[0]) return;
            inBuy[0] = true;
            SiegeItem clickedItem = null;
            switch (in) {
                case 0:
                    clickedItem = new TowerItem();
                    break;
                case 2:
                    clickedItem = new BridgeItem();
                    break;
                case 4:
                    clickedItem = new LadderItem();
                    break;
                case 6:
                    clickedItem = new ObsidianTntItem();
                    break;
                case 8:
                    clickedItem = new NormalTntItem();
                    break;
                case 10:
                    clickedItem = new SolidifyItem();
                    break;
                case 12:
                    clickedItem = new FreezeItem();
                    break;
                case 14:
                    clickedItem = new BatteringRamItem();
                    break;
                case 16:
                    clickedItem = new LockPickItem();
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
                ep.sendTranslation("siege.shop.nospace");
                inBuy[0] = false;
                return;
            }
            SiegeItem finalClickedItem = clickedItem.setShowPrice(true);
            ep.getBagMoneyBearer().addMoney(-clickedItem.getPrice(), success -> {
                if (!success) {
                    ep.sendTranslation("siege.shop.nomoney");
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
                            ep.sendTranslation("siege.shop.nospace");
                            inBuy[0] = false;
                            return;
                        }
                        ep.getBukkitPlayer().updateInventory();
                        ep.sendTranslation("siege.shop.success");
                        inBuy[0] = false;
                    }
                });
            });
        })));
    }
}
