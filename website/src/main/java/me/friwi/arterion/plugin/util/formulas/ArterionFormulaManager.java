package me.friwi.arterion.plugin.util.formulas;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseFormula;
import me.friwi.arterion.plugin.util.database.enums.ClassEnum;
import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;
import me.friwi.arterion.website.WebApplication;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArterionFormulaManager {
    /**
     * Register all available bindings here
     */
    public final ReflectionBinding BIND_PERLEVEL = new ReflectionBinding("perlevel", Integer.class);
    public final ReflectionBinding BIND_GUILD = new ReflectionBinding("guild", Object.class);
    public final ReflectionBinding BIND_PLAYER = new ReflectionBinding("player", FakePlayer.class);

    /**
     * Register your formulas here
     */
    public final ArterionFormula JOB_LEVEL_CURVE = new ArterionFormula("job.level.curve", BIND_PERLEVEL);
    public final ArterionFormula JOB_LEVEL_MAX = new ArterionFormula("job.level.max");

    public final ArterionFormula FIGHT_DURATION = new ArterionFormula("fight.duration");
    public final ArterionFormula FIGHT_POST_DURATION = new ArterionFormula("fight.post.duration");

    public final ArterionFormula GUILD_GUILDBLOCK_FEE = new ArterionFormula("guild.guildblock.fee", BIND_PLAYER);
    public final ArterionFormula GUILD_GUILDBLOCK_DISTANCE = new ArterionFormula("guild.guildblock.distance", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MIN = new ArterionFormula("guild.guildblock.min", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MAX = new ArterionFormula("guild.guildblock.max", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MINY = new ArterionFormula("guild.guildblock.min_y", BIND_GUILD);
    public final ArterionFormula GUILD_GUILDBLOCK_MAXY = new ArterionFormula("guild.guildblock.max_y", BIND_GUILD);
    public final ArterionFormula GUILD_MAXMEMBERS = new ArterionFormula("guild.maxmembers");
    public final ArterionFormula GUILD_VAULT_DROP = new ArterionFormula("guild.vault.drop", BIND_GUILD);

    public final ArterionFormula TPA_PRICE = new ArterionFormula("tpa.price");

    public final ArterionFormula PLAYER_HOMEBLOCK_FEE = new ArterionFormula("player.homeblock.fee", BIND_PLAYER);

    public final ArterionFormula CAPTUREPOINT_GRAVERUIN_XP_MULTIPLIER = new ArterionFormula("capturepoint.graveruin.xp_multiplier");
    public final ArterionFormula CAPTUREPOINT_DESERTTEMPLE_GOLD_MULTIPLIER = new ArterionFormula("capturepoint.deserttemple.gold_multiplier");

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

    public final ArterionFormulaArray SKILL_MANA = new ArterionFormulaArray("skill.mana", SkillEnum.values(), BIND_PLAYER);
    public final ArterionFormulaArray SKILL_COOLDOWN = new ArterionFormulaArray("skill.cooldown", SkillEnum.values(), BIND_PLAYER);

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

    private Map<String, ArterionFormula> formulaMap;

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
        //Fill it with values from our db
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        List<DatabaseFormula> formulas = db.findAll(DatabaseFormula.class);
        db.commit();
        for (DatabaseFormula entry : formulas) {
            ArterionFormula formula = formulaMap.get(entry.getIdentifier());
            if (formula != null && entry.getFormula() != null) {
                formula.setFormula(entry.getFormula());
            }
        }
    }

    public Number evaluate(String key, Object... values) {
        ArterionFormula formula = formulaMap.get(key);
        if (formula == null) return null;
        return formula.evaluate(values);
    }

    public Set<String> getAvailableFormulas() {
        return this.formulaMap.keySet();
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
