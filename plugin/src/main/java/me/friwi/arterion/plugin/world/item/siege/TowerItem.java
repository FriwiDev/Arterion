package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
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

import java.util.concurrent.atomic.AtomicBoolean;

public class TowerItem extends SiegeItem implements BuildingItem {
    public TowerItem(ItemStack stack) {
        super(CustomItemType.SIEGE_TOWER, stack);
    }

    public TowerItem() {
        super(CustomItemType.SIEGE_TOWER);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.IRON_BLOCK, this.getType(), "siege.tower", getPrice());
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

        int range = 3;
        int layers = ArterionPlugin.getInstance().getFormulaManager().SIEGE_TOWER_LAYERS.evaluateInt();
        int speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_TOWER_SPEED.evaluateInt();
        int remove_delay = ArterionPlugin.getInstance().getFormulaManager().SIEGE_TOWER_REMOVE_DELAY.evaluateInt();
        int remove_speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_TOWER_REMOVE_SPEED.evaluateInt();

        if (!checkLayer(block, range, false)) {
            arterionPlayer.sendTranslation("siege.tower.instructions");
            return false;
        }

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

        AtomicBoolean destroyed = new AtomicBoolean(false);
        TemporaryBlockCompound comp = ArterionPlugin.getInstance().getTemporaryBlockManager().createCompound();

        Binding<Location> destroyBinding = Hooks.BLOCK_BREAK_EVENT_HOOK.subscribe(block.getLocation(), evt -> {
            evt.setCancelled(true);
            comp.rollback(evt.getBlock());
            destroyed.set(true);
            ArterionPlayer other = ArterionPlayerUtil.get(evt.getPlayer());
            arterionPlayer.sendTranslation("siege.tower.destroyed", arterionPlayer.getLanguage().translateObject(other));
            other.sendTranslation("siege.tower.destroyed_success", other.getLanguage().translateObject(arterionPlayer));
            return null;
        });

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int layer = 0;
            Block b = block;

            @Override
            public void run() {
                if (!checkLayer(b, range, true) || destroyed.get()) {
                    cancel();
                    beginDestroy(comp, remove_delay, remove_speed, b.getLocation(), false, false, destroyed, destroyBinding);
                    return;
                }

                b.getWorld().playSound(b.getLocation(), Sound.STEP_WOOD, 0.8f, 1f);

                //Outer logs
                for (int x = -2; x <= 2; x += 4) {
                    for (int z = -2; z <= 2; z += 4) {
                        Block b1 = b.getLocation().add(x, 0, z).getBlock();
                        comp.backupAndMark(b1);
                        b1.setType(Material.LOG);
                    }
                }

                //Inner logs
                comp.backupAndMark(b);
                if (layer == 0) {
                    b.setType(Material.OBSIDIAN);
                } else {
                    b.setType(Material.LOG);
                }

                //Ladders
                if (layer > 0) {
                    for (int x = -1; x <= 1; x += 2) {
                        Block b1 = b.getLocation().add(x, 0, 0).getBlock();
                        comp.backupAndMark(b1);
                        b1.setTypeIdAndData(Material.LADDER.getId(), (byte) (x < 0 ? 4 : 5), true);
                    }
                    for (int z = -1; z <= 1; z += 2) {
                        Block b1 = b.getLocation().add(0, 0, z).getBlock();
                        comp.backupAndMark(b1);
                        b1.setTypeIdAndData(Material.LADDER.getId(), (byte) (z < 0 ? 0 : 3), true);
                    }
                }

                //Floor
                if (layer % 4 == 3) {
                    //Center planks
                    for (int x = -1; x <= 1; x += 2) {
                        for (int z = -1; z <= 1; z += 2) {
                            Block b1 = b.getLocation().add(x, 0, z).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.WOOD.getId(), (byte) 1, true);
                        }
                    }
                    //Outer horizontal logs
                    for (int x = -2; x <= 2; x += 4) {
                        for (int z = -1; z <= 1; z += 1) {
                            Block b1 = b.getLocation().add(x, 0, z).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.LOG.getId(), (byte) 8, true);
                            b1 = b.getLocation().add(z, 0, x).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.LOG.getId(), (byte) 4, true);
                        }
                    }
                    //Outer stairs
                    for (int x = -3; x <= 3; x += 6) {
                        for (int z = -2; z <= 2; z += 4) {
                            Block b1 = b.getLocation().add(x, 0, z).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_WOOD_STAIRS.getId(), (byte) (x < 0 ? 4 : 5), true);
                            b1 = b.getLocation().add(z, 0, x).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_WOOD_STAIRS.getId(), (byte) (x < 0 ? 6 : 7), true);
                        }
                    }
                }

                //Outer fences
                if (layer > 3 && layer % 4 == 0) {
                    for (int x = -2; x <= 2; x += 4) {
                        for (int z = -3; z <= 3; z += 6) {
                            Block b1 = b.getLocation().add(x, 0, z).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_FENCE.getId(), (byte) 0, true);
                            b1 = b.getLocation().add(z, 0, x).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_FENCE.getId(), (byte) 0, true);
                        }
                    }
                }

                //Inner fences
                if (layer > 3 && layer % 4 <= 1) {
                    for (int x = -1; x <= 1; x += 2) {
                        for (int z = -2; z <= 2; z += 4) {
                            Block b1 = b.getLocation().add(x, 0, z).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_FENCE.getId(), (byte) 0, true);
                            b1 = b.getLocation().add(z, 0, x).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_FENCE.getId(), (byte) 0, true);
                        }
                    }
                }

                //Inner stairs
                if (layer % 4 == 2) {
                    for (int x = -1; x <= 1; x += 2) {
                        for (int z = -2; z <= 2; z += 4) {
                            Block b1 = b.getLocation().add(x, 0, z).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_WOOD_STAIRS.getId(), (byte) (x < 0 ? 5 : 4), true);
                            b1 = b.getLocation().add(z, 0, x).getBlock();
                            comp.backupAndMark(b1);
                            b1.setTypeIdAndData(Material.SPRUCE_WOOD_STAIRS.getId(), (byte) (x < 0 ? 7 : 6), true);
                        }
                    }
                }


                b = b.getRelative(BlockFace.UP);
                layer++;
                if (layer >= layers) {
                    cancel();
                    beginDestroy(comp, remove_delay, remove_speed, b.getLocation(), false, false, destroyed, destroyBinding);
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

    private boolean checkLayer(Block b, int range, boolean checkCenter) {
        if (b.getY() >= b.getWorld().getMaxHeight()) return false;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                if (x == 0 && z == 0 && !checkCenter) continue;
                if (b.getLocation().add(x, 0, z).getBlock().getType().isSolid()) return false;
            }
        }
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
        recipe.shape("WLW", "WLW", "AAA");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.LOG);
        recipe.setIngredient('L', Material.LADDER);
        recipe.setIngredient('A', Material.ANVIL);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }

        // Create our custom recipe variable
        recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape("WLW", "WLW", "AAA");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.LOG_2);
        recipe.setIngredient('L', Material.LADDER);
        recipe.setIngredient('A', Material.ANVIL);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.tower";
    }
}
