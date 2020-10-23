package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.economy.TransferDirection;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.ui.gui.TextGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BankVillagerInteractHandler implements VillagerInteractHandler {
    @Override
    public void handleInteract(ArterionPlayer ep) {
        ep.openGui(new ItemGUI(ep, ep.getTranslation("gui.bank.choose"), () -> {
            ItemStack[] stacks = new ItemStack[9];
            stacks[1] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, ep.getTranslation("gui.bank.deposit"));
            stacks[4] = NamedItemUtil.create(Material.GOLD_INGOT, 1, ep.getTranslation("gui.bank.item.name"), ep.getTranslation("gui.bank.item.amount", ep.getBankMoneyBearer().getCachedMoney() / 100f, ArterionPlugin.getInstance().getFormulaManager().PLAYER_BANK_LIMIT.evaluateInt(ep) / 100f));
            stacks[7] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, ep.getTranslation("gui.bank.withdraw"));
            return stacks;
        }, ((clickType, in) -> {
            if (in == 4) return;
            TransferDirection dir = in == 1 ? TransferDirection.DEPOSIT : TransferDirection.WITHDRAW;
            ep.closeGui();
            ep.openGui(new TextGUI(ep, ep.getTranslation("gui.bank.asksubtitle"), () -> {
                return new String[]{ep.getTranslation(dir == TransferDirection.DEPOSIT ? "gui.bank.askdeposit" : "gui.bank.askwithdraw")};
            }, result -> {
                long amount = 0;
                if (result.equalsIgnoreCase("all")) {
                    if (dir == TransferDirection.DEPOSIT) {
                        amount = ep.getBagMoneyBearer().getCachedMoney();
                    } else {
                        amount = ep.getBankMoneyBearer().getCachedMoney();
                    }
                } else {
                    try {
                        double i = Math.round(Double.parseDouble(result.replace(",", ".")) * 100);
                        if (i > Integer.MAX_VALUE || i < Integer.MIN_VALUE) {
                            ep.sendTranslation("gui.bank.nonumber");
                            return;
                        }
                        amount = (int) i; //Translate to cents
                    } catch (NumberFormatException e) {
                        ep.sendTranslation("gui.bank.nonumber");
                        return;
                    }
                }

                long finalAmount = amount;
                TransferDirection finalDir = dir;
                ep.getBagMoneyBearer().transferMoney(dir == TransferDirection.WITHDRAW ? -amount : amount, ep.getBankMoneyBearer(), success -> {
                    if (success) {
                        if (finalDir == TransferDirection.WITHDRAW) {
                            ep.sendTranslation("gui.bank.received", finalAmount / 100f);
                        } else {
                            ep.sendTranslation("gui.bank.sent", finalAmount / 100f);
                        }
                    } else {
                        ep.sendTranslation("gui.bank.error");
                    }
                });
            }));
        })));
    }
}
