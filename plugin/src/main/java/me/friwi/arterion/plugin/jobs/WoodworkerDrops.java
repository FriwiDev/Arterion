package me.friwi.arterion.plugin.jobs;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.loot.LootTableEntry;
import me.friwi.arterion.plugin.util.loot.LootTableEnum;
import me.friwi.arterion.plugin.world.item.GoldItem;
import me.friwi.arterion.plugin.world.item.siege.BridgeItem;
import me.friwi.arterion.plugin.world.item.siege.LadderItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class WoodworkerDrops extends LootTableEnum {
    public static final LootTableEntry GOLD = new LootTableEntry(Material.GOLD_INGOT) {
        @Override
        protected ItemStack getRandomStack(Random random) {
            return new GoldItem((getMinAmount() + random.nextInt(getMaxAmount() - getMinAmount() + 1))).toItemStack();
        }
    };

    public static final LootTableEntry EXP_BOTTLE = new LootTableEntry(Material.EXP_BOTTLE);
    public static final LootTableEntry WEB = new LootTableEntry(Material.WEB);
    public static final LootTableEntry SIEGE_LADDER = new LootTableEntry(new LadderItem().toItemStack());
    public static final LootTableEntry BRIDGE = new LootTableEntry(new BridgeItem().toItemStack());

    public static final WoodworkerDrops INSTANCE = new WoodworkerDrops();

    @Override
    protected void setFormulas() {
        this.weight = ArterionPlugin.getInstance().getFormulaManager().JOB_WOODWORKER_DROPS_WEIGHT;
        this.min = ArterionPlugin.getInstance().getFormulaManager().JOB_WOODWORKER_DROPS_MIN;
        this.max = ArterionPlugin.getInstance().getFormulaManager().JOB_WOODWORKER_DROPS_MAX;
    }
}
