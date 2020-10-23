package me.friwi.arterion.plugin.combat.skill.impl.cleric;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class BlindingExplosionSkill extends RestrictedActiveSkill<SkillContainerData> {

    public BlindingExplosionSkill() {
        super(ClassEnum.CLERIC, SkillSlotEnum.ACTIVE3);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_RANGE.evaluateDouble(p);
        double enemyRange = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_RANGE_ENEMY.evaluateDouble(p);
        int selfHeal = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_SELF.evaluateInt(p);
        int otherHeal = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_OTHER.evaluateInt(p);
        int damage = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_DAMAGE.evaluateInt(p);
        int blindnessTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_BLINDNESS_DURATION.evaluateInt(p) / 50;

        this.printCastMessage(p, null);

        Location loc = p.getBukkitPlayer().getLocation().clone().add(0, 7, 0);

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                p.getClericHealManager().heal(BlindingExplosionSkill.this, selfHeal, getMaxCooldown(p), p);

                for (Entity e : TargetCalculator.getAOETargets(p.getBukkitPlayer(), range)) {
                    if (e instanceof LivingEntity) {
                        if (e instanceof Player && !Combat.isEnemy(p, (LivingEntity) e)) {
                            ArterionPlayer other = ArterionPlayerUtil.get((Player) e);
                            if (p.getPlayerRelation(other) == PlayerRelation.FRIENDLY)
                                other.getClericHealManager().heal(BlindingExplosionSkill.this, otherHeal, getMaxCooldown(p), p);
                        }
                    }
                }

                for (Entity e : TargetCalculator.getAOETargets(p.getBukkitPlayer(), enemyRange)) {
                    if (e instanceof LivingEntity) {
                        if (Combat.isEnemy(p, (LivingEntity) e)) {
                            ArterionPlugin.getInstance().getDamageManager().damage((LivingEntity) e, p, damage, BlindingExplosionSkill.this);
                            if (e instanceof Player) {
                                ArterionPlayer other = ArterionPlayerUtil.get((Player) e);
                                other.getPotionTracker().addPotionEffect(PotionEffectType.BLINDNESS, 0, blindnessTicks);
                            }
                        }
                    }
                }

                ParticleEffect.FLAME.display(0.15f, 0.15f, 0.15f, 1, 32, loc, PARTICLE_RANGE);
                ParticleEffect.FLAME.display(0.15f, 0.15f, 0.15f, 1, 32, loc, PARTICLE_RANGE);
                ParticleEffect.LAVA.display(0.15f, 0.15f, 0.15f, 1, 32, loc, PARTICLE_RANGE);
                loc.getWorld().playSound(loc, Sound.EXPLODE, 0.8f, 1f);
            }
        }, 7);

        //Draw pole
        Location from = p.getBukkitPlayer().getLocation().clone();
        Location to = from.clone().add(0, 7, 0);
        ParticleEffectUtil.drawLine(from, to, 1, locc -> ParticleEffect.FLAME.display(0.25f, 0.5f, 0.25f, 0, 14, locc, PARTICLE_RANGE));

        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_RANGE_ENEMY.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_BLINDNESS_DURATION.evaluateDouble(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_DAMAGE.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_SELF.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_BLINDING_EXPLOSION_HEAL_OTHER.evaluateInt(p)};
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
