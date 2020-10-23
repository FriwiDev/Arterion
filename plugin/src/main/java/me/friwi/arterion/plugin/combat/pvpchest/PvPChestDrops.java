package me.friwi.arterion.plugin.combat.pvpchest;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.loot.LootTableEntry;
import me.friwi.arterion.plugin.util.loot.LootTableEnum;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import me.friwi.arterion.plugin.world.item.siege.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PvPChestDrops extends LootTableEnum {
    public static final LootTableEntry COAL = new LootTableEntry(Material.COAL);
    public static final LootTableEntry IRON_INGOT = new LootTableEntry(Material.IRON_INGOT);
    public static final LootTableEntry GOLD_INGOT = new LootTableEntry(Material.GOLD_INGOT);
    public static final LootTableEntry LAPIS = new LootTableEntry(new ItemStack(Material.INK_SACK, 1, (short) 4));
    public static final LootTableEntry REDSTONE = new LootTableEntry(Material.REDSTONE);
    public static final LootTableEntry EMERALD = new LootTableEntry(Material.EMERALD);
    public static final LootTableEntry DIAMOND = new LootTableEntry(Material.DIAMOND);
    public static final LootTableEntry QUARTZ = new LootTableEntry(Material.QUARTZ);
    public static final LootTableEntry NETHER_WARTS = new LootTableEntry(Material.NETHER_STALK);
    public static final LootTableEntry GLOWSTONE_DUST = new LootTableEntry(Material.GLOWSTONE_DUST);
    public static final LootTableEntry GUN_POWDER = new LootTableEntry(Material.SULPHUR);
    public static final LootTableEntry GHAST_TEAR = new LootTableEntry(Material.GHAST_TEAR);
    public static final LootTableEntry SLIME_BALL = new LootTableEntry(Material.SLIME_BALL);
    public static final LootTableEntry EXP_BOTTLE = new LootTableEntry(Material.EXP_BOTTLE);
    public static final LootTableEntry WEB = new LootTableEntry(Material.WEB);
    public static final LootTableEntry SADDLE = new LootTableEntry(Material.SADDLE);

    public static final LootTableEntry NORMAL_TNT = new LootTableEntry(new NormalTntItem().toItemStack());
    public static final LootTableEntry OBSIDIAN_TNT = new LootTableEntry(new ObsidianTntItem().toItemStack());
    public static final LootTableEntry TOWER = new LootTableEntry(new TowerItem().toItemStack());
    public static final LootTableEntry LOCK_PICK = new LootTableEntry(new LockPickItem().toItemStack());
    public static final LootTableEntry BATTERING_RAM = new LootTableEntry(new BatteringRamItem().toItemStack());
    public static final LootTableEntry SIEGE_LADDER = new LootTableEntry(new LadderItem().toItemStack());
    public static final LootTableEntry SOLIDIFY = new LootTableEntry(new SolidifyItem().toItemStack());
    public static final LootTableEntry FREEZER = new LootTableEntry(new FreezeItem().toItemStack());
    public static final LootTableEntry BRIDGE = new LootTableEntry(new BridgeItem().toItemStack());
    public static final LootTableEntry MORGOTH_DUNGEON_KEY = new LootTableEntry(new MorgothDungeonKeyItem().toItemStack());


    public static final PvPChestDrops INSTANCE = new PvPChestDrops();

    @Override
    protected void setFormulas() {
        this.weight = ArterionPlugin.getInstance().getFormulaManager().PVPCHEST_DROPS_WEIGHT;
        this.min = ArterionPlugin.getInstance().getFormulaManager().PVPCHEST_DROPS_MIN;
        this.max = ArterionPlugin.getInstance().getFormulaManager().PVPCHEST_DROPS_MAX;
    }
}
