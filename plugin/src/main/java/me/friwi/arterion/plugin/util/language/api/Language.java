package me.friwi.arterion.plugin.util.language.api;

import com.meowj.langutils.lang.LanguageHelper;
import com.meowj.langutils.lang.convert.EnumItem;
import com.meowj.langutils.lang.convert.EnumLang;
import com.meowj.langutils.lang.convert.ItemEntry;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.config.api.ConfigAPI;
import me.friwi.arterion.plugin.util.config.api.ConfigHashmap;
import me.friwi.arterion.plugin.util.config.api.Configureable;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.TranslationBuilder;
import me.friwi.arterion.plugin.util.language.translateables.ArterionPlayerStringTranslateableConverter;
import me.friwi.arterion.plugin.util.language.translateables.GuildStringTranslateableConverter;
import me.friwi.arterion.plugin.util.language.translateables.StringTranslateable;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Configureable
public class Language {
    public final Translation AND;
    String lang;
    @ConfigHashmap
    Map<String, String> map;
    Map<String, Translation> transMap = new TreeMap<>();
    private EnumLang enumLang;
    private TypeConverter<ArterionPlayer, StringTranslateable> conv1 = new ArterionPlayerStringTranslateableConverter();
    private TypeConverter<Guild, StringTranslateable> conv2 = new GuildStringTranslateableConverter();

    public Language(String lang) {
        this.lang = lang;
        ConfigAPI.readConfig(this, Language.class.getResourceAsStream("/" + lang + ".txt"));
        buildTranslations();
        this.AND = getTranslation("and");
        enumLang = EnumLang.get(lang);
    }

    private void buildTranslations() {
        for (Map.Entry<String, String> e : map.entrySet()) {
            transMap.put(e.getKey(), TranslationBuilder.buildTranslation(this, e.getValue()));
        }
    }

    public String getLocale() {
        return lang;
    }

    public Translation getTranslation(String key) {
        return transMap.get(key);
    }

    public EnumLang getEnumLang() {
        return enumLang;
    }

    public String translateObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Guild) {
            return conv2.convert((Guild) obj).getCaption(this, "", "");
        }
        if (obj instanceof Player) {
            obj = ArterionPlayerUtil.get((Player) obj);
        }
        if (obj instanceof ArterionPlayer) {
            return conv1.convert((ArterionPlayer) obj).getCaption(this, "", "");
        }
        if (obj instanceof DatabasePlayer) {
            return this.getTranslation(((DatabasePlayer) obj).getRank().getRankTranslation()).translate(((DatabasePlayer) obj).getName()).getMessage() + ((DatabasePlayer) obj).getName();
        }
        if (obj instanceof Villager) {
            DatabasePlayer player = ArterionPlugin.getInstance().getCombatLoggingHandler().getDatabasePlayer((Villager) obj);
            if (player != null) {
                return this.getTranslation(player.getRank().getRankTranslation()).translate(player.getName()).getMessage() + player.getName();
            }
        }
        if (obj instanceof Item) {
            obj = ((Item) obj).getItemStack();
        }
        if (obj instanceof Entity) {
            return LanguageHelper.getEntityDisplayName((Entity) obj, enumLang);
        }
        if (obj instanceof EntityType) {
            return LanguageHelper.getEntityName((EntityType) obj, enumLang);
        }
        if (obj instanceof Block) {
            return LanguageHelper.translateToLocal(EnumItem.get(new ItemEntry(((Block) obj).getType())).getUnlocalizedName(), enumLang);
        }
        if (obj instanceof Material) {
            return LanguageHelper.translateToLocal(EnumItem.get(new ItemEntry((Material) obj)).getUnlocalizedName(), enumLang);
        }
        if (obj instanceof ItemStack) {
            String enchs = "";
            if (!((ItemStack) obj).getEnchantments().isEmpty()) {
                enchs = " (";
                int size = ((ItemStack) obj).getEnchantments().size();
                int i = 0;
                for (Map.Entry<Enchantment, Integer> ent : ((ItemStack) obj).getEnchantments().entrySet()) {
                    enchs += LanguageHelper.getEnchantmentDisplayName(ent.getKey(), ent.getValue(), enumLang);
                    if (i < size - 1) enchs += ", ";
                    i++;
                }
                enchs += ")";
            }
            ItemMeta meta1 = ((ItemStack) obj).getItemMeta();
            if (meta1 instanceof EnchantmentStorageMeta) {
                if (!((EnchantmentStorageMeta) meta1).getStoredEnchants().isEmpty()) {
                    enchs = " (";
                    int size = ((EnchantmentStorageMeta) meta1).getStoredEnchants().size();
                    int i = 0;
                    for (Map.Entry<Enchantment, Integer> ent : ((EnchantmentStorageMeta) meta1).getStoredEnchants().entrySet()) {
                        enchs += LanguageHelper.getEnchantmentDisplayName(ent.getKey(), ent.getValue(), enumLang);
                        if (i < size - 1) enchs += ", ";
                        i++;
                    }
                    enchs += ")";
                }
            }
            if (((ItemStack) obj).getType() == Material.POTION) {
                try {
                    PotionMeta meta = (PotionMeta) ((ItemStack) obj).getItemMeta();
                    List<PotionEffect> effects = meta.getCustomEffects();
                    if (effects != null) {
                        for (PotionEffect ent : effects) {
                            enchs += " " + LanguageHelper.getEnchantmentLevelName(ent.getAmplifier() + 1, enumLang) + " (";
                            enchs += TimeFormatUtil.formatSeconds(ent.getDuration() / 20) + ")";
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return LanguageHelper.getItemDisplayName((ItemStack) obj, enumLang) + enchs;
        }
        if (obj instanceof Enchantment) {
            return LanguageHelper.getEnchantmentName((Enchantment) obj, enumLang);
        }
        if (obj instanceof Map.Entry) {
            if (((Map.Entry) obj).getKey() instanceof Enchantment && ((Map.Entry) obj).getValue() instanceof Integer) {
                return LanguageHelper.getEnchantmentDisplayName((Map.Entry<Enchantment, Integer>) obj, enumLang);
            }
        }
        return obj.toString();
    }
}