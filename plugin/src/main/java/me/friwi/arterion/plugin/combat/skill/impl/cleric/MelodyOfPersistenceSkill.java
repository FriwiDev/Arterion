package me.friwi.arterion.plugin.combat.skill.impl.cleric;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MelodyOfPersistenceSkill extends RestrictedActiveSkill<SkillContainerData> {

    public MelodyOfPersistenceSkill() {
        super(ClassEnum.CLERIC, SkillSlotEnum.ACTIVE4);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_RANGE.evaluateDouble(p);
        int selfHeal = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_SELF.evaluateInt(p);
        int otherHeal = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_OTHER.evaluateInt(p);
        int speedTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_SPEED_DURATION.evaluateInt(p) / 50;

        this.printCastMessage(p, null);

        List<Entity> affected = TargetCalculator.getAOETargetsWithRelation(p, range, PlayerRelation.FRIENDLY);
        Map<ArterionPlayer, Integer> healed = new HashMap<>();

        for (Entity e : affected) {
            if (e instanceof Player) {
                ArterionPlayer x = ArterionPlayerUtil.get((Player) e);
                x.getPotionTracker().addPotionEffect(PotionEffectType.SPEED, 2, speedTicks);
            }
        }

        p.getPotionTracker().addPotionEffect(PotionEffectType.SPEED, 2, speedTicks);

        //Notes
        for (int i : new Integer[]{0, 5, 6, 7, 13, 14, 15, 22, 24, 5}) {
            ParticleEffect.NOTE.display(new ParticleEffect.NoteColor(i), p.getBukkitPlayer().getLocation().clone().add((Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2 + 1, (Math.random() - 0.5) * 2), PARTICLE_RANGE);
        }
        ParticleEffect.HEART.display(1f, 1f, 1f, 0f, 7, p.getBukkitPlayer().getLocation().clone().add(0, 1, 0), PARTICLE_RANGE);


        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                heal(p, p, selfHeal, healed);

                for (Entity e : affected) {
                    if (e instanceof Player) {
                        ArterionPlayer x = ArterionPlayerUtil.get((Player) e);
                        heal(p, x, otherHeal, healed);
                    }
                }

                p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.NOTE_PLING, 1f, 0.6f + tick * 0.1f);

                tick++;
                if (tick >= 5) {
                    cancel();
                }
            }
        }, 0, 3);

        return true;
    }

    public void heal(ArterionPlayer p, ArterionPlayer x, int heal, Map<ArterionPlayer, Integer> prev) {
        if (x.getHealth() <= 0) return;
        if (prev.containsKey(x)) {
            x.heal(prev.get(x));
        } else {
            prev.put(x, x.getClericHealManager().heal(this, heal, getMaxCooldown(p), p));
        }
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_SPEED_DURATION.evaluateDouble(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_SELF.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_MELODY_OF_PERSISTENCE_HEAL_OTHER.evaluateInt(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        setSkillDataContainer(p, new SkillContainerData());
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        setSkillDataContainer(p, null);
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return 0;
    }
}
