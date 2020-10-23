package me.friwi.arterion.plugin.world.villager;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlacksmithVillagerInteractHandler implements VillagerInteractHandler {
    private Map<UUID, ItemStack> confirmItems = new HashMap<>();

    @Override
    public void handleInteract(ArterionPlayer ep) {
        ItemStack stack = ep.getBukkitPlayer().getItemInHand().clone();
        int enchcount = 0;
        for (int i : stack.getEnchantments().values()) enchcount += i;
        int durabilityLost = stack.getDurability();
        if (durabilityLost <= 0) {
            ep.sendTranslation("blacksmith.alreadyrepaired");
            return;
        }
        NBTItem item = new NBTItem(stack);
        Integer repaircount = item.getInteger("art_repaircount");
        if (repaircount == null) repaircount = 0;
        int price = ArterionPlugin.getInstance().getFormulaManager().BLACKSMITH_REPAIR_COST.evaluateInt(enchcount, durabilityLost, repaircount);
        ItemStack confirmed = confirmItems.remove(ep.getBukkitPlayer().getUniqueId());
        if (confirmed == null || !confirmed.equals(stack)) {
            //Tell player price and add to confirm map
            ep.sendTranslation("blacksmith.confirm", price / 100f);
            confirmItems.put(ep.getBukkitPlayer().getUniqueId(), stack);
            return;
        } else {
            //Player just confirmed the item repair
            Integer finalRepaircount = repaircount;
            ep.getBagMoneyBearer().addMoney(-price, success -> {
                if (!success) {
                    ep.sendTranslation("blacksmith.nomoney");
                    return;
                }
                ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        if (!ep.getBukkitPlayer().getItemInHand().equals(stack)) {
                            //Abort action, player is glitching by switching items
                            ep.getBagMoneyBearer().addMoney(price, s -> {
                            });
                            return;
                        }
                        NBTItem changedItem = new NBTItem(stack);
                        changedItem.setInteger("art_repaircount", finalRepaircount + 1);
                        ItemStack repaired = changedItem.getItem();
                        repaired.setDurability((short) 0);
                        ep.getBukkitPlayer().setItemInHand(repaired);
                        ep.sendTranslation("blacksmith.success");
                        ep.getBukkitPlayer().playSound(ep.getBukkitPlayer().getLocation(), Sound.ANVIL_USE, 1f, 1f);
                    }
                });
            });
        }
    }
}
