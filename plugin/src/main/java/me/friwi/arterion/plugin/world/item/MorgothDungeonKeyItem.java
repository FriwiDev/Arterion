package me.friwi.arterion.plugin.world.item;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MorgothDungeonKeyItem extends BlackMarketItem implements DescribedItem {
    public MorgothDungeonKeyItem(ItemStack stack) {
        super(CustomItemType.DUNGEON_KEY_MORGOTH, stack);
    }

    public MorgothDungeonKeyItem() {
        super(CustomItemType.DUNGEON_KEY_MORGOTH);
    }

    public static boolean deductKey(ArterionPlayer player) {
        ItemStack[] contents = player.getBukkitPlayer().getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) continue;
            CustomItem c = CustomItemUtil.getCustomItem(contents[i]);
            if (c instanceof MorgothDungeonKeyItem) {
                if (contents[i].getAmount() > 1) {
                    contents[i].setAmount(contents[i].getAmount() - 1);
                    player.getBukkitPlayer().getInventory().setItem(i, contents[i]);
                } else {
                    player.getBukkitPlayer().getInventory().setItem(i, null);
                }
                player.getBukkitPlayer().updateInventory();
                return true;
            }
        }
        return false;
    }

    public static boolean restoreKey(ArterionPlayer player) {
        int slot = player.getBukkitPlayer().getInventory().firstEmpty();
        if (slot == -1) return false;
        player.getBukkitPlayer().getInventory().setItem(slot, new MorgothDungeonKeyItem().toItemStack());
        player.getBukkitPlayer().updateInventory();
        return true;
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        return NBTItemUtil.setShouldStack(toItemStack(Material.TRIPWIRE_HOOK, this.getType(), "item.dungeon.morgoth", getPrice()), false);
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
        return false;
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
