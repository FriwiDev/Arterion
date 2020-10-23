package me.friwi.arterion.plugin.combat.skill.impl.paladin;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class GustOfWindSkill extends RestrictedActiveSkill<SkillContainerData> {

    public GustOfWindSkill() {
        super(ClassEnum.PALADIN, SkillSlotEnum.ACTIVE3);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_GUST_OF_WIND_RANGE.evaluateDouble(p);

        this.printCastMessage(p, null);

        p.getPotionTracker().removeAllPotionEffects(new PotionEffectType[]{
                PotionEffectType.SLOW,
                PotionEffectType.SLOW_DIGGING,
                PotionEffectType.HARM,
                PotionEffectType.CONFUSION,
                PotionEffectType.BLINDNESS,
                PotionEffectType.HUNGER,
                PotionEffectType.WEAKNESS,
                PotionEffectType.POISON
        });

        for (Entity e : TargetCalculator.getAOETargets(p.getBukkitPlayer(), range)) {
            if (e instanceof LivingEntity) {
                if (Combat.isEnemy(p, (LivingEntity) e)) {
                    if (e instanceof Player) {
                        ArterionPlayer attackedPlayer = ArterionPlayerUtil.get((Player) e);
                        attackedPlayer.getBAC().disableMovementChecksFor(4000);
                    }
                    //Knockback
                    Vector mot = e.getLocation().toVector().subtract(p.getBukkitPlayer().getLocation().toVector()).normalize();
                    mot.multiply(ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_GUST_OF_WIND_KNOCKBACK_XZ.evaluateDouble(p));
                    mot.setY(ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_GUST_OF_WIND_KNOCKBACK_Y.evaluateDouble(p));
                    e.setVelocity(mot);
                } else {
                    if (e instanceof Player) {
                        ArterionPlayer other = ArterionPlayerUtil.get((Player) e);
                        if (p.getPlayerRelation(other) == PlayerRelation.FRIENDLY) {
                            other.getPotionTracker().removeAllPotionEffects(new PotionEffectType[]{
                                    PotionEffectType.SLOW,
                                    PotionEffectType.SLOW_DIGGING,
                                    PotionEffectType.HARM,
                                    PotionEffectType.CONFUSION,
                                    PotionEffectType.BLINDNESS,
                                    PotionEffectType.HUNGER,
                                    PotionEffectType.WEAKNESS,
                                    PotionEffectType.POISON
                            });
                        }
                    }
                }
            }
        }

        //Play the explosion
        p.getBukkitPlayer().getWorld().playEffect(p.getBukkitPlayer().getLocation().clone().add(0, 1.5, 0), Effect.EXPLOSION_LARGE, 1);
        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.GHAST_FIREBALL, 0.8f, 1f);

        Location loc = p.getBukkitPlayer().getLocation().clone();

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                tick++;
                for (int i = 0; i < 4; i++) {
                    Vector add = new Vector(i % 2 == 0 ? -1 : 1, 1.5, i >= 2 ? -1 : 1).multiply(tick);
                    p.getBukkitPlayer().getWorld().playEffect(loc.clone().add(add), Effect.EXPLOSION_LARGE, 1);
                }
                if (tick >= 6) cancel();
            }
        }, 2, 2);

        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_GUST_OF_WIND_RANGE.evaluateDouble(p)};
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
