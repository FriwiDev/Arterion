//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.friwi.arterion.mobs.CustomEntityType;
import net.minecraft.server.v1_8_R3.EntityMinecartAbstract.EnumMinecartType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityTypes {
    private static final Logger b = LogManager.getLogger();
    private static final Map<String, Class<? extends Entity>> c = Maps.newHashMap();
    private static final Map<Class<? extends Entity>, String> d = Maps.newHashMap();
    private static final Map<Integer, Class<? extends Entity>> e = Maps.newHashMap();
    private static final Map<Class<? extends Entity>, Integer> f = Maps.newHashMap();
    private static final Map<String, Integer> g = Maps.newHashMap();
    public static final Map<Integer, EntityTypes.MonsterEggInfo> eggInfo = Maps.newLinkedHashMap();

    public static void a(Class<? extends Entity> var0, String var1, int var2) {
        //TODO
        /*if (c.containsKey(var1)) {
            throw new IllegalArgumentException("ID is already registered: " + var1);
        } else if (e.containsKey(var2)) {
            throw new IllegalArgumentException("ID is already registered: " + var2);
        } else if (var2 == 0) {
            throw new IllegalArgumentException("Cannot register to reserved id: " + var2);
        } else if (var0 == null) {
            throw new IllegalArgumentException("Cannot register null clazz for id: " + var2);
        } else {*/
            c.put(var1, var0);
            d.put(var0, var1);
            e.put(var2, var0);
            f.put(var0, var2);
            g.put(var1, var2);
        //}
    }

    public static void a(Class<? extends Entity> var0, String var1, int var2, int var3, int var4) {
        a(var0, var1, var2);
        eggInfo.put(var2, new EntityTypes.MonsterEggInfo(var2, var3, var4));
    }

    public static Entity createEntityByName(String var0, World var1) {
        Entity var2 = null;

        try {
            Class var3 = (Class)c.get(var0);
            if (var3 != null) {
                var2 = (Entity)var3.getConstructor(World.class).newInstance(var1);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return var2;
    }

    public static Entity a(NBTTagCompound var0, World var1) {
        Entity var2 = null;
        if ("Minecart".equals(var0.getString("id"))) {
            var0.setString("id", EnumMinecartType.a(var0.getInt("Type")).b());
            var0.remove("Type");
        }

        try {
            Class var3 = (Class)c.get(var0.getString("id"));
            if (var3 != null) {
                var2 = (Entity)var3.getConstructor(World.class).newInstance(var1);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        if (var2 != null) {
            var2.f(var0);
        } else {
            b.warn("Skipping Entity with id " + var0.getString("id"));
        }

        return var2;
    }

    public static Entity a(int var0, World var1) {
        Entity var2 = null;

        try {
            Class var3 = a(var0);
            if (var3 != null) {
                var2 = (Entity)var3.getConstructor(World.class).newInstance(var1);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        if (var2 == null) {
            b.warn("Skipping Entity with id " + var0);
        }

        return var2;
    }

    public static int a(Entity var0) {
        Integer var1 = (Integer)f.get(var0.getClass());
        return var1 == null ? 0 : var1;
    }

    public static Class<? extends Entity> a(int var0) {
        return (Class)e.get(var0);
    }

    public static String b(Entity var0) {
        return (String)d.get(var0.getClass());
    }

    public static String b(int var0) {
        return (String)d.get(a(var0));
    }

    public static void a() {
    }

    public static List<String> b() {
        Set var0 = c.keySet();
        ArrayList var1 = Lists.newArrayList();
        Iterator var2 = var0.iterator();

        while(var2.hasNext()) {
            String var3 = (String)var2.next();
            Class var4 = (Class)c.get(var3);
            if ((var4.getModifiers() & 1024) != 1024) {
                var1.add(var3);
            }
        }

        var1.add("LightningBolt");
        return var1;
    }

    public static boolean a(Entity var0, String var1) {
        String var2 = b(var0);
        if (var2 == null && var0 instanceof EntityHuman) {
            var2 = "Player";
        } else if (var2 == null && var0 instanceof EntityLightning) {
            var2 = "LightningBolt";
        }

        return var1.equals(var2);
    }

    public static boolean b(String var0) {
        return "Player".equals(var0) || b().contains(var0);
    }

    static {
        //TODO
        CustomEntityType.registerEntities();

        a(EntityItem.class, "Item", 1);
        a(EntityExperienceOrb.class, "XPOrb", 2);
        a(EntityEgg.class, "ThrownEgg", 7);
        a(EntityLeash.class, "LeashKnot", 8);
        a(EntityPainting.class, "Painting", 9);
        a(EntityArrow.class, "Arrow", 10);
        a(EntitySnowball.class, "Snowball", 11);
        a(EntityLargeFireball.class, "Fireball", 12);
        a(EntitySmallFireball.class, "SmallFireball", 13);
        a(EntityEnderPearl.class, "ThrownEnderpearl", 14);
        a(EntityEnderSignal.class, "EyeOfEnderSignal", 15);
        a(EntityPotion.class, "ThrownPotion", 16);
        a(EntityThrownExpBottle.class, "ThrownExpBottle", 17);
        a(EntityItemFrame.class, "ItemFrame", 18);
        a(EntityWitherSkull.class, "WitherSkull", 19);
        a(EntityTNTPrimed.class, "PrimedTnt", 20);
        a(EntityFallingBlock.class, "FallingSand", 21);
        a(EntityFireworks.class, "FireworksRocketEntity", 22);
        a(EntityArmorStand.class, "ArmorStand", 30);
        a(EntityBoat.class, "Boat", 41);
        a(EntityMinecartRideable.class, EnumMinecartType.RIDEABLE.b(), 42);
        a(EntityMinecartChest.class, EnumMinecartType.CHEST.b(), 43);
        a(EntityMinecartFurnace.class, EnumMinecartType.FURNACE.b(), 44);
        a(EntityMinecartTNT.class, EnumMinecartType.TNT.b(), 45);
        a(EntityMinecartHopper.class, EnumMinecartType.HOPPER.b(), 46);
        a(EntityMinecartMobSpawner.class, EnumMinecartType.SPAWNER.b(), 47);
        a(EntityMinecartCommandBlock.class, EnumMinecartType.COMMAND_BLOCK.b(), 40);
        a(EntityInsentient.class, "Mob", 48);
        a(EntityMonster.class, "Monster", 49);
        a(EntityCreeper.class, "Creeper", 50, 894731, 0);
        a(EntitySkeleton.class, "Skeleton", 51, 12698049, 4802889);
        a(EntitySpider.class, "Spider", 52, 3419431, 11013646);
        a(EntityGiantZombie.class, "Giant", 53);
        a(EntityZombie.class, "Zombie", 54, 44975, 7969893);
        a(EntitySlime.class, "Slime", 55, 5349438, 8306542);
        a(EntityGhast.class, "Ghast", 56, 16382457, 12369084);
        a(EntityPigZombie.class, "PigZombie", 57, 15373203, 5009705);
        a(EntityEnderman.class, "Enderman", 58, 1447446, 0);
        a(EntityCaveSpider.class, "CaveSpider", 59, 803406, 11013646);
        a(EntitySilverfish.class, "Silverfish", 60, 7237230, 3158064);
        a(EntityBlaze.class, "Blaze", 61, 16167425, 16775294);
        a(EntityMagmaCube.class, "LavaSlime", 62, 3407872, 16579584);
        a(EntityEnderDragon.class, "EnderDragon", 63);
        a(EntityWither.class, "WitherBoss", 64);
        a(EntityBat.class, "Bat", 65, 4996656, 986895);
        a(EntityWitch.class, "Witch", 66, 3407872, 5349438);
        a(EntityEndermite.class, "Endermite", 67, 1447446, 7237230);
        a(EntityGuardian.class, "Guardian", 68, 5931634, 15826224);
        a(EntityPig.class, "Pig", 90, 15771042, 14377823);
        a(EntitySheep.class, "Sheep", 91, 15198183, 16758197);
        a(EntityCow.class, "Cow", 92, 4470310, 10592673);
        a(EntityChicken.class, "Chicken", 93, 10592673, 16711680);
        a(EntitySquid.class, "Squid", 94, 2243405, 7375001);
        a(EntityWolf.class, "Wolf", 95, 14144467, 13545366);
        a(EntityMushroomCow.class, "MushroomCow", 96, 10489616, 12040119);
        a(EntitySnowman.class, "SnowMan", 97);
        a(EntityOcelot.class, "Ozelot", 98, 15720061, 5653556);
        a(EntityIronGolem.class, "VillagerGolem", 99);
        a(EntityHorse.class, "EntityHorse", 100, 12623485, 15656192);
        a(EntityRabbit.class, "Rabbit", 101, 10051392, 7555121);
        a(EntityVillager.class, "Villager", 120, 5651507, 12422002);
        a(EntityEnderCrystal.class, "EnderCrystal", 200);
    }

    public static class MonsterEggInfo {
        public final int a;
        public final int b;
        public final int c;
        public final Statistic killEntityStatistic;
        public final Statistic e;

        public MonsterEggInfo(int var1, int var2, int var3) {
            this.a = var1;
            this.b = var2;
            this.c = var3;
            this.killEntityStatistic = StatisticList.a(this);
            this.e = StatisticList.b(this);
        }
    }
}
