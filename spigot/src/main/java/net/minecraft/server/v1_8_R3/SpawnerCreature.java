//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import me.friwi.recordable.PlayerMobCounter;
import net.minecraft.server.v1_8_R3.BiomeBase.BiomeMeta;
import net.minecraft.server.v1_8_R3.EntityInsentient.EnumEntityPositionType;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHashSet;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.github.paperspigot.exception.ServerInternalException;

public final class SpawnerCreature {
    private static final int a = (int)Math.pow(17.0D, 2.0D);
    private final LongHashSet b = new LongHashSet();

    //TODO
    private static PlayerMobCounter mobCounter = world->world.getPlayers().size();
    public static void setPlayerMobCounter(PlayerMobCounter mobCounter){
        SpawnerCreature.mobCounter = mobCounter;
    }

    public SpawnerCreature() {
    }

    private int getEntityCount(WorldServer server, Class oClass) {
        int i = 0;
        Iterator it = this.b.iterator();

        while(it.hasNext()) {
            Long coord = (Long)it.next();
            int x = LongHash.msw(coord);
            int z = LongHash.lsw(coord);
            if (!server.chunkProviderServer.unloadQueue.contains(coord) && server.isChunkLoaded(x, z, true)) {
                i += server.getChunkAt(x, z).entityCount.get(oClass);
            }
        }

        return i;
    }

    //TODO New method
    public int a(WorldServer worldserver, boolean flag, boolean flag1, boolean flag2) {
        int playerCount = mobCounter.getInterestedPlayers(worldserver.getWorld());
        if(playerCount<=0)playerCount=1;
        int f = playerCount;
        if(f<=0)f=1;
        int ret = 0;
        for(int i = 0; i<f; i++){
            ret += this.a(worldserver, flag, flag1, flag2, playerCount);
        }
        return ret;
    }

    //TODO Modify method signature: add playerCount
    public int a(WorldServer worldserver, boolean flag, boolean flag1, boolean flag2, int playerCount) {
        if (!flag && !flag1) {
            return 0;
        } else {
            this.b.clear();
            int i = 0;
            Iterator iterator = worldserver.players.iterator();

            while(true) {
                EntityHuman entityhuman;
                int j;
                int i1;
                do {
                    if (!iterator.hasNext()) {
                        int j1 = 0;
                        BlockPosition blockposition = worldserver.getSpawn();
                        EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
                        j = aenumcreaturetype.length;

                        label153:
                        for(i1 = 0; i1 < j; ++i1) {
                            EnumCreatureType enumcreaturetype = aenumcreaturetype[i1];
                            int limit = enumcreaturetype.b();
                            //TODO insert *playerCount
                            switch(enumcreaturetype.ordinal()) {
                                case 0:
                                    limit = worldserver.getWorld().getMonsterSpawnLimit()*playerCount;
                                    break;
                                case 1:
                                    limit = worldserver.getWorld().getAnimalSpawnLimit()*playerCount;
                                    break;
                                case 2:
                                    limit = worldserver.getWorld().getAmbientSpawnLimit()*playerCount;
                                    break;
                                case 3:
                                    limit = worldserver.getWorld().getWaterAnimalSpawnLimit()*playerCount;
                            }

                            if (limit != 0) {
                                int mobcnt = 0;
                                if ((!enumcreaturetype.d() || flag1) && (enumcreaturetype.d() || flag) && (!enumcreaturetype.e() || flag2)) {
                                    worldserver.a(enumcreaturetype.a());
                                    int var10000 = limit * i / a;
                                    if ((mobcnt = this.getEntityCount(worldserver, enumcreaturetype.a())) <= limit * i / 256) {
                                        Iterator iterator1 = this.b.iterator();
                                        int moblimit = limit * i / 256 - mobcnt + 1;

                                        label150:
                                        while(true) {
                                            int i2;
                                            int j2;
                                            int k2;
                                            Block block;
                                            do {
                                                if (!iterator1.hasNext() || moblimit <= 0) {
                                                    continue label153;
                                                }

                                                long key = (Long)iterator1.next();
                                                BlockPosition blockposition1 = getRandomPosition(worldserver, LongHash.msw(key), LongHash.lsw(key));
                                                i2 = blockposition1.getX();
                                                j2 = blockposition1.getY();
                                                k2 = blockposition1.getZ();
                                                block = worldserver.getType(blockposition1).getBlock();
                                            } while(block.isOccluding());

                                            int l2 = 0;

                                            for(int i3 = 0; i3 < 3; ++i3) {
                                                int j3 = i2;
                                                int k3 = j2;
                                                int l3 = k2;
                                                byte b1 = 6;
                                                BiomeMeta biomebase_biomemeta = null;
                                                GroupDataEntity groupdataentity = null;

                                                for(int i4 = 0; i4 < 4; ++i4) {
                                                    j3 += worldserver.random.nextInt(b1) - worldserver.random.nextInt(b1);
                                                    k3 += worldserver.random.nextInt(1) - worldserver.random.nextInt(1);
                                                    l3 += worldserver.random.nextInt(b1) - worldserver.random.nextInt(b1);
                                                    BlockPosition blockposition2 = new BlockPosition(j3, k3, l3);
                                                    float f = (float)j3 + 0.5F;
                                                    float f1 = (float)l3 + 0.5F;
                                                    if (!worldserver.isPlayerNearbyWhoAffectsSpawning((double)f, (double)k3, (double)f1, 24.0D) && blockposition.c((double)f, (double)k3, (double)f1) >= 576.0D) {
                                                        if (biomebase_biomemeta == null) {
                                                            biomebase_biomemeta = worldserver.a(enumcreaturetype, blockposition2);
                                                            if (biomebase_biomemeta == null) {
                                                                break;
                                                            }
                                                        }

                                                        if (worldserver.a(enumcreaturetype, biomebase_biomemeta, blockposition2) && a(EntityPositionTypes.a(biomebase_biomemeta.b), worldserver, blockposition2)) {
                                                            EntityInsentient entityinsentient;
                                                            try {
                                                                entityinsentient = (EntityInsentient)biomebase_biomemeta.b.getConstructor(World.class).newInstance(worldserver);
                                                            } catch (Exception var41) {
                                                                var41.printStackTrace();
                                                                ServerInternalException.reportInternalException(var41);
                                                                return j1;
                                                            }

                                                            entityinsentient.setPositionRotation((double)f, (double)k3, (double)f1, worldserver.random.nextFloat() * 360.0F, 0.0F);
                                                            if (entityinsentient.bR() && entityinsentient.canSpawn()) {
                                                                groupdataentity = entityinsentient.prepare(worldserver.E(new BlockPosition(entityinsentient)), groupdataentity);
                                                                if (entityinsentient.canSpawn()) {
                                                                    ++l2;
                                                                    worldserver.addEntity(entityinsentient, SpawnReason.NATURAL);
                                                                }

                                                                --moblimit;
                                                                if (moblimit <= 0 || l2 >= entityinsentient.bV()) {
                                                                    continue label150;
                                                                }
                                                            }

                                                            j1 += l2;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        return j1;
                    }

                    entityhuman = (EntityHuman)iterator.next();
                } while(entityhuman.isSpectator() && entityhuman.affectsSpawning);

                int l = MathHelper.floor(entityhuman.locX / 16.0D);
                j = MathHelper.floor(entityhuman.locZ / 16.0D);
                byte b0 = worldserver.spigotConfig.mobSpawnRange;
                b0 = b0 > worldserver.spigotConfig.viewDistance ? (byte)worldserver.spigotConfig.viewDistance : b0;
                b0 = b0 > 8 ? 8 : b0;

                for(i1 = -b0; i1 <= b0; ++i1) {
                    for(int k = -b0; k <= b0; ++k) {
                        boolean flag3 = i1 == -b0 || i1 == b0 || k == -b0 || k == b0;
                        long chunkCoords = LongHash.toLong(i1 + l, k + j);
                        if (!this.b.contains(chunkCoords)) {
                            ++i;
                            if (!flag3 && worldserver.getWorldBorder().isInBounds(i1 + l, k + j)) {
                                this.b.add(chunkCoords);
                            }
                        }
                    }
                }
            }
        }
    }

    protected static BlockPosition getRandomPosition(World world, int i, int j) {
        Chunk chunk = world.getChunkAt(i, j);
        int k = i * 16 + world.random.nextInt(16);
        int l = j * 16 + world.random.nextInt(16);
        int i1 = MathHelper.c(chunk.f(new BlockPosition(k, 0, l)) + 1, 16);
        int j1 = world.random.nextInt(i1 > 0 ? i1 : chunk.g() + 16 - 1);
        return new BlockPosition(k, j1, l);
    }

    public static boolean a(EnumEntityPositionType entityinsentient_enumentitypositiontype, World world, BlockPosition blockposition) {
        if (!world.getWorldBorder().a(blockposition)) {
            return false;
        } else {
            Block block = world.getType(blockposition).getBlock();
            if (entityinsentient_enumentitypositiontype == EnumEntityPositionType.IN_WATER) {
                return block.getMaterial().isLiquid() && world.getType(blockposition.down()).getBlock().getMaterial().isLiquid() && !world.getType(blockposition.up()).getBlock().isOccluding();
            } else {
                BlockPosition blockposition1 = blockposition.down();
                if (!World.a(world, blockposition1)) {
                    return false;
                } else {
                    Block block1 = world.getType(blockposition1).getBlock();
                    boolean flag = block1 != Blocks.BEDROCK && block1 != Blocks.BARRIER;
                    return flag && !block.isOccluding() && !block.getMaterial().isLiquid() && !world.getType(blockposition.up()).getBlock().isOccluding();
                }
            }
        }
    }

    public static void a(World world, BiomeBase biomebase, int i, int j, int k, int l, Random random) {
        List list = biomebase.getMobs(EnumCreatureType.CREATURE);
        if (!list.isEmpty()) {
            while(random.nextFloat() < biomebase.g()) {
                BiomeMeta biomebase_biomemeta = (BiomeMeta)WeightedRandom.a(world.random, list);
                int i1 = biomebase_biomemeta.c + random.nextInt(1 + biomebase_biomemeta.d - biomebase_biomemeta.c);
                GroupDataEntity groupdataentity = null;
                int j1 = i + random.nextInt(k);
                int k1 = j + random.nextInt(l);
                int l1 = j1;
                int i2 = k1;

                for(int j2 = 0; j2 < i1; ++j2) {
                    boolean flag = false;

                    for(int k2 = 0; !flag && k2 < 4; ++k2) {
                        BlockPosition blockposition = world.r(new BlockPosition(j1, 0, k1));
                        if (a(EnumEntityPositionType.ON_GROUND, world, blockposition)) {
                            EntityInsentient entityinsentient;
                            try {
                                entityinsentient = (EntityInsentient)biomebase_biomemeta.b.getConstructor(World.class).newInstance(world);
                            } catch (Exception var21) {
                                var21.printStackTrace();
                                ServerInternalException.reportInternalException(var21);
                                continue;
                            }

                            entityinsentient.setPositionRotation((double)((float)j1 + 0.5F), (double)blockposition.getY(), (double)((float)k1 + 0.5F), random.nextFloat() * 360.0F, 0.0F);
                            groupdataentity = entityinsentient.prepare(world.E(new BlockPosition(entityinsentient)), groupdataentity);
                            world.addEntity(entityinsentient, SpawnReason.CHUNK_GEN);
                            flag = true;
                        }

                        j1 += random.nextInt(5) - random.nextInt(5);

                        for(k1 += random.nextInt(5) - random.nextInt(5); j1 < i || j1 >= i + k || k1 < j || k1 >= j + k; k1 = i2 + random.nextInt(5) - random.nextInt(5)) {
                            j1 = l1 + random.nextInt(5) - random.nextInt(5);
                        }
                    }
                }
            }
        }

    }
}
