package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.combat.skill.impl.barbar.*;
import me.friwi.arterion.plugin.combat.skill.impl.cleric.*;
import me.friwi.arterion.plugin.combat.skill.impl.forestrunner.*;
import me.friwi.arterion.plugin.combat.skill.impl.mage.*;
import me.friwi.arterion.plugin.combat.skill.impl.none.HealSkill;
import me.friwi.arterion.plugin.combat.skill.impl.paladin.*;
import me.friwi.arterion.plugin.combat.skill.impl.shadowrunner.*;

public enum SkillEnum {
    //All
    HEAL(new HealSkill()),

    //Paladin
    RESISTANCE(new ResistanceSkill()),
    BLESSING_OF_THE_GODS(new BlessingOfTheGodsSkill()),
    CHAIN(new ChainSkill()),
    HELPING_HAND(new HelpingHandSkill()),
    GUST_OF_WIND(new GustOfWindSkill()),

    //Barbar
    BERSERK_RAGE(new BerserkRageSkill()),
    RAGE(new RageSkill()),
    ENFORCED_ARMOR(new EnforcedArmorSkill()),
    MIGHTY_HIT(new MightyHitSkill()),
    STOMP(new StompSkill()),

    //Shadowrunner
    AMBUSH(new AmbushSkill()),
    THROAT_CUT(new ThroatCutSkill()),
    SHADOW_CLONE(new ShadowCloneSkill()),
    SHADOW_CAPE(new ShadowCapeSkill()),
    ACID_BOMB(new AcidBombSkill()),

    //Forestrunner
    HEADSHOT(new HeadshotSkill()),
    ARROW_HAIL(new ArrowHailSkill()),
    ARCANE_SHOT(new ArcaneShotSkill()),
    JUMP(new JumpSkill()),
    THROW_NET(new ThrowNetSkill()),

    //Mage
    MANA_STEAL(new ManaStealSkill()),
    ARCANE_SHIFT(new ArcaneShiftSkill()),
    CHAIN_LIGHTNING(new ChainLightningSkill()),
    FIREBALL(new FireballSkill()),
    FIRE_STORM(new FireStormSkill()),

    //Cleric
    REPEAT(new RepeatSkill()),
    HEALING_BREATH(new HealingBreathSkill()),
    BLINDING_EXPLOSION(new BlindingExplosionSkill()),
    MELODY_OF_PERSISTENCE(new MelodyOfPersistenceSkill()),
    DIVINE_BLESSING(new DivineBlessingSkill());

    private Skill skill;

    SkillEnum(Skill skill) {
        this.skill = skill;
        this.skill.setSkillType(this);
    }

    public Skill getSkill() {
        return skill;
    }
}
