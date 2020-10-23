package me.friwi.arterion.plugin.world.item;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class CustomItem {
    protected ItemStack stack;
    private CustomItemType type;

    public CustomItem(CustomItemType type, ItemStack stack) {
        this.type = type;
        this.stack = stack;
        this.parseItem();
    }

    public CustomItem(CustomItemType type) {
        this.type = type;
    }

    public CustomItemType getType() {
        return type;
    }

    protected abstract void parseItem();

    public abstract ItemStack toItemStack();

    public abstract boolean onPickup(ArterionPlayer player, Item item);

    public abstract boolean onDrop(ArterionPlayer player, Item item);

    public abstract boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block);

    public abstract boolean onPlace(ArterionPlayer arterionPlayer, Block block);

    public abstract boolean onInteract(ArterionPlayer arterionPlayer, Block block, BlockFace blockFace);

    public abstract boolean onSwitchInventory(ArterionPlayer arterionPlayer);

    public abstract boolean onInventoryPickup(Inventory inventory);
}
