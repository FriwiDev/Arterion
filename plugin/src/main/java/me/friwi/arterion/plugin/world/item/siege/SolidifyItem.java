package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.temporaryblock.TemporaryBlockCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class SolidifyItem extends SiegeItem {
    public SolidifyItem(ItemStack stack) {
        super(CustomItemType.SIEGE_SOLIDIFY, stack);
    }

    public SolidifyItem() {
        super(CustomItemType.SIEGE_SOLIDIFY);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.OBSIDIAN, this.getType(), "siege.solidify", getPrice());
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
        return false;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onInteract(ArterionPlayer arterionPlayer, Block block, BlockFace blockFace) {
        int radius = 5;
        int regen_delay = ArterionPlugin.getInstance().getFormulaManager().SIEGE_SOLIDIFY_REMOVE_DELAY.evaluateInt();
        int regen_speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_SOLIDIFY_REMOVE_SPEED.evaluateInt();
        Block b = arterionPlayer.getBukkitPlayer().getLocation().getBlock();
        if (!checkUseOn(arterionPlayer, b)) return false;
        if ((b.getType() == Material.LAVA || b.getType() == Material.STATIONARY_LAVA)
                && !b.getRelative(BlockFace.UP).getType().isSolid() && !b.getRelative(BlockFace.UP, 2).getType().isSolid()
                && !b.getRelative(BlockFace.UP).isLiquid() && !b.getRelative(BlockFace.UP, 2).isLiquid()) {
            TemporaryBlockCompound comp = ArterionPlugin.getInstance().getTemporaryBlockManager().createCompound();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block other = b.getLocation().add(x, 0, z).getBlock();
                    if ((other.getType() == Material.LAVA || other.getType() == Material.STATIONARY_LAVA) && other.getLocation().distance(b.getLocation()) < radius) {
                        if (comp.backupAndMark(other)) {
                            other.setType(Material.OBSIDIAN);
                        }
                    }
                }
            }
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                @Override
                public void run() {
                    if (comp.isEmpty()) {
                        cancel();
                        return;
                    }
                    comp.rollbackOne();
                    b.getWorld().playSound(b.getLocation(), Sound.STEP_STONE, 0.8f, 1f);
                }
            }, regen_delay, regen_speed);
            //Teleport player on main block
            Location above = b.getLocation().clone().add(0.5, 1, 0.5);
            above.setDirection(arterionPlayer.getBukkitPlayer().getLocation().getDirection());
            arterionPlayer.getBukkitPlayer().teleport(above);
            //Play sound
            b.getWorld().playSound(b.getLocation(), Sound.STEP_STONE, 0.8f, 1f);
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
        } else {
            arterionPlayer.sendTranslation("siege.solidify.instructions");
        }
        return false;
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
        recipe.shape("OBO", "FOF");

        // Set what the letters represent.
        recipe.setIngredient('B', Material.LAVA_BUCKET);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('F', Material.FIREBALL);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.solidify";
    }
}
