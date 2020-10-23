package me.friwi.arterion.plugin.jobs;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.loot.LootTableEntry;
import me.friwi.arterion.plugin.util.loot.LootTableEnum;
import me.friwi.arterion.plugin.world.item.GoldItem;
import me.friwi.arterion.plugin.world.item.siege.NormalTntItem;
import me.friwi.arterion.plugin.world.item.siege.ObsidianTntItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FisherDrops extends LootTableEnum {
    public static final LootTableEntry GOLD = new LootTableEntry(Material.GOLD_INGOT) {
        @Override
        protected ItemStack getRandomStack(Random random) {
            return new GoldItem((getMinAmount() + random.nextInt(getMaxAmount() - getMinAmount() + 1))).toItemStack();
        }
    };
    public static final LootTableEntry EXP_BOTTLE = new LootTableEntry(Material.EXP_BOTTLE);
    public static final LootTableEntry GHAST_TEAR = new LootTableEntry(Material.GHAST_TEAR);
    public static final LootTableEntry MAGMA_CREAM = new LootTableEntry(Material.MAGMA_CREAM);
    public static final LootTableEntry BLAZE_ROD = new LootTableEntry(Material.BLAZE_ROD);
    public static final LootTableEntry NORMAL_TNT = new LootTableEntry(new NormalTntItem().toItemStack());
    public static final LootTableEntry OBSIDIAN_TNT = new LootTableEntry(new ObsidianTntItem().toItemStack());

    public static final FisherDrops INSTANCE = new FisherDrops();

    @Override
    protected void setFormulas() {
        this.weight = ArterionPlugin.getInstance().getFormulaManager().JOB_FISHER_DROPS_WEIGHT;
        this.min = ArterionPlugin.getInstance().getFormulaManager().JOB_FISHER_DROPS_MIN;
        this.max = ArterionPlugin.getInstance().getFormulaManager().JOB_FISHER_DROPS_MAX;
    }
}
