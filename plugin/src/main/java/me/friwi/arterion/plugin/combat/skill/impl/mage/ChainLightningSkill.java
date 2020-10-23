package me.friwi.arterion.plugin.combat.skill.impl.mage;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.*;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.*;

public class ChainLightningSkill extends TargetActiveSkill<SkillContainerData, LivingEntity> {

    private LinkedList<LivingEntity> blackListed = new LinkedList<>();

    public ChainLightningSkill() {
        super(ClassEnum.MAGE, SkillSlotEnum.ACTIVE4, LivingEntity.class);
    }

    @Override
    public double getRange(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_RANGE.evaluateDouble(p);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_COUNT.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_MAX_PER_PLAYER.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_DAMAGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_JUMP_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_PER_ENTITY_COOLDOWN.evaluateInt(p) / 1000d};
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
        return p.getRegion() != null && p.getRegion().isPvp();
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return 0;
    }

    @Override
    public boolean castWithTarget(ArterionPlayer p, LivingEntity target) {
        if (target == null || !Combat.isEnemy(p, target)) {
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.notarget.hotbar", this));
            return true;
        }

        if (blackListed.contains(target)) {
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.chain_lightning.denied.hotbar", this));
            return false;
        }

        if (target.getHealth() <= 0) return false;
        this.printCastMessage(p, target);

        double dmg = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_DAMAGE.evaluateDouble(p);
        int count = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_COUNT.evaluateInt(p);
        int max = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_MAX_PER_PLAYER.evaluateInt(p);
        double speed = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_SPEED.evaluateDouble(p);
        double jumpRange = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_JUMP_RANGE.evaluateDouble(p);
        int perEntityCooldownTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_CHAIN_LIGHTNING_PER_ENTITY_COOLDOWN.evaluateInt(p) / 50;

        LinkedList<LivingEntity> myBlackListed = new LinkedList<>();

        Map<LivingEntity, Integer> hit = new HashMap<>();
        onHitLivingEntity(target, p, hit, dmg, 1, count, myBlackListed, perEntityCooldownTicks);

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int i = 1;
            boolean selectNewTarget = true;
            LivingEntity currentTarget = target;
            Location lightningLocation = target.getLocation().clone().add(0, 1, 0);

            @Override
            public void run() {
                if (i >= count || (!selectNewTarget && currentTarget == null)) {
                    cancel();
                    return;
                }
                if (selectNewTarget) {
                    selectNewTarget = false;
                    List<Entity> targets = TargetCalculator.getAOETargets(currentTarget, jumpRange);
                    Collections.shuffle(targets);
                    LivingEntity oldTarget = currentTarget;
                    currentTarget = null;
                    for (Entity e : targets) {
                        if (e instanceof LivingEntity && doesQualify((LivingEntity) e, p, hit, max, myBlackListed)
                                && Combat.isEnemy(p, (LivingEntity) e) && oldTarget.hasLineOfSight(e)) {
                            currentTarget = (LivingEntity) e;
                            break;
                        }
                    }
                }
                if (currentTarget == null) {
                    cancel();
                    return;
                }

                Location goal = currentTarget.getLocation().clone().add(0, 1, 0);

                if (!goal.getWorld().equals(lightningLocation.getWorld()) || goal.distance(lightningLocation) > jumpRange + 1) {
                    //Player dodged
                    cancel();
                    return;
                }

                double dist = goal.distance(lightningLocation);
                Vector mot = goal.toVector().subtract(lightningLocation.toVector()).normalize().multiply(speed);
                Location to = lightningLocation.clone().add(mot);
                if (mot.length() > dist) {
                    i++;
                    if (!doesQualify(currentTarget, p, hit, max, myBlackListed)) {
                        cancel();
                        return;
                    }
                    onHitLivingEntity(currentTarget, p, hit, dmg, i, count, myBlackListed, perEntityCooldownTicks);
                    selectNewTarget = true;
                    to = goal;
                }

                //Draw line
                Location from = lightningLocation.clone();
                ParticleEffectUtil.drawLine(from, to, 10, locc -> ParticleEffect.FIREWORKS_SPARK.display(0, 0, 0, 0, 1, locc, PARTICLE_RANGE));

                //Update lightning location
                lightningLocation = to;
            }
        }, 1, 1);
        return true;
    }

    public void onHitLivingEntity(LivingEntity e, ArterionPlayer p, Map<LivingEntity, Integer> hit, double dmg, int count, int possible, LinkedList<LivingEntity> myBlackListed, int perEntityCooldownTicks) {
        ArterionPlugin.getInstance().getDamageManager().damage(e, p, dmg, this);
        ParticleEffect.VILLAGER_ANGRY.display(0.5f, 0.5f, 0.5f, 0, 7, e.getLocation().clone().add(0, 1, 0), PARTICLE_RANGE);
        e.getWorld().playSound(e.getLocation(), Sound.BAT_HURT, 1f, 2f);
        if (!hit.containsKey(e)) hit.put(e, 1);
        else hit.put(e, hit.get(e) + 1);
        p.scheduleHotbarCard(new ChainLightningHitCard(p, count, possible));
        myBlackListed.add(e);
        blackListed.add(e);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                myBlackListed.remove(e);
                blackListed.remove(e);
            }
        }, perEntityCooldownTicks);
    }

    public boolean doesQualify(LivingEntity e, ArterionPlayer p, Map<LivingEntity, Integer> hit, int max, List<LivingEntity> myBlackListed) {
        if (!myBlackListed.contains(e) && blackListed.contains(e))
            return false; //Another chain lightning damaged this entity recently
        if (e.getHealth() <= 0) return false;
        if (!Combat.isEnemy(p, e)) return false;
        if (hit.containsKey(e)) return hit.get(e) < max;
        else return true;
    }
}
