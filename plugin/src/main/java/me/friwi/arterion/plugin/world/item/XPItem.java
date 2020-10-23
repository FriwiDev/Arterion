package me.friwi.arterion.plugin.world.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class XPItem extends CustomItem {
    private long amount;

    public XPItem(ItemStack stack) {
        super(CustomItemType.XP, stack);
    }

    public XPItem(long amount) {
        super(CustomItemType.XP);
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    protected void parseItem() {
        NBTItem nbti = new NBTItem(stack);
        if (nbti.hasKey("E_amount")) {
            this.amount = nbti.getLong("E_amount");
        }
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack stack = NamedItemUtil.create(Material.PAPER,
                LanguageAPI.getDefaultLanguage().getTranslation("item.xp.name").translate().getMessage(),
                LanguageAPI.getDefaultLanguage().getTranslation("item.xp.sub").translate(amount).getMessage());
        stack = NBTItemUtil.setType(stack, this.getType());
        NBTItem nbti = new NBTItem(stack);
        nbti.setLong("E_amount", this.amount);
        return nbti.getItem();
    }

    @Override
    public boolean onPickup(ArterionPlayer player, Item item) {
        return true;
    }

    @Override
    public boolean onDrop(ArterionPlayer player, Item item) {
        return true;
    }

    @Override
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block) {
        return true;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        return true;
    }

    @Override
    public boolean onInteract(ArterionPlayer player, Block block, BlockFace blockFace) {
        ItemStack stack = player.getBukkitPlayer().getItemInHand();
        if (stack != null) {
            if (player.getSelectedClass() == null || player.getSelectedClass() == ClassEnum.NONE) {
                player.sendTranslation("item.xp.noclass");
                return false;
            }
            if (stack.getAmount() > 1) {
                stack.setAmount(stack.getAmount() - 1);
            } else {
                stack = null;
            }
            player.getBukkitPlayer().setItemInHand(stack);
            player.getBukkitPlayer().updateInventory();
            player.addXP((int) amount);
            player.sendTranslation("item.xp.awarded", amount);
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.EAT, 1f, 1f);
        }
        return false;
    }

    @Override
    public boolean onSwitchInventory(ArterionPlayer arterionPlayer) {
        return true;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return !(inventory.getHolder() instanceof Hopper);
    }
}
