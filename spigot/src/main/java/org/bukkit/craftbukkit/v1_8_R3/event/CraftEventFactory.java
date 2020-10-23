//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.bukkit.craftbukkit.v1_8_R3.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.friwi.recordable.ProjectileHitTargetEvent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Statistic.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftStatistic;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftDamageSource;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent.PowerCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.BookMeta;

public class CraftEventFactory {
    public static final DamageSource MELTING;
    public static final DamageSource POISON;
    public static Block blockDamage;
    public static Entity entityDamage;
    private static final Function<? super Double, Double> ZERO;

    static {
        MELTING = CraftDamageSource.copyOf(DamageSource.BURN);
        POISON = CraftDamageSource.copyOf(DamageSource.MAGIC);
        ZERO = Functions.constant(-0.0D);
    }

    public CraftEventFactory() {
    }

    private static boolean canBuild(CraftWorld world, Player player, int x, int z) {
        WorldServer worldServer = world.getHandle();
        int spawnSize = Bukkit.getServer().getSpawnRadius();
        if (world.getHandle().dimension != 0) {
            return true;
        } else if (spawnSize <= 0) {
            return true;
        } else if (((CraftServer)Bukkit.getServer()).getHandle().getOPs().isEmpty()) {
            return true;
        } else if (player.isOp()) {
            return true;
        } else {
            BlockPosition chunkcoordinates = worldServer.getSpawn();
            int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getY()));
            return distanceFromSpawn > spawnSize;
        }
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(World world, EntityHuman who, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        CraftServer craftServer = world.getServer();
        Player player = who == null ? null : (Player)who.getBukkitEntity();
        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        boolean canBuild = true;

        for(int i = 0; i < blockStates.size(); ++i) {
            if (!canBuild(craftWorld, player, ((BlockState)blockStates.get(i)).getX(), ((BlockState)blockStates.get(i)).getZ())) {
                canBuild = false;
                break;
            }
        }

        BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, player.getItemInHand(), player, canBuild);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(World world, EntityHuman who, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        CraftServer craftServer = world.getServer();
        Player player = who == null ? null : (Player)who.getBukkitEntity();
        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        Block placedBlock = replacedBlockState.getBlock();
        boolean canBuild = canBuild(craftWorld, player, placedBlock.getX(), placedBlock.getZ());
        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, player.getItemInHand(), player, canBuild);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static SpawnerSpawnEvent callSpawnerSpawnEvent(Entity spawnee, int spawnerX, int spawnerY, int spawnerZ) {
        CraftEntity entity = spawnee.getBukkitEntity();
        BlockState state = entity.getWorld().getBlockAt(spawnerX, spawnerY, spawnerZ).getState();
        if (!(state instanceof CreatureSpawner)) {
            state = null;
        }

        SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, (CreatureSpawner)state);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(EntityHuman who, int clickedX, int clickedY, int clickedZ, EnumDirection clickedFace, ItemStack itemInHand) {
        return (PlayerBucketEmptyEvent)getPlayerBucketEvent(false, who, clickedX, clickedY, clickedZ, clickedFace, itemInHand, Items.BUCKET);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(EntityHuman who, int clickedX, int clickedY, int clickedZ, EnumDirection clickedFace, ItemStack itemInHand, Item bucket) {
        return (PlayerBucketFillEvent)getPlayerBucketEvent(true, who, clickedX, clickedY, clickedZ, clickedFace, itemInHand, bucket);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, EntityHuman who, int clickedX, int clickedY, int clickedZ, EnumDirection clickedFace, ItemStack itemstack, Item item) {
        Player player = who == null ? null : (Player)who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());
        CraftWorld craftWorld = (CraftWorld)player.getWorld();
        CraftServer craftServer = (CraftServer)player.getServer();
        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);
        PlayerEvent event = null;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketFillEvent)event).setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
        } else {
            event = new PlayerBucketEmptyEvent(player, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketEmptyEvent)event).setCancelled(!canBuild(craftWorld, player, clickedX, clickedZ));
        }

        craftServer.getPluginManager().callEvent((Event)event);
        return (PlayerEvent)event;
    }

    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, ItemStack itemstack) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            throw new IllegalArgumentException(String.format("%s performing %s with %s", who, action, itemstack));
        } else {
            return callPlayerInteractEvent(who, action, new BlockPosition(0, 256, 0), EnumDirection.SOUTH, itemstack);
        }
    }

    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, BlockPosition position, EnumDirection direction, ItemStack itemstack) {
        return callPlayerInteractEvent(who, action, position, direction, itemstack, false);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, BlockPosition position, EnumDirection direction, ItemStack itemstack, boolean cancelledBlock) {
        Player player = who == null ? null : (Player)who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        CraftWorld craftWorld = (CraftWorld)player.getWorld();
        CraftServer craftServer = (CraftServer)player.getServer();
        Block blockClicked = craftWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
        BlockFace blockFace = CraftBlock.notchToBlockFace(direction);
        if (position.getY() > 255) {
            blockClicked = null;
            switch(action.ordinal()) {
                case 0:
                    action = Action.LEFT_CLICK_AIR;
                    break;
                case 1:
                    action = Action.RIGHT_CLICK_AIR;
            }
        }

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace);
        if (cancelledBlock) {
            event.setUseInteractedBlock(Result.DENY);
        }

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityShootBowEvent callEntityShootBowEvent(EntityLiving who, ItemStack itemstack, EntityArrow entityArrow, float force) {
        LivingEntity shooter = (LivingEntity)who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        Arrow arrow = (Arrow)entityArrow.getBukkitEntity();
        if (itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)) {
            itemInHand = null;
        }

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, arrow, force);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockDamageEvent callBlockDamageEvent(EntityHuman who, int x, int y, int z, ItemStack itemstack, boolean instaBreak) {
        Player player = who == null ? null : (Player)who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        CraftWorld craftWorld = (CraftWorld)player.getWorld();
        CraftServer craftServer = (CraftServer)player.getServer();
        Block blockClicked = craftWorld.getBlockAt(x, y, z);
        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static CreatureSpawnEvent callCreatureSpawnEvent(EntityLiving entityliving, SpawnReason spawnReason) {
        LivingEntity entity = (LivingEntity)entityliving.getBukkitEntity();
        CraftServer craftServer = (CraftServer)entity.getServer();
        CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTameEvent callEntityTameEvent(EntityInsentient entity, EntityHuman tamer) {
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        AnimalTamer bukkitTamer = tamer != null ? tamer.getBukkitEntity() : null;
        CraftServer craftServer = (CraftServer)bukkitEntity.getServer();
        entity.persistent = true;
        EntityTameEvent event = new EntityTameEvent((LivingEntity)bukkitEntity, bukkitTamer);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static ItemSpawnEvent callItemSpawnEvent(EntityItem entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item)entityitem.getBukkitEntity();
        CraftServer craftServer = (CraftServer)entity.getServer();
        ItemSpawnEvent event = new ItemSpawnEvent(entity, entity.getLocation());
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static ItemDespawnEvent callItemDespawnEvent(EntityItem entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item)entityitem.getBukkitEntity();
        ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static ItemMergeEvent callItemMergeEvent(EntityItem merging, EntityItem mergingWith) {
        org.bukkit.entity.Item entityMerging = (org.bukkit.entity.Item)merging.getBukkitEntity();
        org.bukkit.entity.Item entityMergingWith = (org.bukkit.entity.Item)mergingWith.getBukkitEntity();
        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PotionSplashEvent callPotionSplashEvent(EntityPotion potion, Map<LivingEntity, Double> affectedEntities) {
        ThrownPotion thrownPotion = (ThrownPotion)potion.getBukkitEntity();
        PotionSplashEvent event = new PotionSplashEvent(thrownPotion, affectedEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockFadeEvent callBlockFadeEvent(Block block, net.minecraft.server.v1_8_R3.Block type) {
        BlockState state = block.getState();
        state.setTypeId(net.minecraft.server.v1_8_R3.Block.getId(type));
        BlockFadeEvent event = new BlockFadeEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void handleBlockSpreadEvent(Block block, Block source, net.minecraft.server.v1_8_R3.Block type, int data) {
        BlockState state = block.getState();
        state.setTypeId(net.minecraft.server.v1_8_R3.Block.getId(type));
        state.setRawData((byte)data);
        BlockSpreadEvent event = new BlockSpreadEvent(block, source, state);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            state.update(true);
        }

    }

    public static EntityDeathEvent callEntityDeathEvent(EntityLiving victim) {
        return callEntityDeathEvent(victim, new ArrayList(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(EntityLiving victim, List<org.bukkit.inventory.ItemStack> drops) {
        CraftLivingEntity entity = (CraftLivingEntity)victim.getBukkitEntity();
        EntityDeathEvent event = new EntityDeathEvent(entity, drops, victim.getExpReward());
        CraftWorld world = (CraftWorld)entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);
        victim.expToDrop = event.getDroppedExp();
        Iterator var5 = event.getDrops().iterator();

        while(var5.hasNext()) {
            org.bukkit.inventory.ItemStack stack = (org.bukkit.inventory.ItemStack)var5.next();
            if (stack != null && stack.getType() != Material.AIR && stack.getAmount() != 0) {
                world.dropItemNaturally(entity.getLocation(), stack);
            }
        }

        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(EntityPlayer victim, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory) {
        CraftPlayer entity = victim.getBukkitEntity();
        PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, victim.getExpReward(), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        org.bukkit.World world = entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);
        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();
        if (event.getKeepInventory()) {
            return event;
        } else {
            Iterator var7 = event.getDrops().iterator();

            while(var7.hasNext()) {
                org.bukkit.inventory.ItemStack stack = (org.bukkit.inventory.ItemStack)var7.next();
                if (stack != null && stack.getType() != Material.AIR) {
                    world.dropItemNaturally(entity.getLocation(), stack);
                }
            }

            return event;
        }
    }

    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent(address, motd, numPlayers, maxPlayers);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        DamageCause cause;
        if (source.isExplosion()) {
            Entity damager = entityDamage;
            entityDamage = null;
            Object event;
            if (damager == null) {
                event = new EntityDamageByBlockEvent((Block)null, entity.getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions);
            } else if (entity instanceof EntityEnderDragon && ((EntityEnderDragon)entity).target == damager) {
                event = new EntityDamageEvent(entity.getBukkitEntity(), DamageCause.ENTITY_EXPLOSION, modifiers, modifierFunctions);
            } else {
                if (damager instanceof TNTPrimed) {
                    cause = DamageCause.BLOCK_EXPLOSION;
                } else {
                    cause = DamageCause.ENTITY_EXPLOSION;
                }

                event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            }

            callEvent((Event)event);
            if (!((EntityDamageEvent)event).isCancelled()) {
                ((EntityDamageEvent)event).getEntity().setLastDamageCause((EntityDamageEvent)event);
            }

            return (EntityDamageEvent)event;
        } else if (source instanceof EntityDamageSource) {
            Entity damager = source.getEntity();
            cause = DamageCause.ENTITY_ATTACK;
            if (source instanceof EntityDamageSourceIndirect) {
                damager = ((EntityDamageSourceIndirect)source).getProximateDamageSource();
                if (damager.getBukkitEntity() instanceof ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (damager.getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if ("thorns".equals(source.translationIndex)) {
                cause = DamageCause.THORNS;
            }

            return callEntityDamageEvent(damager, entity, cause, modifiers, modifierFunctions);
        } else {
            EntityDamageEvent event;
            if (source == DamageSource.OUT_OF_WORLD) {
                event = (EntityDamageEvent)callEvent(new EntityDamageByBlockEvent((Block)null, entity.getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions));
                if (!event.isCancelled()) {
                    event.getEntity().setLastDamageCause(event);
                }

                return event;
            } else if (source == DamageSource.LAVA) {
                event = (EntityDamageEvent)callEvent(new EntityDamageByBlockEvent((Block)null, entity.getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions));
                if (!event.isCancelled()) {
                    event.getEntity().setLastDamageCause(event);
                }

                return event;
            } else {
                if (blockDamage != null) {
                    cause = null;
                    Block damager = blockDamage;
                    blockDamage = null;
                    if (source == DamageSource.CACTUS) {
                        cause = DamageCause.CONTACT;
                        event = (EntityDamageEvent)callEvent(new EntityDamageByBlockEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions));
                        if (!event.isCancelled()) {
                            event.getEntity().setLastDamageCause(event);
                        }

                        return event;
                    } else {
                        throw new RuntimeException(String.format("Unhandled damage of %s by %s from %s", entity, damager, source.translationIndex));
                    }
                } else if (entityDamage == null) {
                    cause = null;
                    if (source == DamageSource.FIRE) {
                        cause = DamageCause.FIRE;
                    } else if (source == DamageSource.STARVE) {
                        cause = DamageCause.STARVATION;
                    } else if (source == DamageSource.WITHER) {
                        cause = DamageCause.WITHER;
                    } else if (source == DamageSource.STUCK) {
                        cause = DamageCause.SUFFOCATION;
                    } else if (source == DamageSource.DROWN) {
                        cause = DamageCause.DROWNING;
                    } else if (source == DamageSource.BURN) {
                        cause = DamageCause.FIRE_TICK;
                    } else if (source == MELTING) {
                        cause = DamageCause.MELTING;
                    } else if (source == POISON) {
                        cause = DamageCause.POISON;
                    } else if (source == DamageSource.MAGIC) {
                        cause = DamageCause.MAGIC;
                    } else if (source == DamageSource.FALL) {
                        cause = DamageCause.FALL;
                    } else if (source == DamageSource.GENERIC) {
                        return new EntityDamageEvent(entity.getBukkitEntity(), (DamageCause)null, modifiers, modifierFunctions);
                    }

                    if (cause != null) {
                        return callEntityDamageEvent((Entity)null, entity, cause, modifiers, modifierFunctions);
                    } else {
                        throw new RuntimeException(String.format("Unhandled damage of %s from %s", entity, source.translationIndex));
                    }
                } else {
                    cause = null;
                    CraftEntity damager = entityDamage.getBukkitEntity();
                    entityDamage = null;
                    if (source != DamageSource.ANVIL && source != DamageSource.FALLING_BLOCK) {
                        if (damager instanceof LightningStrike) {
                            cause = DamageCause.LIGHTNING;
                        } else {
                            if (source != DamageSource.FALL) {
                                throw new RuntimeException(String.format("Unhandled damage of %s by %s from %s", entity, damager.getHandle(), source.translationIndex));
                            }

                            cause = DamageCause.FALL;
                        }
                    } else {
                        cause = DamageCause.FALLING_BLOCK;
                    }

                    event = (EntityDamageEvent)callEvent(new EntityDamageByEntityEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions));
                    if (!event.isCancelled()) {
                        event.getEntity().setLastDamageCause(event);
                    }

                    return event;
                }
            }
        }
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        Object event;
        if (damager != null) {
            event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        } else {
            event = new EntityDamageEvent(damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        }

        callEvent((Event)event);
        if (!((EntityDamageEvent)event).isCancelled()) {
            ((EntityDamageEvent)event).getEntity().setLastDamageCause((EntityDamageEvent)event);
        }

        return (EntityDamageEvent)event;
    }

    public static EntityDamageEvent handleLivingEntityDamageEvent(Entity damagee, DamageSource source, double rawDamage, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption) {
        Map<DamageModifier, Double> modifiers = new EnumMap(DamageModifier.class);
        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, rawDamage);
        modifierFunctions.put(DamageModifier.BASE, ZERO);
        if (source == DamageSource.FALLING_BLOCK || source == DamageSource.ANVIL) {
            modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
            modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
        }

        if (damagee instanceof EntityHuman) {
            modifiers.put(DamageModifier.BLOCKING, blockingModifier);
            modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        }

        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        return handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, true);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage) {
        if (entity instanceof EntityEnderCrystal && !(source instanceof EntityDamageSource)) {
            return false;
        } else {
            EnumMap<DamageModifier, Double> modifiers = new EnumMap(DamageModifier.class);
            EnumMap<DamageModifier, Function<? super Double, Double>> functions = new EnumMap(DamageModifier.class);
            modifiers.put(DamageModifier.BASE, damage);
            functions.put(DamageModifier.BASE, ZERO);
            EntityDamageEvent event = handleEntityDamageEvent(entity, source, modifiers, functions);
            if (event == null) {
                return false;
            } else {
                return event.isCancelled() || cancelOnZeroDamage && event.getDamage() == 0.0D;
            }
        }
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(EntityHuman entity, int expAmount) {
        Player player = (Player)entity.getBukkitEntity();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void handleBlockGrowEvent(World world, int x, int y, int z, net.minecraft.server.v1_8_R3.Block type, int data) {
        Block block = world.getWorld().getBlockAt(x, y, z);
        CraftBlockState state = (CraftBlockState)block.getState();
        state.setTypeId(net.minecraft.server.v1_8_R3.Block.getId(type));
        state.setRawData((byte)data);
        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            state.update(true);
        }

    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(EntityHuman entity, int level) {
        FoodLevelChangeEvent event = new FoodLevelChangeEvent(entity.getBukkitEntity(), level);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PigZapEvent callPigZapEvent(Entity pig, Entity lightning, Entity pigzombie) {
        PigZapEvent event = new PigZapEvent((Pig)pig.getBukkitEntity(), (LightningStrike)lightning.getBukkitEntity(), (PigZombie)pigzombie.getBukkitEntity());
        pig.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static HorseJumpEvent callHorseJumpEvent(Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((Horse)horse.getBukkitEntity(), power);
        horse.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material) {
        return callEntityChangeBlockEvent(entity, block, material, 0);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, Block block, Material material) {
        return callEntityChangeBlockEvent(entity.getBukkitEntity(), block, material, 0);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, Block block, Material material, boolean cancelled) {
        return callEntityChangeBlockEvent(entity.getBukkitEntity(), block, material, 0, cancelled);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, int x, int y, int z, net.minecraft.server.v1_8_R3.Block type, int data) {
        Block block = entity.world.getWorld().getBlockAt(x, y, z);
        Material material = CraftMagicNumbers.getMaterial(type);
        return callEntityChangeBlockEvent(entity.getBukkitEntity(), block, material, data);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material, int data) {
        return callEntityChangeBlockEvent(entity, block, material, data, false);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(org.bukkit.entity.Entity entity, Block block, Material material, int data, boolean cancelled) {
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity, block, material, (byte)data);
        event.setCancelled(cancelled);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper)creeper.getBukkitEntity(), (LightningStrike)lightning.getBukkitEntity(), cause);
        creeper.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetEvent callEntityTargetEvent(Entity entity, Entity target, TargetReason reason) {
        EntityTargetEvent event = new EntityTargetEvent(entity.getBukkitEntity(), target == null ? null : target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, EntityLiving target, TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(entity.getBukkitEntity(), (LivingEntity)target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, int x, int y, int z) {
        org.bukkit.entity.Entity entity1 = entity.getBukkitEntity();
        Block block = entity1.getWorld().getBlockAt(x, y, z);
        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity)entity1, block);
        entity1.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static Container callInventoryOpenEvent(EntityPlayer player, Container container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static Container callInventoryOpenEvent(EntityPlayer player, Container container, boolean cancelled) {
        if (player.activeContainer != player.defaultContainer) {
            player.playerConnection.a(new PacketPlayInCloseWindow(player.activeContainer.windowId));
        }

        CraftServer server = player.world.getServer();
        CraftPlayer craftPlayer = player.getBukkitEntity();
        player.activeContainer.transferTo(container, craftPlayer);
        InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            container.transferTo(player.activeContainer, craftPlayer);
            return null;
        } else {
            return container;
        }
    }

    public static ItemStack callPreCraftEvent(InventoryCrafting matrix, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, matrix.resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));
        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);
        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();
        return CraftItemStack.asNMSCopy(bitem);
    }

    public static ProjectileLaunchEvent callProjectileLaunchEvent(Entity entity) {
        Projectile bukkitEntity = (Projectile)entity.getBukkitEntity();
        ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ProjectileHitEvent callProjectileHitEvent(Entity entity, MovingObjectPosition pos) {
        //TODO call custom event
        ProjectileHitEvent event = new ProjectileHitTargetEvent((Projectile)entity.getBukkitEntity(),
                pos.type==MovingObjectPosition.EnumMovingObjectType.BLOCK?entity.getWorld().getWorld().getBlockAt(pos.a().getX(), pos.a().getY(), pos.a().getZ()):null,
                pos.type==MovingObjectPosition.EnumMovingObjectType.ENTITY?pos.entity.getBukkitEntity():null);
        entity.world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static ExpBottleEvent callExpBottleEvent(Entity entity, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle)entity.getBukkitEntity();
        ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(World world, int x, int y, int z, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(world.getWorld().getBlockAt(x, y, z), oldCurrent, newCurrent);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static NotePlayEvent callNotePlayEvent(World world, int x, int y, int z, byte instrument, byte note) {
        NotePlayEvent event = new NotePlayEvent(world.getWorld().getBlockAt(x, y, z), Instrument.getByType(instrument), new Note(note));
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(EntityHuman human, ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player)human.getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, int igniterX, int igniterY, int igniterZ) {
        org.bukkit.World bukkitWorld = world.getWorld();
        Block igniter = bukkitWorld.getBlockAt(igniterX, igniterY, igniterZ);
        IgniteCause cause;
        switch(igniter.getType().ordinal()) {
            case 10:
            case 11:
                cause = IgniteCause.LAVA;
                break;
            case 23:
                cause = IgniteCause.FLINT_AND_STEEL;
                break;
            case 51:
            default:
                cause = IgniteCause.SPREAD;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, igniter);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Entity igniter) {
        org.bukkit.World bukkitWorld = world.getWorld();
        org.bukkit.entity.Entity bukkitIgniter = igniter.getBukkitEntity();
        IgniteCause cause;
        switch(bukkitIgniter.getType().ordinal()) {
            case 6:
            case 7:
                cause = IgniteCause.FIREBALL;
                break;
            case 57:
                cause = IgniteCause.ENDER_CRYSTAL;
                break;
            case 61:
                cause = IgniteCause.LIGHTNING;
                break;
            default:
                cause = IgniteCause.FLINT_AND_STEEL;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, bukkitIgniter);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Explosion explosion) {
        org.bukkit.World bukkitWorld = world.getWorld();
        org.bukkit.entity.Entity igniter = explosion.source == null ? null : explosion.source.getBukkitEntity();
        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, IgniteCause cause, Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(world.getWorld().getBlockAt(x, y, z), cause, igniter.getBukkitEntity());
        world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void handleInventoryCloseEvent(EntityHuman human) {
        InventoryCloseEvent event = new InventoryCloseEvent(human.activeContainer.getBukkitView());
        human.world.getServer().getPluginManager().callEvent(event);
        human.activeContainer.transferTo(human.defaultContainer, human.getBukkitEntity());
    }

    public static void handleEditBookEvent(EntityPlayer player, ItemStack newBookItem) {
        int itemInHandIndex = player.inventory.itemInHandIndex;
        PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent(player.getBukkitEntity(), player.inventory.itemInHandIndex, (BookMeta)CraftItemStack.getItemMeta(player.inventory.getItemInHand()), (BookMeta)CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == Items.WRITTEN_BOOK);
        player.world.getServer().getPluginManager().callEvent(editBookEvent);
        ItemStack itemInHand = player.inventory.getItem(itemInHandIndex);
        if (itemInHand != null && itemInHand.getItem() == Items.WRITABLE_BOOK) {
            if (!editBookEvent.isCancelled()) {
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(Items.WRITTEN_BOOK);
                }

                CraftMetaBook meta = (CraftMetaBook)editBookEvent.getNewBookMeta();
                List<IChatBaseComponent> pages = meta.pages;

                for(int i = 0; i < pages.size(); ++i) {
                    pages.set(i, stripEvents((IChatBaseComponent)pages.get(i)));
                }

                CraftItemStack.setItemMeta(itemInHand, meta);
            }

            Slot slot = player.activeContainer.getSlot(player.inventory, itemInHandIndex);
            player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, slot.rawSlotIndex, itemInHand));
        }

    }

    private static IChatBaseComponent stripEvents(IChatBaseComponent c) {
        ChatModifier modi = c.getChatModifier();
        if (modi != null) {
            modi.setChatClickable((ChatClickable)null);
            modi.setChatHoverable((ChatHoverable)null);
        }

        c.setChatModifier(modi);
        if (c instanceof ChatMessage) {
            ChatMessage cm = (ChatMessage)c;
            Object[] oo = cm.j();

            for(int i = 0; i < oo.length; ++i) {
                Object o = oo[i];
                if (o instanceof IChatBaseComponent) {
                    oo[i] = stripEvents((IChatBaseComponent)o);
                }
            }
        }

        List<IChatBaseComponent> ls = c.a();
        if (ls != null) {
            for(int i = 0; i < ls.size(); ++i) {
                ls.set(i, stripEvents((IChatBaseComponent)ls.get(i)));
            }
        }

        return c;
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(EntityInsentient entity, EntityHuman player) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (Player)player.getBukkitEntity());
        entity.world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(EntityInsentient entity, Entity leashHolder, EntityHuman player) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(entity.getBukkitEntity(), leashHolder.getBukkitEntity(), (Player)player.getBukkitEntity());
        entity.world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static Cancellable handleStatisticsIncrease(EntityHuman entityHuman, Statistic statistic, int current, int incrementation) {
        Player player = ((EntityPlayer)entityHuman).getBukkitEntity();
        Object event;
        if (statistic instanceof Achievement) {
            if (current != 0) {
                return null;
            }

            event = new PlayerAchievementAwardedEvent(player, CraftStatistic.getBukkitAchievement((Achievement)statistic));
        } else {
            org.bukkit.Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
            if (stat == null) {
                System.err.println("Unhandled statistic: " + statistic);
                return null;
            }

            switch(stat.ordinal()) {
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 31:
                    return null;
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                default:
                    if (stat.getType() == Type.UNTYPED) {
                        event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation);
                    } else if (stat.getType() == Type.ENTITY) {
                        EntityType entityType = CraftStatistic.getEntityTypeFromStatistic(statistic);
                        event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, entityType);
                    } else {
                        Material material = CraftStatistic.getMaterialFromStatistic(statistic);
                        event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, material);
                    }
            }
        }

        entityHuman.world.getServer().getPluginManager().callEvent((Event)event);
        return (Cancellable)event;
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(EntityFireworks firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework)firework.getBukkitEntity());
        firework.world.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLocaleChangeEvent callPlayerLocaleChangeEvent(EntityHuman who, String oldLocale, String newLocale) {
        Player player = (Player)who.getBukkitEntity();
        PlayerLocaleChangeEvent event = new PlayerLocaleChangeEvent(player, oldLocale, newLocale);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
