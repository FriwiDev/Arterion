package me.friwi.arterion.plugin.combat.skill.impl.forestrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.ArrowActiveSkill;
import me.friwi.arterion.plugin.combat.skill.ParticleEffectUtil;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;

public class ArrowHailSkill extends ArrowActiveSkill<ArrowActiveSkill.ArrowActiveSkillContainerData> {

    private Method arrowGetHandle;
    private Field arrowFromPlayer;

    public ArrowHailSkill() {
        super(ClassEnum.FORESTRUNNER, SkillSlotEnum.ACTIVE5);
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
        ParticleEffect.ParticleColor color = new ParticleEffect.OrdinaryColor(35, 35, 0);
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
            ArterionPlugin.getInstance().getDamageManager().damage((LivingEntity) hit, p, ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARROW_HAIL_DAMAGE.evaluateDouble(p), this);
        }
        onHit(p, projectile);
    }

    @Override
    public void onHitBlock(ArterionPlayer p, Projectile projectile, Block hit) {
        onHit(p, projectile);
    }

    private void onHit(ArterionPlayer p, Projectile projectile) {
        if (projectile.getLocation().getY() < 0 || projectile.getLocation().getY() > projectile.getWorld().getMaxHeight())
            return;
        LinkedList<Block> spawnLocations = new LinkedList<>();
        if (projectile.getLocation().getDirection().getY() < 0) {
            //Ceiling mode
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    Block b = calculateCeiling(projectile.getLocation().clone().add(x, 0, z).getBlock());
                    if (b != null) spawnLocations.add(b);
                }
            }
        } else {
            //Floor mode
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    Block b = calculateFloor(projectile.getLocation().clone().add(x, 0, z).getBlock());
                    if (b != null) spawnLocations.add(b);
                }
            }
        }
        //Randomize hail
        Collections.shuffle(spawnLocations);
        //Preform spawning
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                if (spawnLocations.isEmpty()) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 2 && !spawnLocations.isEmpty(); i++) {
                    Block b = spawnLocations.removeFirst();
                    spawnArrow(p, b, projectile.getFireTicks());
                }
            }
        }, 0, 1);
    }

    private Block calculateCeiling(Block block) {
        //Scan downwards until we find the first solid block
        int y = 0;
        for (; y < 15; y++) {
            block = block.getRelative(BlockFace.DOWN);
            if (block.getType().isSolid()) {
                break;
            }
        }
        //Scan upwards until we find a solid block again (covers the case when the ceiling is higher at this position)
        for (int i = 0; i < 15 + y; i++) {
            block = block.getRelative(BlockFace.UP);
            if (block.getType().isSolid()) {
                block = block.getRelative(BlockFace.DOWN);
                break;
            }
        }
        return block.getType().isSolid() ? null : block;
    }

    private Block calculateFloor(Block block) {
        //Scan upwards until we find the first non-solid block
        for (int i = 0; i < 15; i++) {
            block = block.getRelative(BlockFace.UP);
            if (!block.getType().isSolid()) {
                break;
            }
        }
        //Scan upwards until we find a solid block
        for (int i = 0; i < 15; i++) {
            block = block.getRelative(BlockFace.UP);
            if (block.getType().isSolid()) {
                return block.getRelative(BlockFace.DOWN);
            }
        }
        return block;
    }

    private void spawnArrow(ArterionPlayer p, Block b, int fireTicks) {
        if (b.getType().isSolid()) return;
        Arrow arrow = p.getBukkitPlayer().getWorld().spawnArrow(b.getLocation().clone().add(0.5, -0.5, 0.5), new Vector(0, -1, 0), 17f / 20f, 0);
        arrow = (Arrow) NBTInjector.patchEntity(arrow);

        NBTCompound ent = NBTInjector.getNbtData(arrow);
        ent.setDouble("art_dmg", 0d);
        try {
            if (arrowGetHandle == null) arrowGetHandle = arrow.getClass().getMethod("getHandle");
            Object mce = arrowGetHandle.invoke(arrow);
            if (arrowFromPlayer == null) arrowFromPlayer = mce.getClass().getField("fromPlayer");
            arrowFromPlayer.set(mce, 0);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        arrow.setShooter(p.getBukkitPlayer());
        arrow.setFireTicks(fireTicks);
        Binding<Projectile>[] binding = new Binding[1];
        Arrow finalArrow = arrow;
        binding[0] = Hooks.PROJECTILE_HIT_TARGET_EVENT_HOOK.subscribe(arrow, evt -> {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    Hooks.PROJECTILE_HIT_TARGET_EVENT_HOOK.unsubscribe(binding[0]);
                }
            }, 1);
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    if (!finalArrow.isDead()) finalArrow.remove();
                }
            }, 6 * 20);
            return evt;
        });
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{
                ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARROW_HAIL_EXPIRE.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARROW_HAIL_DAMAGE.evaluateDouble(p)
        };
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_ARCANE_SHOT_EXPIRE.evaluateInt(p);
    }
}
