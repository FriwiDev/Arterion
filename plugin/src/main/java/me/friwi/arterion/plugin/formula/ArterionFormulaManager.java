package me.friwi.arterion.plugin.formula;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.gamemode.artefact.ArtefactDrops;
import me.friwi.arterion.plugin.combat.group.Group;
import me.friwi.arterion.plugin.combat.pvpchest.PvPChestDrops;
import me.friwi.arterion.plugin.combat.skill.SkillEnum;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.guild.GuildUpgradeLevel;
import me.friwi.arterion.plugin.jobs.FarmerDrops;
import me.friwi.arterion.plugin.jobs.FisherDrops;
import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.jobs.WoodworkerDrops;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.config.api.ConfigAPI;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseFormula;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArterionFormulaManager {
    /**
     * Register all available bindings here
     */
    public final ReflectionBinding BIND_PLAYER = new ReflectionBinding("player", ArterionPlayer.class);
    public final ReflectionBinding BIND_GROUP = new ReflectionBinding("group", Group.class);
    public final ReflectionBinding BIND_GUILD = new ReflectionBinding("guild", Guild.class);
    public final ReflectionBinding BIND_OBSIDIAN = new ReflectionBinding("obsidian", Integer.class);
    public final ReflectionBinding BIND_PERLEVEL = new ReflectionBinding("perlevel", Integer.class);
    public final ReflectionBinding BIND_JOBLEVEL = new ReflectionBinding("joblevel", Integer.class);
    public final ReflectionBinding BIND_ENCHCOUNT = new ReflectionBinding("enchcount", Integer.class);
    public final ReflectionBinding BIND_DURABILITYLOST = new ReflectionBinding("durabilitylost", Integer.class);
    public final ReflectionBinding BIND_RANDOM = new ReflectionBinding("random", Double.class);
    public final ReflectionBinding BIND_BOW_CHARGE = new ReflectionBinding("bowcharge", Double.class);
    public final ReflectionBinding BIND_DISTANCE = new ReflectionBinding("distance", Double.class);
    public final ReflectionBinding BIND_DMGALL = new ReflectionBinding("dmgall", Integer.class);
    public final ReflectionBinding BIND_SMITE = new ReflectionBinding("smite", Integer.class);
    public final ReflectionBinding BIND_ARTHROPODS = new ReflectionBinding("arthropods", Integer.class);
    public final ReflectionBinding BIND_ARROWDMG = new ReflectionBinding("arrowdmg", Integer.class);
    public final ReflectionBinding BIND_PROTECTION = new ReflectionBinding("protection", Integer.class);
    public final ReflectionBinding BIND_FIRE = new ReflectionBinding("fire", Integer.class);
    public final ReflectionBinding BIND_FALL = new ReflectionBinding("fall", Integer.class);
    public final ReflectionBinding BIND_EXPLOSIONS = new ReflectionBinding("explosions", Integer.class);
    public final ReflectionBinding BIND_PROJECTILE = new ReflectionBinding("projectile", Integer.class);
    public final ReflectionBinding BIND_FALLDISTANCE = new ReflectionBinding("falldistance", Integer.class);
    public final ReflectionBinding BIND_POTIONLEVEL = new ReflectionBinding("potionlevel", Integer.class);
    public final ReflectionBinding BIND_STRENGTHLEVEL = new ReflectionBinding("strengthlevel", Integer.class);
    public final ReflectionBinding BIND_WEAKNESSLEVEL = new ReflectionBinding("weaknesslevel", Integer.class);
    public final ReflectionBinding BIND_MAXHEALTH = new ReflectionBinding("maxhealth", Integer.class);
    public final ReflectionBinding BIND_DAMAGE = new ReflectionBinding("damage", Double.class);
    public final ReflectionBinding BIND_KILLER = new ReflectionBinding("killer", DatabasePlayer.class);
    public final ReflectionBinding BIND_KILLED = new ReflectionBinding("killed", DatabasePlayer.class);
    public final ReflectionBinding BIND_REPAIRCOUNT = new ReflectionBinding("repaircount", Integer.class);
    public final ReflectionBinding BIND_PRESTIGE_POINTS = new ReflectionBinding("prestigepoints", Integer.class);

    /**
     * Register your formulas here
     */
    public final ArterionFormula EXPLOSION_REGEN_DELAY = new ArterionFormula("explosion.regen.delay");
    public final ArterionFormula EXPLOSION_REGEN_SPEED = new ArterionFormula("explosion.regen.speed");
    public final ArterionFormula UI_TITLE_FADEIN = new ArterionFormula("ui.title.fadein");
    public final ArterionFormula UI_TITLE_SHOW = new ArterionFormula("ui.title.show");
    public final ArterionFormula UI_TITLE_FADEOUT = new ArterionFormula("ui.title.fadeout");
    public final ArterionFormula PLAYER_BAG_INITIAL = new ArterionFormula("player.bag.initial");
    public final ArterionFormula PLAYER_BANK_LIMIT = new ArterionFormula("player.bank.limit", BIND_PLAYER);
    public final ArterionFormula PLAYER_AD_DELAY = new ArterionFormula("player.ad.delay", BIND_PLAYER);
    public final ArterionFormula PLAYER_AD_FEE = new ArterionFormula("player.ad.fee", BIND_PLAYER);
    public final ArterionFormula PLAYER_INVITE_TIMEOUT = new ArterionFormula("player.invite.timeout");

    public final ArterionFormula PLAYER_LEVEL_CURVE = new ArterionFormula("player.level.curve", BIND_PERLEVEL);
    public final ArterionFormula PLAYER_LEVEL_MAX = new ArterionFormula("player.level.max");
    public final ArterionFormula PLAYER_PRESTIGE_LEVEL_CURVE = new ArterionFormula("player.prestige_level.curve", BIND_PERLEVEL);
    public final ArterionFormula PLAYER_PRESTIGE_LEVEL_MAX = new ArterionFormula("player.prestige_level.max");
    public final ArterionFormula PLAYER_CLASS_FREECHANGE = new ArterionFormula("player.class.freechange");
    public final ArterionFormula PLAYER_CLASS_PRICECHANGE = new ArterionFormula("player.class.pricechange");

    public final ArterionFormula PLAYER_HOMEBLOCK_FEE = new ArterionFormula("player.homeblock.fee", BIND_PLAYER);
    public final ArterionFormula PLAYER_HOMEBLOCK_MIN = new ArterionFormula("player.homeblock.min", BIND_PLAYER);
    public final ArterionFormula PLAYER_HOMEBLOCK_MAX = new ArterionFormula("player.homeblock.max", BIND_PLAYER);
    public final ArterionFormula PLAYER_HOMEBLOCK_MINY = new ArterionFormula("player.homeblock.min_y", BIND_PLAYER);
    public final ArterionFormula PLAYER_HOMEBLOCK_MAXY = new ArterionFormula("player.homeblock.max_y", BIND_PLAYER);
    public final ArterionFormula PLAYER_HOMEBLOCK_AUTOREMOVE = new ArterionFormula("player.homeblock.autoremove");
    public final ArterionFormula PLAYER_HOMEBLOCK_SHOWCLAIM = new ArterionFormula("player.homeblock.showclaim");

    public final ArterionFormula PLAYER_NEWBIE_PROTECTION = new ArterionFormula("player.newbie.protection");
    public final ArterionFormula PLAYER_COMBATLOG_DURATION = new ArterionFormula("player.combatlog.duration");
    public final ArterionFormula PLAYER_COMBAT_PLAYERDISTANCE = new ArterionFormula("player.combat.playerdistance");
    public final ArterionFormula PLAYER_COMBAT_MOBDISTANCE = new ArterionFormula("player.combat.mobdistance");

    public final ArterionFormula GROUP_MAXMEMBERS = new ArterionFormula("group.maxmembers");
    public final ArterionFormula GROUP_XP_MULTIPLIER = new ArterionFormula("group.xpmultiplier", BIND_GROUP);
    public final ArterionFormula GROUP_GOLD_MULTIPLIER = new ArterionFormula("group.goldmultiplier", BIND_GROUP);

    public final ArterionFormula GUILD_GUILDBLOCK_FEE = new ArterionFormula("guild.guildblock.fee", BIND_PLAYER);
    public final ArterionFormula GUILD_GUILDBLOCK_DISTANCE = new ArterionFormula("guild.guildblock.distance", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_FREE_SPACE_X_Z = new ArterionFormula("guild.guildblock.free_space_x_z", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_FREE_SPACE_Y = new ArterionFormula("guild.guildblock.free_space_y", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MIN = new ArterionFormula("guild.guildblock.min", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MAX = new ArterionFormula("guild.guildblock.max", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MINY = new ArterionFormula("guild.guildblock.min_y", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MAXY = new ArterionFormula("guild.guildblock.max_y", BIND_GUILD);
    public final ArterionFormula GUILD_MAXMEMBERS = new ArterionFormula("guild.maxmembers");
    public final ArterionFormula GUILD_XP_MULTIPLIER = new ArterionFormula("guild.xpmultiplier", BIND_GUILD);
    public final ArterionFormula GUILD_GOLD_MULTIPLIER = new ArterionFormula("guild.goldmultiplier", BIND_GUILD);
    public final ArterionFormulaArray GUILD_CLAIM_SIZE = new ArterionFormulaArray("guild.claim.size", GuildUpgradeLevel.values(), BIND_GUILD);
    public final ArterionFormulaArray GUILD_VAULT_ROWS = new ArterionFormulaArray("guild.vault.rows", GuildUpgradeLevel.values(), BIND_GUILD);
    public final ArterionFormulaArray GUILD_OFFICER_SIZE = new ArterionFormulaArray("guild.officer.size", GuildUpgradeLevel.values(), BIND_GUILD);
    public final ArterionFormulaArray GUILD_UPGRADE_CLAIM_PRICE = new ArterionFormulaArray("guild.upgrade.claim_prize", GuildUpgradeLevel.values(), BIND_GUILD);
    public final ArterionFormulaArray GUILD_UPGRADE_VAULT_PRICE = new ArterionFormulaArray("guild.upgrade.vault_prize", GuildUpgradeLevel.values(), BIND_GUILD);
    public final ArterionFormulaArray GUILD_UPGRADE_OFFICER_PRICE = new ArterionFormulaArray("guild.upgrade.officer_prize", GuildUpgradeLevel.values(), BIND_GUILD);
    public final ArterionFormula GUILD_VAULT_DROP = new ArterionFormula("guild.vault.drop", BIND_GUILD);
    public final ArterionFormula GUILD_CLAIM_COOLDOWN = new ArterionFormula("guild.claim.cooldown", BIND_GUILD);
    public final ArterionFormula GUILD_TAX = new ArterionFormula("guild.tax", BIND_OBSIDIAN);
    public final ArterionFormula GUILD_DISBAND_TIME = new ArterionFormula("guild.disband.time", BIND_GUILD);
    public final ArterionFormula GUILD_SEARCH_PRICE = new ArterionFormula("guild.search.price", BIND_GUILD);

    public final ArterionFormula FIGHT_GUILD_HOME_CD = new ArterionFormula("fight.guild.homecd");
    public final ArterionFormula FIGHT_STEAL_JACKPOT = new ArterionFormula("fight.steal.jackpot");
    public final ArterionFormula FIGHT_STEAL_ONCE = new ArterionFormula("fight.steal.once");
    public final ArterionFormula FIGHT_STEAL_DURATION = new ArterionFormula("fight.steal.duration");
    public final ArterionFormula FIGHT_STEAL_RANGE = new ArterionFormula("fight.steal.range");
    public final ArterionFormula FIGHT_FEE_MULTIPLIER = new ArterionFormula("fight.fee.multiplier");
    public final ArterionFormula FIGHT_DURATION = new ArterionFormula("fight.duration");
    public final ArterionFormula FIGHT_PRE_DURATION = new ArterionFormula("fight.pre.duration");
    public final ArterionFormula FIGHT_LOGIN_DEFENDER_DURATION = new ArterionFormula("fight.login.defender.duration");
    public final ArterionFormula FIGHT_POST_DURATION = new ArterionFormula("fight.post.duration");
    public final ArterionFormula FIGHT_GUILDBLOCK_HP = new ArterionFormula("fight.guildblock.hp", BIND_GUILD);
    public final ArterionFormulaArray FIGHT_GUILDBLOCK_DMG = new ArterionFormulaArray("fight.guildblock.dmg", new Object[]{Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE, Material.WOOD_PICKAXE, "other"});
    public final ArterionFormula FIGHT_ATTACKER_ADVANTAGE = new ArterionFormula("fight.attacker.advantage", BIND_GUILD);
    public final ArterionFormula FIGHT_NOENEMIES_COOLDOWN = new ArterionFormula("fight.noenemies.cooldown");
    public final ArterionFormula FIGHT_REPEAT_COOLDOWN = new ArterionFormula("fight.repeat.cooldown");
    public final ArterionFormula FIGHT_PROTECTION_COOLDOWN = new ArterionFormula("fight.protection.cooldown");
    public final ArterionFormula FIGHT_PROTECTION_INITIAL = new ArterionFormula("fight.protection.initial");
    public final ArterionFormula FIGHT_BLOCKDESTROY_DURATION = new ArterionFormula("fight.blockdestroy.duration");

    public final ArterionFormula BLACKSMITH_REPAIR_COST = new ArterionFormula("blacksmith.repair.cost", BIND_ENCHCOUNT, BIND_DURABILITYLOST, BIND_REPAIRCOUNT);

    public final ArterionFormulaArray DMG_ENTITY_MAXHEALTH = new ArterionFormulaArray("dmg.entity.maxhealth", new Object[]{
            EntityType.BAT,
            EntityType.BLAZE,
            EntityType.CAVE_SPIDER,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.CREEPER,
            EntityType.CREEPER + "_CHARGED",
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.GUARDIAN + "_ELDER",
            EntityType.HORSE,
            EntityType.IRON_GOLEM,
            EntityType.MAGMA_CUBE + "_1",
            EntityType.MAGMA_CUBE + "_2",
            EntityType.MAGMA_CUBE + "_4",
            EntityType.MUSHROOM_COW,
            EntityType.OCELOT,
            EntityType.PIG,
            EntityType.PIG_ZOMBIE,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.SLIME + "_1",
            EntityType.SLIME + "_2",
            EntityType.SLIME + "_4",
            EntityType.SNOWMAN,
            EntityType.SPIDER,
            EntityType.SQUID,
            EntityType.VILLAGER,
            EntityType.WITCH,
            EntityType.WITHER,
            EntityType.WITHER_SKULL,
            EntityType.WOLF,
            EntityType.ZOMBIE
    }, BIND_RANDOM);
    public final ArterionFormulaArray DMG_ENTITY_GOLD = new ArterionFormulaArray("dmg.entity.gold", new Object[]{
            EntityType.BAT,
            EntityType.BLAZE,
            EntityType.CAVE_SPIDER,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.CREEPER,
            EntityType.CREEPER + "_CHARGED",
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.GUARDIAN + "_ELDER",
            EntityType.HORSE,
            EntityType.IRON_GOLEM,
            EntityType.MAGMA_CUBE + "_1",
            EntityType.MAGMA_CUBE + "_2",
            EntityType.MAGMA_CUBE + "_4",
            EntityType.MUSHROOM_COW,
            EntityType.OCELOT,
            EntityType.PIG,
            EntityType.PIG_ZOMBIE,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.SLIME + "_1",
            EntityType.SLIME + "_2",
            EntityType.SLIME + "_4",
            EntityType.SNOWMAN,
            EntityType.SPIDER,
            EntityType.SQUID,
            EntityType.VILLAGER,
            EntityType.WITCH,
            EntityType.WITHER,
            EntityType.WITHER_SKULL,
            EntityType.WOLF,
            EntityType.ZOMBIE
    }, BIND_RANDOM);
    public final ArterionFormulaArray DMG_ENTITY_XP = new ArterionFormulaArray("dmg.entity.xp", new Object[]{
            EntityType.BAT,
            EntityType.BLAZE,
            EntityType.CAVE_SPIDER,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.CREEPER,
            EntityType.CREEPER + "_CHARGED",
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.GUARDIAN + "_ELDER",
            EntityType.HORSE,
            EntityType.IRON_GOLEM,
            EntityType.MAGMA_CUBE + "_1",
            EntityType.MAGMA_CUBE + "_2",
            EntityType.MAGMA_CUBE + "_4",
            EntityType.MUSHROOM_COW,
            EntityType.OCELOT,
            EntityType.PIG,
            EntityType.PIG_ZOMBIE,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.SLIME + "_1",
            EntityType.SLIME + "_2",
            EntityType.SLIME + "_4",
            EntityType.SNOWMAN,
            EntityType.SPIDER,
            EntityType.SQUID,
            EntityType.VILLAGER,
            EntityType.WITCH,
            EntityType.WITHER,
            EntityType.WITHER_SKULL,
            EntityType.WOLF,
            EntityType.ZOMBIE
    }, BIND_RANDOM);
    public final ArterionFormulaArray DMG_WEAPON = new ArterionFormulaArray("dmg.weapon", new Object[]{
            Material.DIAMOND_SWORD,
            Material.IRON_SWORD,
            Material.GOLD_SWORD,
            Material.STONE_SWORD,
            Material.WOOD_SWORD,

            Material.DIAMOND_HOE,
            Material.IRON_HOE,
            Material.GOLD_HOE,
            Material.STONE_HOE,
            Material.WOOD_HOE,

            Material.DIAMOND_AXE,
            Material.IRON_AXE,
            Material.GOLD_AXE,
            Material.STONE_AXE,
            Material.WOOD_AXE,

            Material.DIAMOND_SPADE,
            Material.IRON_SPADE,
            Material.GOLD_SPADE,
            Material.STONE_SPADE,
            Material.WOOD_SPADE,

            Material.STICK,

            Material.BOW,

            "other"
    }, BIND_RANDOM, BIND_DMGALL, BIND_SMITE, BIND_ARTHROPODS);
    public final ArterionFormulaArray DMG_ENTITY_ATTACK = new ArterionFormulaArray("dmg.entity.attack", new Object[]{
            EntityType.BLAZE,
            EntityType.CAVE_SPIDER,
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.GUARDIAN + "_ELDER",
            EntityType.IRON_GOLEM,
            EntityType.MAGMA_CUBE + "_1",
            EntityType.MAGMA_CUBE + "_2",
            EntityType.MAGMA_CUBE + "_4",
            EntityType.OCELOT,
            EntityType.PIG_ZOMBIE,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.SLIME + "_1",
            EntityType.SLIME + "_2",
            EntityType.SLIME + "_4",
            EntityType.SNOWMAN,
            EntityType.SPIDER,
            EntityType.WITCH,
            EntityType.WITHER,
            EntityType.WITHER_SKULL,
            EntityType.WOLF,
            EntityType.ZOMBIE
    }, BIND_RANDOM);
    public final ArterionFormulaArray DMG_CRITICAL_WEAPON = new ArterionFormulaArray("dmg.critical.weapon", new Object[]{
            Material.DIAMOND_SWORD,
            Material.IRON_SWORD,
            Material.GOLD_SWORD,
            Material.STONE_SWORD,
            Material.WOOD_SWORD,

            Material.DIAMOND_HOE,
            Material.IRON_HOE,
            Material.GOLD_HOE,
            Material.STONE_HOE,
            Material.WOOD_HOE,

            Material.DIAMOND_AXE,
            Material.IRON_AXE,
            Material.GOLD_AXE,
            Material.STONE_AXE,
            Material.WOOD_AXE,

            Material.DIAMOND_SPADE,
            Material.IRON_SPADE,
            Material.GOLD_SPADE,
            Material.STONE_SPADE,
            Material.WOOD_SPADE,

            Material.STICK,

            Material.BOW,

            "other"
    }, BIND_RANDOM, BIND_DMGALL, BIND_SMITE, BIND_ARTHROPODS);
    public final ArterionFormula DMG_BOW = new ArterionFormula("dmg.bow", BIND_BOW_CHARGE, BIND_RANDOM, BIND_ARROWDMG);
    public final ArterionFormula DMG_EXPLOSION = new ArterionFormula("dmg.explosion", BIND_DISTANCE);
    public final ArterionFormula DMG_FIRE_TICK = new ArterionFormula("dmg.firetick");
    public final ArterionFormula DMG_POISON = new ArterionFormula("dmg.poison");
    public final ArterionFormula DMG_WITHER = new ArterionFormula("dmg.wither");
    public final ArterionFormula DMG_THORNS = new ArterionFormula("dmg.thorns", BIND_RANDOM);
    public final ArterionFormula DMG_FALL = new ArterionFormula("dmg.fall", BIND_FALLDISTANCE);
    public final ArterionFormulaArray DMG_ARMOR = new ArterionFormulaArray("dmg.armor", new Object[]{
            Material.DIAMOND_HELMET,
            Material.IRON_HELMET,
            Material.GOLD_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.LEATHER_HELMET,

            Material.DIAMOND_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.GOLD_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.LEATHER_CHESTPLATE,

            Material.DIAMOND_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.GOLD_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.LEATHER_LEGGINGS,

            Material.DIAMOND_BOOTS,
            Material.IRON_BOOTS,
            Material.GOLD_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.LEATHER_BOOTS,

            "other"
    }, BIND_RANDOM, BIND_PROTECTION, BIND_FIRE, BIND_FALL, BIND_EXPLOSIONS, BIND_PROJECTILE);
    public final ArterionFormula DMG_RESISTANCE = new ArterionFormula("dmg.resistance", BIND_RANDOM, BIND_POTIONLEVEL);
    public final ArterionFormula DMG_BOOST = new ArterionFormula("dmg.boost", BIND_RANDOM, BIND_STRENGTHLEVEL, BIND_WEAKNESSLEVEL);
    public final ArterionFormula DMG_XP_DAY_MODIFIER = new ArterionFormula("dmg.xp_day_modifier");
    public final ArterionFormulaArray DMG_PLAYER_MAXHP = new ArterionFormulaArray("dmg.player.maxhp", new Object[]{
            ClassEnum.BARBAR,
            ClassEnum.CLERIC,
            ClassEnum.FORESTRUNNER,
            ClassEnum.MAGE,
            ClassEnum.PALADIN,
            ClassEnum.SHADOWRUNNER,

            "other"
    }, BIND_PLAYER);

    public final ArterionFormulaArray SKILL_PLAYER_MAXMANA = new ArterionFormulaArray("skill.player.maxmana", new Object[]{
            ClassEnum.BARBAR,
            ClassEnum.CLERIC,
            ClassEnum.FORESTRUNNER,
            ClassEnum.MAGE,
            ClassEnum.PALADIN,
            ClassEnum.SHADOWRUNNER,

            "other"
    }, BIND_PLAYER);
    public final ArterionFormulaArray SKILL_PLAYER_MANAREGEN = new ArterionFormulaArray("skill.player.manaregen", new Object[]{
            ClassEnum.BARBAR,
            ClassEnum.CLERIC,
            ClassEnum.FORESTRUNNER,
            ClassEnum.MAGE,
            ClassEnum.PALADIN,
            ClassEnum.SHADOWRUNNER,

            "other"
    }, BIND_PLAYER);
    public final ArterionFormulaArray SKILL_MANA = new ArterionFormulaArray("skill.mana", SkillEnum.values(), BIND_PLAYER);
    public final ArterionFormulaArray SKILL_COOLDOWN = new ArterionFormulaArray("skill.cooldown", SkillEnum.values(), BIND_PLAYER);
    public final ArterionFormulaArray SKILL_LEVEL = new ArterionFormulaArray("skill.level", SkillEnum.values());

    public final ArterionFormula SKILL_NONE_HEAL = new ArterionFormula("skill.none.heal", BIND_PLAYER);

    public final ArterionFormula SKILL_PALADIN_RESISTANCE_RANGE = new ArterionFormula("skill.paladin.resistance.range", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_RESISTANCE_PERENEMY = new ArterionFormula("skill.paladin.resistance.perenemy", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_BLESSING_OF_THE_GODS_RANGE = new ArterionFormula("skill.paladin.blessing_of_the_gods.range", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_BLESSING_OF_THE_GODS_DURATION = new ArterionFormula("skill.paladin.blessing_of_the_gods.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_PALADIN_CHAIN_INNER_RANGE = new ArterionFormula("skill.paladin.chain.inner_range", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_CHAIN_OUTER_RANGE = new ArterionFormula("skill.paladin.chain.outer_range", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_CHAIN_WEAKNESS_DURATION = new ArterionFormula("skill.paladin.chain.weakness_duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_PALADIN_CHAIN_SLOWNESS_DURATION = new ArterionFormula("skill.paladin.chain.slowness_duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_PALADIN_CHAIN_REVEAL_DURATION = new ArterionFormula("skill.paladin.chain.reveal_duration"); //Milliseconds
    public final ArterionFormula SKILL_PALADIN_CHAIN_KNOCKBACK_XZ = new ArterionFormula("skill.paladin.chain.knockback_xz", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_CHAIN_KNOCKBACK_Y = new ArterionFormula("skill.paladin.chain.knockback_y", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_HELPING_HAND_RANGE = new ArterionFormula("skill.paladin.helping_hand.range", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_HELPING_HAND_HEAL = new ArterionFormula("skill.paladin.helping_hand.heal", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_HELPING_HAND_OTHER_HEAL = new ArterionFormula("skill.paladin.helping_hand.other_heal", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_GUST_OF_WIND_RANGE = new ArterionFormula("skill.paladin.gust_of_wind.range", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_GUST_OF_WIND_KNOCKBACK_XZ = new ArterionFormula("skill.paladin.gust_of_wind.knockback_xz", BIND_PLAYER);
    public final ArterionFormula SKILL_PALADIN_GUST_OF_WIND_KNOCKBACK_Y = new ArterionFormula("skill.paladin.gust_of_wind.knockback_y", BIND_PLAYER);


    public final ArterionFormula SKILL_BARBAR_BERSERK_RAGE_INCREASE = new ArterionFormula("skill.barbar.berserk_rage.increase", BIND_PLAYER); //Percent
    public final ArterionFormula SKILL_BARBAR_ENFORCED_ARMOR_INCREASE = new ArterionFormula("skill.barbar.enforced_armor.increase", BIND_PLAYER); //Percent
    public final ArterionFormula SKILL_BARBAR_ENFORCED_ARMOR_DURATION = new ArterionFormula("skill.barbar.enforced_armor.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_BARBAR_MIGHTY_HIT_INCREASE = new ArterionFormula("skill.barbar.mighty_hit.increase", BIND_PLAYER); //Multiplier
    public final ArterionFormula SKILL_BARBAR_MIGHTY_HIT_DURATION = new ArterionFormula("skill.barbar.mighty_hit.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_BARBAR_RAGE_DURATION = new ArterionFormula("skill.barbar.rage.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_BARBAR_STOMP_RANGE = new ArterionFormula("skill.barbar.stomp.range", BIND_PLAYER);
    public final ArterionFormula SKILL_BARBAR_STOMP_SELF_LAUNCH = new ArterionFormula("skill.barbar.stomp.self_launch", BIND_PLAYER);
    public final ArterionFormula SKILL_BARBAR_STOMP_ENEMY_LAUNCH = new ArterionFormula("skill.barbar.stomp.other_launch", BIND_PLAYER);
    public final ArterionFormula SKILL_BARBAR_STOMP_DAMAGE = new ArterionFormula("skill.barbar.stomp.damage", BIND_PLAYER);

    public final ArterionFormula SKILL_SHADOWRUNNER_THROAT_CUT_RANGE = new ArterionFormula("skill.shadowrunner.throat_cut.range", BIND_PLAYER);
    public final ArterionFormula SKILL_SHADOWRUNNER_THROAT_CUT_DAMAGE = new ArterionFormula("skill.shadowrunner.throat_cut.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_SHADOWRUNNER_SHADOW_CLONE_DURATION = new ArterionFormula("skill.shadowrunner.shadow_clone.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_SHADOWRUNNER_SHADOW_CAPE_DURATION = new ArterionFormula("skill.shadowrunner.shadow_cape.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_SHADOWRUNNER_AMBUSH_INCREASE = new ArterionFormula("skill.shadowrunner.ambush.increase", BIND_PLAYER); //Percent
    public final ArterionFormula SKILL_SHADOWRUNNER_ACID_BOMB_RANGE = new ArterionFormula("skill.shadowrunner.acid_bomb.range", BIND_PLAYER);
    public final ArterionFormula SKILL_SHADOWRUNNER_ACID_BOMB_DAMAGE = new ArterionFormula("skill.shadowrunner.acid_bomb.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_SHADOWRUNNER_ACID_BOMB_DURATION = new ArterionFormula("skill.shadowrunner.acid_bomb.duration", BIND_PLAYER); //Milliseconds

    public final ArterionFormula SKILL_FORESTRUNNER_JUMP_DISTANCE = new ArterionFormula("skill.forestrunner.jump.distance", BIND_PLAYER);
    public final ArterionFormula SKILL_FORESTRUNNER_JUMP_HEIGHT = new ArterionFormula("skill.forestrunner.jump.height", BIND_PLAYER);
    public final ArterionFormula SKILL_FORESTRUNNER_THROW_NET_FORCE = new ArterionFormula("skill.forestrunner.throw_net.force", BIND_PLAYER);
    public final ArterionFormula SKILL_FORESTRUNNER_THROW_NET_ANGLE = new ArterionFormula("skill.forestrunner.throw_net.angle", BIND_PLAYER);
    public final ArterionFormula SKILL_FORESTRUNNER_THROW_NET_REGEN_DELAY = new ArterionFormula("skill.forestrunner.throw_net.regen_delay", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_FORESTRUNNER_THROW_NET_REGEN_SPEED = new ArterionFormula("skill.forestrunner.throw_net.regen_speed", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_FORESTRUNNER_ARCANE_SHOT_DURATION = new ArterionFormula("skill.forestrunner.arcane_shot.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_FORESTRUNNER_ARCANE_SHOT_EXPIRE = new ArterionFormula("skill.forestrunner.arcane_shot.expire", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_FORESTRUNNER_ARCANE_SHOT_DAMAGE_BOOST = new ArterionFormula("skill.forestrunner.arcane_shot.damage_boost", BIND_PLAYER); //Precentage
    public final ArterionFormula SKILL_FORESTRUNNER_ARROW_HAIL_DAMAGE = new ArterionFormula("skill.forestrunner.arrow_hail.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_FORESTRUNNER_ARROW_HAIL_EXPIRE = new ArterionFormula("skill.forestrunner.arrow_hail.expire", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_FORESTRUNNER_HEADSHOT_INCREASE = new ArterionFormula("skill.forestrunner.headshot.increase", BIND_PLAYER); //Percent

    public final ArterionFormula SKILL_MAGE_FIREBALL_DAMAGE = new ArterionFormula("skill.mage.fireball.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_FIREBALL_DURATION = new ArterionFormula("skill.mage.fireball.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_MAGE_FIREBALL_SPEED = new ArterionFormula("skill.mage.fireball.speed", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_FIREBALL_RANGE = new ArterionFormula("skill.mage.fireball.range", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_ARCAN_SHIFT_RANGE = new ArterionFormula("skill.mage.arcane_shift.range", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_RANGE = new ArterionFormula("skill.mage.chain_lightning.range", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_COUNT = new ArterionFormula("skill.mage.chain_lightning.count", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_MAX_PER_PLAYER = new ArterionFormula("skill.mage.chain_lightning.max_per_player", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_DAMAGE = new ArterionFormula("skill.mage.chain_lightning.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_SPEED = new ArterionFormula("skill.mage.chain_lightning.speed", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_JUMP_RANGE = new ArterionFormula("skill.mage.chain_lightning.jump_range", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_CHAIN_LIGHTNING_PER_ENTITY_COOLDOWN = new ArterionFormula("skill.mage.chain_lightning.per_entity_cooldown", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_MAGE_FIRE_STORM_RANGE = new ArterionFormula("skill.mage.fire_storm.range", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_FIRE_STORM_DURATION = new ArterionFormula("skill.mage.fire_storm.duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_MAGE_FIRE_STORM_DAMAGE = new ArterionFormula("skill.mage.fire_storm.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_FIRE_STORM_AOE = new ArterionFormula("skill.mage.fire_storm.aoe", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_FIRE_STORM_FIRE_DURATION = new ArterionFormula("skill.mage.fire_storm.fire_duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_MAGE_FIRE_STORM_PULL = new ArterionFormula("skill.mage.fire_storm.pull", BIND_PLAYER);
    public final ArterionFormula SKILL_MAGE_MANA_STEAL_PERCENT = new ArterionFormula("skill.mage.mana_steal.percent", BIND_PLAYER); //Percent

    public final ArterionFormula SKILL_CLERIC_HEALING_BREATH_RANGE = new ArterionFormula("skill.cleric.healing_breath.range", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_HEALING_BREATH_SPEED = new ArterionFormula("skill.cleric.healing_breath.speed", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_HEALING_BREATH_DIVERSION = new ArterionFormula("skill.cleric.healing_breath.diversion", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_HEALING_BREATH_HEAL_OTHER = new ArterionFormula("skill.cleric.healing_breath.heal_other", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_BLINDING_EXPLOSION_RANGE = new ArterionFormula("skill.cleric.blinding_explosion.range", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_BLINDING_EXPLOSION_RANGE_ENEMY = new ArterionFormula("skill.cleric.blinding_explosion.range_enemy", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_BLINDING_EXPLOSION_DAMAGE = new ArterionFormula("skill.cleric.blinding_explosion.damage", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_SELF = new ArterionFormula("skill.cleric.blinding_explosion.heal_self", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_OTHER = new ArterionFormula("skill.cleric.blinding_explosion.heal_other", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_BLINDING_EXPLOSION_BLINDNESS_DURATION = new ArterionFormula("skill.cleric.blinding_explosion.blindness_duration", BIND_PLAYER); //Milliseconds
    public final ArterionFormula SKILL_CLERIC_MELODY_OF_PERSISTENCE_RANGE = new ArterionFormula("skill.cleric.melody_of_persistence.range", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_SELF = new ArterionFormula("skill.cleric.melody_of_persistence.heal_self", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_OTHER = new ArterionFormula("skill.cleric.melody_of_persistence.heal_other", BIND_PLAYER);
    public final ArterionFormula SKILL_CLERIC_MELODY_OF_PERSISTENCE_SPEED_DURATION = new ArterionFormula("skill.cleric.melody_of_persistence.speed_duration", BIND_PLAYER); //Millisekunden
    public final ArterionFormula SKILL_CLERIC_DIVINE_BLESSING_DURATION = new ArterionFormula("skill.cleric.divine_blessing.duration", BIND_PLAYER); //Millisekunden
    public final ArterionFormula SKILL_CLERIC_REPEAT_DURATION = new ArterionFormula("skill.cleric.repeat.duration", BIND_PLAYER); //Millisekunden
    public final ArterionFormula SKILL_CLERIC_REPEAT_WEAKNESS_DURATION = new ArterionFormula("skill.cleric.repeat.weakness_duration", BIND_PLAYER); //Millisekunden
    public final ArterionFormula SKILL_CLERIC_REPEAT_BLINDNESS_DURATION = new ArterionFormula("skill.cleric.repeat.blindness_duration", BIND_PLAYER); //Millisekunden
    public final ArterionFormula SKILL_CLERIC_REPEAT_SLOWNESS_DURATION = new ArterionFormula("skill.cleric.repeat.slowness_duration", BIND_PLAYER); //Millisekunden


    public final ArterionFormula SIEGE_BATTERING_RAM_REGEN_DELAY = new ArterionFormula("siege.battering_ram.regen_delay");
    public final ArterionFormula SIEGE_BRIDGE_LAYERS = new ArterionFormula("siege.bridge.layers");
    public final ArterionFormula SIEGE_BRIDGE_SPEED = new ArterionFormula("siege.bridge.speed");
    public final ArterionFormula SIEGE_BRIDGE_REMOVE_DELAY = new ArterionFormula("siege.bridge.remove_delay");
    public final ArterionFormula SIEGE_BRIDGE_REMOVE_SPEED = new ArterionFormula("siege.bridge.remove_speed");
    public final ArterionFormula SIEGE_FREEZE_REMOVE_DELAY = new ArterionFormula("siege.freeze.remove_delay");
    public final ArterionFormula SIEGE_FREEZE_REMOVE_SPEED = new ArterionFormula("siege.freeze.remove_speed");
    public final ArterionFormula SIEGE_LADDER_LAYERS = new ArterionFormula("siege.ladder.layers");
    public final ArterionFormula SIEGE_LADDER_SPEED = new ArterionFormula("siege.ladder.speed");
    public final ArterionFormula SIEGE_LADDER_REMOVE_DELAY = new ArterionFormula("siege.ladder.remove_delay");
    public final ArterionFormula SIEGE_LADDER_REMOVE_SPEED = new ArterionFormula("siege.ladder.remove_speed");
    public final ArterionFormula SIEGE_LOCKPICK_DELAY = new ArterionFormula("siege.lockpick.delay");
    public final ArterionFormula SIEGE_LOCKPICK_REDUCED_CHANCE = new ArterionFormula("siege.lockpick.reduced_chance");
    public final ArterionFormula SIEGE_LOCKPICK_NORMAL_CHANCE = new ArterionFormula("siege.lockpick.normal_chance");
    public final ArterionFormula SIEGE_TNT_COOLDOWN = new ArterionFormula("siege.tnt.cooldown");
    public final ArterionFormula SIEGE_OBSIDIAN_TNT_RANGE = new ArterionFormula("siege.obsidian_tnt.range");
    public final ArterionFormula SIEGE_OBSIDIAN_TNT_COOLDOWN = new ArterionFormula("siege.obsidian_tnt.cooldown");
    public final ArterionFormula SIEGE_SOLIDIFY_REMOVE_DELAY = new ArterionFormula("siege.solidify.remove_delay");
    public final ArterionFormula SIEGE_SOLIDIFY_REMOVE_SPEED = new ArterionFormula("siege.solidify.remove_speed");
    public final ArterionFormula SIEGE_TOWER_LAYERS = new ArterionFormula("siege.tower.layers");
    public final ArterionFormula SIEGE_TOWER_SPEED = new ArterionFormula("siege.tower.speed");
    public final ArterionFormula SIEGE_TOWER_REMOVE_DELAY = new ArterionFormula("siege.tower.remove_delay");
    public final ArterionFormula SIEGE_TOWER_REMOVE_SPEED = new ArterionFormula("siege.tower.remove_speed");

    public final ArterionFormulaArray SIEGE_PRICE = new ArterionFormulaArray("siege.price", new Object[]{
            CustomItemType.SIEGE_SOLIDIFY,
            CustomItemType.SIEGE_FREEZE,
            CustomItemType.SIEGE_LADDER,
            CustomItemType.SIEGE_TOWER,
            CustomItemType.SIEGE_BRIDGE,
            CustomItemType.SIEGE_LOCKPICK,
            CustomItemType.SIEGE_NORMAL_TNT,
            CustomItemType.SIEGE_OBSIDIAN_TNT,
            CustomItemType.SIEGE_BATTERING_RAM
    });

    public final ArterionFormula JOB_LEVEL_CURVE = new ArterionFormula("job.level.curve", BIND_PERLEVEL);
    public final ArterionFormula JOB_LEVEL_MAX = new ArterionFormula("job.level.max");
    public final ArterionFormulaArray JOB_WOODWORKER_XP = new ArterionFormulaArray("job.woodworker.xp", JobEnum.WOODWORKER.getXpMaterials(), BIND_RANDOM);
    public final ArterionFormulaArray JOB_FARMER_XP = new ArterionFormulaArray("job.farmer.xp", JobEnum.FARMER.getXpMaterials(), BIND_RANDOM);
    public final ArterionFormulaArray JOB_MINER_XP = new ArterionFormulaArray("job.miner.xp", JobEnum.MINER.getXpMaterials(), BIND_RANDOM);
    public final ArterionFormula JOB_FISHER_XP = new ArterionFormula("job.fisher.xp", BIND_RANDOM);
    public final ArterionFormulaArray JOB_DROP_CHANCE = new ArterionFormulaArray("job.drop_chance", JobEnum.values(), BIND_JOBLEVEL);

    public final ArterionFormulaArray JOB_WOODWORKER_DROPS_WEIGHT = new ArterionFormulaArray("job.woodworker.drops.weight", WoodworkerDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_WOODWORKER_DROPS_MIN = new ArterionFormulaArray("job.woodworker.drops.min", WoodworkerDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_WOODWORKER_DROPS_MAX = new ArterionFormulaArray("job.woodworker.drops.max", WoodworkerDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_FARMER_DROPS_WEIGHT = new ArterionFormulaArray("job.farmer.drops.weight", FarmerDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_FARMER_DROPS_MIN = new ArterionFormulaArray("job.farmer.drops.min", FarmerDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_FARMER_DROPS_MAX = new ArterionFormulaArray("job.farmer.drops.max", FarmerDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_FISHER_DROPS_WEIGHT = new ArterionFormulaArray("job.fisher.drops.weight", FisherDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_FISHER_DROPS_MIN = new ArterionFormulaArray("job.fisher.drops.min", FisherDrops.INSTANCE.values());
    public final ArterionFormulaArray JOB_FISHER_DROPS_MAX = new ArterionFormulaArray("job.fisher.drops.max", FisherDrops.INSTANCE.values());

    public final ArterionFormulaArray ARTEFACT_DROPS_WEIGHT = new ArterionFormulaArray("artefact.drops.weight", ArtefactDrops.INSTANCE.values());
    public final ArterionFormulaArray ARTEFACT_DROPS_MIN = new ArterionFormulaArray("artefact.drops.min", ArtefactDrops.INSTANCE.values());
    public final ArterionFormulaArray ARTEFACT_DROPS_MAX = new ArterionFormulaArray("artefact.drops.max", ArtefactDrops.INSTANCE.values());
    public final ArterionFormula ARTEFACT_DROPS_INTERVAL = new ArterionFormula("artefact.drops.interval");
    public final ArterionFormula ARTEFACT_BLOCK_MAXHP = new ArterionFormula("artefact.block.maxhp");
    public final ArterionFormula ARTEFACT_GUILD_TAX_MULTIPLIER = new ArterionFormula("artefact.guild.tax_multiplier");
    public final ArterionFormula ARTEFACT_CRISTAL_MAXHP = new ArterionFormula("artefact.cristal.maxhp");
    public final ArterionFormula ARTEFACT_CRISTAL_RESPAWN_TICKS = new ArterionFormula("artefact.cristal.respawn_ticks");
    public final ArterionFormula ARTEFACT_CARRIER_COMBAT_DISTANCE = new ArterionFormula("artefact.carrier.combat_distance");
    public final ArterionFormula ARTEFACT_REPLAY_CHUNK_DISTANCE = new ArterionFormula("artefact.replay.chunk_distance");

    public final ArterionFormula CAPTUREPOINT_MAX_POINTS = new ArterionFormula("capturepoint.max_points");
    public final ArterionFormula CAPTUREPOINT_MAX_POINTS_PER_SECOND = new ArterionFormula("capturepoint.max_points_per_second");
    public final ArterionFormula CAPTUREPOINT_CAPTURE_DISTANCE = new ArterionFormula("capturepoint.capture_distance");
    public final ArterionFormula CAPTUREPOINT_CLAIM_TICKS = new ArterionFormula("capturepoint.claim_ticks");
    public final ArterionFormula CAPTUREPOINT_REPLAY_CHUNK_DISTANCE = new ArterionFormula("capturepoint.replay.chunk_distance");
    public final ArterionFormula CAPTUREPOINT_GRAVERUIN_XP_MULTIPLIER = new ArterionFormula("capturepoint.graveruin.xp_multiplier");
    public final ArterionFormula CAPTUREPOINT_DESERTTEMPLE_GOLD_MULTIPLIER = new ArterionFormula("capturepoint.deserttemple.gold_multiplier");

    public final ArterionFormula DUNGEON_MORGOTH_MAXHP = new ArterionFormula("dungeon.morgoth.maxhp");
    public final ArterionFormula DUNGEON_MORGOTH_DAMAGE = new ArterionFormula("dungeon.morgoth.damage");
    public final ArterionFormula DUNGEON_MORGOTH_KEY_CHANCE = new ArterionFormula("dungeon.morgoth.key.chance");
    public final ArterionFormula DUNGEON_MORGOTH_COOLDOWN = new ArterionFormula("dungeon.morgoth.cooldown");
    public final ArterionFormula DUNGEON_MORGOTH_MAX_FIGHT_DURATION = new ArterionFormula("dungeon.morgoth.max_fight_duration");
    public final ArterionFormula DUNGEON_MORGOTH_REWARD_GOLD = new ArterionFormula("dungeon.morgoth.reward.gold");
    public final ArterionFormula DUNGEON_MORGOTH_REWARD_XP = new ArterionFormula("dungeon.morgoth.reward.xp");

    public final ArterionFormulaArray BLACK_MARKET_PRICE = new ArterionFormulaArray("black_market.price", new Object[]{
            CustomItemType.DUNGEON_KEY_MORGOTH
    });

    public final ArterionFormulaArray PVPCHEST_DROPS_WEIGHT = new ArterionFormulaArray("pvpchest.drops.weight", PvPChestDrops.INSTANCE.values());
    public final ArterionFormulaArray PVPCHEST_DROPS_MIN = new ArterionFormulaArray("pvpchest.drops.min", PvPChestDrops.INSTANCE.values());
    public final ArterionFormulaArray PVPCHEST_DROPS_MAX = new ArterionFormulaArray("pvpchest.drops.max", PvPChestDrops.INSTANCE.values());
    public final ArterionFormula PVPCHEST_DROP_AMOUNT = new ArterionFormula("pvpchest.drop_amount", BIND_RANDOM);
    public final ArterionFormula PVPCHEST_AMOUNT = new ArterionFormula("pvpchest.amount");

    public final ArterionFormula TPA_PRICE = new ArterionFormula("tpa.price");

    public final ArterionFormula DMG_BOSS_CHANCE = new ArterionFormula("dmg.boss_chance");

    public final ArterionFormula QUEST_RETURN_PRICE = new ArterionFormula("quest.return_price");

    public final ArterionFormula PRESTIGE_ATTACK = new ArterionFormula("prestige.attack", BIND_PRESTIGE_POINTS);
    public final ArterionFormula PRESTIGE_DEFENSE = new ArterionFormula("prestige.defense", BIND_PRESTIGE_POINTS);
    public final ArterionFormula PRESTIGE_HEALTH = new ArterionFormula("prestige.health", BIND_PRESTIGE_POINTS);
    public final ArterionFormula PRESTIGE_COOLDOWN = new ArterionFormula("prestige.cooldown", BIND_PRESTIGE_POINTS);

    private Map<String, ArterionFormula> formulaMap;
    private ArterionFormulaConfig formulaConfig;

    public ArterionFormulaManager() {
        //Fetch all existing formulas from this class
        formulaMap = new HashMap<>();
        for (Field f : getClass().getDeclaredFields()) {
            if (ArterionFormula.class.isAssignableFrom(f.getType())) {
                try {
                    ArterionFormula form = (ArterionFormula) f.get(this);
                    formulaMap.put(form.getKey(), form);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ArterionFormulaArray.class.isAssignableFrom(f.getType())) {
                try {
                    ArterionFormulaArray forma = (ArterionFormulaArray) f.get(this);
                    for (ArterionFormula form : forma.getFormulas()) {
                        formulaMap.put(form.getKey(), form);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //Fill it with values from our config
        this.formulaConfig = ConfigAPI.readConfig(new ArterionFormulaConfig());
        for (Map.Entry<String, String> entry : this.formulaConfig.formulas.entrySet()) {
            ArterionFormula formula = formulaMap.get(entry.getKey());
            if (formula != null) {
                formula.setFormula(entry.getValue());
            }
        }
        //Sync current values to DB (for website)
        this.syncToDb();
    }

    private void syncToDb() {
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                for (ArterionFormula formula : formulaMap.values()) {
                    DatabaseFormula dbf = new DatabaseFormula(formula.getKey(), formula.getTextRepresentation());
                    db.save(dbf);
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {

            }

            @Override
            public void onTransactionError() {
                System.err.println("ERROR: Failed to sync formulas to DB!");
            }
        }.execute();
    }

    public Number evaluate(String key, Object... values) {
        ArterionFormula formula = formulaMap.get(key);
        if (formula == null) return null;
        return formula.evaluate(values);
    }

    public Set<String> getAvailableFormulas() {
        return this.formulaMap.keySet();
    }

    public boolean updateFormula(String key, String textRepresentation) throws IllegalArgumentException {
        ArterionFormula formula = formulaMap.get(key);
        if (formula == null) return false;
        formula.setFormula(textRepresentation);
        this.formulaConfig.formulas.put(key, textRepresentation);
        ConfigAPI.writeConfig(this.formulaConfig);
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                DatabaseFormula dbf = new DatabaseFormula(formula.getKey(), formula.getTextRepresentation());
                db.save(dbf);
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {

            }

            @Override
            public void onTransactionError() {
                System.err.println("ERROR: Failed to sync formulas to DB!");
            }
        }.execute();
        return true;
    }

    public ReflectionBinding[] getBindings(String key) {
        ArterionFormula formula = formulaMap.get(key);
        if (formula == null) return null;
        return formula.getBindings();
    }

    public ArterionFormula getFormula(String key) {
        return formulaMap.get(key);
    }

    public String getTextRepresentation(String key) {
        ArterionFormula formula = formulaMap.get(key);
        if (formula == null) return null;
        return formula.getTextRepresentation();
    }
}
