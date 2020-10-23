package me.friwi.arterion.plugin.combat.skill.impl.cleric;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

import static me.friwi.arterion.plugin.combat.PlayerRelation.FRIENDLY;

public class HealingBreathSkill extends RestrictedActiveSkill<SkillContainerData> {

    public HealingBreathSkill() {
        super(ClassEnum.CLERIC, SkillSlotEnum.ACTIVE2);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_HEALING_BREATH_RANGE.evaluateDouble(p);
        double speed = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_HEALING_BREATH_SPEED.evaluateDouble(p);
        double diversion = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_HEALING_BREATH_DIVERSION.evaluateDouble(p);
        int otherHeal = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_HEALING_BREATH_HEAL_OTHER.evaluateInt(p);

        this.printCastMessage(p, null);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ZOMBIE_INFECT, 1f, 1f);

        Vector dir = p.getBukkitPlayer().getLocation().getDirection().clone().normalize();
        Location startPoint = p.getBukkitPlayer().getLocation().clone().add(0, p.getBukkitPlayer().getEyeHeight(), 0);

        double stepsPerTick = 10;
        speed /= stepsPerTick;
        diversion /= stepsPerTick;

        Vector distanceVector = dir.clone().crossProduct(dir.clone().add(new Vector(0, 1, 0))).normalize().multiply(diversion);
        Vector growing = dir.clone().multiply(speed).add(distanceVector);
        Vector perGrowth = growing.clone();
        double limit = growing.clone().normalize().crossProduct(dir).length();

        Set<LivingEntity> healed = new HashSet<>();

        double finalSpeed = speed;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            double perGrowthLength = growing.length();
            double length = perGrowthLength;
            double anglePerTick = Math.PI * 12 / 20 / stepsPerTick;
            double angle = 0;
            ParticleEffect.OrdinaryColor color = new ParticleEffect.OrdinaryColor(200, 200, 0);

            @Override
            public void run() {
                for (int i = 0; i < stepsPerTick; i++) {
                    growing.add(perGrowth);
                    angle += anglePerTick;
                    length += perGrowthLength;
                    Vector rotated = rotateAxis(growing.clone(), dir, angle);
                    Location x = startPoint.clone().add(rotated);
                    ParticleEffect.FLAME.display(0, 0, 0, 0, 1, x.clone().add(dir.clone().multiply(0.2)), PARTICLE_RANGE);
                    ParticleEffect.REDSTONE.display(color, x, PARTICLE_RANGE);
                }
                if (tick % 2 == 0) {
                    for (Entity e : TargetCalculator.getAOETargetsWithRelation(p, range * 2, FRIENDLY)) {
                        if (e instanceof Player) {
                            Vector v = e.getLocation().clone().add(0, 1, 0).subtract(startPoint).toVector().normalize();
                            if (v.clone().crossProduct(dir).length() <= limit && v.dot(dir) >= 0) {
                                if (healed.add((LivingEntity) e)) {
                                    ArterionPlayerUtil.get((Player) e).getClericHealManager().heal(HealingBreathSkill.this, otherHeal, getMaxCooldown(p), p);
                                    for (ArterionPlayer o : p.getFriendlyPlayerList().getFriendlies()) {
                                        o.getBukkitPlayer().playSound(e.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                                    }
                                    ParticleEffect.VILLAGER_HAPPY.display(0.5f, 0.5f, 0.5f, 0, 10, e.getLocation().clone().add(0, 1, 0), PARTICLE_RANGE);
                                }
                            }
                        }
                    }
                }
                tick++;
                if (length >= range) cancel();
            }
        }, 1, 1);

        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_HEALING_BREATH_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_HEALING_BREATH_HEAL_OTHER.evaluateInt(p)};
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

    public Vector rotateAxis(Vector dir, Vector axis, double theta) {
        Vector n = axis.clone().normalize();
        Vector x = dir.clone();
        Vector partA = n.clone().multiply(n.clone().dot(x));
        Vector partB = n.clone().crossProduct(x).multiply(Math.cos(theta)).crossProduct(n);
        Vector partC = n.clone().crossProduct(x).multiply(Math.sin(theta));
        return partA.add(partB).add(partC);
    }
}
