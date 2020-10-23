package me.friwi.arterion.mobs;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.function.BiConsumer;

public class MorgothSummoner {
    private MorgothHorse horse;
    private MorgothSkeleton skeleton;

    public MorgothSummoner(Location loc, double maxHp, BiConsumer<LivingEntity, Float> onShoot){
        horse = new MorgothHorse(((CraftWorld)loc.getWorld()).getHandle());
        horse.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(horse, CreatureSpawnEvent.SpawnReason.CUSTOM);
        skeleton = new MorgothSkeleton(((CraftWorld)loc.getWorld()).getHandle());
        skeleton.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(skeleton, CreatureSpawnEvent.SpawnReason.CUSTOM);
        getHorse().setPassenger(getSkeleton());
        getHorse().setMaxHealth(maxHp);
        getHorse().setHealth(maxHp);
        getSkeleton().setMaxHealth(maxHp);
        getSkeleton().setHealth(maxHp);
        skeleton.onShoot = onShoot;
        applyCustomAttributes();
    }

    public void shootArrow(LivingEntity entity, float f){
        skeleton.shootArrow(entity, f);
    }

    public void applyCustomAttributes(){
        horse.applyCustomAttributes();
    }

    public Horse getHorse() {
        return (Horse) horse.getBukkitEntity();
    }

    public Skeleton getSkeleton() {
        return (Skeleton) skeleton.getBukkitEntity();
    }
}
