package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TargetCalculator {
    public static final double THRESHOLD = 1.8;
    public static final HashSet<Material> TRANSPARENT = new HashSet<>();

    static {
        for (Material mat : Material.values()) {
            if (!mat.isSolid()) TRANSPARENT.add(mat);
        }
    }

    public static List<Entity> getAOETargets(final LivingEntity entity, double range) {
        List<Entity> nearby = entity.getNearbyEntities(range, range, range);
        Iterator<Entity> it = nearby.iterator();
        while (it.hasNext()) {
            Entity other = it.next();
            if (!(other instanceof LivingEntity) || !entity.hasLineOfSight(other) || entity.getLocation().distance(other.getLocation()) > range)
                it.remove();
        }
        return nearby;
    }

    public static List<Entity> getAOETargetsOfEntity(final Entity entity, double range) {
        List<Entity> nearby = entity.getNearbyEntities(range, range, range);
        Iterator<Entity> it = nearby.iterator();
        while (it.hasNext()) {
            Entity other = it.next();
            if (!(other instanceof LivingEntity) || !((LivingEntity) other).hasLineOfSight(entity) || entity.getLocation().distance(other.getLocation()) > range)
                it.remove();
        }
        return nearby;
    }

    public static List<Entity> getAOETargetsWithRelation(final ArterionPlayer player, double range, PlayerRelation wantedRelation) {
        List<Entity> nearby = player.getBukkitPlayer().getNearbyEntities(range, range, range);
        Iterator<Entity> it = nearby.iterator();
        while (it.hasNext()) {
            Entity other = it.next();
            if (!(other instanceof LivingEntity) || !player.getBukkitPlayer().hasLineOfSight(other) || player.getBukkitPlayer().getLocation().distance(other.getLocation()) > range) {
                it.remove();
                continue;
            }
            PlayerRelation rel = null;
            if (other instanceof Player) {
                rel = player.getPlayerRelation(ArterionPlayerUtil.get((Player) other));
            } else {
                rel = PlayerRelation.ENEMY;
            }

            if (rel != wantedRelation) {
                it.remove();
                continue;
            }
        }
        return nearby;
    }

    public static Entity getTargetEntity(final LivingEntity entity, double range) {
        return getTarget(entity, entity.getNearbyEntities(range, range, range));
    }

    public static <T extends Entity> T getTarget(final LivingEntity entity,
                                                 final Iterable<T> entities) {
        if (entity == null)
            return null;
        T target = null;
        for (final T other : entities) {
            final Vector n = other.getLocation().toVector()
                    .subtract(entity.getLocation().toVector());
            if (other instanceof LivingEntity && entity.getLocation().getDirection().normalize().crossProduct(n)
                    .lengthSquared() < THRESHOLD
                    && n.normalize().dot(
                    entity.getLocation().getDirection().normalize()) >= 0) {
                if (entity.hasLineOfSight(other)) {
                    if (target == null
                            || target.getLocation().distanceSquared(
                            entity.getLocation()) > other.getLocation()
                            .distanceSquared(entity.getLocation())) {
                        target = other;
                    }
                }
            }
        }
        return target;
    }

    public static List<Entity> getAOETargetsFromNonLivingEntity(Entity ent, double range) {
        List<Entity> nearby = ent.getNearbyEntities(range, range, range);
        Iterator<Entity> it = nearby.iterator();
        while (it.hasNext()) {
            Entity other = it.next();
            if (!(other instanceof LivingEntity) || !((LivingEntity) other).hasLineOfSight(ent)) it.remove();
        }
        return nearby;
    }

    public static Block getTargetBlock(LivingEntity e, int range) {
        return e.getTargetBlock(TRANSPARENT, range);
    }
}
