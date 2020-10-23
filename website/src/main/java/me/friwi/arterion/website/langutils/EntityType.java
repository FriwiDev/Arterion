package me.friwi.arterion.website.langutils;

public enum EntityType {
    CREEPER("Creeper", 50),
    SKELETON("Skeleton", 51),
    SPIDER("Spider", 52),
    GIANT("Giant", 53),
    ZOMBIE("Zombie", 54),
    SLIME("Slime", 55),
    GHAST("Ghast", 56),
    PIG_ZOMBIE("PigZombie", 57),
    ENDERMAN("Enderman", 58),
    CAVE_SPIDER("CaveSpider", 59),
    SILVERFISH("Silverfish", 60),
    BLAZE("Blaze", 61),
    MAGMA_CUBE("LavaSlime", 62),
    ENDER_DRAGON("EnderDragon", 63),
    WITHER("WitherBoss", 64),
    BAT("Bat", 65),
    WITCH("Witch", 66),
    ENDERMITE("Endermite", 67),
    GUARDIAN("Guardian", 68),
    PIG("Pig", 90),
    SHEEP("Sheep", 91),
    COW("Cow", 92),
    CHICKEN("Chicken", 93),
    SQUID("Squid", 94),
    WOLF("Wolf", 95),
    MUSHROOM_COW("MushroomCow", 96),
    SNOWMAN("SnowMan", 97),
    OCELOT("Ozelot", 98),
    IRON_GOLEM("VillagerGolem", 99),
    HORSE("EntityHorse", 100),
    RABBIT("Rabbit", 101),
    VILLAGER("Villager", 120);

    private String name;
    private short typeId;
    private boolean independent;

    private EntityType(String name, int typeId) {
        this(name, typeId, true);
    }

    private EntityType(String name, int typeId, boolean independent) {
        this.name = name;
        this.typeId = (short) typeId;
        this.independent = independent;
    }

    public short getTypeId() {
        return typeId;
    }
}