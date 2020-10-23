package me.friwi.arterion.plugin.combat.gamemode.artefact;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.loot.LootTableEntry;
import me.friwi.arterion.plugin.util.loot.LootTableEnum;
import me.friwi.arterion.plugin.world.item.GoldItem;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import me.friwi.arterion.plugin.world.item.siege.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ArtefactDrops extends LootTableEnum {
    public static final LootTableEntry EXP_BOTTLE = new LootTableEntry(Material.EXP_BOTTLE);
    public static final LootTableEntry OBSIDIAN = new LootTableEntry(Material.OBSIDIAN);
    public static final LootTableEntry IRON_INGOT = new LootTableEntry(Material.IRON_INGOT);
    public static final LootTableEntry EMERALD = new LootTableEntry(Material.EMERALD);
    public static final LootTableEntry DIAMOND = new LootTableEntry(Material.DIAMOND);
    public static final LootTableEntry BLAZE_ROD = new LootTableEntry(Material.BLAZE_ROD);
    public static final LootTableEntry GHAST_TEAR = new LootTableEntry(Material.GHAST_TEAR);
    public static final LootTableEntry MAGMA_CREAM = new LootTableEntry(Material.MAGMA_CREAM);
    public static final LootTableEntry GUN_POWDER = new LootTableEntry(Material.SULPHUR);
    public static final LootTableEntry GOLD_INGOT = new LootTableEntry(Material.GOLD_INGOT);
    public static final LootTableEntry IRON_HELMET = new LootTableEntry(Material.IRON_HELMET);
    public static final LootTableEntry IRON_CHESTPLATE = new LootTableEntry(Material.IRON_CHESTPLATE);
    public static final LootTableEntry IRON_LEGGINGS = new LootTableEntry(Material.IRON_LEGGINGS);
    public static final LootTableEntry IRON_BOOTS = new LootTableEntry(Material.IRON_BOOTS);
    public static final LootTableEntry IRON_SWORD = new LootTableEntry(Material.IRON_SWORD);
    public static final LootTableEntry IRON_AXE = new LootTableEntry(Material.IRON_AXE);
    public static final LootTableEntry IRON_HOE = new LootTableEntry(Material.IRON_HOE);
    public static final LootTableEntry IRON_PICKAXE = new LootTableEntry(Material.IRON_PICKAXE);
    public static final LootTableEntry IRON_SHOVEL = new LootTableEntry(Material.IRON_SPADE);
    public static final LootTableEntry STICK = new LootTableEntry(Material.STICK);
    public static final LootTableEntry BOW = new LootTableEntry(Material.BOW);
    public static final LootTableEntry GOLD = new LootTableEntry(Material.GOLD_INGOT) {
        @Override
        protected ItemStack getRandomStack(Random random) {
            return new GoldItem((getMinAmount() + random.nextInt(getMaxAmount() - getMinAmount() + 1))).toItemStack();
        }
    };
    public static final LootTableEntry NORMAL_TNT = new LootTableEntry(new NormalTntItem().toItemStack());
    public static final LootTableEntry OBSIDIAN_TNT = new LootTableEntry(new ObsidianTntItem().toItemStack());
    public static final LootTableEntry TOWER = new LootTableEntry(new TowerItem().toItemStack());
    public static final LootTableEntry LOCK_PICK = new LootTableEntry(new LockPickItem().toItemStack());
    public static final LootTableEntry BATTERING_RAM = new LootTableEntry(new BatteringRamItem().toItemStack());
    public static final LootTableEntry SIEGE_LADDER = new LootTableEntry(new LadderItem().toItemStack());
    public static final LootTableEntry SOLIDIFY = new LootTableEntry(new SolidifyItem().toItemStack());
    public static final LootTableEntry FREEZER = new LootTableEntry(new FreezeItem().toItemStack());
    public static final LootTableEntry MORGOTH_DUNGEON_KEY = new LootTableEntry(new MorgothDungeonKeyItem().toItemStack());

    public static final ArtefactDrops INSTANCE = new ArtefactDrops();

    @Override
    protected void setFormulas() {
        this.weight = ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_DROPS_WEIGHT;
        this.min = ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_DROPS_MIN;
        this.max = ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_DROPS_MAX;
    }
}
