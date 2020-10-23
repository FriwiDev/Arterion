package me.friwi.arterion.website.langutils;

/*
 * Copyright (c) 2015 Jerrell Fang
 *
 * This project is Open Source and distributed under The MIT License (MIT)
 * (http://opensource.org/licenses/MIT)
 *
 * You should have received a copy of the The MIT License along with
 * this project.   If not, see <http://opensource.org/licenses/MIT>.
 */

import org.springframework.context.MessageSource;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Meow J on 7/7/2015.
 * <p>
 * A list of {@link EntityType}
 *
 * @author Meow J
 */
public enum EnumEntity {

    CREEPER(EntityType.CREEPER, "entity.Creeper.name"),
    SKELETON(EntityType.SKELETON, "entity.Skeleton.name"),
    SPIDER(EntityType.SPIDER, "entity.Spider.name"),
    //GIANT(EntityType.GIANT, "entity.Giant.name"),
    ZOMBIE(EntityType.ZOMBIE, "entity.Zombie.name"),
    SLIME(EntityType.SLIME, "entity.Slime.name"),
    GHAST(EntityType.GHAST, "entity.Ghast.name"),
    ZOMBIE_PIGMAN(EntityType.PIG_ZOMBIE, "entity.PigZombie.name"),
    ENDERMAN(EntityType.ENDERMAN, "entity.Enderman.name"),
    SILVERFISH(EntityType.SILVERFISH, "entity.Silverfish.name"),
    CAVE_SPIDER(EntityType.CAVE_SPIDER, "entity.CaveSpider.name"),
    BLAZE(EntityType.BLAZE, "entity.Blaze.name"),
    MAGMA_CUBE(EntityType.MAGMA_CUBE, "entity.LavaSlime.name"),
    MOOSHROOM(EntityType.MUSHROOM_COW, "entity.MushroomCow.name"),
    VILLAGER(EntityType.VILLAGER, "entity.Villager.name"),
    IRON_GOLEM(EntityType.IRON_GOLEM, "entity.VillagerGolem.name"),
    SNOW_GOLEM(EntityType.SNOWMAN, "entity.SnowMan.name"),
    ENDER_DRAGON(EntityType.ENDER_DRAGON, "entity.EnderDragon.name"),
    WITHER(EntityType.WITHER, "entity.WitherBoss.name"),
    WITCH(EntityType.WITCH, "entity.Witch.name"),
    PIG(EntityType.PIG, "entity.Pig.name"),
    SHEEP(EntityType.SHEEP, "entity.Sheep.name"),
    COW(EntityType.COW, "entity.Cow.name"),
    CHICKEN(EntityType.CHICKEN, "entity.Chicken.name"),
    SQUID(EntityType.SQUID, "entity.Squid.name"),
    WOLF(EntityType.WOLF, "entity.Wolf.name"),
    OCELOT(EntityType.OCELOT, "entity.Ozelot.name"),
    BAT(EntityType.BAT, "entity.Bat.name"),
    HORSE(EntityType.HORSE, "entity.EntityHorse.name"),
    RABBIT(EntityType.RABBIT, "entity.Rabbit.name");

    private static final Map<EntityType, EnumEntity> lookup = new HashMap<EntityType, EnumEntity>();

    static {
        for (EnumEntity entity : EnumSet.allOf(EnumEntity.class))
            lookup.put(entity.getType(), entity);
    }

    private EntityType type;
    private String unlocalizedName;

    EnumEntity(EntityType type, String unlocalizedName) {
        this.type = type;
        this.unlocalizedName = unlocalizedName;
    }

    /**
     * @param entityType The Entity type.
     * @return The index of an entity based on entity type
     */
    public static EnumEntity get(EntityType entityType) {
        return lookup.containsKey(entityType) ? lookup.get(entityType) : null;
    }

    public static EnumEntity byId(int id) {
        for (EnumEntity e : values()) {
            if (e.getType().getTypeId() == id) return e;
        }
        return null;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public EntityType getType() {
        return type;
    }

    public String getName(MessageSource source, Locale locale) {
        return source.getMessage(getUnlocalizedName(), new Object[0], locale);
    }
}
