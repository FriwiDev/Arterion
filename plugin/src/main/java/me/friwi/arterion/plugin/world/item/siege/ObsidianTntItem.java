package me.friwi.arterion.plugin.world.item.siege;

import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.InstantHotbarMessageCard;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ObsidianTntItem extends SiegeItem {
    private List<UUID> usingPlayers = new LinkedList<>();

    public ObsidianTntItem(ItemStack stack) {
        super(CustomItemType.SIEGE_OBSIDIAN_TNT, stack);
    }

    public ObsidianTntItem() {
        super(CustomItemType.SIEGE_OBSIDIAN_TNT);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.TNT, this.getType(), "siege.obsidian_tnt", getPrice());
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
        if (block == null || blockFace == null) return false;
        block = block.getRelative(blockFace);
        if (arterionPlayer.getSkillSlots().getLastObsiTnt() + ArterionPlugin.getInstance().getFormulaManager().SIEGE_OBSIDIAN_TNT_COOLDOWN.evaluateInt() > System.currentTimeMillis()) {
            long remain = arterionPlayer.getSkillSlots().getLastObsiTnt() + ArterionPlugin.getInstance().getFormulaManager().SIEGE_OBSIDIAN_TNT_COOLDOWN.evaluateInt() - System.currentTimeMillis();
            remain /= 1000;
            arterionPlayer.scheduleHotbarCard(new InstantHotbarMessageCard(1000, arterionPlayer.getTranslation("siege.obsidian_tnt.oncooldown", remain)));
            return false;
        }
        if (!checkUseOn(arterionPlayer, block)) return false;
        arterionPlayer.getSkillSlots().setLastObsiTnt(System.currentTimeMillis());

        int range = ArterionPlugin.getInstance().getFormulaManager().SIEGE_OBSIDIAN_TNT_RANGE.evaluateInt();

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

        TNTPrimed tnt = block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
        Hooks.ENTITY_EXPLODE_EVENT_HOOK.subscribe(tnt, evt -> {
            evt.blockList().clear();

            List<Block> blockList = new LinkedList<>();

            Block center = tnt.getLocation().getBlock();
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        Block affected = center.getLocation().add(x, y, z).getBlock();
                        if (affected.getType() == Material.OBSIDIAN) {
                            blockList.add(affected);
                        }
                    }
                }
            }

            ArterionPlugin.getInstance().getExplosionHandler().handleExplosion(evt.getLocation(), Lists.newLinkedList(blockList), (b, type, data) -> {
                b.setType(Material.AIR, false);
                FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation().clone().add(evt.getLocation()).multiply(0.5).add(0, 0.2, 0), type, data);
                fb.setDropItem(false);
                fb.setHurtEntities(false);
                Vector vec = fb.getLocation().toVector().subtract(evt.getLocation().toVector()).multiply(0.1);
                vec.setY(0.6 + Math.random() * 0.6);
                fb.setVelocity(vec);
                return fb;
            }, true, true);

            return null;
        });
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
        recipe.shape("ODO", "DTD", "ODO");

        // Set what the letters represent.
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('T', Material.TNT);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.obsidian_tnt";
    }
}
