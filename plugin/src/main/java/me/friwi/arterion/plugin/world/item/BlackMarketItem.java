package me.friwi.arterion.plugin.world.item;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import org.bukkit.inventory.ItemStack;

public abstract class BlackMarketItem extends CustomItem {
    private boolean showPrice = false;

    public BlackMarketItem(CustomItemType type, ItemStack stack) {
        super(type, stack);
    }

    public BlackMarketItem(CustomItemType type) {
        super(type);
    }

    public boolean shouldShowPrice() {
        return showPrice;
    }

    public BlackMarketItem setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
        return this;
    }

    public Long getPrice() {
        if (!shouldShowPrice()) return null;
        ArterionFormula formula = ArterionPlugin.getInstance().getFormulaManager().BLACK_MARKET_PRICE.get(this.getType().name());
        if (formula != null && formula.isDeclared()) {
            return Long.valueOf(formula.evaluateInt());
        }
        return Long.valueOf(0);
    }
}
