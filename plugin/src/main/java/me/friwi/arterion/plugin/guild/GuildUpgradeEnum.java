package me.friwi.arterion.plugin.guild;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public enum GuildUpgradeEnum {
    VAULT("vault", GuildUpgradeLevel.LEVEL_6, new ItemStack(Material.ENDER_CHEST), (guild, newLevel) -> {
        //Change vault size
        guild.getVault().updateSize();
        guild.sendTranslation("guild.upgrade.unlock.vault", newLevel.ordinal() + 1);
    }),
    REGION("region", GuildUpgradeLevel.LEVEL_6, new ItemStack(Material.MAP), (guild, newLevel) -> {
        //Reclaim region
        guild.reclaimRegion();
        guild.sendTranslation("guild.upgrade.unlock.region", newLevel.ordinal() + 1);
    }),
    OFFICER("officer", GuildUpgradeLevel.LEVEL_3, new ItemStack(Material.GOLD_CHESTPLATE), (guild, newLevel) -> {
        //Nothing to do
        guild.sendTranslation("guild.upgrade.unlock.officer", newLevel.ordinal() + 1);
    });

    private String name;
    private GuildUpgradeLevel maxLevel;
    private ItemStack icon;
    private BiConsumer<Guild, GuildUpgradeLevel> onUpgrade;

    GuildUpgradeEnum(String name, GuildUpgradeLevel maxLevel, ItemStack icon, BiConsumer<Guild, GuildUpgradeLevel> onUpgrade) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.icon = icon;
        this.onUpgrade = onUpgrade;
    }

    public String getName(Language lang) {
        return lang.getTranslation("guild.upgrade.name." + name.toLowerCase()).translate().getMessage();
    }

    public GuildUpgradeLevel getMaxLevel() {
        return maxLevel;
    }

    public void onUpgrade(Guild guild, GuildUpgradeLevel newLevel) {
        onUpgrade.accept(guild, newLevel);
    }

    public int getPrice(GuildUpgradeLevel newLevel) {
        switch (this) {
            case VAULT:
                return ArterionPlugin.getInstance().getFormulaManager().GUILD_UPGRADE_VAULT_PRICE.get(newLevel.name()).evaluateInt();
            case REGION:
                return ArterionPlugin.getInstance().getFormulaManager().GUILD_UPGRADE_CLAIM_PRICE.get(newLevel.name()).evaluateInt();
            case OFFICER:
                return ArterionPlugin.getInstance().getFormulaManager().GUILD_UPGRADE_OFFICER_PRICE.get(newLevel.name()).evaluateInt();
        }
        return 0;
    }

    public int getValue(GuildUpgradeLevel level) {
        switch (this) {
            case VAULT:
                return ArterionPlugin.getInstance().getFormulaManager().GUILD_VAULT_ROWS.get(level.name()).evaluateInt();
            case REGION:
                return ArterionPlugin.getInstance().getFormulaManager().GUILD_CLAIM_SIZE.get(level.name()).evaluateInt();
            case OFFICER:
                return ArterionPlugin.getInstance().getFormulaManager().GUILD_OFFICER_SIZE.get(level.name()).evaluateInt();
        }
        return 0;
    }

    public ItemStack getIcon(Guild guild, Language lang) {
        GuildUpgradeLevel current = guild.getUpgradeLevel(this);
        if (current.ordinal() >= getMaxLevel().ordinal()) {
            String name = getName(lang);
            String level = lang.getTranslation("guild.upgrade.maxlevel").translate().getMessage();
            String desc = lang.getTranslation("guild.upgrade.desc." + name().toLowerCase()).translate(getValue(current)).getMessage();
            ItemStack alter = icon.clone();
            alter.setAmount(current.ordinal() + 1);
            return NamedItemUtil.modify(alter, name, level, "", desc);
        } else {
            String name = getName(lang);
            String level = lang.getTranslation("guild.upgrade.level").translate(current.ordinal() + 1).getMessage();
            String desc = lang.getTranslation("guild.upgrade.desc." + name().toLowerCase()).translate(getValue(current)).getMessage();
            String nextdesc = lang.getTranslation("guild.upgrade.descnext." + name().toLowerCase()).translate(getValue(current.next()) - getValue(current)).getMessage();
            String price = lang.getTranslation("guild.upgrade.cost").translate(getPrice(current.next()) / 100d).getMessage();
            ItemStack alter = icon.clone();
            alter.setAmount(current.ordinal() + 1);
            return NamedItemUtil.modify(alter, name, level, "", desc, nextdesc, price);
        }
    }
}
