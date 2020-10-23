package me.friwi.arterion.plugin.combat.skill.impl.cleric;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.ActiveSkill;
import me.friwi.arterion.plugin.combat.skill.PassiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RepeatSkill extends PassiveSkill<RepeatSkill.RepeatSkillContainerData> {

    public RepeatSkill() {
        super(ClassEnum.CLERIC);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_WEAKNESS_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_SLOWNESS_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_BLINDNESS_DURATION.evaluateInt(p) / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with damage hook
        Binding<Entity> binding = Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), tuple -> {
            RepeatSkillContainerData data = getSkillDataContainer(p);
            int duration = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_DURATION.evaluateInt(p);
            if (data.lastSkill != null && data.lastSkillUse + duration > System.currentTimeMillis()) {
                data.lastSkillUse = 0;
                PotionEffectType type = null;
                int ticks = 0;
                int amplifier = 1;

                if (data.lastSkill instanceof HealingBreathSkill) {
                    type = PotionEffectType.WEAKNESS;
                    ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_WEAKNESS_DURATION.evaluateInt(p) / 50;
                } else if (data.lastSkill instanceof BlindingExplosionSkill) {
                    type = PotionEffectType.BLINDNESS;
                    ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_BLINDNESS_DURATION.evaluateInt(p) / 50;
                } else {
                    type = PotionEffectType.SLOW;
                    ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_REPEAT_SLOWNESS_DURATION.evaluateInt(p) / 50;
                }

                if (tuple.getFirstValue() instanceof Player) {
                    ArterionPlayer victim = ArterionPlayerUtil.get((Player) tuple.getFirstValue());
                    victim.getPotionTracker().addPotionEffect(type, amplifier, ticks);
                } else if (tuple.getFirstValue() instanceof LivingEntity) {
                    ((LivingEntity) tuple.getFirstValue()).addPotionEffect(new PotionEffect(type, ticks, amplifier));
                }
            }
            return tuple;
        });
        Binding<ArterionPlayer> skillCast = Hooks.PLAYER_POST_SKILL_CAST_HOOK.subscribe(p, skill -> {
            if (skill instanceof HealingBreathSkill || skill instanceof BlindingExplosionSkill || skill instanceof MelodyOfPersistenceSkill) {
                RepeatSkillContainerData data = getSkillDataContainer(p);
                data.lastSkill = (ActiveSkill) skill;
                data.lastSkillUse = System.currentTimeMillis();
            }
            return skill;
        });
        setSkillDataContainer(p, new RepeatSkillContainerData(binding, skillCast));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        RepeatSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.unsubscribe(data.binding);
            Hooks.PLAYER_POST_SKILL_CAST_HOOK.unsubscribe(data.skillCast);
        }
    }

    protected class RepeatSkillContainerData extends SkillContainerData {
        private Binding<Entity> binding;
        private Binding<ArterionPlayer> skillCast;
        private ActiveSkill lastSkill = null;
        private long lastSkillUse = 0;


        private RepeatSkillContainerData(Binding<Entity> binding, Binding<ArterionPlayer> skillCast) {
            this.binding = binding;
            this.skillCast = skillCast;
        }
    }
}
