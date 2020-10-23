package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.InstantHotbarMessageCard;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class NormalTntItem extends SiegeItem {
    private List<UUID> usingPlayers = new LinkedList<>();

    public NormalTntItem(ItemStack stack) {
        super(CustomItemType.SIEGE_NORMAL_TNT, stack);
    }

    public NormalTntItem() {
        super(CustomItemType.SIEGE_NORMAL_TNT);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.TNT, this.getType(), "siege.normal_tnt", getPrice());
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
        if (arterionPlayer.getSkillSlots().getLastTnt() + ArterionPlugin.getInstance().getFormulaManager().SIEGE_TNT_COOLDOWN.evaluateInt() > System.currentTimeMillis()) {
            long remain = arterionPlayer.getSkillSlots().getLastTnt() + ArterionPlugin.getInstance().getFormulaManager().SIEGE_TNT_COOLDOWN.evaluateInt() - System.currentTimeMillis();
            remain /= 1000;
            arterionPlayer.scheduleHotbarCard(new InstantHotbarMessageCard(1000, arterionPlayer.getTranslation("siege.normal_tnt.oncooldown", remain)));
            return false;
        }
        if (!checkUseOn(arterionPlayer, block)) return false;
        arterionPlayer.getSkillSlots().setLastTnt(System.currentTimeMillis());
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

        block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
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
        //Remove old tnt recipe
        Iterator<Recipe> it = Bukkit.getServer().recipeIterator();
        Recipe r;
        while (it.hasNext()) {
            r = it.next();
            if (r != null && r.getResult().getType() == Material.TNT) {
                it.remove();
            }
        }

        // Create our custom recipe variable
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape("GSG", "SGS", "GSG");

        // Set what the letters represent.
        recipe.setIngredient('G', Material.SULPHUR);
        recipe.setIngredient('S', Material.SAND);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.normal_tnt";
    }
}
