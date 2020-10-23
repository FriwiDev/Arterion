package me.friwi.arterion.plugin.combat.skill.impl.forestrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.ArrowActiveSkill;
import me.friwi.arterion.plugin.combat.skill.ParticleEffectUtil;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.combat.skill.impl.shadowrunner.ThroatCutSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class ArcaneShotSkill extends ArrowActiveSkill<ArrowActiveSkill.ArrowActiveSkillContainerData> {

    public ArcaneShotSkill() {
        super(ClassEnum.FORESTRUNNER, SkillSlotEnum.ACTIVE4);
    }

    @Override
    public long getAllowedShootDelay(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_EXPIRE.evaluateInt(p);
    }

    @Override
    public ArrowActiveSkillContainerData createContainerData(ArterionPlayer p) {
        return new ArrowActiveSkillContainerData();
    }

    @Override
    public double applyToProjectile(ArterionPlayer p, Entity projectile, double damage) {
        ParticleEffect.ParticleColor color = new ParticleEffect.OrdinaryColor(200, 0, 200);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            Location last = projectile.getLocation().clone();

            @Override
            public void run() {
                if (projectile.isDead() || projectile.isOnGround()) {
                    cancel();
                    return;
                }
                ParticleEffectUtil.drawLine(last, projectile.getLocation(), 4, loc -> ParticleEffect.REDSTONE.display(color, loc, PARTICLE_RANGE));
                last = projectile.getLocation().clone();
            }
        }, 1, 1);
        return damage;
    }

    @Override
    public void onHitEntity(ArterionPlayer p, Projectile projectile, Entity hit) {
        if (hit instanceof LivingEntity) {
            if (!Combat.isEnemy(p, (LivingEntity) hit)) {
                onHitBlock(p, projectile, null);
                return;
            }
            //Mark enemy
            if (hit instanceof Player) {
                ArterionPlayer marked = ArterionPlayerUtil.get((Player) hit);
                p.getFriendlyPlayerList().addMarkedPlayer(marked);
                Binding<Entity> binding = Hooks.PRIMARY_ATTACK_DAMAGE_RECEIVE_HOOK.subscribe(hit, tuple -> {
                    double damageBoost = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DAMAGE_BOOST.evaluateDouble(p) / 100d;
                    tuple.setSecondValue(tuple.getSecondValue() * (1 + damageBoost));
                    return tuple;
                });
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        p.getFriendlyPlayerList().removeMarkedPlayer(marked);
                        Hooks.PRIMARY_ATTACK_DAMAGE_RECEIVE_HOOK.unsubscribe(binding);
                    }
                }, ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DURATION.evaluateInt(p) / 50);
            } else {
                Binding<Entity> binding = Hooks.PRIMARY_ATTACK_DAMAGE_RECEIVE_HOOK.subscribe(hit, tuple -> {
                    double damageBoost = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DAMAGE_BOOST.evaluateDouble(p) / 100d;
                    tuple.setSecondValue(tuple.getSecondValue() * (1 + damageBoost));
                    return tuple;
                });
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        Hooks.PRIMARY_ATTACK_DAMAGE_RECEIVE_HOOK.unsubscribe(binding);
                    }
                }, ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DURATION.evaluateInt(p) / 50);
            }

            this.getSkillDataContainer(p).setActiveUntil(System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DURATION.evaluateInt(p));
            this.updateToMod(p);

            //Teleport enemy
            LivingEntity target = p.getBukkitPlayer();

            Location behindTarget = target.getLocation().clone().add(hit.getLocation().clone().subtract(target.getLocation()).toVector().normalize());
            behindTarget.setY(target.getLocation().getY());

            //Fix location and handle block stuck
            behindTarget = ThroatCutSkill.findSafeLocation(target.getLocation(), behindTarget, false, 3);
            if (behindTarget == null) {
                p.scheduleHotbarCard(new SkillFailCard(p, "skill.nospace.hotbar", this));
                return;
            }

            //Target the enemy
            if (!target.getLocation().equals(behindTarget)) {
                behindTarget.setDirection(target.getEyeLocation().clone().subtract(behindTarget.clone().add(0, ((LivingEntity) hit).getEyeHeight(), 0)).toVector());
            }
            if (behindTarget.getPitch() <= -89.5)
                behindTarget.setDirection(target.getLocation().getDirection().clone().multiply(-1));

            Location backup = hit.getLocation().clone();

            //Perform tp
            Entity tp = hit;
            while (tp.getVehicle() != null) tp = tp.getVehicle();
            tp.teleport(behindTarget);

            //Draw line
            Location from = backup.clone().add(0, 1, 0);
            Location to = hit.getLocation().clone().add(0, 1, 0);
            ParticleEffectUtil.drawLine(from, to, 3, locc -> ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(200, 0, 200), locc, PARTICLE_RANGE));

            //Draw ender effects
            backup.getWorld().playEffect(backup, Effect.ENDER_SIGNAL, 0);
            hit.getWorld().playEffect(hit.getLocation(), Effect.ENDER_SIGNAL, 0);

            //Play sounds
            backup.getWorld().playSound(backup, Sound.ENDERMAN_TELEPORT, 1f, 1f);
            hit.getWorld().playSound(hit.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
        } else {
            onHitBlock(p, projectile, null);
        }
    }

    @Override
    public void onHitBlock(ArterionPlayer p, Projectile projectile, Block hit) {
        projectile.getWorld().playSound(projectile.getLocation(), Sound.FIZZ, 1f, 1f);
        p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.FIZZ, 0.8f, 1f);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{
                ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_EXPIRE.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_DAMAGE_BOOST.evaluateDouble(p)
        };
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_EXPIRE.evaluateInt(p);
    }
}
