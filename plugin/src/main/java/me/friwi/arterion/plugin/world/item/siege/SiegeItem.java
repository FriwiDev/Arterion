package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.item.CraftableItem;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.item.DescribedItem;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SiegeItem extends CustomItem implements CraftableItem, DescribedItem, PlayerRegionBoundItem {
    private boolean showPrice = false;

    public SiegeItem(CustomItemType type, ItemStack stack) {
        super(type, stack);
    }

    public SiegeItem(CustomItemType type) {
        super(type);
    }

    public void printUseMessage(ArterionPlayer caster) {
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            String casterName = ap.getLanguage().translateObject(caster);
            if (ap != null && p.getWorld().equals(caster.getBukkitPlayer().getWorld()) && p.getLocation().distance(caster.getBukkitPlayer().getLocation()) < 120) {
                ap.sendTranslation("siege.use", casterName, getName(ap.getLanguage(), getName()));
            }
        }
        for (RecordingCreator r : ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings()) {
            String casterName = LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(caster);
            if (r.isLocationRelevant(caster.getBukkitPlayer().getLocation())) {
                r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("siege.replay.use").translate(casterName, getName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE), getName())).getMessage());
            }
        }

        String casterName = LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(caster);
        Bukkit.getServer().getConsoleSender().sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("siege.use").translate(casterName, getName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE), getName())).getMessage());
    }

    public abstract String getName();

    public String getName(Language language, String key) {
        return language.getTranslation(key + ".name").translate().getMessage().replace("\247l", "");
    }

    public boolean shouldShowPrice() {
        return showPrice;
    }

    public SiegeItem setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
        return this;
    }

    public Long getPrice() {
        if (!shouldShowPrice()) return null;
        ArterionFormula formula = ArterionPlugin.getInstance().getFormulaManager().SIEGE_PRICE.get(this.getType().name());
        if (formula != null && formula.isDeclared()) {
            return Long.valueOf(formula.evaluateInt());
        }
        return Long.valueOf(0);
    }
}
