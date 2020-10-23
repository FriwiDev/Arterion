package me.friwi.arterion.website.wiki;

import me.friwi.arterion.plugin.util.database.enums.ClassEnum;
import me.friwi.arterion.plugin.util.formulas.*;
import me.friwi.arterion.website.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiClassesController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/classes")
    public String wiki_classes(Model model) {
        FakePlayer level0 = new FakePlayer();
        FakePlayer level100 = new FakePlayer(100, 0);
        ArterionFormulaManager f = WebApplication.getFormulaManager();


        model.addAttribute("heal_cd", f.SKILL_COOLDOWN.get(SkillEnum.HEAL.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("heal_mana", f.SKILL_MANA.get(SkillEnum.HEAL.name()).evaluateInt());
        model.addAttribute("heal_0", FormulaFormatter.toString(f.SKILL_NONE_HEAL));

        //Paladin
        ArterionFormula maxhp = f.DMG_PLAYER_MAXHP.get(ClassEnum.PALADIN.name());
        ArterionFormula maxmana = f.SKILL_PLAYER_MAXMANA.get(ClassEnum.PALADIN.name());
        model.addAttribute("paladin_hp", FormulaFormatter.toString(maxhp));
        model.addAttribute("paladin_hp0", maxhp.evaluateInt(level0));
        model.addAttribute("paladin_hp100", maxhp.evaluateInt(level100));
        model.addAttribute("paladin_mana", FormulaFormatter.toString(maxmana));
        model.addAttribute("paladin_mana0", maxmana.evaluateInt(level0));
        model.addAttribute("paladin_mana100", maxmana.evaluateInt(level100));

        model.addAttribute("paladin_passive_0", f.SKILL_PALADIN_RESISTANCE_RANGE.evaluateInt());
        model.addAttribute("paladin_passive_1", f.SKILL_PALADIN_RESISTANCE_PERENEMY.evaluateDouble());

        model.addAttribute("paladin_2_mana", f.SKILL_MANA.get(SkillEnum.HELPING_HAND.name()).evaluateInt());
        model.addAttribute("paladin_2_cd", f.SKILL_COOLDOWN.get(SkillEnum.HELPING_HAND.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("paladin_2_0", f.SKILL_PALADIN_HELPING_HAND_OTHER_HEAL.evaluateInt());
        model.addAttribute("paladin_2_1", f.SKILL_PALADIN_HELPING_HAND_HEAL.evaluateInt());
        model.addAttribute("paladin_2_2", f.SKILL_PALADIN_HELPING_HAND_RANGE.evaluateDouble());

        model.addAttribute("paladin_3_mana", f.SKILL_MANA.get(SkillEnum.GUST_OF_WIND.name()).evaluateInt());
        model.addAttribute("paladin_3_cd", f.SKILL_COOLDOWN.get(SkillEnum.GUST_OF_WIND.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("paladin_3_0", f.SKILL_PALADIN_GUST_OF_WIND_RANGE.evaluateDouble());

        model.addAttribute("paladin_4_mana", f.SKILL_MANA.get(SkillEnum.CHAIN.name()).evaluateInt());
        model.addAttribute("paladin_4_cd", f.SKILL_COOLDOWN.get(SkillEnum.CHAIN.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("paladin_4_0", f.SKILL_PALADIN_CHAIN_INNER_RANGE.evaluateDouble());
        model.addAttribute("paladin_4_1", f.SKILL_PALADIN_CHAIN_WEAKNESS_DURATION.evaluateDouble() / 1000);
        model.addAttribute("paladin_4_2", f.SKILL_PALADIN_CHAIN_SLOWNESS_DURATION.evaluateDouble() / 1000);
        model.addAttribute("paladin_4_3", f.SKILL_PALADIN_CHAIN_OUTER_RANGE.evaluateDouble());
        model.addAttribute("paladin_4_4", f.SKILL_PALADIN_CHAIN_REVEAL_DURATION.evaluateDouble() / 1000);

        model.addAttribute("paladin_5_mana", f.SKILL_MANA.get(SkillEnum.BLESSING_OF_THE_GODS.name()).evaluateInt());
        model.addAttribute("paladin_5_cd", f.SKILL_COOLDOWN.get(SkillEnum.BLESSING_OF_THE_GODS.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("paladin_5_0", f.SKILL_PALADIN_BLESSING_OF_THE_GODS_RANGE.evaluateDouble());
        model.addAttribute("paladin_5_1", f.SKILL_PALADIN_BLESSING_OF_THE_GODS_DURATION.evaluateDouble() / 1000);

        //Berserk

        maxhp = f.DMG_PLAYER_MAXHP.get(ClassEnum.BARBAR.name());
        maxmana = f.SKILL_PLAYER_MAXMANA.get(ClassEnum.BARBAR.name());
        model.addAttribute("berserk_hp", FormulaFormatter.toString(maxhp));
        model.addAttribute("berserk_hp0", maxhp.evaluateInt(level0));
        model.addAttribute("berserk_hp100", maxhp.evaluateInt(level100));
        model.addAttribute("berserk_mana", FormulaFormatter.toString(maxmana));
        model.addAttribute("berserk_mana0", maxmana.evaluateInt(level0));
        model.addAttribute("berserk_mana100", maxmana.evaluateInt(level100));

        model.addAttribute("berserk_passive_0", f.SKILL_BARBAR_BERSERK_RAGE_INCREASE.evaluateDouble());

        model.addAttribute("berserk_2_mana", f.SKILL_MANA.get(SkillEnum.MIGHTY_HIT.name()).evaluateInt());
        model.addAttribute("berserk_2_cd", f.SKILL_COOLDOWN.get(SkillEnum.MIGHTY_HIT.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("berserk_2_0", f.SKILL_BARBAR_MIGHTY_HIT_DURATION.evaluateDouble() / 1000);
        model.addAttribute("berserk_2_1", f.SKILL_BARBAR_MIGHTY_HIT_INCREASE.evaluateDouble());

        model.addAttribute("berserk_3_mana", f.SKILL_MANA.get(SkillEnum.ENFORCED_ARMOR.name()).evaluateInt());
        model.addAttribute("berserk_3_cd", f.SKILL_COOLDOWN.get(SkillEnum.ENFORCED_ARMOR.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("berserk_3_0", f.SKILL_BARBAR_ENFORCED_ARMOR_DURATION.evaluateDouble() / 1000);
        model.addAttribute("berserk_3_1", f.SKILL_BARBAR_ENFORCED_ARMOR_INCREASE.evaluateDouble());

        model.addAttribute("berserk_4_mana", f.SKILL_MANA.get(SkillEnum.STOMP.name()).evaluateInt());
        model.addAttribute("berserk_4_cd", f.SKILL_COOLDOWN.get(SkillEnum.STOMP.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("berserk_4_0", f.SKILL_BARBAR_STOMP_SELF_LAUNCH.evaluateDouble());
        model.addAttribute("berserk_4_1", f.SKILL_BARBAR_STOMP_RANGE.evaluateDouble());
        model.addAttribute("berserk_4_2", f.SKILL_BARBAR_STOMP_ENEMY_LAUNCH.evaluateDouble());
        model.addAttribute("berserk_4_3", f.SKILL_BARBAR_STOMP_DAMAGE.evaluateDouble());

        model.addAttribute("berserk_5_mana", f.SKILL_MANA.get(SkillEnum.RAGE.name()).evaluateInt());
        model.addAttribute("berserk_5_cd", f.SKILL_COOLDOWN.get(SkillEnum.RAGE.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("berserk_5_0", f.SKILL_BARBAR_RAGE_DURATION.evaluateDouble() / 1000);

        //Shadowrunner

        maxhp = f.DMG_PLAYER_MAXHP.get(ClassEnum.SHADOWRUNNER.name());
        maxmana = f.SKILL_PLAYER_MAXMANA.get(ClassEnum.SHADOWRUNNER.name());
        model.addAttribute("shadowrunner_hp", FormulaFormatter.toString(maxhp));
        model.addAttribute("shadowrunner_hp0", maxhp.evaluateInt(level0));
        model.addAttribute("shadowrunner_hp100", maxhp.evaluateInt(level100));
        model.addAttribute("shadowrunner_mana", FormulaFormatter.toString(maxmana));
        model.addAttribute("shadowrunner_mana0", maxmana.evaluateInt(level0));
        model.addAttribute("shadowrunner_mana100", maxmana.evaluateInt(level100));

        model.addAttribute("shadowrunner_passive_0", f.SKILL_SHADOWRUNNER_AMBUSH_INCREASE.evaluateDouble());

        model.addAttribute("shadowrunner_2_mana", f.SKILL_MANA.get(SkillEnum.THROAT_CUT.name()).evaluateInt());
        model.addAttribute("shadowrunner_2_cd", f.SKILL_COOLDOWN.get(SkillEnum.THROAT_CUT.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("shadowrunner_2_0", f.SKILL_SHADOWRUNNER_THROAT_CUT_RANGE.evaluateDouble());
        model.addAttribute("shadowrunner_2_1", f.SKILL_SHADOWRUNNER_THROAT_CUT_DAMAGE.evaluateDouble());

        model.addAttribute("shadowrunner_3_mana", f.SKILL_MANA.get(SkillEnum.SHADOW_CLONE.name()).evaluateInt());
        model.addAttribute("shadowrunner_3_cd", f.SKILL_COOLDOWN.get(SkillEnum.SHADOW_CLONE.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("shadowrunner_3_0", f.SKILL_SHADOWRUNNER_SHADOW_CLONE_DURATION.evaluateDouble() / 1000);

        model.addAttribute("shadowrunner_4_mana", f.SKILL_MANA.get(SkillEnum.SHADOW_CAPE.name()).evaluateInt());
        model.addAttribute("shadowrunner_4_cd", f.SKILL_COOLDOWN.get(SkillEnum.SHADOW_CAPE.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("shadowrunner_4_0", f.SKILL_SHADOWRUNNER_SHADOW_CAPE_DURATION.evaluateDouble() / 1000);
        model.addAttribute("shadowrunner_4_1", f.SKILL_PALADIN_CHAIN_REVEAL_DURATION.evaluateDouble() / 1000);

        model.addAttribute("shadowrunner_5_mana", f.SKILL_MANA.get(SkillEnum.ACID_BOMB.name()).evaluateInt());
        model.addAttribute("shadowrunner_5_cd", f.SKILL_COOLDOWN.get(SkillEnum.ACID_BOMB.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("shadowrunner_5_0", f.SKILL_SHADOWRUNNER_ACID_BOMB_RANGE.evaluateDouble());
        model.addAttribute("shadowrunner_5_1", f.SKILL_SHADOWRUNNER_ACID_BOMB_DURATION.evaluateDouble() / 1000);
        model.addAttribute("shadowrunner_5_2", f.SKILL_SHADOWRUNNER_ACID_BOMB_DAMAGE.evaluateDouble());


        //Forestrunner

        maxhp = f.DMG_PLAYER_MAXHP.get(ClassEnum.FORESTRUNNER.name());
        maxmana = f.SKILL_PLAYER_MAXMANA.get(ClassEnum.FORESTRUNNER.name());
        model.addAttribute("forestrunner_hp", FormulaFormatter.toString(maxhp));
        model.addAttribute("forestrunner_hp0", maxhp.evaluateInt(level0));
        model.addAttribute("forestrunner_hp100", maxhp.evaluateInt(level100));
        model.addAttribute("forestrunner_mana", FormulaFormatter.toString(maxmana));
        model.addAttribute("forestrunner_mana0", maxmana.evaluateInt(level0));
        model.addAttribute("forestrunner_mana100", maxmana.evaluateInt(level100));

        model.addAttribute("forestrunner_passive_0", f.SKILL_FORESTRUNNER_HEADSHOT_INCREASE.evaluateDouble());

        model.addAttribute("forestrunner_2_mana", f.SKILL_MANA.get(SkillEnum.JUMP.name()).evaluateInt());
        model.addAttribute("forestrunner_2_cd", f.SKILL_COOLDOWN.get(SkillEnum.JUMP.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("forestrunner_2_0", f.SKILL_FORESTRUNNER_JUMP_DISTANCE.evaluateDouble());

        model.addAttribute("forestrunner_3_mana", f.SKILL_MANA.get(SkillEnum.THROW_NET.name()).evaluateInt());
        model.addAttribute("forestrunner_3_cd", f.SKILL_COOLDOWN.get(SkillEnum.THROW_NET.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("forestrunner_3_0", f.SKILL_FORESTRUNNER_THROW_NET_REGEN_DELAY.evaluateDouble() / 1000);

        model.addAttribute("forestrunner_4_mana", f.SKILL_MANA.get(SkillEnum.ARCANE_SHOT.name()).evaluateInt());
        model.addAttribute("forestrunner_4_cd", f.SKILL_COOLDOWN.get(SkillEnum.ARCANE_SHOT.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("forestrunner_4_0", f.SKILL_FORESTRUNNER_ARCANE_SHOT_EXPIRE.evaluateDouble() / 1000);
        model.addAttribute("forestrunner_4_1", f.SKILL_FORESTRUNNER_ARCANE_SHOT_DURATION.evaluateDouble() / 1000);
        model.addAttribute("forestrunner_4_2", f.SKILL_FORESTRUNNER_ARCANE_SHOT_DAMAGE_BOOST.evaluateDouble());

        model.addAttribute("forestrunner_5_mana", f.SKILL_MANA.get(SkillEnum.ARROW_HAIL.name()).evaluateInt());
        model.addAttribute("forestrunner_5_cd", f.SKILL_COOLDOWN.get(SkillEnum.ARROW_HAIL.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("forestrunner_5_0", f.SKILL_FORESTRUNNER_ARROW_HAIL_EXPIRE.evaluateDouble() / 1000);
        model.addAttribute("forestrunner_5_1", f.SKILL_FORESTRUNNER_ARROW_HAIL_DAMAGE.evaluateDouble());


        //Mage

        maxhp = f.DMG_PLAYER_MAXHP.get(ClassEnum.MAGE.name());
        maxmana = f.SKILL_PLAYER_MAXMANA.get(ClassEnum.MAGE.name());
        model.addAttribute("mage_hp", FormulaFormatter.toString(maxhp));
        model.addAttribute("mage_hp0", maxhp.evaluateInt(level0));
        model.addAttribute("mage_hp100", maxhp.evaluateInt(level100));
        model.addAttribute("mage_mana", FormulaFormatter.toString(maxmana));
        model.addAttribute("mage_mana0", maxmana.evaluateInt(level0));
        model.addAttribute("mage_mana100", maxmana.evaluateInt(level100));

        model.addAttribute("mage_passive_0", f.SKILL_MAGE_MANA_STEAL_PERCENT.evaluateDouble());

        model.addAttribute("mage_2_mana", f.SKILL_MANA.get(SkillEnum.FIREBALL.name()).evaluateInt());
        model.addAttribute("mage_2_cd", f.SKILL_COOLDOWN.get(SkillEnum.FIREBALL.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("mage_2_0", f.SKILL_MAGE_FIREBALL_RANGE.evaluateDouble());
        model.addAttribute("mage_2_1", f.SKILL_MAGE_FIREBALL_DURATION.evaluateDouble() / 1000);
        model.addAttribute("mage_2_2", f.SKILL_MAGE_FIREBALL_DAMAGE.evaluateDouble());

        model.addAttribute("mage_3_mana", f.SKILL_MANA.get(SkillEnum.ARCANE_SHIFT.name()).evaluateInt());
        model.addAttribute("mage_3_cd", f.SKILL_COOLDOWN.get(SkillEnum.ARCANE_SHIFT.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("mage_3_0", f.SKILL_MAGE_ARCAN_SHIFT_RANGE.evaluateDouble());

        model.addAttribute("mage_4_mana", f.SKILL_MANA.get(SkillEnum.CHAIN_LIGHTNING.name()).evaluateInt());
        model.addAttribute("mage_4_cd", f.SKILL_COOLDOWN.get(SkillEnum.CHAIN_LIGHTNING.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("mage_4_0", f.SKILL_MAGE_CHAIN_LIGHTNING_RANGE.evaluateDouble());
        model.addAttribute("mage_4_1", f.SKILL_MAGE_CHAIN_LIGHTNING_COUNT.evaluateDouble());
        model.addAttribute("mage_4_2", f.SKILL_MAGE_CHAIN_LIGHTNING_JUMP_RANGE.evaluateDouble());
        model.addAttribute("mage_4_3", f.SKILL_MAGE_CHAIN_LIGHTNING_MAX_PER_PLAYER.evaluateDouble());
        model.addAttribute("mage_4_4", FormulaFormatter.toString(f.SKILL_MAGE_CHAIN_LIGHTNING_DAMAGE));
        model.addAttribute("mage_4_5", f.SKILL_MAGE_CHAIN_LIGHTNING_PER_ENTITY_COOLDOWN.evaluateDouble() / 1000);

        model.addAttribute("mage_5_mana", f.SKILL_MANA.get(SkillEnum.FIRE_STORM.name()).evaluateInt());
        model.addAttribute("mage_5_cd", f.SKILL_COOLDOWN.get(SkillEnum.FIRE_STORM.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("mage_5_0", f.SKILL_MAGE_FIRE_STORM_RANGE.evaluateDouble());
        model.addAttribute("mage_5_1", f.SKILL_MAGE_FIRE_STORM_AOE.evaluateDouble());
        model.addAttribute("mage_5_2", f.SKILL_MAGE_FIRE_STORM_DURATION.evaluateDouble() / 1000);
        model.addAttribute("mage_5_3", f.SKILL_MAGE_FIRE_STORM_DAMAGE.evaluateDouble());
        model.addAttribute("mage_5_4", f.SKILL_MAGE_FIRE_STORM_FIRE_DURATION.evaluateDouble() / 1000);


        //Cleric

        maxhp = f.DMG_PLAYER_MAXHP.get(ClassEnum.CLERIC.name());
        maxmana = f.SKILL_PLAYER_MAXMANA.get(ClassEnum.CLERIC.name());
        model.addAttribute("cleric_hp", FormulaFormatter.toString(maxhp));
        model.addAttribute("cleric_hp0", maxhp.evaluateInt(level0));
        model.addAttribute("cleric_hp100", maxhp.evaluateInt(level100));
        model.addAttribute("cleric_mana", FormulaFormatter.toString(maxmana));
        model.addAttribute("cleric_mana0", maxmana.evaluateInt(level0));
        model.addAttribute("cleric_mana100", maxmana.evaluateInt(level100));

        model.addAttribute("cleric_passive_0", f.SKILL_CLERIC_REPEAT_DURATION.evaluateDouble() / 1000);
        model.addAttribute("cleric_passive_1", f.SKILL_CLERIC_REPEAT_WEAKNESS_DURATION.evaluateDouble() / 1000);
        model.addAttribute("cleric_passive_2", f.SKILL_CLERIC_REPEAT_BLINDNESS_DURATION.evaluateDouble() / 1000);
        model.addAttribute("cleric_passive_3", f.SKILL_CLERIC_REPEAT_SLOWNESS_DURATION.evaluateDouble() / 1000);

        model.addAttribute("cleric_2_mana", f.SKILL_MANA.get(SkillEnum.HEALING_BREATH.name()).evaluateInt());
        model.addAttribute("cleric_2_cd", f.SKILL_COOLDOWN.get(SkillEnum.HEALING_BREATH.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("cleric_2_0", f.SKILL_CLERIC_HEALING_BREATH_RANGE.evaluateDouble());
        model.addAttribute("cleric_2_1", f.SKILL_CLERIC_HEALING_BREATH_HEAL_OTHER.evaluateDouble());

        model.addAttribute("cleric_3_mana", f.SKILL_MANA.get(SkillEnum.BLINDING_EXPLOSION.name()).evaluateInt());
        model.addAttribute("cleric_3_cd", f.SKILL_COOLDOWN.get(SkillEnum.BLINDING_EXPLOSION.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("cleric_3_0", f.SKILL_CLERIC_BLINDING_EXPLOSION_RANGE_ENEMY.evaluateDouble());
        model.addAttribute("cleric_3_1", f.SKILL_CLERIC_BLINDING_EXPLOSION_BLINDNESS_DURATION.evaluateDouble() / 1000);
        model.addAttribute("cleric_3_2", f.SKILL_CLERIC_BLINDING_EXPLOSION_DAMAGE.evaluateDouble());
        model.addAttribute("cleric_3_3", f.SKILL_CLERIC_BLINDING_EXPLOSION_RANGE.evaluateDouble());
        model.addAttribute("cleric_3_4", f.SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_OTHER.evaluateDouble());
        model.addAttribute("cleric_3_5", f.SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_SELF.evaluateDouble());

        model.addAttribute("cleric_4_mana", f.SKILL_MANA.get(SkillEnum.MELODY_OF_PERSISTENCE.name()).evaluateInt());
        model.addAttribute("cleric_4_cd", f.SKILL_COOLDOWN.get(SkillEnum.MELODY_OF_PERSISTENCE.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("cleric_4_0", f.SKILL_CLERIC_MELODY_OF_PERSISTENCE_RANGE.evaluateDouble());
        model.addAttribute("cleric_4_1", f.SKILL_CLERIC_MELODY_OF_PERSISTENCE_SPEED_DURATION.evaluateDouble() / 1000);
        model.addAttribute("cleric_4_2", f.SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_OTHER.evaluateDouble());
        model.addAttribute("cleric_4_3", f.SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_SELF.evaluateDouble());

        model.addAttribute("cleric_5_mana", f.SKILL_MANA.get(SkillEnum.DIVINE_BLESSING.name()).evaluateInt());
        model.addAttribute("cleric_5_cd", f.SKILL_COOLDOWN.get(SkillEnum.DIVINE_BLESSING.name()).evaluateInt() / 1000 + "s");
        model.addAttribute("cleric_5_0", f.SKILL_CLERIC_DIVINE_BLESSING_DURATION.evaluateDouble() / 1000);
        return "wiki/classes";
    }
}
