package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
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
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicBoolean;

public class BridgeItem extends SiegeItem implements BuildingItem {
    public BridgeItem(ItemStack stack) {
        super(CustomItemType.SIEGE_BRIDGE, stack);
    }

    public BridgeItem() {
        super(CustomItemType.SIEGE_BRIDGE);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.SPRUCE_FENCE_GATE, this.getType(), "siege.bridge", getPrice());
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
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block blockBegin) {
        if (!checkUseOn(arterionPlayer, blockBegin)) return false;

        int range = 1;
        int layers = ArterionPlugin.getInstance().getFormulaManager().SIEGE_BRIDGE_LAYERS.evaluateInt();
        int speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_BRIDGE_SPEED.evaluateInt();
        int remove_delay = ArterionPlugin.getInstance().getFormulaManager().SIEGE_BRIDGE_REMOVE_DELAY.evaluateInt();
        int remove_speed = ArterionPlugin.getInstance().getFormulaManager().SIEGE_BRIDGE_REMOVE_SPEED.evaluateInt();

        BlockFace direction = null;
        Vector d = arterionPlayer.getBukkitPlayer().getLocation().getDirection();
        if (Math.abs(d.getX()) > Math.abs(d.getZ())) {
            if (d.getX() < 0) {
                direction = BlockFace.WEST;
            } else {
                direction = BlockFace.EAST;
            }
        } else {
            if (d.getZ() < 0) {
                direction = BlockFace.NORTH;
            } else {
                direction = BlockFace.SOUTH;
            }
        }

        Block block = blockBegin.getRelative(BlockFace.DOWN).getRelative(direction);

        Block check = block;
        for (int i = 0; i < 3; i++) {
            if (!checkLayer(check, range, direction)) {
                arterionPlayer.sendTranslation("siege.bridge.instructions");
                return false;
            }
            check = check.getRelative(direction);
        }

        //Check if bridge will hit something
        check = block;
        boolean collided = false;
        for (int i = 0; i < layers + 1; i++) {
            if (!checkLayer(check, range, direction)) {
                collided = true;
                break;
            }
            check = check.getRelative(direction);
        }
        if (!collided) {
            arterionPlayer.sendTranslation("siege.bridge.instructions2");
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
            arterionPlayer.sendTranslation("siege.bridge.destroyed", arterionPlayer.getLanguage().translateObject(other));
            other.sendTranslation("siege.bridge.destroyed_success", other.getLanguage().translateObject(arterionPlayer));
            return null;
        });

        BlockFace finalDirection = direction;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int layer = 0;
            Block b = block;
            boolean noX = finalDirection == BlockFace.WEST || finalDirection == BlockFace.EAST;

            @Override
            public void run() {
                if (!checkLayer(b, range, finalDirection) || destroyed.get()) {
                    cancel();
                    beginDestroy(comp, remove_delay, remove_speed, b.getLocation(), noX, !noX, destroyed, destroyBinding);
                    return;
                }

                b.getWorld().playSound(b.getLocation(), Sound.STEP_WOOD, 0.8f, 1f);

                //Fence gate
                if (layer == 0) {
                    if (comp.backupAndMark(blockBegin)) {
                        int a = 0;
                        if (finalDirection == BlockFace.WEST) a = 1;
                        else if (finalDirection == BlockFace.NORTH) a = 2;
                        else if (finalDirection == BlockFace.EAST) a = 3;
                        blockBegin.setTypeIdAndData(Material.SPRUCE_FENCE_GATE.getId(), (byte) (4 + a), true);
                    }
                }

                //Build middle
                comp.backupAndMark(b);
                if (layer == 0) {
                    b.setType(Material.IRON_BLOCK);
                } else {
                    b.setTypeIdAndData(Material.LOG.getId(), (byte) (noX ? 5 : 9), true);
                }

                //Build outer stairs
                for (int x = -1; x <= 1; x += 2) {
                    Block b1 = b.getLocation().add(noX ? 0 : x, 0, noX ? x : 0).getBlock();
                    comp.backupAndMark(b1);
                    if (x < 0) {
                        b1.setTypeIdAndData(Material.SPRUCE_WOOD_STAIRS.getId(), (byte) (noX ? 6 : 4), true);
                    } else {
                        b1.setTypeIdAndData(Material.SPRUCE_WOOD_STAIRS.getId(), (byte) (noX ? 7 : 5), true);
                    }
                }

                b = b.getRelative(finalDirection);
                layer++;
                if (layer >= layers) {
                    cancel();
                    beginDestroy(comp, remove_delay, remove_speed, b.getLocation(), noX, !noX, destroyed, destroyBinding);
                    return;
                }
            }
        }, 1, speed);

        return false;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block blockBegin) {
        return false;
    }

    private boolean checkLayer(Block b, int range, BlockFace direction) {
        boolean checkX = direction == BlockFace.NORTH || direction == BlockFace.SOUTH;
        for (int x = -range; x <= range; x++) {
            if (b.getLocation().add(checkX ? x : 0, 0, checkX ? 0 : x).getBlock().getType().isSolid()) return false;
            //Check below
            for (int y = 1; y <= 3; y++) {
                Block check = b.getLocation().add(checkX ? x : 0, -y, checkX ? 0 : x).getBlock();
                if (ArterionChunkUtil.getNonNull(check.getChunk()).isTemporaryBlock(check))
                    return false;
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
        recipe.shape("WWW", "OLO", "WWW");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.LOG);
        recipe.setIngredient('L', Material.LADDER);
        recipe.setIngredient('O', Material.IRON_BLOCK);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }

        // Create our custom recipe variable
        recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape("WWW", "OLO", "WWW");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.LOG_2);
        recipe.setIngredient('L', Material.LADDER);
        recipe.setIngredient('O', Material.IRON_BLOCK);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.bridge";
    }
}
