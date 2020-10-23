package me.friwi.arterion.plugin.combat.skill.impl.mage;

import com.darkblade12.particleeffect.ParticleEffect;
import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.ProjectileActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.recordable.FireballRandomUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.List;

public class FireballSkill extends ProjectileActiveSkill<SkillContainerData> {

    public FireballSkill() {
        super(ClassEnum.MAGE, SkillSlotEnum.ACTIVE2);
    }

    @Override
    public Projectile createProjectileOnCast(ArterionPlayer p) {
        LargeFireball ball = p.getBukkitPlayer().launchProjectile(LargeFireball.class);
        ball.setBounce(false);
        ball.setIsIncendiary(false);
        FireballRandomUtil.setEliminateRandomFactors(ball, true);
        Vector dir = p.getBukkitPlayer().getLocation().getDirection().clone().normalize();
        ball.setDirection(dir.clone());
        ball.setVelocity(dir.clone().multiply(ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_SPEED.evaluateDouble(p)));

        this.printCastMessage(p, null);

        int tickInterval = 1;
        int amountPerSpawn = 3;

        Binding<Entity> binding = Hooks.ENTITY_EXPLODE_EVENT_HOOK.subscribe(ball, evt -> {
            for (Block b : evt.blockList()) {
                Location loc = b.getLocation().clone();
                loc.setY(evt.getLocation().getY());
                FallingBlock fb = b.getWorld().spawnFallingBlock(loc.add(evt.getLocation()).multiply(0.5).add(0, 1, 0), b.getTypeId(), b.getData());
                fb.setDropItem(false);
                fb.setHurtEntities(false);
                Vector vec = fb.getLocation().toVector().subtract(evt.getLocation().toVector()).multiply(0.1);
                vec.setY(0.6 + Math.random() * 0.6);
                fb.setVelocity(vec);
                Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.subscribe(fb, evt1 -> {
                    fb.remove();
                    ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(fb.getMaterial(), fb.getBlockData()), 0, 0, 0, 1, 20, fb.getLocation(), 30);
                    fb.getWorld().playSound(fb.getLocation(), Sound.DIG_GRASS, 1f, 1f);
                    evt1.setCancelled(true);
                    return evt1;
                });
            }
            List<Block> blocks = Lists.newArrayList(evt.blockList());
            evt.blockList().clear();

            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    for (Block b : blocks) {
                        b.getState().update();
                    }
                }
            }, 3l);

            return null;
        });

        Hooks.ENTITY_EXPLOSION_DAMAGE_DEAL_HOOK.subscribe(ball, tuple -> {
            tuple.setSecondValue(0d);
            return tuple;
        });

        LargeFireball finalBall = ball;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                if (finalBall.isDead() || finalBall.isOnGround()) {
                    Hooks.ENTITY_EXPLODE_EVENT_HOOK.unsubscribe(binding);
                    cancel();
                    return;
                }
                ParticleEffect.FLAME.display(0.1f, 0.1f, 0.1f, 0.05f, amountPerSpawn, finalBall.getLocation(), PARTICLE_RANGE);
            }
        }, 0, tickInterval);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.GHAST_FIREBALL, 0.8f, 1f);

        return ball;
    }

    private void applyToEntity(ArterionPlayer p, Projectile ball, Entity hit) {
        int fireTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_DURATION.evaluateInt(p) / 50;
        if (hit.getFireTicks() < fireTicks) hit.setFireTicks(fireTicks);
        ArterionPlugin.getInstance().getDamageManager().damage((LivingEntity) hit, p, ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_DAMAGE.evaluateDouble(p), this);
        if (hit instanceof Player) {
            ArterionPlayer attackedPlayer = ArterionPlayerUtil.get((Player) hit);
            attackedPlayer.getBAC().disableMovementChecksFor(1000);
        }
        Vector vel = hit.getLocation().toVector().subtract(ball.getLocation().toVector());
        vel.setY(0);
        vel.normalize();
        vel.multiply(0.65);
        vel.setY(0.25);
        hit.setVelocity(vel);
    }

    @Override
    public void onHitEntity(ArterionPlayer p, Projectile projectile, Entity hit) {
        onHit(p, projectile, hit);
    }

    @Override
    public void onHitBlock(ArterionPlayer p, Projectile projectile, Block hit) {
        onHit(p, projectile, null);
    }

    private void onHit(ArterionPlayer p, Projectile projectile, Entity ent) {
        projectile.remove();
        ParticleEffect.LAVA.display(0.5f, 0.5f, 0.5f, 1f, 25, projectile.getLocation(), PARTICLE_RANGE);

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_RANGE.evaluateDouble(p);

        //Deal damage
        boolean found = false;
        for (Entity hit : projectile.getNearbyEntities(range, range, range)) {
            if (hit.equals(ent)) found = true;
            if (hit instanceof LivingEntity && Combat.isEnemy(p, (LivingEntity) hit)) {
                applyToEntity(p, projectile, hit);
            }
        }
        if (!found && ent instanceof LivingEntity && Combat.isEnemy(p, (LivingEntity) ent)) {
            applyToEntity(p, projectile, ent);
        }
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_DAMAGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIREBALL_RANGE.evaluateDouble(p)
        };
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
}
