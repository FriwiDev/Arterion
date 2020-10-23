//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import me.friwi.recordable.CustomPlayerConnection;
import me.friwi.recordable.impl.PacketCreationListener;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_8_R3.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_8_R3.command.ConsoleCommandCompleter;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInitialSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.spigotmc.CaseInsensitiveMap;
import org.spigotmc.SpigotConfig;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public abstract class PlayerList {
    public static final File a = new File("banned-players.json");
    public static final File b = new File("banned-ips.json");
    public static final File c = new File("ops.json");
    public static final File d = new File("whitelist.json");
    private static final Logger f = LogManager.getLogger();
    private static final SimpleDateFormat g = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    public final List<EntityPlayer> players = new CopyOnWriteArrayList();
    private final Map<UUID, EntityPlayer> j = Maps.newHashMap();
    private final GameProfileBanList k;
    private final IpBanList l;
    private final OpList operators;
    private final WhiteList whitelist;
    private final Map<UUID, ServerStatisticManager> o;
    public IPlayerFileData playerFileData;
    private boolean hasWhitelist;
    protected int maxPlayers;
    private int r;
    private EnumGamemode s;
    private boolean t;
    private int u;
    private CraftServer cserver;
    private final Map<String, EntityPlayer> playersByName = new CaseInsensitiveMap();

    public PlayerList(MinecraftServer minecraftserver) {
        this.cserver = minecraftserver.server = new CraftServer(minecraftserver, this);
        minecraftserver.console = ColouredConsoleSender.getInstance();
        minecraftserver.reader.addCompleter(new ConsoleCommandCompleter(minecraftserver.server));
        this.k = new GameProfileBanList(a);
        this.l = new IpBanList(b);
        this.operators = new OpList(c);
        this.whitelist = new WhiteList(d);
        this.o = Maps.newHashMap();
        this.server = minecraftserver;
        this.k.a(false);
        this.l.a(false);
        this.maxPlayers = 8;
    }

    public void a(NetworkManager networkmanager, EntityPlayer entityplayer) {
        GameProfile gameprofile = entityplayer.getProfile();
        UserCache usercache = this.server.getUserCache();
        GameProfile gameprofile1 = usercache.a(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        usercache.a(gameprofile);
        NBTTagCompound nbttagcompound = this.a(entityplayer);
        if (nbttagcompound != null && nbttagcompound.hasKey("bukkit")) {
            NBTTagCompound bukkit = nbttagcompound.getCompound("bukkit");
            s = bukkit.hasKeyOfType("lastKnownName", 8) ? bukkit.getString("lastKnownName") : s;
        }

        Location originalLoc = new Location(entityplayer.world.getWorld(), entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        PlayerInitialSpawnEvent event = new PlayerInitialSpawnEvent(entityplayer.getBukkitEntity(), originalLoc);
        this.server.server.getPluginManager().callEvent(event);
        Location newLoc = event.getSpawnLocation();
        entityplayer.world = ((CraftWorld)newLoc.getWorld()).getHandle();
        entityplayer.locX = newLoc.getX();
        entityplayer.locY = newLoc.getY();
        entityplayer.locZ = newLoc.getZ();
        entityplayer.yaw = newLoc.getYaw();
        entityplayer.pitch = newLoc.getPitch();
        entityplayer.dimension = ((CraftWorld)newLoc.getWorld()).getHandle().dimension;
        entityplayer.spawnWorld = entityplayer.world.worldData.getName();
        entityplayer.spawnIn(this.server.getWorldServer(entityplayer.dimension));
        entityplayer.playerInteractManager.a((WorldServer)entityplayer.world);
        String s1 = "local";
        if (networkmanager.getSocketAddress() != null) {
            s1 = networkmanager.getSocketAddress().toString();
        }

        Player bukkitPlayer = entityplayer.getBukkitEntity();
        PlayerSpawnLocationEvent ev = new PlayerSpawnLocationEvent(bukkitPlayer, bukkitPlayer.getLocation());
        Bukkit.getPluginManager().callEvent(ev);
        Location loc = ev.getSpawnLocation();
        WorldServer world = ((CraftWorld)loc.getWorld()).getHandle();
        entityplayer.spawnIn(world);
        entityplayer.setPosition(loc.getX(), loc.getY(), loc.getZ());
        entityplayer.setYawPitch(loc.getYaw(), loc.getPitch());
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);
        WorldData worlddata = worldserver.getWorldData();
        BlockPosition blockposition = worldserver.getSpawn();
        this.a(entityplayer, (EntityPlayer)null, worldserver);
        //TODO
        PlayerConnection playerconnection = new CustomPlayerConnection(this.server, networkmanager, entityplayer);
        playerconnection.sendPacket(new PacketPlayOutLogin(entityplayer.getId(), entityplayer.playerInteractManager.getGameMode(), worlddata.isHardcore(), worldserver.worldProvider.getDimension(), worldserver.getDifficulty(), Math.min(this.getMaxPlayers(), 60), worlddata.getType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
        entityplayer.getBukkitEntity().sendSupportedChannels();
        playerconnection.sendPacket(new PacketPlayOutCustomPayload("MC|Brand", (new PacketDataSerializer(Unpooled.buffer())).a(this.getServer().getServerModName())));
        playerconnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerconnection.sendPacket(new PacketPlayOutSpawnPosition(blockposition));
        playerconnection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        entityplayer.getStatisticManager().d();
        entityplayer.getStatisticManager().updateStatistics(entityplayer);
        this.sendScoreboard((ScoreboardServer)worldserver.getScoreboard(), entityplayer);
        this.server.aH();
        String joinMessage;
        if (!entityplayer.getName().equalsIgnoreCase(s)) {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined.renamed", new Object[]{entityplayer.getName(), s});
        } else {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined", new Object[]{entityplayer.getName()});
        }

        this.onPlayerJoin(entityplayer, joinMessage);
        worldserver = this.server.getWorldServer(entityplayer.dimension);
        playerconnection.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        this.b(entityplayer, worldserver);
        if (this.server.getResourcePack().length() > 0) {
            entityplayer.setResourcePack(this.server.getResourcePack(), this.server.getResourcePackHash());
        }

        Iterator iterator = entityplayer.getEffects().iterator();

        while(iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect)iterator.next();
            playerconnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }

        entityplayer.syncInventory();
        if (nbttagcompound != null && nbttagcompound.hasKeyOfType("Riding", 10)) {
            Entity entity = EntityTypes.a(nbttagcompound.getCompound("Riding"), worldserver);
            if (entity != null) {
                entity.attachedToPlayer = true;
                worldserver.addEntity(entity);
                entityplayer.mount(entity);
                entity.attachedToPlayer = false;
            }
        }

        f.info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getId() + " at ([" + entityplayer.world.worldData.getName() + "]" + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
    }

    public void sendScoreboard(ScoreboardServer scoreboardserver, EntityPlayer entityplayer) {
        HashSet hashset = Sets.newHashSet();
        Iterator iterator = scoreboardserver.getTeams().iterator();

        while(iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam)iterator.next();
            entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(scoreboardteam, 0));
        }

        for(int i = 0; i < 19; ++i) {
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjectiveForSlot(i);
            if (scoreboardobjective != null && !hashset.contains(scoreboardobjective)) {
                List list = scoreboardserver.getScoreboardScorePacketsForObjective(scoreboardobjective);
                Iterator iterator1 = list.iterator();

                while(iterator1.hasNext()) {
                    Packet packet = (Packet)iterator1.next();
                    entityplayer.playerConnection.sendPacket(packet);
                }

                hashset.add(scoreboardobjective);
            }
        }

    }

    public void setPlayerFileData(WorldServer[] aworldserver) {
        if (this.playerFileData == null) {
            this.playerFileData = aworldserver[0].getDataManager().getPlayerFileData();
            aworldserver[0].getWorldBorder().a(new IWorldBorderListener() {
                public void a(WorldBorder worldborder, double d0) {
                    PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, EnumWorldBorderAction.SET_SIZE));
                }

                public void a(WorldBorder worldborder, double d0, double d1, long i) {
                    PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, EnumWorldBorderAction.LERP_SIZE));
                }

                public void a(WorldBorder worldborder, double d0, double d1) {
                    PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, EnumWorldBorderAction.SET_CENTER));
                }

                public void a(WorldBorder worldborder, int i) {
                    PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, EnumWorldBorderAction.SET_WARNING_TIME));
                }

                public void b(WorldBorder worldborder, int i) {
                    PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, EnumWorldBorderAction.SET_WARNING_BLOCKS));
                }

                public void b(WorldBorder worldborder, double d0) {
                }

                public void c(WorldBorder worldborder, double d0) {
                }
            });
        }
    }

    public void a(EntityPlayer entityplayer, WorldServer worldserver) {
        WorldServer worldserver1 = entityplayer.u();
        if (worldserver != null) {
            worldserver.getPlayerChunkMap().removePlayer(entityplayer);
        }

        worldserver1.getPlayerChunkMap().addPlayer(entityplayer);
        worldserver1.chunkProviderServer.getChunkAt((int)entityplayer.locX >> 4, (int)entityplayer.locZ >> 4);
    }

    public int d() {
        return PlayerChunkMap.getFurthestViewableBlock(this.s());
    }

    public NBTTagCompound a(EntityPlayer entityplayer) {
        NBTTagCompound nbttagcompound = ((WorldServer)this.server.worlds.get(0)).getWorldData().i();
        NBTTagCompound nbttagcompound1;
        if (entityplayer.getName().equals(this.server.S()) && nbttagcompound != null) {
            entityplayer.f(nbttagcompound);
            nbttagcompound1 = nbttagcompound;
            f.debug("loading single player");
        } else {
            nbttagcompound1 = this.playerFileData.load(entityplayer);
        }

        return nbttagcompound1;
    }

    protected void savePlayerFile(EntityPlayer entityplayer) {
        this.playerFileData.save(entityplayer);
        ServerStatisticManager serverstatisticmanager = (ServerStatisticManager)this.o.get(entityplayer.getUniqueID());
        if (serverstatisticmanager != null) {
            serverstatisticmanager.b();
        }

    }

    public void onPlayerJoin(EntityPlayer entityplayer, String joinMessage) {
        this.players.add(entityplayer);
        this.playersByName.put(entityplayer.getName(), entityplayer);
        this.j.put(entityplayer.getUniqueID(), entityplayer);
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(this.cserver.getPlayer(entityplayer), joinMessage);
        this.cserver.getPluginManager().callEvent(playerJoinEvent);
        joinMessage = playerJoinEvent.getJoinMessage();
        int i;
        if (joinMessage != null && joinMessage.length() > 0) {
            IChatBaseComponent[] var5;
            int var6 = (var5 = CraftChatMessage.fromString(joinMessage)).length;

            for(i = 0; i < var6; ++i) {
                IChatBaseComponent line = var5[i];
                this.server.getPlayerList().sendAll(new PacketPlayOutChat(line));
            }
        }

        ChunkIOExecutor.adjustPoolSize(this.getPlayerCount());
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{entityplayer});

        for(i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer1 = (EntityPlayer)this.players.get(i);
            if (entityplayer1.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                entityplayer1.playerConnection.sendPacket(packet);
            }

            if (entityplayer.getBukkitEntity().canSee(entityplayer1.getBukkitEntity())) {
                entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{entityplayer1}));
            }
        }

        if (entityplayer.world == worldserver && !worldserver.players.contains(entityplayer)) {
            worldserver.addEntity(entityplayer);
            this.a((EntityPlayer)entityplayer, (WorldServer)null);
        }

    }

    public void d(EntityPlayer entityplayer) {
        entityplayer.u().getPlayerChunkMap().movePlayer(entityplayer);
    }

    public String disconnect(EntityPlayer entityplayer) {
        entityplayer.b(StatisticList.f);
        CraftEventFactory.handleInventoryCloseEvent(entityplayer);
        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(this.cserver.getPlayer(entityplayer), "§e" + entityplayer.getName() + " left the game.");
        this.cserver.getPluginManager().callEvent(playerQuitEvent);
        entityplayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        this.savePlayerFile(entityplayer);
        WorldServer worldserver = entityplayer.u();
        if (entityplayer.vehicle != null && !(entityplayer.vehicle instanceof EntityPlayer)) {
            worldserver.removeEntity(entityplayer.vehicle);
            f.debug("removing player mount");
        }

        worldserver.kill(entityplayer);
        worldserver.getPlayerChunkMap().removePlayer(entityplayer);
        this.players.remove(entityplayer);
        this.playersByName.remove(entityplayer.getName());
        UUID uuid = entityplayer.getUniqueID();
        EntityPlayer entityplayer1 = (EntityPlayer)this.j.get(uuid);
        if (entityplayer1 == entityplayer) {
            this.j.remove(uuid);
            this.o.remove(uuid);
        }

        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new EntityPlayer[]{entityplayer});

        for(int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer2 = (EntityPlayer)this.players.get(i);
            if (entityplayer2.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                entityplayer2.playerConnection.sendPacket(packet);
            } else {
                entityplayer2.getBukkitEntity().removeDisconnectingPlayer(entityplayer.getBukkitEntity());
            }
        }

        this.cserver.getScoreboardManager().removePlayer(entityplayer.getBukkitEntity());
        ChunkIOExecutor.adjustPoolSize(this.getPlayerCount());
        return playerQuitEvent.getQuitMessage();
    }

    public EntityPlayer attemptLogin(LoginListener loginlistener, GameProfile gameprofile, String hostname) {
        UUID uuid = EntityHuman.a(gameprofile);
        ArrayList arraylist = Lists.newArrayList();

        EntityPlayer entityplayer;
        for(int i = 0; i < this.players.size(); ++i) {
            entityplayer = (EntityPlayer)this.players.get(i);
            if (entityplayer.getUniqueID().equals(uuid)) {
                arraylist.add(entityplayer);
            }
        }

        Iterator iterator = arraylist.iterator();

        while(iterator.hasNext()) {
            entityplayer = (EntityPlayer)iterator.next();
            this.savePlayerFile(entityplayer);
            entityplayer.playerConnection.disconnect("You logged in from another location");
        }

        SocketAddress socketaddress = loginlistener.networkManager.getSocketAddress();
        EntityPlayer entity = new EntityPlayer(this.server, this.server.getWorldServer(0), gameprofile, new PlayerInteractManager(this.server.getWorldServer(0)));
        Player player = entity.getBukkitEntity();
        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((InetSocketAddress)socketaddress).getAddress(), ((InetSocketAddress)loginlistener.networkManager.getRawAddress()).getAddress());
        String s;
        if (this.getProfileBans().isBanned(gameprofile) && !((GameProfileBanEntry)this.getProfileBans().get(gameprofile)).hasExpired()) {
            GameProfileBanEntry gameprofilebanentry = (GameProfileBanEntry)this.k.get(gameprofile);
            s = "You are banned from this server!\nReason: " + gameprofilebanentry.getReason();
            if (gameprofilebanentry.getExpires() != null) {
                s = s + "\nYour ban will be removed on " + g.format(gameprofilebanentry.getExpires());
            }

            if (!gameprofilebanentry.hasExpired()) {
                event.disallow(Result.KICK_BANNED, s);
            }
        } else if (!this.isWhitelisted(gameprofile)) {
            event.disallow(Result.KICK_WHITELIST, SpigotConfig.whitelistMessage);
        } else if (this.getIPBans().isBanned(socketaddress) && !this.getIPBans().get(socketaddress).hasExpired()) {
            IpBanEntry ipbanentry = this.l.get(socketaddress);
            s = "Your IP address is banned from this server!\nReason: " + ipbanentry.getReason();
            if (ipbanentry.getExpires() != null) {
                s = s + "\nYour ban will be removed on " + g.format(ipbanentry.getExpires());
            }

            event.disallow(Result.KICK_BANNED, s);
        } else if (this.players.size() >= this.maxPlayers && !this.f(gameprofile)) {
            event.disallow(Result.KICK_FULL, SpigotConfig.serverFullMessage);
        }

        this.cserver.getPluginManager().callEvent(event);
        if (event.getResult() != Result.ALLOWED) {
            loginlistener.disconnect(event.getKickMessage());
            return null;
        } else {
            return entity;
        }
    }

    public EntityPlayer processLogin(GameProfile gameprofile, EntityPlayer player) {
        return player;
    }

    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag) {
        return this.moveToWorld(entityplayer, i, flag, (Location)null, true);
    }

    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag, Location location, boolean avoidSuffocation) {
        entityplayer.u().getTracker().untrackPlayer(entityplayer);
        entityplayer.u().getPlayerChunkMap().removePlayer(entityplayer);
        this.players.remove(entityplayer);
        this.playersByName.remove(entityplayer.getName());
        this.server.getWorldServer(entityplayer.dimension).removeEntity(entityplayer);
        BlockPosition blockposition = entityplayer.getBed();
        boolean flag1 = entityplayer.isRespawnForced();
        EntityPlayer entityplayer1 = entityplayer;
        World fromWorld = entityplayer.getBukkitEntity().getWorld();
        entityplayer.viewingCredits = false;
        entityplayer.playerConnection = entityplayer.playerConnection;
        entityplayer.copyTo(entityplayer, flag);
        entityplayer.d(entityplayer.getId());
        entityplayer.o(entityplayer);
        BlockPosition blockposition1;
        if (location == null) {
            boolean isBedSpawn = false;
            CraftWorld cworld = (CraftWorld)this.server.server.getWorld(entityplayer.spawnWorld);
            if (cworld != null && blockposition != null) {
                blockposition1 = EntityHuman.getBed(cworld.getHandle(), blockposition, flag1);
                if (blockposition1 != null) {
                    isBedSpawn = true;
                    location = new Location(cworld, (double)blockposition1.getX() + 0.5D, (double)blockposition1.getY(), (double)blockposition1.getZ() + 0.5D);
                } else {
                    entityplayer.setRespawnPosition((BlockPosition)null, true);
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(0, 0.0F));
                }
            }

            if (location == null) {
                cworld = (CraftWorld)this.server.server.getWorlds().get(0);
                blockposition = cworld.getHandle().getSpawn();
                location = new Location(cworld, (double)blockposition.getX() + 0.5D, (double)blockposition.getY(), (double)blockposition.getZ() + 0.5D);
            }

            Player respawnPlayer = this.cserver.getPlayer(entityplayer);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
            this.cserver.getPluginManager().callEvent(respawnEvent);
            if (entityplayer.playerConnection.isDisconnected()) {
                return entityplayer;
            }

            location = respawnEvent.getRespawnLocation();
            entityplayer.reset();
        } else {
            location.setWorld(this.server.getWorldServer(i).getWorld());
        }

        WorldServer worldserver = ((CraftWorld)location.getWorld()).getHandle();
        entityplayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        worldserver.chunkProviderServer.getChunkAt((int)entityplayer.locX >> 4, (int)entityplayer.locZ >> 4);

        while(avoidSuffocation && !worldserver.getCubes(entityplayer1, entityplayer1.getBoundingBox()).isEmpty() && entityplayer1.locY < 256.0D) {
            entityplayer1.setPosition(entityplayer1.locX, entityplayer1.locY + 1.0D, entityplayer1.locZ);
        }

        byte actualDimension = (byte)worldserver.getWorld().getEnvironment().getId();
        if (fromWorld.getEnvironment() == worldserver.getWorld().getEnvironment()) {
            entityplayer1.playerConnection.sendPacket(new PacketPlayOutRespawn((byte)(actualDimension >= 0 ? -1 : 0), worldserver.getDifficulty(), worldserver.getWorldData().getType(), entityplayer.playerInteractManager.getGameMode()));
        }

        entityplayer1.playerConnection.sendPacket(new PacketPlayOutRespawn(actualDimension, worldserver.getDifficulty(), worldserver.getWorldData().getType(), entityplayer1.playerInteractManager.getGameMode()));
        entityplayer1.spawnIn(worldserver);
        entityplayer1.dead = false;
        entityplayer1.playerConnection.teleport(new Location(worldserver.getWorld(), entityplayer1.locX, entityplayer1.locY, entityplayer1.locZ, entityplayer1.yaw, entityplayer1.pitch));
        entityplayer1.setSneaking(false);
        blockposition1 = worldserver.getSpawn();
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(blockposition1));
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutExperience(entityplayer1.exp, entityplayer1.expTotal, entityplayer1.expLevel));
        this.b(entityplayer1, worldserver);
        if (!entityplayer.playerConnection.isDisconnected()) {
            worldserver.getPlayerChunkMap().addPlayer(entityplayer1);
            worldserver.addEntity(entityplayer1);
            this.players.add(entityplayer1);
            this.playersByName.put(entityplayer1.getName(), entityplayer1);
            this.j.put(entityplayer1.getUniqueID(), entityplayer1);
        }

        this.updateClient(entityplayer);
        entityplayer.updateAbilities();
        Iterator var20 = entityplayer.getEffects().iterator();

        while(var20.hasNext()) {
            Object o1 = var20.next();
            MobEffect mobEffect = (MobEffect)o1;
            entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobEffect));
        }

        entityplayer1.setHealth(entityplayer1.getHealth());
        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(entityplayer.getBukkitEntity(), fromWorld);
            this.server.server.getPluginManager().callEvent(event);
        }

        if (entityplayer.playerConnection.isDisconnected()) {
            this.savePlayerFile(entityplayer);
        }

        return entityplayer1;
    }

    public void changeDimension(EntityPlayer entityplayer, int i, TeleportCause cause) {
        WorldServer exitWorld = null;
        if (entityplayer.dimension < 10) {
            Iterator var5 = this.server.worlds.iterator();

            while(var5.hasNext()) {
                WorldServer world = (WorldServer)var5.next();
                if (world.dimension == i) {
                    exitWorld = world;
                }
            }
        }

        Location enter = entityplayer.getBukkitEntity().getLocation();
        Location exit = null;
        boolean useTravelAgent = false;
        if (exitWorld != null) {
            if (cause == TeleportCause.END_PORTAL && i == 0) {
                exit = entityplayer.getBukkitEntity().getBedSpawnLocation();
                if (exit == null || ((CraftWorld)exit.getWorld()).getHandle().dimension != 0) {
                    exit = exitWorld.getWorld().getSpawnLocation();
                }
            } else {
                exit = this.calculateTarget(enter, exitWorld);
                useTravelAgent = true;
            }
        }

        TravelAgent agent = exit != null ? (TravelAgent)((CraftWorld)exit.getWorld()).getHandle().getTravelAgent() : CraftTravelAgent.DEFAULT;
        agent.setCanCreatePortal(cause != TeleportCause.END_PORTAL);
        PlayerPortalEvent event = new PlayerPortalEvent(entityplayer.getBukkitEntity(), enter, exit, agent, cause);
        event.useTravelAgent(useTravelAgent);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled() && event.getTo() != null) {
            exit = cause != TeleportCause.END_PORTAL && event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo();
            if (exit != null) {
                exitWorld = ((CraftWorld)exit.getWorld()).getHandle();
                PlayerTeleportEvent tpEvent = new PlayerTeleportEvent(entityplayer.getBukkitEntity(), enter, exit, cause);
                Bukkit.getServer().getPluginManager().callEvent(tpEvent);
                if (!tpEvent.isCancelled() && tpEvent.getTo() != null) {
                    Vector velocity = entityplayer.getBukkitEntity().getVelocity();
                    boolean before = exitWorld.chunkProviderServer.forceChunkLoad;
                    exitWorld.chunkProviderServer.forceChunkLoad = true;
                    exitWorld.getTravelAgent().adjustExit(entityplayer, exit, velocity);
                    exitWorld.chunkProviderServer.forceChunkLoad = before;
                    this.moveToWorld(entityplayer, exitWorld.dimension, true, exit, false);
                    if (entityplayer.motX != velocity.getX() || entityplayer.motY != velocity.getY() || entityplayer.motZ != velocity.getZ()) {
                        entityplayer.getBukkitEntity().setVelocity(velocity);
                    }

                }
            }
        }
    }

    public void changeWorld(Entity entity, int i, WorldServer worldserver, WorldServer worldserver1) {
        Location exit = this.calculateTarget(entity.getBukkitEntity().getLocation(), worldserver1);
        this.repositionEntity(entity, exit, true);
    }

    public Location calculateTarget(Location enter, net.minecraft.server.v1_8_R3.World target) {
        WorldServer worldserver = ((CraftWorld)enter.getWorld()).getHandle();
        WorldServer worldserver1 = target.getWorld().getHandle();
        int i = worldserver.dimension;
        double y = enter.getY();
        float yaw = enter.getYaw();
        float pitch = enter.getPitch();
        double d0 = enter.getX();
        double d1 = enter.getZ();
        double d2 = 8.0D;
        if (worldserver1.dimension == -1) {
            d0 = MathHelper.a(d0 / d2, worldserver1.getWorldBorder().b() + 16.0D, worldserver1.getWorldBorder().d() - 16.0D);
            d1 = MathHelper.a(d1 / d2, worldserver1.getWorldBorder().c() + 16.0D, worldserver1.getWorldBorder().e() - 16.0D);
        } else if (worldserver1.dimension == 0) {
            d0 = MathHelper.a(d0 * d2, worldserver1.getWorldBorder().b() + 16.0D, worldserver1.getWorldBorder().d() - 16.0D);
            d1 = MathHelper.a(d1 * d2, worldserver1.getWorldBorder().c() + 16.0D, worldserver1.getWorldBorder().e() - 16.0D);
        } else {
            BlockPosition blockposition;
            if (i == 1) {
                worldserver1 = (WorldServer)this.server.worlds.get(0);
                blockposition = worldserver1.getSpawn();
            } else {
                blockposition = worldserver1.getDimensionSpawn();
            }

            d0 = (double)blockposition.getX();
            y = (double)blockposition.getY();
            d1 = (double)blockposition.getZ();
        }

        if (i != 1) {
            worldserver.methodProfiler.a("placing");
            d0 = (double)MathHelper.clamp((int)d0, -29999872, 29999872);
            d1 = (double)MathHelper.clamp((int)d1, -29999872, 29999872);
        }

        return new Location(worldserver1.getWorld(), d0, y, d1, yaw, pitch);
    }

    public void repositionEntity(Entity entity, Location exit, boolean portal) {
        WorldServer worldserver = (WorldServer)entity.world;
        WorldServer worldserver1 = ((CraftWorld)exit.getWorld()).getHandle();
        int i = worldserver.dimension;
        entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
        if (entity.isAlive()) {
            worldserver.entityJoinedWorld(entity, false);
        }

        worldserver.methodProfiler.b();
        if (i != 1) {
            worldserver.methodProfiler.a("placing");
            if (entity.isAlive()) {
                if (portal) {
                    Vector velocity = entity.getBukkitEntity().getVelocity();
                    worldserver1.getTravelAgent().adjustExit(entity, exit, velocity);
                    entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
                    if (entity.motX != velocity.getX() || entity.motY != velocity.getY() || entity.motZ != velocity.getZ()) {
                        entity.getBukkitEntity().setVelocity(velocity);
                    }
                }

                worldserver1.addEntity(entity);
                worldserver1.entityJoinedWorld(entity, false);
            }

            worldserver.methodProfiler.b();
        }

        entity.spawnIn(worldserver1);
    }

    public void tick() {
        if (++this.u > 600) {
            this.sendAll(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, this.players));
            this.u = 0;
        }

    }

    public void sendAll(Packet packet) {
        for(int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer)this.players.get(i)).playerConnection.sendPacket(packet);
        }

    }

    public void sendAll(Packet packet, EntityHuman entityhuman) {
        for(int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer)this.players.get(i);
            if (entityhuman == null || !(entityhuman instanceof EntityPlayer) || entityplayer.getBukkitEntity().canSee(((EntityPlayer)entityhuman).getBukkitEntity())) {
                ((EntityPlayer)this.players.get(i)).playerConnection.sendPacket(packet);
            }
        }

    }

    public void sendAll(Packet packet, net.minecraft.server.v1_8_R3.World world) {
        for(int i = 0; i < world.players.size(); ++i) {
            ((EntityPlayer)world.players.get(i)).playerConnection.sendPacket(packet);
        }

    }

    public void a(Packet packet, int i) {
        for(int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer = (EntityPlayer)this.players.get(j);
            if (entityplayer.dimension == i) {
                entityplayer.playerConnection.sendPacket(packet);
            }
        }

    }

    public void a(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ScoreboardTeamBase scoreboardteambase = entityhuman.getScoreboardTeam();
        if (scoreboardteambase != null) {
            Collection collection = scoreboardteambase.getPlayerNameSet();
            Iterator iterator = collection.iterator();

            while(iterator.hasNext()) {
                String s = (String)iterator.next();
                EntityPlayer entityplayer = this.getPlayer(s);
                if (entityplayer != null && entityplayer != entityhuman) {
                    entityplayer.sendMessage(ichatbasecomponent);
                }
            }
        }

    }

    public void b(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ScoreboardTeamBase scoreboardteambase = entityhuman.getScoreboardTeam();
        if (scoreboardteambase == null) {
            this.sendMessage(ichatbasecomponent);
        } else {
            for(int i = 0; i < this.players.size(); ++i) {
                EntityPlayer entityplayer = (EntityPlayer)this.players.get(i);
                if (entityplayer.getScoreboardTeam() != scoreboardteambase) {
                    entityplayer.sendMessage(ichatbasecomponent);
                }
            }
        }

    }

    public String b(boolean flag) {
        String s = "";
        ArrayList arraylist = Lists.newArrayList(this.players);

        for(int i = 0; i < arraylist.size(); ++i) {
            if (i > 0) {
                s = s + ", ";
            }

            s = s + ((EntityPlayer)arraylist.get(i)).getName();
            if (flag) {
                s = s + " (" + ((EntityPlayer)arraylist.get(i)).getUniqueID().toString() + ")";
            }
        }

        return s;
    }

    public String[] f() {
        String[] astring = new String[this.players.size()];

        for(int i = 0; i < this.players.size(); ++i) {
            astring[i] = ((EntityPlayer)this.players.get(i)).getName();
        }

        return astring;
    }

    public GameProfile[] g() {
        GameProfile[] agameprofile = new GameProfile[this.players.size()];

        for(int i = 0; i < this.players.size(); ++i) {
            agameprofile[i] = ((EntityPlayer)this.players.get(i)).getProfile();
        }

        return agameprofile;
    }

    public GameProfileBanList getProfileBans() {
        return this.k;
    }

    public IpBanList getIPBans() {
        return this.l;
    }

    public void addOp(GameProfile gameprofile) {
        this.operators.add(new OpListEntry(gameprofile, this.server.p(), this.operators.b(gameprofile)));
        Player player = this.server.server.getPlayer(gameprofile.getId());
        if (player != null) {
            player.recalculatePermissions();
        }

    }

    public void removeOp(GameProfile gameprofile) {
        this.operators.remove(gameprofile);
        Player player = this.server.server.getPlayer(gameprofile.getId());
        if (player != null) {
            player.recalculatePermissions();
        }

    }

    public boolean isWhitelisted(GameProfile gameprofile) {
        return !this.hasWhitelist || this.operators.d(gameprofile) || this.whitelist.d(gameprofile);
    }

    public boolean isOp(GameProfile gameprofile) {
        return this.operators.d(gameprofile) || this.server.T() && ((WorldServer)this.server.worlds.get(0)).getWorldData().v() && this.server.S().equalsIgnoreCase(gameprofile.getName()) || this.t;
    }

    public EntityPlayer getPlayer(String s) {
        return (EntityPlayer)this.playersByName.get(s);
    }

    public void sendPacketNearby(double d0, double d1, double d2, double d3, int i, Packet packet) {
        this.sendPacketNearby((EntityHuman)null, d0, d1, d2, d3, i, packet);
    }

    public void sendPacketNearby(EntityHuman entityhuman, double d0, double d1, double d2, double d3, int i, Packet packet) {
        for(int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer = (EntityPlayer)this.players.get(j);
            if ((entityhuman == null || !(entityhuman instanceof EntityPlayer) || entityplayer.getBukkitEntity().canSee(((EntityPlayer)entityhuman).getBukkitEntity())) && entityplayer != entityhuman && entityplayer.dimension == i) {
                double d4 = d0 - entityplayer.locX;
                double d5 = d1 - entityplayer.locY;
                double d6 = d2 - entityplayer.locZ;
                if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
                    entityplayer.playerConnection.sendPacket(packet);
                }
            }
        }

        //TODO Replay
        PacketCreationListener.interceptSendWithPosition(i, (int)d0, (int)d2, packet);
    }

    public void savePlayers() {
        for(int i = 0; i < this.players.size(); ++i) {
            this.savePlayerFile((EntityPlayer)this.players.get(i));
        }

    }

    public void addWhitelist(GameProfile gameprofile) {
        this.whitelist.add(new WhiteListEntry(gameprofile));
    }

    public void removeWhitelist(GameProfile gameprofile) {
        this.whitelist.remove(gameprofile);
    }

    public WhiteList getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelisted() {
        return this.whitelist.getEntries();
    }

    public OpList getOPs() {
        return this.operators;
    }

    public String[] n() {
        return this.operators.getEntries();
    }

    public void reloadWhitelist() {
    }

    public void b(EntityPlayer entityplayer, WorldServer worldserver) {
        WorldBorder worldborder = entityplayer.world.getWorldBorder();
        entityplayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldborder, EnumWorldBorderAction.INITIALIZE));
        entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")));
        if (worldserver.S()) {
            entityplayer.setPlayerWeather(WeatherType.DOWNFALL, false);
            entityplayer.updateWeather(-worldserver.p, worldserver.p, -worldserver.r, worldserver.r);
        }

    }

    public void updateClient(EntityPlayer entityplayer) {
        entityplayer.updateInventory(entityplayer.defaultContainer);
        entityplayer.getBukkitEntity().updateScaledHealth();
        entityplayer.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public String[] getSeenPlayers() {
        return ((WorldServer)this.server.worlds.get(0)).getDataManager().getPlayerFileData().getSeenPlayers();
    }

    public boolean getHasWhitelist() {
        return this.hasWhitelist;
    }

    public void setHasWhitelist(boolean flag) {
        this.hasWhitelist = flag;
    }

    public List<EntityPlayer> b(String s) {
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.players.iterator();

        while(iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();
            if (entityplayer.w().equals(s)) {
                arraylist.add(entityplayer);
            }
        }

        return arraylist;
    }

    public int s() {
        return this.r;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public NBTTagCompound t() {
        return null;
    }

    private void a(EntityPlayer entityplayer, EntityPlayer entityplayer1, net.minecraft.server.v1_8_R3.World world) {
        if (entityplayer1 != null) {
            entityplayer.playerInteractManager.setGameMode(entityplayer1.playerInteractManager.getGameMode());
        } else if (this.s != null) {
            entityplayer.playerInteractManager.setGameMode(this.s);
        }

        entityplayer.playerInteractManager.b(world.getWorldData().getGameType());
    }

    public void u() {
        for(int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer)this.players.get(i)).playerConnection.disconnect(this.server.server.getShutdownMessage());
        }

    }

    public void sendMessage(IChatBaseComponent[] iChatBaseComponents) {
        IChatBaseComponent[] var2 = iChatBaseComponents;
        int var3 = iChatBaseComponents.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            IChatBaseComponent component = var2[var4];
            this.sendMessage(component, true);
        }

    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.server.sendMessage(ichatbasecomponent);
        int i = flag ? 1 : 0;
        this.sendAll(new PacketPlayOutChat(CraftChatMessage.fixComponent(ichatbasecomponent), (byte)i));
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.sendMessage(ichatbasecomponent, true);
    }

    public ServerStatisticManager a(EntityHuman entityhuman) {
        UUID uuid = entityhuman.getUniqueID();
        ServerStatisticManager serverstatisticmanager = uuid == null ? null : (ServerStatisticManager)this.o.get(uuid);
        if (serverstatisticmanager == null) {
            File file = new File(this.server.getWorldServer(0).getDataManager().getDirectory(), "stats");
            File file1 = new File(file, uuid.toString() + ".json");
            if (!file1.exists()) {
                File file2 = new File(file, entityhuman.getName() + ".json");
                if (file2.exists() && file2.isFile()) {
                    file2.renameTo(file1);
                }
            }

            serverstatisticmanager = new ServerStatisticManager(this.server, file1);
            serverstatisticmanager.a();
            this.o.put(uuid, serverstatisticmanager);
        }

        return serverstatisticmanager;
    }

    public void a(int i) {
        this.r = i;
        if (this.server.worldServer != null) {
            WorldServer[] aworldserver = this.server.worldServer;
            int var10000 = aworldserver.length;

            for(int k = 0; k < this.server.worlds.size(); ++k) {
                WorldServer worldserver = (WorldServer)this.server.worlds.get(0);
                if (worldserver != null) {
                    worldserver.getPlayerChunkMap().a(i);
                }
            }
        }

    }

    public List<EntityPlayer> v() {
        return this.players;
    }

    public EntityPlayer a(UUID uuid) {
        return (EntityPlayer)this.j.get(uuid);
    }

    public boolean f(GameProfile gameprofile) {
        return false;
    }
}
