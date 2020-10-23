package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.HomeblockItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PrivateClaimVillagerInteractHandler implements VillagerInteractHandler {
    @Override
    public void handleInteract(ArterionPlayer ep) {
        ep.openGui(new ItemGUI(ep, ep.getTranslation("gui.homeblockbuy.choose"), () -> {
            ItemStack[] stacks = new ItemStack[9];
            stacks[4] = NamedItemUtil.create(Material.ENDER_PORTAL_FRAME, 1, ep.getTranslation("gui.homeblockbuy.item.name"), ep.getTranslation("gui.homeblockbuy.item.amount", ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_FEE.evaluateInt(ep) / 100F));
            return stacks;
        }, ((clickType, in) -> {
            final int[] space = {ep.getBukkitPlayer().getInventory().firstEmpty()};
            if (space[0] == -1) {
                ep.sendTranslation("gui.homeblockbuy.nospace");
                return;
            }
            ep.closeGui();
            ep.getBagMoneyBearer().addMoney(-ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_FEE.evaluateInt(ep), success -> {
                if (!success) {
                    ep.sendTranslation("gui.homeblockbuy.nomoney");
                    return;
                }
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        ItemStack item = new HomeblockItem().toItemStack();
                        if (ep.getBukkitPlayer().getInventory().firstEmpty() == -1) {
                            ep.getBukkitPlayer().getWorld().dropItem(ep.getBukkitPlayer().getLocation(), ep.getBukkitPlayer().getInventory().getItem(space[0]));
                        } else {
                            space[0] = ep.getBukkitPlayer().getInventory().firstEmpty();
                        }
                        ep.getBukkitPlayer().getInventory().setItem(space[0], item);
                        ep.sendTranslation("gui.homeblockbuy.success");
                    }
                });
            });
        })));
    }
}
