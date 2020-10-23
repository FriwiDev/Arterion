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

public class BatteringRamItem extends SiegeItem {
    public BatteringRamItem(ItemStack stack) {
        super(CustomItemType.SIEGE_BATTERING_RAM, stack);
    }

    public BatteringRamItem() {
        super(CustomItemType.SIEGE_BATTERING_RAM);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.HOPPER, this.getType(), "siege.battering_ram", getPrice());
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
    public boolean onInteract(ArterionPlayer arterionPlayer, Block b, BlockFace blockFace) {
        int regen_delay = ArterionPlugin.getInstance().getFormulaManager().SIEGE_BATTERING_RAM_REGEN_DELAY.evaluateInt();
        if (b == null) {
            arterionPlayer.sendTranslation("siege.battering_ram.instructions");
            return false;
        }
        if (!checkUseOn(arterionPlayer, b)) return false;
        if (b.getType() == Material.IRON_DOOR_BLOCK || b.getType() == Material.IRON_TRAPDOOR) {
            TemporaryBlockCompound comp = ArterionPlugin.getInstance().getTemporaryBlockManager().createCompound();
            if (comp.backupAndMark(b)) {
                boolean door = b.getType() == Material.IRON_DOOR_BLOCK;
                b.setType(Material.AIR, false);
                if (door) {
                    if (b.getRelative(BlockFace.UP).getType() == Material.IRON_DOOR_BLOCK) {
                        Block b1 = b.getRelative(BlockFace.UP);
                        if (comp.backupAndMark(b1)) {
                            b1.setType(Material.AIR, false);
                        }
                    } else if (b.getRelative(BlockFace.DOWN).getType() == Material.IRON_DOOR_BLOCK) {
                        Block b1 = b.getRelative(BlockFace.DOWN);
                        if (comp.backupAndMark(b1)) {
                            b1.setType(Material.AIR, false);
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
            }, regen_delay, 1);
            //Play sound
            b.getWorld().playSound(b.getLocation(), Sound.ZOMBIE_WOODBREAK, 0.8f, 1f);
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
            arterionPlayer.sendTranslation("siege.battering_ram.instructions");
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
        recipe.shape("IBB", "ISH", "IBB");

        // Set what the letters represent.
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.NETHER_BRICK_ITEM);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('H', Material.TRIPWIRE_HOOK);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.battering_ram";
    }
}
