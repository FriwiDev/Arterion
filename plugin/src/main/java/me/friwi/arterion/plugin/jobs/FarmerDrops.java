package me.friwi.arterion.plugin.jobs;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.loot.LootTableEntry;
import me.friwi.arterion.plugin.util.loot.LootTableEnum;
import me.friwi.arterion.plugin.world.item.GoldItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FarmerDrops extends LootTableEnum {
    public static final LootTableEntry GOLD = new LootTableEntry(Material.GOLD_INGOT) {
        @Override
        protected ItemStack getRandomStack(Random random) {
            return new GoldItem((getMinAmount() + random.nextInt(getMaxAmount() - getMinAmount() + 1))).toItemStack();
        }
    };
    public static final LootTableEntry GOLDEN_CARROT = new LootTableEntry(Material.GOLDEN_CARROT);
    public static final LootTableEntry NETHER_WARTS = new LootTableEntry(Material.NETHER_STALK);
    public static final LootTableEntry RABBIT_FOOT = new LootTableEntry(Material.RABBIT_FOOT);
    public static final LootTableEntry GUN_POWDER = new LootTableEntry(Material.SULPHUR);
    public static final LootTableEntry GLOWSTONE_DUST = new LootTableEntry(Material.GLOWSTONE_DUST);

    public static final FarmerDrops INSTANCE = new FarmerDrops();

    @Override
    protected void setFormulas() {
        this.weight = ArterionPlugin.getInstance().getFormulaManager().JOB_FARMER_DROPS_WEIGHT;
        this.min = ArterionPlugin.getInstance().getFormulaManager().JOB_FARMER_DROPS_MIN;
        this.max = ArterionPlugin.getInstance().getFormulaManager().JOB_FARMER_DROPS_MAX;
    }
}
