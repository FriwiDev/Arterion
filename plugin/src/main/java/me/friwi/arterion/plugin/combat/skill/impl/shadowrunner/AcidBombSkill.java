package me.friwi.arterion.plugin.combat.skill.impl.shadowrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.ProjectileActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.recordable.InvisibleEntityUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class AcidBombSkill extends ProjectileActiveSkill<SkillContainerData> {

    public AcidBombSkill() {
        super(ClassEnum.SHADOWRUNNER, SkillSlotEnum.ACTIVE5);
    }

    @Override
    public Projectile createProjectileOnCast(ArterionPlayer p) {
        Snowball ball = p.getBukkitPlayer().launchProjectile(Snowball.class);
        ball.setVelocity(ball.getVelocity().multiply(0.65));
        Item item = p.getBukkitPlayer().getWorld().dropItem(ball.getLocation().clone(), new ItemStack(Material.WOOL, 1, (short) 15));
        item.setPickupDelay(1000000);
        item.setVelocity(ball.getVelocity());
        InvisibleEntityUtil.setInvisible(ball, true);

        this.printCastMessage(p, null);

        int tickInterval = 1;
        int amountPerSpawn = 5;

        Snowball finalBall = ball;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                if (finalBall.isDead()) {
                    item.remove();
                    cancel();
                    return;
                }
                item.teleport(ball.getLocation());
                item.setVelocity(ball.getVelocity());
                ParticleEffect.SMOKE_NORMAL.display(0, 0.5f, 0, 0.05f, amountPerSpawn, finalBall.getLocation(), PARTICLE_RANGE);
            }
        }, 0, tickInterval);

        return ball;
    }

    @Override
    public void onHitEntity(ArterionPlayer p, Projectile projectile, Entity hit) {
        onHit(p, projectile);
    }

    @Override
    public void onHitBlock(ArterionPlayer p, Projectile projectile, Block hit) {
        onHit(p, projectile);
    }

    private void onHit(ArterionPlayer p, Projectile projectile) {
        projectile.remove();
        ParticleEffect.SMOKE_LARGE.display(0.2f, 0.5f, 0.2f, 1f, 7, projectile.getLocation(), PARTICLE_RANGE);
        ParticleEffect.SMOKE_LARGE.display(0.1f, 0.5f, 0.1f, 0.1f, 10, projectile.getLocation(), PARTICLE_RANGE);
        ParticleEffect.SMOKE_LARGE.display(0f, 0.5f, 0f, 0.05f, 17, projectile.getLocation(), PARTICLE_RANGE);
        projectile.getWorld().playSound(projectile.getLocation(), Sound.FIZZ, 0.8f, 1f);

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_RANGE.evaluateDouble(p);
        double damage = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_DAMAGE.evaluateDouble(p);
        int ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_DURATION.evaluateInt(p) / 50;

        List<Entity> nearby = TargetCalculator.getAOETargetsFromNonLivingEntity(projectile, range);

        boolean hitOne = false;

        for (Entity ent : nearby) {
            if (ent instanceof LivingEntity && Combat.isEnemy(p, (LivingEntity) ent)) {
                ArterionPlugin.getInstance().getDamageManager().damage((LivingEntity) ent, p, damage, this);
                if (ent instanceof Player) {
                    ArterionPlayer ap = ArterionPlayerUtil.get((Player) ent);
                    ap.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ticks, 0));
                    ap.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0));
                    ap.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 0));
                } else {
                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ticks, 0));
                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0));
                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 0));
                }
                hitOne = true;
            }
        }

        if (hitOne) {
            this.getSkillDataContainer(p).setActiveUntil(System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_DURATION.evaluateInt(p));
            this.updateToMod(p);
        }
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{
                ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_DAMAGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_ACID_BOMB_DURATION.evaluateInt(p) / 1000d
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
