//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Explosion {
    private final boolean a;
    private final boolean b;
    private final Random c = new Random();
    private final World world;
    private final double posX;
    private final double posY;
    private final double posZ;
    public final Entity source;
    private final float size;
    private final List<BlockPosition> blocks = Lists.newArrayList();
    private final Map<EntityHuman, Vec3D> k = Maps.newHashMap();
    public boolean wasCanceled = false;

    public Explosion(World world, Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        this.world = world;
        this.source = entity;
        this.size = (float)Math.max((double)f, 0.0D);
        this.posX = d0;
        this.posY = d1;
        this.posZ = d2;
        this.a = flag;
        this.b = flag1;
    }

    public void a() {
        if (this.size >= 0.1F) {
            HashSet hashset = Sets.newHashSet();

            int i;
            int j;
            for(int k = 0; k < 16; ++k) {
                for(i = 0; i < 16; ++i) {
                    for(j = 0; j < 16; ++j) {
                        if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                            double d0 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                            double d1 = (double)((float)i / 15.0F * 2.0F - 1.0F);
                            double d2 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 /= d3;
                            d1 /= d3;
                            d2 /= d3;
                            float f = this.size * (0.7F + this.world.random.nextFloat() * 0.6F);
                            double d4 = this.posX;
                            double d5 = this.posY;

                            for(double d6 = this.posZ; f > 0.0F; f -= 0.22500001F) {
                                BlockPosition blockposition = new BlockPosition(d4, d5, d6);
                                IBlockData iblockdata = this.world.getType(blockposition);
                                if (iblockdata.getBlock().getMaterial() != Material.AIR) {
                                    //TODO Calculate f2 manually
                                    //float f2 = this.source != null ? this.source.a(this, this.world, blockposition, iblockdata) : iblockdata.getBlock().a((Entity)null);
                                    float f2 = this.source != null ? this.source.a(this, this.world, blockposition, iblockdata) : iblockdata.getBlock().a((Entity)null);
                                    f2 = Math.min(f2, (Blocks.DIRT.a((Entity)null)+Blocks.STONE.a((Entity)null))/2f);
                                    if(iblockdata.getBlock() == Blocks.END_STONE){
                                        f2 = (Blocks.DIRT.a((Entity)null)+Blocks.STONE.a((Entity)null))/2f*1.5f;
                                    }else if(iblockdata.getBlock() == Blocks.OBSIDIAN
                                            || iblockdata.getBlock() == Blocks.IRON_DOOR
                                            || iblockdata.getBlock() == Blocks.IRON_TRAPDOOR
                                            || iblockdata.getBlock() == Blocks.BEDROCK
                                            || iblockdata.getBlock() == Blocks.END_PORTAL_FRAME
                                            || iblockdata.getBlock() == Blocks.END_PORTAL){
                                        f2 = this.source != null ? this.source.a(this, this.world, blockposition, iblockdata) : iblockdata.getBlock().a((Entity)null);
                                    }
                                    //TODO End
                                    f -= (f2 + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && (this.source == null || this.source.a(this, this.world, blockposition, iblockdata, f)) && blockposition.getY() < 256 && blockposition.getY() >= 0) {
                                    hashset.add(blockposition);
                                }

                                d4 += d0 * 0.30000001192092896D;
                                d5 += d1 * 0.30000001192092896D;
                                d6 += d2 * 0.30000001192092896D;
                            }
                        }
                    }
                }
            }

            this.blocks.addAll(hashset);
            float f3 = this.size * 2.0F;
            i = MathHelper.floor(this.posX - (double)f3 - 1.0D);
            j = MathHelper.floor(this.posX + (double)f3 + 1.0D);
            int l = MathHelper.floor(this.posY - (double)f3 - 1.0D);
            int i1 = MathHelper.floor(this.posY + (double)f3 + 1.0D);
            int j1 = MathHelper.floor(this.posZ - (double)f3 - 1.0D);
            int k1 = MathHelper.floor(this.posZ + (double)f3 + 1.0D);
            List list = this.world.a(this.source, new AxisAlignedBB((double)i, (double)l, (double)j1, (double)j, (double)i1, (double)k1), new Predicate<Entity>() {
                public boolean apply(Entity entity) {
                    return IEntitySelector.d.apply(entity) && !entity.dead;
                }
            });
            Vec3D vec3d = new Vec3D(this.posX, this.posY, this.posZ);

            for(int l1 = 0; l1 < list.size(); ++l1) {
                Entity entity = (Entity)list.get(l1);
                if (!entity.aW()) {
                    double d7 = entity.f(this.posX, this.posY, this.posZ) / (double)f3;
                    if (d7 <= 1.0D) {
                        double d8 = entity.locX - this.posX;
                        double d9 = entity.locY + (double)entity.getHeadHeight() - this.posY;
                        double d10 = entity.locZ - this.posZ;
                        double d11 = (double)MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
                        if (d11 != 0.0D) {
                            d8 /= d11;
                            d9 /= d11;
                            d10 /= d11;
                            double d12 = (double)this.getBlockDensity(vec3d, entity.getBoundingBox());
                            double d13 = (1.0D - d7) * d12;
                            CraftEventFactory.entityDamage = this.source;
                            entity.forceExplosionKnockback = false;
                            boolean wasDamaged = entity.damageEntity(DamageSource.explosion(this), (float)((int)((d13 * d13 + d13) / 2.0D * 8.0D * (double)f3 + 1.0D)));
                            CraftEventFactory.entityDamage = null;
                            if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock || entity.forceExplosionKnockback) {
                                double d14 = entity instanceof EntityHuman && this.world.paperSpigotConfig.disableExplosionKnockback ? 0.0D : EnchantmentProtection.a(entity, d13);
                                entity.g(d8 * d14, d9 * d14, d10 * d14);
                                if (entity instanceof EntityHuman && !((EntityHuman)entity).abilities.isInvulnerable && !this.world.paperSpigotConfig.disableExplosionKnockback) {
                                    this.k.put((EntityHuman)entity, new Vec3D(d8 * d13, d9 * d13, d10 * d13));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public void a(boolean flag) {
        float volume = this.source instanceof EntityTNTPrimed ? this.world.paperSpigotConfig.tntExplosionVolume : 4.0F;
        this.world.makeSound(this.posX, this.posY, this.posZ, "random.explode", volume, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F);
        if (this.size >= 2.0F && this.b) {
            this.world.addParticle(EnumParticle.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D, new int[0]);
        } else {
            this.world.addParticle(EnumParticle.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D, new int[0]);
        }

        Iterator iterator;
        BlockPosition blockposition;
        if (this.b) {
            org.bukkit.World bworld = this.world.getWorld();
            org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
            Location location = new Location(bworld, this.posX, this.posY, this.posZ);
            List<Block> blockList = Lists.newArrayList();

            for(int i1 = this.blocks.size() - 1; i1 >= 0; --i1) {
                BlockPosition cpos = (BlockPosition)this.blocks.get(i1);
                Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (bblock.getType() != org.bukkit.Material.AIR) {
                    blockList.add(bblock);
                }
            }

            boolean cancelled;
            List bukkitBlocks;
            float yield;
            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, 0.3F);
                this.world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 0.3F);
                this.world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            }

            this.blocks.clear();
            Iterator var11 = bukkitBlocks.iterator();

            while(var11.hasNext()) {
                Block bblock = (Block)var11.next();
                BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
                this.blocks.add(coords);
            }

            if (cancelled) {
                this.wasCanceled = true;
                return;
            }

            iterator = this.blocks.iterator();

            while(iterator.hasNext()) {
                blockposition = (BlockPosition)iterator.next();
                net.minecraft.server.v1_8_R3.Block block = this.world.getType(blockposition).getBlock();
                this.world.spigotConfig.antiXrayInstance.updateNearbyBlocks(this.world, blockposition);
                if (flag) {
                    double d0 = (double)((float)blockposition.getX() + this.world.random.nextFloat());
                    double d1 = (double)((float)blockposition.getY() + this.world.random.nextFloat());
                    double d2 = (double)((float)blockposition.getZ() + this.world.random.nextFloat());
                    double d3 = d0 - this.posX;
                    double d4 = d1 - this.posY;
                    double d5 = d2 - this.posZ;
                    double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 /= d6;
                    d4 /= d6;
                    d5 /= d6;
                    double d7 = 0.5D / (d6 / (double)this.size + 0.1D);
                    d7 *= (double)(this.world.random.nextFloat() * this.world.random.nextFloat() + 0.3F);
                    d3 *= d7;
                    d4 *= d7;
                    d5 *= d7;
                    this.world.addParticle(EnumParticle.EXPLOSION_NORMAL, (d0 + this.posX * 1.0D) / 2.0D, (d1 + this.posY * 1.0D) / 2.0D, (d2 + this.posZ * 1.0D) / 2.0D, d3, d4, d5, new int[0]);
                    this.world.addParticle(EnumParticle.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
                }

                if (block.getMaterial() != Material.AIR) {
                    if (block.a(this)) {
                        block.dropNaturally(this.world, blockposition, this.world.getType(blockposition), yield, 0);
                    }

                    this.world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
                    block.wasExploded(this.world, blockposition, this);
                }
            }
        }

        if (this.a) {
            iterator = this.blocks.iterator();

            while(iterator.hasNext()) {
                blockposition = (BlockPosition)iterator.next();
                if (this.world.getType(blockposition).getBlock().getMaterial() == Material.AIR && this.world.getType(blockposition.down()).getBlock().o() && this.c.nextInt(3) == 0 && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
                    this.world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                }
            }
        }

    }

    public Map<EntityHuman, Vec3D> b() {
        return this.k;
    }

    public EntityLiving getSource() {
        return this.source == null ? null : (this.source instanceof EntityTNTPrimed ? ((EntityTNTPrimed)this.source).getSource() : (this.source instanceof EntityLiving ? (EntityLiving)this.source : (this.source instanceof EntityFireball ? ((EntityFireball)this.source).shooter : null)));
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    public List<BlockPosition> getBlocks() {
        return this.blocks;
    }

    private float getBlockDensity(Vec3D vec3d, AxisAlignedBB aabb) {
        if (!this.world.paperSpigotConfig.optimizeExplosions) {
            return this.world.a(vec3d, aabb);
        } else {
            Explosion.CacheKey key = new Explosion.CacheKey(this, aabb);
            Float blockDensity = (Float)this.world.explosionDensityCache.get(key);
            if (blockDensity == null) {
                blockDensity = this.world.a(vec3d, aabb);
                this.world.explosionDensityCache.put(key, blockDensity);
            }

            return blockDensity;
        }
    }

    static class CacheKey {
        private final World world;
        private final double posX;
        private final double posY;
        private final double posZ;
        private final double minX;
        private final double minY;
        private final double minZ;
        private final double maxX;
        private final double maxY;
        private final double maxZ;

        public CacheKey(Explosion explosion, AxisAlignedBB aabb) {
            this.world = explosion.world;
            this.posX = explosion.posX;
            this.posY = explosion.posY;
            this.posZ = explosion.posZ;
            this.minX = aabb.a;
            this.minY = aabb.b;
            this.minZ = aabb.c;
            this.maxX = aabb.d;
            this.maxY = aabb.e;
            this.maxZ = aabb.f;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                Explosion.CacheKey cacheKey = (Explosion.CacheKey)o;
                if (Double.compare(cacheKey.posX, this.posX) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.posY, this.posY) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.posZ, this.posZ) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.minX, this.minX) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.minY, this.minY) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.minZ, this.minZ) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.maxX, this.maxX) != 0) {
                    return false;
                } else if (Double.compare(cacheKey.maxY, this.maxY) != 0) {
                    return false;
                } else {
                    return Double.compare(cacheKey.maxZ, this.maxZ) != 0 ? false : this.world.equals(cacheKey.world);
                }
            } else {
                return false;
            }
        }

        public int hashCode() {
            int result = this.world.hashCode();
            long temp = Double.doubleToLongBits(this.posX);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.posY);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.posZ);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.minX);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.minY);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.minZ);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.maxX);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.maxY);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(this.maxZ);
            result = 31 * result + (int)(temp ^ temp >>> 32);
            return result;
        }
    }
}
