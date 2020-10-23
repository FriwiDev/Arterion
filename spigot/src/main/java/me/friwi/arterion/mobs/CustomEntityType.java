package me.friwi.arterion.mobs;

import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.entity.EntityType;

public enum CustomEntityType {
    MORGOTH_SKELETON("Morgoth-Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, MorgothSkeleton.class),
    MORGOTH_HORSE("Morgoth-Horse", 100, EntityType.HORSE, EntityHorse.class, MorgothHorse.class);

    private String name;
    private int id;
    private EntityType entityType;
    private Class<? extends EntityInsentient> nmsClass;
    private Class<? extends EntityInsentient> customClass;

    private CustomEntityType(String name, int id, EntityType entityType, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass){
        this.name = name;
        this.id = id;
        this.entityType = entityType;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
    }

    public String getName(){
        return this.name;
    }

    public int getID(){
        return this.id;
    }

    public EntityType getEntityType(){
        return this.entityType;
    }

    public Class<? extends EntityInsentient> getNMSClass(){
        return this.nmsClass;
    }

    public Class<? extends EntityInsentient> getCustomClass(){
        return this.customClass;
    }

    public static void registerEntities(){
        for (CustomEntityType entity : values()){
            EntityTypes.a(entity.getCustomClass(), entity.getName(), entity.getID());
        }
    }

}
