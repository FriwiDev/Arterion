package me.friwi.arterion.plugin.world.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.GoldEarnHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GoldItem extends CustomItem {
    private long amount;

    public GoldItem(ItemStack stack) {
        super(CustomItemType.GOLD, stack);
    }

    public GoldItem(long amount) {
        super(CustomItemType.GOLD);
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
            this.amount = nbti.getLong("E_amount") * stack.getAmount(); //If two same value item got merged...
        }
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(Material.GOLD_INGOT, 1);
        stack = NBTItemUtil.setType(stack, this.getType());
        NBTItem nbti = new NBTItem(stack);
        nbti.setLong("E_amount", this.amount);
        return nbti.getItem();
    }

    @Override
    public boolean onPickup(ArterionPlayer player, Item item) {
        GoldEarnHandler.earnGold(player, amount);
        //Disallow event and remove item
        item.remove();
        return false;
    }

    @Override
    public boolean onDrop(ArterionPlayer player, Item item) {
        //Allow dropping, but kill item
        //This should never happen anyways
        item.remove();
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
        return true;
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
