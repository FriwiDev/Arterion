package me.friwi.arterion.plugin.world.item.lock;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.world.item.CraftableItem;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.item.DescribedItem;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.LockUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public abstract class LockItem extends CustomItem implements CraftableItem, DescribedItem {

    public LockItem(CustomItemType type, ItemStack stack) {
        super(type, stack);
    }

    public LockItem(CustomItemType type) {
        super(type);
    }

    protected abstract Material getLockMaterial();

    protected abstract Lock generateLock(ArterionPlayer arterionPlayer);

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(getLockMaterial(), this.getType(), "lock." + getType().name().toLowerCase(), null);
        return stack;
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
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onInteract(ArterionPlayer arterionPlayer, Block block, BlockFace blockFace) {
        if (block == null) return true;
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Lock lock = generateLock(arterionPlayer);
            if (lock == null) return false;
            String result = LockUtil.addLock(arterionPlayer, (Chest) block.getState(), lock);
            if (result == null) {
                return true;
            } else {
                arterionPlayer.sendTranslation("lock.place." + result);
                if (result.equalsIgnoreCase("success")) {
                    ItemStack inHand = arterionPlayer.getBukkitPlayer().getItemInHand();
                    if (inHand.getAmount() <= 1) {
                        inHand = null;
                    } else {
                        inHand.setAmount(inHand.getAmount() - 1);
                    }
                    arterionPlayer.getBukkitPlayer().setItemInHand(inHand);
                    arterionPlayer.getBukkitPlayer().updateInventory();
                }
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean onSwitchInventory(ArterionPlayer arterionPlayer) {
        return true;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return true;
    }

    @Override
    public void registerRecipes() {
        // Create our custom recipe variable
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape("BIB", "IHI", "BIB");

        // Set what the letters represent.
        recipe.setIngredient('B', Material.IRON_FENCE);
        recipe.setIngredient('I', getLockMaterial());
        recipe.setIngredient('H', Material.TRIPWIRE_HOOK);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }
}
