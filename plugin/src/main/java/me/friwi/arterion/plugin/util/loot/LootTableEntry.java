package me.friwi.arterion.plugin.util.loot;

import me.friwi.arterion.plugin.formula.ArterionFormula;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class LootTableEntry {
    private ItemStack stack;
    private ArterionFormula weight, min, max;

    private String name;

    public LootTableEntry(Material material) {
        this(material, (short) 0);
    }

    public LootTableEntry(Material material, short data) {
        this.stack = new ItemStack(material, 1, data);
    }

    public LootTableEntry(ItemStack stack) {
        this.stack = stack;
    }

    protected ItemStack getRandomStack(Random random) {
        ItemStack ret = stack.clone();
        ret.setAmount(getMinAmount() + random.nextInt(getMaxAmount() - getMinAmount() + 1));
        return ret;
    }

    double getWeight() {
        return weight.isDeclared() ? weight.evaluateDouble() : 0;
    }

    public void setWeight(ArterionFormula weight) {
        this.weight = weight;
    }

    public int getMinAmount() {
        return min.isDeclared() ? min.evaluateInt() : 1;
    }

    public int getMaxAmount() {
        return max.isDeclared() ? max.evaluateInt() : 1;
    }

    public void setMin(ArterionFormula min) {
        this.min = min;
    }

    public void setMax(ArterionFormula max) {
        this.max = max;
    }

    public String name() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name();
    }
}
