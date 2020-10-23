package me.friwi.arterion.plugin.world.item;

import me.friwi.arterion.plugin.world.item.lock.GuildLockItem;
import me.friwi.arterion.plugin.world.item.lock.OfficerLockItem;
import me.friwi.arterion.plugin.world.item.lock.PrivateLockItem;
import me.friwi.arterion.plugin.world.item.siege.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CustomItemUtil {
    private static final CustomItem DEFAULT_ITEM = new DefaultItem();

    public static CustomItem getCustomItem(ItemStack stack) {
        CustomItemType type = NBTItemUtil.getType(stack);
        switch (type) {
            case NONE:
                return DEFAULT_ITEM;
            case GOLD:
                return new GoldItem(stack);
            case HOMEBLOCK:
                return new HomeblockItem(stack);
            case GUILDBLOCK:
                return new GuildblockItem(stack);
            case SKILL:
                return new SkillItem(stack);
            case PRIVATE_LOCK:
                return new PrivateLockItem(stack);
            case OFFICER_LOCK:
                return new OfficerLockItem(stack);
            case GUILD_LOCK:
                return new GuildLockItem(stack);
            case SIEGE_TOWER:
                return new TowerItem(stack);
            case SIEGE_BRIDGE:
                return new BridgeItem(stack);
            case SIEGE_BATTERING_RAM:
                return new BatteringRamItem(stack);
            case SIEGE_FREEZE:
                return new FreezeItem(stack);
            case SIEGE_LADDER:
                return new LadderItem(stack);
            case SIEGE_LOCKPICK:
                return new LockPickItem(stack);
            case SIEGE_NORMAL_TNT:
                return new NormalTntItem(stack);
            case SIEGE_OBSIDIAN_TNT:
                return new ObsidianTntItem(stack);
            case SIEGE_SOLIDIFY:
                return new SolidifyItem(stack);
            case DUNGEON_KEY_MORGOTH:
                return new MorgothDungeonKeyItem(stack);
            case XP:
                return new XPItem(stack);
            default:
                return DEFAULT_ITEM;
        }
    }

    public static void registerRecipes() {
        new PrivateLockItem().registerRecipes();
        new GuildLockItem().registerRecipes();
        new OfficerLockItem().registerRecipes();
        new BatteringRamItem().registerRecipes();
        new FreezeItem().registerRecipes();
        new LockPickItem().registerRecipes();
        new NormalTntItem().registerRecipes();
        new ObsidianTntItem().registerRecipes();
        new SolidifyItem().registerRecipes();
        new TowerItem().registerRecipes();
        new BridgeItem().registerRecipes();
        new LadderItem().registerRecipes();

        registerArrowRecipe();
    }

    public static void registerArrowRecipe() {
        // Create our custom recipe variable
        ShapedRecipe recipe = new ShapedRecipe(new ItemStack(Material.ARROW, 10));

        //Beware; this is case sensitive.
        recipe.shape("I  ", " W ", "  S");

        // Set what the letters represent.
        recipe.setIngredient('W', Material.STRING);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('I', Material.IRON_INGOT);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }
}
