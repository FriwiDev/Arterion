package me.friwi.arterion.plugin.world.item;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DefaultItem extends CustomItem {
    public DefaultItem(ItemStack stack) {
        super(CustomItemType.NONE, stack);
    }

    public DefaultItem() {
        super(CustomItemType.NONE);
    }

    @Override
    protected void parseItem() {
    }

    @Override
    public ItemStack toItemStack() {
        throw new UnsupportedOperationException("Default item can not generate an itemstack");
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
        return true;
    }

    @Override
    public boolean onSwitchInventory(ArterionPlayer arterionPlayer) {
        return true;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return true;
    }
}
