package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.temporaryblock.TemporaryBlockCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.concurrent.atomic.AtomicBoolean;

public class LadderItem extends SiegeItem implements BuildingItem {
    public LadderItem(ItemStack stack) {
        super(CustomItemType.SIEGE_LADDER, stack);
    }

    public LadderItem() {
        super(CustomItemType.SIEGE_LADDER);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.LADDER, this.getType(), "siege.ladder", getPrice());
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
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block) {
        if (!checkUseOn(arterionPlayer, block)) return false;

        int layers = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LADDER_LAYERS.evaluateInt();
        int speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LADDER_SPEED.evaluateInt();
        int remove_delay = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LADDER_REMOVE_DELAY.evaluateInt();
        int remove_speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LADDER_REMOVE_SPEED.evaluateInt();

        //Remove the item from hand
        this.printUseMessage(arterionPlayer);
        ItemStack inHand = arterionPlayer.getBukkitPlayer().getItemInHand();
        if (inHand.getAmount() <= 1) {
            inHand = null;
        } else {
            inHand.setAmount(inHand.getAmount() - 1);
        }
        arterionPlayer.getBukkitPlayer().setItemInHand(inHand);
        arterionPlayer.getBukkitPlayer().updateInventory();

        TemporaryBlockCompound comp = ArterionPlugin.getInstance().getTemporaryBlockManager().createCompound();

        byte data = block.getData();
        BlockFace attached = BlockFace.SOUTH;
        if (data == (byte) 3) attached = BlockFace.NORTH;
        if (data == (byte) 4) attached = BlockFace.EAST;
        if (data == (byte) 5) attached = BlockFace.WEST;

        BlockFace finalAttached = attached;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int layer = 0;
            Block b = block;
            Block bh = block.getRelative(finalAttached);
            Block b1 = b.getRelative(BlockFace.DOWN);
            Block b1h = bh.getRelative(BlockFace.DOWN);

            @Override
            public void run() {
                if (!checkLayer(b, bh)) {
                    b = null;
                    bh = null;
                }
                if (!checkLayer(b1, b1h)) {
                    b1 = null;
                    b1h = null;
                }
                if (b == null && b1 == null) {
                    cancel();
                    beginDestroy(comp, remove_delay, remove_speed, block.getRelative(finalAttached).getLocation(), false, false, new AtomicBoolean(false), null);
                    return;
                }

                if (b != null) b.getWorld().playSound(b.getLocation(), Sound.STEP_WOOD, 0.8f, 1f);
                if (b1 != null) b1.getWorld().playSound(b1.getLocation(), Sound.STEP_WOOD, 0.8f, 1f);

                //Place ladders
                if (b != null && comp.backupAndMark(b)) {
                    b.setTypeIdAndData(Material.LADDER.getId(), data, false);
                }
                if (b1 != null && comp.backupAndMark(b1)) {
                    b1.setTypeIdAndData(Material.LADDER.getId(), data, false);
                }

                if (b != null) {
                    b = b.getRelative(BlockFace.UP);
                    bh = bh.getRelative(BlockFace.UP);
                }
                if (b1 != null) {
                    b1 = b1.getRelative(BlockFace.DOWN);
                    b1h = b1h.getRelative(BlockFace.DOWN);
                }
                layer++;
                if (layer >= layers) {
                    cancel();
                    beginDestroy(comp, remove_delay, remove_speed, block.getRelative(finalAttached).getLocation(), false, false, new AtomicBoolean(false), null);
                    return;
                }
            }
        }, 1, speed);

        return false;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    private boolean checkLayer(Block b, Block behind) {
        if (b == null || behind == null) return false;
        if (b.getY() >= b.getWorld().getMaxHeight() || b.getY() <= 0) return false;
        if (b.getType() == Material.LADDER || b.getType().isSolid()) return false;
        if (!behind.getType().isSolid() || behind.getType() == Material.CACTUS) return false;
        return true;
    }

    @Override
    public boolean onInteract(ArterionPlayer arterionPlayer, Block b, BlockFace blockFace) {
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

    @Override
    public void registerRecipes() {
        // Create our custom recipe variable
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape("WSW", "WIW", "WSW");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.LOG);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('I', Material.IRON_INGOT);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }

        // Create our custom recipe variable
        recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape("WSW", "WIW", "WSW");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.LOG_2);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('I', Material.IRON_INGOT);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.ladder";
    }
}
