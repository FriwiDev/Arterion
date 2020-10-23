package me.friwi.arterion.plugin.combat.quest;

import com.google.common.collect.ImmutableList;
import me.friwi.arterion.plugin.combat.quest.goal.DeliverItemQuestGoal;
import me.friwi.arterion.plugin.combat.quest.goal.EntityKillQuestGoal;
import me.friwi.arterion.plugin.combat.quest.goal.MineBlockQuestGoal;
import me.friwi.arterion.plugin.combat.quest.reward.GoldQuestReward;
import me.friwi.arterion.plugin.combat.quest.reward.ItemQuestReward;
import me.friwi.arterion.plugin.combat.quest.reward.XPQuestReward;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class QuestEnum {
    public static final int QUEST_AMOUNT = 50;

    public static Quest getQuestById(int id) {
        switch (id) {
            case 0:
                return new Quest(0, "pumpkinhunter", ImmutableList.of(
                        new MineBlockQuestGoal(12, Material.PUMPKIN),
                        new DeliverItemQuestGoal(new ItemStack(Material.PUMPKIN, 9))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_INGOT, 4)),
                        new ItemQuestReward(new ItemStack(Material.PUMPKIN, 7))
                ));
            case 1:
                return new Quest(1, "enderkiller", ImmutableList.of(
                        new EntityKillQuestGoal(24, EntityType.ENDERMAN),
                        new DeliverItemQuestGoal(new ItemStack(Material.ENDER_PEARL, 11))
                ), ImmutableList.of(
                        new XPQuestReward(750 * 2),
                        new ItemQuestReward(new ItemStack(Material.ENDER_PEARL, 8)),
                        new ItemQuestReward(new ItemStack(Material.EYE_OF_ENDER, 1))
                ));
            case 2:
                return new Quest(2, "slaveworker", ImmutableList.of(
                        new MineBlockQuestGoal(896, Material.STONE),
                        new DeliverItemQuestGoal(new ItemStack(Material.COBBLESTONE, 896))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.STONE, 704)),
                        new ItemQuestReward(new ItemStack(Material.DIAMOND, 2))
                ));
            case 3:
                return new Quest(3, "killerinstinct", ImmutableList.of(
                        new EntityKillQuestGoal(55, EntityType.ZOMBIE),
                        new EntityKillQuestGoal(23, EntityType.SKELETON),
                        new EntityKillQuestGoal(12, EntityType.SPIDER)
                ), ImmutableList.of(
                        new XPQuestReward(4000),
                        new ItemQuestReward(new ItemStack(Material.BLAZE_ROD, 5))
                ));
            case 4:
                return new Quest(4, "shroomeater", ImmutableList.of(
                        new MineBlockQuestGoal(12, Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_INGOT, 5)),
                        new ItemQuestReward(new ItemStack(Material.DIAMOND, 2))
                ));
            case 5:
                return new Quest(5, "woodhunter", ImmutableList.of(
                        new MineBlockQuestGoal(256, Material.LOG, Material.LOG_2)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.APPLE, 28)),
                        new ItemQuestReward(new ItemStack(Material.LOG_2, 64, (byte) 1))
                ));
            case 6:
                return new Quest(6, "sandworker", ImmutableList.of(
                        new MineBlockQuestGoal(576, Material.SAND),
                        new MineBlockQuestGoal(128, Material.SANDSTONE),
                        new DeliverItemQuestGoal(new ItemStack(Material.SAND, 576))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.GLASS_BOTTLE, 384))
                ));
            case 7:
                return new Quest(7, "mineworker", ImmutableList.of(
                        new MineBlockQuestGoal(24, Material.COAL_ORE),
                        new MineBlockQuestGoal(48, Material.IRON_ORE),
                        new MineBlockQuestGoal(20, Material.GOLD_ORE),
                        new DeliverItemQuestGoal(new ItemStack(Material.COAL, 12)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_ORE, 48)),
                        new DeliverItemQuestGoal(new ItemStack(Material.GOLD_ORE, 20))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_INGOT, 48)),
                        new ItemQuestReward(new ItemStack(Material.GOLD_INGOT, 20))
                ));
            case 8:
                ItemStack ret = new ItemStack(Material.DIAMOND_SWORD);
                ret.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                return new Quest(8, "killerweapon", ImmutableList.of(
                        new MineBlockQuestGoal(2, Material.DIAMOND_ORE),
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND_SWORD, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(ret)
                ));
            case 9:
                return new Quest(9, "brewer", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.POTION, 12, (short) 8265)),
                        new DeliverItemQuestGoal(new ItemStack(Material.POTION, 15, (short) 8258)),
                        new DeliverItemQuestGoal(new ItemStack(Material.POTION, 6, (short) 8259))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.POTION, 6, (short) 8233)),
                        new ItemQuestReward(new ItemStack(Material.POTION, 6, (short) 8265)),
                        new ItemQuestReward(new ItemStack(Material.POTION, 8, (short) 8226)),
                        new ItemQuestReward(new ItemStack(Material.POTION, 7, (short) 8258)),
                        new ItemQuestReward(new ItemStack(Material.POTION, 4, (short) 8259))
                ));


            case 10:
                return new Quest(10, "dirtfarmer", ImmutableList.of(
                        new MineBlockQuestGoal(512, Material.DIRT, Material.GRASS),
                        new DeliverItemQuestGoal(new ItemStack(Material.DIRT, 512))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.GRASS, 128)),
                        new ItemQuestReward(new ItemStack(Material.DIRT, 250)),
                        new ItemQuestReward(new ItemStack(Material.EXP_BOTTLE, 6))
                ));
            case 11:
                return new Quest(11, "carrotfan", ImmutableList.of(
                        new MineBlockQuestGoal(256, Material.CARROT),
                        new DeliverItemQuestGoal(new ItemStack(Material.CARROT_ITEM, 256))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.CARROT_ITEM, 228)),
                        new ItemQuestReward(new ItemStack(Material.GOLDEN_CARROT, 28)),
                        new XPQuestReward(500)
                ));
            case 12:
                return new Quest(12, "elitekiller", ImmutableList.of(
                        new EntityKillQuestGoal(70, EntityType.ZOMBIE),
                        new EntityKillQuestGoal(45, EntityType.SKELETON),
                        new DeliverItemQuestGoal(new ItemStack(Material.ROTTEN_FLESH, 128))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.BREAD, 64)),
                        new ItemQuestReward(new ItemStack(Material.DIAMOND, 5)),
                        new XPQuestReward(5000)
                ));
            case 13:
                return new Quest(13, "shepherd", ImmutableList.of(
                        new EntityKillQuestGoal(10, EntityType.SHEEP),
                        new DeliverItemQuestGoal(new ItemStack(Material.WOOL, 32))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.COOKED_MUTTON, 12)),
                        new ItemQuestReward(new ItemStack(Material.WOOL, 10, (byte) 14)),
                        new ItemQuestReward(new ItemStack(Material.WOOL, 22)),
                        new XPQuestReward(500)
                ));
            case 14:
                /*ItemStack twoSharpTwo = new ItemStack(Material.ENCHANTED_BOOK, 2);
                twoSharpTwo.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
                ItemStack twoProtOne = new ItemStack(Material.ENCHANTED_BOOK, 2);
                twoProtOne.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                ItemStack oneFallOne = new ItemStack(Material.ENCHANTED_BOOK);
                oneFallOne.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 1);
                ItemStack oneDurabilityTwo = new ItemStack(Material.ENCHANTED_BOOK);
                oneDurabilityTwo.addUnsafeEnchantment(Enchantment.DURABILITY, 2);

                ret = new ItemStack(Material.ENCHANTED_BOOK);
                ret.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
                return new Quest(14, "bookworm", ImmutableList.of(
                        new DeliverItemQuestGoal(twoSharpTwo),
                        new DeliverItemQuestGoal(twoProtOne),
                        new DeliverItemQuestGoal(oneFallOne),
                        new DeliverItemQuestGoal(oneDurabilityTwo)
                ), ImmutableList.of(
                        new ItemQuestReward(ret),
                        new XPQuestReward(2000)
                ));*/
                return new Quest(14, "dungeon", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND, 8)),
                        new DeliverItemQuestGoal(new ItemStack(Material.REDSTONE, 7)),
                        new DeliverItemQuestGoal(new ItemStack(Material.EMERALD, 2)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_ORE, 4))
                ), ImmutableList.of(
                        new ItemQuestReward(new MorgothDungeonKeyItem().toItemStack()),
                        new XPQuestReward(750)
                ));
            case 15:
                return new Quest(15, "spiderkiller", ImmutableList.of(
                        new EntityKillQuestGoal(50, EntityType.SPIDER),
                        new DeliverItemQuestGoal(new ItemStack(Material.STRING, 25))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.WEB, 15))
                ));
            case 16:
                return new Quest(16, "bucket", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.LAVA_BUCKET, 13)),
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND, 2))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.OBSIDIAN, 18))
                ));
            case 17:
                return new Quest(17, "alchemist", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.NETHER_STALK, 20)),
                        new DeliverItemQuestGoal(new ItemStack(Material.GLASS_BOTTLE, 16)),
                        new DeliverItemQuestGoal(new ItemStack(Material.SUGAR, 10)),
                        new DeliverItemQuestGoal(new ItemStack(Material.MAGMA_CREAM, 3)),
                        new DeliverItemQuestGoal(new ItemStack(Material.REDSTONE, 6))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.POTION, 12, (short) 8258)),
                        new ItemQuestReward(new ItemStack(Material.POTION, 12, (short) 8259)),
                        new XPQuestReward(350)
                ));
            case 18:
                return new Quest(18, "slavework", ImmutableList.of(
                        new MineBlockQuestGoal(128, Material.STONE),
                        new DeliverItemQuestGoal(new ItemStack(Material.COBBLESTONE, 128))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_INGOT, 5)),
                        new GoldQuestReward(25000),
                        new XPQuestReward(1500)
                ));
            case 19:
                return new Quest(19, "guardiankiller", ImmutableList.of(
                        new EntityKillQuestGoal(20, EntityType.GUARDIAN)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.DIAMOND, 4)),
                        new GoldQuestReward(5000),
                        new XPQuestReward(1250)
                ));


            case 20:
                return new Quest(20, "hellfire", ImmutableList.of(
                        new MineBlockQuestGoal(128, Material.QUARTZ_ORE),
                        new DeliverItemQuestGoal(new ItemStack(Material.QUARTZ, 128))
                ), ImmutableList.of(
                        new GoldQuestReward(30000),
                        new XPQuestReward(500)
                ));
            case 21:
                return new Quest(21, "hellkiller", ImmutableList.of(
                        new EntityKillQuestGoal(75, EntityType.PIG_ZOMBIE)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.GOLD_INGOT, 3)),
                        new GoldQuestReward(25000),
                        new XPQuestReward(750)
                ));
            case 22:
                return new Quest(22, "blacksmith", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.FURNACE, 3)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_HELMET, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_CHESTPLATE, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_LEGGINGS, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_BOOTS, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_INGOT, 10)),
                        new XPQuestReward(3000)
                ));
            case 23:
                /*ItemStack oneSharpOne = new ItemStack(Material.ENCHANTED_BOOK);
                oneSharpOne.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                return new Quest(23, "enchanter", ImmutableList.of(
                        new DeliverItemQuestGoal(oneSharpOne)
                ), ImmutableList.of(
                        new GoldQuestReward(10000),
                        new XPQuestReward(400)
                ));*/
            case 24:
                return new Quest(24, "hunter", ImmutableList.of(
                        new EntityKillQuestGoal(5, EntityType.COW),
                        new EntityKillQuestGoal(3, EntityType.SHEEP),
                        new EntityKillQuestGoal(5, EntityType.CHICKEN),
                        new EntityKillQuestGoal(2, EntityType.PIG)
                ), ImmutableList.of(
                        new GoldQuestReward(15000),
                        new XPQuestReward(300)
                ));
            case 25:
                return new Quest(25, "mountainworker", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND, 10))
                ), ImmutableList.of(
                        new GoldQuestReward(25000),
                        new XPQuestReward(750)
                ));
            case 26:
                return new Quest(26, "rubbish", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.ROTTEN_FLESH, 64)),
                        new DeliverItemQuestGoal(new ItemStack(Material.SPIDER_EYE, 15)),
                        new DeliverItemQuestGoal(new ItemStack(Material.SULPHUR, 20)),
                        new DeliverItemQuestGoal(new ItemStack(Material.BONE, 15))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.GOLD_INGOT, 5)),
                        new GoldQuestReward(5000)
                ));
            case 27:
                return new Quest(27, "bankier", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND, 2)),
                        new DeliverItemQuestGoal(new ItemStack(Material.GOLD_INGOT, 5)),
                        new DeliverItemQuestGoal(new ItemStack(Material.IRON_INGOT, 20))
                ), ImmutableList.of(
                        new XPQuestReward(3100)
                ));
            case 28:
                return new Quest(28, "tool_blacksmith", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND_PICKAXE, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.DIAMOND_AXE, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.DIAMOND, 5)),
                        new XPQuestReward(400)
                ));
            case 29:
                return new Quest(29, "leveldesert", ImmutableList.of(
                        new MineBlockQuestGoal(400, Material.SAND),
                        new DeliverItemQuestGoal(new ItemStack(Material.SAND, 200))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.DIAMOND_SPADE, 1)),
                        new GoldQuestReward(5000),
                        new XPQuestReward(1000)
                ));


            case 30:
                return new Quest(30, "scientist", ImmutableList.of(
                        new MineBlockQuestGoal(3, Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2),
                        new MineBlockQuestGoal(3, Material.QUARTZ_ORE),
                        new MineBlockQuestGoal(3, Material.SAND),
                        new MineBlockQuestGoal(3, Material.LOG, Material.LOG_2)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.GOLD_INGOT, 10)),
                        new GoldQuestReward(25000),
                        new XPQuestReward(1000)
                ));
            case 31:
                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.OXYGEN, 3);
                return new Quest(31, "diver", ImmutableList.of(
                        new MineBlockQuestGoal(4, Material.PRISMARINE),
                        new DeliverItemQuestGoal(new ItemStack(Material.PRISMARINE_SHARD, 2))
                ), ImmutableList.of(
                        new ItemQuestReward(helmet),
                        new XPQuestReward(950)
                ));
            case 32:
                return new Quest(32, "flowerfarmer", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.RED_ROSE, 10)),
                        new DeliverItemQuestGoal(new ItemStack(Material.YELLOW_FLOWER, 10))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.FLOWER_POT_ITEM, 3)),
                        new GoldQuestReward(2000),
                        new XPQuestReward(450)
                ));
            case 33:
                ItemStack oneSharpTwo = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) oneSharpTwo.getItemMeta();
                meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 2, true);
                oneSharpTwo.setItemMeta(meta);
                ItemStack oneDurabilityTwo = new ItemStack(Material.ENCHANTED_BOOK);
                meta = (EnchantmentStorageMeta) oneDurabilityTwo.getItemMeta();
                meta.addStoredEnchant(Enchantment.DURABILITY, 2, true);
                oneDurabilityTwo.setItemMeta(meta);
                return new Quest(33, "mage", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.ENCHANTMENT_TABLE, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(oneSharpTwo),
                        new ItemQuestReward(oneDurabilityTwo),
                        new XPQuestReward(350)
                ));
            case 34:
                ItemStack oneFireTwo = new ItemStack(Material.ENCHANTED_BOOK);
                meta = (EnchantmentStorageMeta) oneFireTwo.getItemMeta();
                meta.addStoredEnchant(Enchantment.FIRE_ASPECT, 2, true);
                oneFireTwo.setItemMeta(meta);
                return new Quest(34, "playwithfire", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.LAVA_BUCKET, 5))
                ), ImmutableList.of(
                        new ItemQuestReward(oneFireTwo)
                ));
            case 35:
                return new Quest(35, "ghosts", ImmutableList.of(
                        new EntityKillQuestGoal(3, EntityType.GHAST)
                ), ImmutableList.of(
                        new GoldQuestReward(5000),
                        new XPQuestReward(550),
                        new ItemQuestReward(new ItemStack(Material.SPONGE, 1))
                ));
            case 36:
                ItemStack oneLureTwo = new ItemStack(Material.ENCHANTED_BOOK);
                oneLureTwo.addUnsafeEnchantment(Enchantment.LURE, 2);
                meta = (EnchantmentStorageMeta) oneLureTwo.getItemMeta();
                meta.addStoredEnchant(Enchantment.LURE, 2, true);
                oneLureTwo.setItemMeta(meta);
                return new Quest(36, "fisher", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.RAW_FISH, 16)),
                        new DeliverItemQuestGoal(new ItemStack(Material.RAW_FISH, 8, (short) 1))
                ), ImmutableList.of(
                        new ItemQuestReward(oneLureTwo),
                        new XPQuestReward(100)
                ));
            case 37:
                return new Quest(37, "horsefarmer", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.APPLE, 8)),
                        new DeliverItemQuestGoal(new ItemStack(Material.WHEAT, 32))
                ), ImmutableList.of(
                        new GoldQuestReward(2000),
                        new XPQuestReward(350),
                        new ItemQuestReward(new ItemStack(Material.SADDLE, 1))
                ));
            case 38:
                return new Quest(38, "good_deal", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.ANVIL, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.ANVIL, 1, (short) 2)),
                        new XPQuestReward(3500)
                ));
            case 39:
                return new Quest(39, "shroomstew", ImmutableList.of(
                        new MineBlockQuestGoal(16, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM),
                        new DeliverItemQuestGoal(new ItemStack(Material.BOWL, 8)),
                        new DeliverItemQuestGoal(new ItemStack(Material.RED_MUSHROOM, 8)),
                        new DeliverItemQuestGoal(new ItemStack(Material.BROWN_MUSHROOM, 8))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.APPLE, 1)),
                        new ItemQuestReward(new ItemStack(Material.GOLD_INGOT, 8)),
                        new XPQuestReward(1200)
                ));


            case 40:
                return new Quest(40, "welcometohell", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.OBSIDIAN, 10)),
                        new DeliverItemQuestGoal(new ItemStack(Material.FLINT_AND_STEEL, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.POTION, 1, (short) 8259)),
                        new XPQuestReward(600)
                ));
            case 41:
                return new Quest(41, "furnishings", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.CHEST, 2)),
                        new DeliverItemQuestGoal(new ItemStack(Material.BED, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.FURNACE, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.WORKBENCH, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.PAINTING, 1))
                ), ImmutableList.of(
                        new XPQuestReward(1250)
                ));
            case 42:
                return new Quest(42, "woodworker", ImmutableList.of(
                        new MineBlockQuestGoal(9, Material.LOG, Material.LOG_2),
                        new DeliverItemQuestGoal(new ItemStack(Material.SAPLING, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.SAPLING, 1, (short) 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.SAPLING, 1, (short) 2))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.SAPLING, 1, (short) 3)),
                        new ItemQuestReward(new ItemStack(Material.SAPLING, 1, (short) 4)),
                        new XPQuestReward(300),
                        new GoldQuestReward(5000)
                ));
            case 43:
                return new Quest(43, "orientation", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.COMPASS, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.WATCH, 1))
                ), ImmutableList.of(
                        new XPQuestReward(1000)
                ));
            case 44:
                return new Quest(44, "upgrade", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.LEATHER_HELMET, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.LEATHER_CHESTPLATE, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.LEATHER_LEGGINGS, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.LEATHER_BOOTS, 1))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_HELMET, 1)),
                        new ItemQuestReward(new ItemStack(Material.IRON_CHESTPLATE, 1)),
                        new ItemQuestReward(new ItemStack(Material.IRON_LEGGINGS, 1)),
                        new ItemQuestReward(new ItemStack(Material.IRON_BOOTS, 1))
                ));
            case 45:
                return new Quest(45, "skeletonkiller", ImmutableList.of(
                        new EntityKillQuestGoal(25, EntityType.SKELETON),
                        new DeliverItemQuestGoal(new ItemStack(Material.BOW, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.BONE, 6))
                ), ImmutableList.of(
                        new XPQuestReward(1285),
                        new GoldQuestReward(3000)
                ));
            case 46:
                return new Quest(46, "wheatbuyer", ImmutableList.of(
                        new MineBlockQuestGoal(120, Material.LONG_GRASS)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.SEEDS, 40)),
                        new XPQuestReward(350)
                ));
            case 47:
                return new Quest(47, "firestone", ImmutableList.of(
                        new MineBlockQuestGoal(40, Material.GRAVEL)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.FLINT, 5)),
                        new XPQuestReward(600)
                ));
            case 48:
                return new Quest(48, "redstonegate", ImmutableList.of(
                        new DeliverItemQuestGoal(new ItemStack(Material.REDSTONE, 16)),
                        new DeliverItemQuestGoal(new ItemStack(Material.DIODE, 2)),
                        new DeliverItemQuestGoal(new ItemStack(Material.STONE_BUTTON, 1)),
                        new DeliverItemQuestGoal(new ItemStack(Material.PISTON_BASE, 2)),
                        new DeliverItemQuestGoal(new ItemStack(Material.REDSTONE_TORCH_ON, 2))
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.IRON_DOOR, 1)),
                        new XPQuestReward(980)
                ));
            case 49:
                return new Quest(49, "witchhunt", ImmutableList.of(
                        new EntityKillQuestGoal(3, EntityType.WITCH)
                ), ImmutableList.of(
                        new ItemQuestReward(new ItemStack(Material.BLAZE_ROD, 1)),
                        new XPQuestReward(790)
                ));
        }
        return null;
    }

    public static Quest getRandomQuest() {
        return getQuestById((int) (Math.random() * QUEST_AMOUNT));
    }
}
