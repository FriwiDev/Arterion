package me.friwi.arterion.mobs;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.entity.Horse;

public class MorgothHorse extends EntityHorse implements IRangedEntity {

    public MorgothHorse(World world){
        super(world);
        final MethodProfiler methodprofiler = world != null && world.methodProfiler != null ? world.methodProfiler : null;
        this.goalSelector = new PathfinderGoalSelector(methodprofiler);
        this.targetSelector = new PathfinderGoalSelector(methodprofiler);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
        this.goalSelector.a(3, new PathfinderGoalFleeSun(this, 1.0D));
        this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.2D, false));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityIronGolem.class, true));
    }

    @Override
    public void a(EntityLiving entityLiving, float v) {

    }

    public void applyCustomAttributes(){
        ((CraftHorse)this.getBukkitEntity()).setVariant(Horse.Variant.SKELETON_HORSE);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.6D);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(64D);
    }

    @Override
    public boolean bR() {
        return false; //Stop eating!
    }
}
