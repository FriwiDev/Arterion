package me.friwi.arterion.mobs;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.util.function.BiConsumer;

public class MorgothSkeleton extends EntitySkeleton {

    public BiConsumer<LivingEntity, Float> onShoot;

    public MorgothSkeleton(World world){
        super(world);
    }

    @Override
    public void a(EntityLiving entityliving, float f){
        if(onShoot!=null)onShoot.accept((LivingEntity) entityliving.getBukkitEntity(), f);
    }

    public void shootArrow(LivingEntity entity, float f){
        super.a(((CraftLivingEntity)entity).getHandle(), f);
    }

}
