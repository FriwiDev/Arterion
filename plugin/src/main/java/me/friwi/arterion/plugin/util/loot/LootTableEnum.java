package me.friwi.arterion.plugin.util.loot;

import me.friwi.arterion.plugin.formula.ArterionFormulaArray;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;

public abstract class LootTableEnum {
    private static final SecureRandom random = new SecureRandom();
    protected ArterionFormulaArray weight, min, max;
    private LootTableEntry[] values;

    public LootTableEnum() {
        registerAll();
    }

    public ItemStack getRandomDrop() {
        if (weight == null) {
            setFormulas();
            for (LootTableEntry current : values()) {
                current.setWeight(weight.get(current.name()));
                current.setMin(min.get(current.name()));
                current.setMax(max.get(current.name()));
            }
        }
        double gen = random.nextDouble() * getSum();
        LootTableEntry last = null;
        double curr_sum = 0;
        for (LootTableEntry current : values()) {
            if (gen < curr_sum) return last.getRandomStack(random);
            last = current;
            curr_sum += current.getWeight();
        }
        return last.getRandomStack(random);
    }

    private double getSum() {
        double sum = 0;
        for (LootTableEntry drop : values()) {
            sum += drop.getWeight();
        }
        return sum;
    }

    private void registerAll() {
        ArrayList<LootTableEntry> loot = new ArrayList<>();
        for (Field f : this.getClass().getDeclaredFields()) {
            if (LootTableEntry.class.isAssignableFrom(f.getType())) {
                try {
                    LootTableEntry table = (LootTableEntry) f.get(null);
                    table.setName(f.getName());
                    loot.add(table);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LootTableEntry[] array = new LootTableEntry[loot.size()];
        values = loot.toArray(array);
    }

    public LootTableEntry[] values() {
        return values;
    }

    protected abstract void setFormulas();

    protected void setWeight(ArterionFormulaArray weight) {
        this.weight = weight;
    }

    protected void setMin(ArterionFormulaArray min) {
        this.min = min;
    }

    protected void setMax(ArterionFormulaArray max) {
        this.max = max;
    }
}
