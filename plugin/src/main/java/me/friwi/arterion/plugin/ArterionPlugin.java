package me.friwi.arterion.plugin;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;
import com.meowj.langutils.lang.convert.EnumLang;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.chat.AnnouncementMessages;
import me.friwi.arterion.plugin.chat.ChatSystem;
import me.friwi.arterion.plugin.combat.afk.AfkTimer;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import me.friwi.arterion.plugin.combat.gamemode.TemporaryWorldManager;
import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.combat.gamemode.capturepoint.CapturePoints;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothManager;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFightManager;
import me.friwi.arterion.plugin.combat.group.GroupSystem;
import me.friwi.arterion.plugin.combat.logging.CombatLoggingHandler;
import me.friwi.arterion.plugin.combat.pvpchest.PvPChestManager;
import me.friwi.arterion.plugin.combat.skill.SkillEnum;
import me.friwi.arterion.plugin.combat.skill.UserTickScheduler;
import me.friwi.arterion.plugin.formula.ArterionFormulaManager;
import me.friwi.arterion.plugin.guild.GuildManager;
import me.friwi.arterion.plugin.guild.fight.GuildFightManager;
import me.friwi.arterion.plugin.listener.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.SanctionManager;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.replay.uploader.ReplayUploader;
import me.friwi.arterion.plugin.shop.ProductUnlocker;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.ui.command.BoosterCommand;
import me.friwi.arterion.plugin.ui.command.CommandManager;
import me.friwi.arterion.plugin.ui.hotbar.HotbarMessageUI;
import me.friwi.arterion.plugin.ui.invite.InvitationSystem;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.ui.tablist.TablistManager;
import me.friwi.arterion.plugin.ui.toplist.TopListRefresher;
import me.friwi.arterion.plugin.util.config.api.ConfigAPI;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.HibernateUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.patch.SpigotPatcher;
import me.friwi.arterion.plugin.util.recording.RecordingManager;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.util.scheduler.Schedulers;
import me.friwi.arterion.plugin.world.ExplosionHandler;
import me.friwi.arterion.plugin.world.block.nonbtblocks.SpecialBlockManager;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import me.friwi.arterion.plugin.world.markedblock.userplacedblocks.UserPlacedBlockManager;
import me.friwi.arterion.plugin.world.region.RegionManager;
import me.friwi.arterion.plugin.world.temporaryblock.TemporaryBlockManager;
import me.friwi.recordable.PlayerMobCounterInjector;
import net.badlion.timers.BadlionTimers;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArterionPlugin extends JavaPlugin {
    public static final ZoneId TIME_ZONE = ZoneId.of("CET");
    public static final ZoneId SERVER_TIME_ZONE = ZoneId.of("Z");
    public static final File REPLAY_DIR = new File("replays");
    public static final String REFLECTION_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static final CopyOnWriteArrayList<Player> ONLINE_PLAYERS = new CopyOnWriteArrayList<>();

    private static ArterionPlugin instance;

    static {
        //Transform spigot
        try {
            SpigotPatcher.inject();
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(0);
        }
        NBTInjector.inject();
        try {
            Field f = NBTInjector.class.getDeclaredField("logger");
            f.setAccessible(true);
            Logger logger = (Logger) f.get(null);
            logger.setLevel(Level.SEVERE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public long shutdownTime = Long.MAX_VALUE;
    public String shutdownReason = "";

    private TimingManager timingManager;
    private ArterionPluginConfig config;
    private Database database = new Database();
    private Schedulers schedulers = new Schedulers();
    private CommandManager commandManager;
    private RegionManager regionManager;
    private TablistManager tablistManager;
    private ChatSystem chatSystem;
    private SanctionManager sanctionManager;
    private HotbarMessageUI hotbarMessageUI;
    private ExplosionHandler explosionHandler;
    private ArterionFormulaManager formulaManager;
    private SpecialBlockManager specialBlockManager;
    private InvitationSystem invitationSystem;
    private GroupSystem groupSystem;
    private GuildManager guildManager;
    private GuildFightManager guildFightManager;
    private DamageManager damageManager;
    private CombatLoggingHandler combatLoggingHandler;
    private UserTickScheduler userTickScheduler;
    private RecordingManager recordingManager;
    private TemporaryBlockManager temporaryBlockManager;
    private UserPlacedBlockManager userPlacedBlockManager;
    private TemporaryWorldManager temporaryWorldManager;
    private ExternalFightManager externalFightManager;
    private PvPChestManager pvpChestManager;

    public ArterionPlugin() {
        instance = this;
        commandManager = new CommandManager(this);
        regionManager = new RegionManager(this);
        tablistManager = new TablistManager();
        chatSystem = new ChatSystem(this);
        sanctionManager = new SanctionManager(this);
        hotbarMessageUI = new HotbarMessageUI(this);
        config = new ArterionPluginConfig();
        explosionHandler = new ExplosionHandler(this);
        specialBlockManager = new SpecialBlockManager(this);
        invitationSystem = new InvitationSystem(this);
        groupSystem = new GroupSystem(this);
        guildManager = new GuildManager(this);
        guildFightManager = new GuildFightManager(this);
        damageManager = new DamageManager();
        combatLoggingHandler = new CombatLoggingHandler(this);
        userTickScheduler = new UserTickScheduler(this);
        recordingManager = new RecordingManager();
        temporaryBlockManager = new TemporaryBlockManager();
        userPlacedBlockManager = new UserPlacedBlockManager(this);
        temporaryWorldManager = new TemporaryWorldManager();
        externalFightManager = new ExternalFightManager();
        pvpChestManager = new PvPChestManager();
    }

    public static ArterionPlugin getInstance() {
        return instance;
    }

    public static List<Player> getOnlinePlayers() {
        return ONLINE_PLAYERS;
    }

    @Override
    public void onEnable() {
        try {
            //Init timings
            timingManager = TimingManager.of(this);

            //Register all internal converters
            ConfigAPI.init();
            ConfigAPI.readConfig(config);

            //Fire up language system
            EnumLang.init();
            LanguageAPI.reloadAllLanguages();

            //Register all commands
            commandManager.registerAll();

            //Start schedulers
            schedulers.start();

            //Connect to database
            boolean dbSuccess = false;
            try {
                dbSuccess = HibernateUtil.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!dbSuccess) {
                getLogger().info("Database connection failed! Shutting down for data safety!");
                Bukkit.getServer().shutdown();
                return;
            }
            //Initialize formulas
            formulaManager = new ArterionFormulaManager();

            //Initialize regions
            regionManager.init();

            //Initialize special blocks and guild/private regions
            specialBlockManager.init();
            guildManager.init();

            //Initialize hotbar ui
            hotbarMessageUI.init();

            //Register listeners
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ChunkLoadListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ChunkUnloadListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
            Bukkit.getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerBucketEmptyListener(this), this);
            Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
            Bukkit.getPluginManager().registerEvents(new AsyncPlayerPreLoginListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ServerListPingListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerCommandPreProcessListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityExplodeListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockExplodeListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityChangeBlockListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerPickupItemListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerDropItemListener(this), this);
            Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
            Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(this), this);
            Bukkit.getPluginManager().registerEvents(new InventoryOpenListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerInteractEntityListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerArmorStandManipulateListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityShootBowListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityPortalListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerPortalListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerGameModeChangeListener(this), this);
            Bukkit.getPluginManager().registerEvents(new EntityRegainHealthListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ProjectileHitTargetListener(this), this);
            Bukkit.getPluginManager().registerEvents(new InventoryPickupItemListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerItemConsumeListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockFromToListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ProjectileLaunchListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerFishListener(this), this);
            Bukkit.getPluginManager().registerEvents(new WorldInitListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BrewListener(this), this);
            Bukkit.getPluginManager().registerEvents(new FurnaceSmeltListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PrepareItemCraftListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerBucketFillListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockPistonExtendListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockPistonRetractListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerVoteListener(), this);
            Bukkit.getPluginManager().registerEvents(new LeavesDecayListener(this), this);
            Bukkit.getPluginManager().registerEvents(new SignChangeListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockFadeListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockFormListener(this), this);
            Bukkit.getPluginManager().registerEvents(new BlockSpreadListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);

            Bukkit.getPluginManager().registerEvents(userPlacedBlockManager, this);

            //Set keepInv gamerule
            for (World w : Bukkit.getWorlds()) {
                w.setGameRuleValue("keepInventory", "true");
                w.setGameRuleValue("announceAdvancements", "false");
            }

            //Start combat logging handler
            combatLoggingHandler.startScheduler();

            //Update user interfaces, mana, ...
            userTickScheduler.startScheduler();

            //Preload all skills
            SkillEnum.values();

            //Enable Badlion timings library
            new BadlionTimers().onEnable(this);

            //Custom mob spawning code (for many players)
            PlayerMobCounterInjector.setPlayerMobCounter(world -> {
                int cnt = 0;
                /*for (Player p : getOnlinePlayers()) {
                    if (!p.getWorld().equals(world)) continue;
                    if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
                    ArterionPlayer x = ArterionPlayerUtil.get(p);
                    if (x.isVanished()) continue;
                    if (x != null) {
                        if (x.getRegion() != null && !x.getRegion().isMobSpawn()) continue;
                    }
                    cnt++;
                }*/
                for (World w : Bukkit.getWorlds()) {
                    cnt += w.getEntities().size();
                }
                if (cnt > 8500) {
                    return 0;
                } else if (cnt < 7000) {
                    return 2;
                } else {
                    return 1;
                }
                //return cnt/10+1;
            });

            //Add crafting recipes for custom items
            CustomItemUtil.registerRecipes();

            //Client mod channel
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, ModConnection.CHANNEL_NAME);
            Bukkit.getMessenger().registerIncomingPluginChannel(this, ModConnection.CHANNEL_NAME, new ModConnection((player, packet) -> {
                ArterionPlayer ap = ArterionPlayerUtil.get(player);
                if (ap != null) {
                    ap.getPlayerScoreboard().handleModPacket(packet);
                }
            }));

            //Artefact
            Artefact.init();

            //Capture point
            CapturePoints.init();

            //Morgoth
            MorgothManager.reset();

            //Start stat tracking
            getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new PlayTimeTask(), 60 * 20, 60 * 20);

            //PvP Chests
            pvpChestManager.init();

            //Announcements in chat
            AnnouncementMessages.startMessageScheduler();

            //Product unlocks
            ProductUnlocker.startProductUnlockScheduler();

            //Top Lists
            TopListRefresher.beginRefreshing();

            //Afk timer
            AfkTimer.beginProcessAfkPlayers();

            //Begin replay upload
            ReplayUploader.scheduleReplayUpload();

            //Booster expire message
            BoosterCommand.scheduleBoosterExpireMessage();

            getLogger().info("ArterionPlugin enabled.");
        } catch (Exception e) {
            getLogger().info("Exception while enabling up plugin! Shutting down for data safety!");
            e.printStackTrace();
            Bukkit.getServer().shutdown();
            return;
        }
    }

    @Override
    public void onDisable() {
        guildFightManager.onDisable();
        if (Artefact.getFight() != null) Artefact.getFight().end(null, null, true);
        if (CapturePoints.DESERT_TEMPLE.getFight() != null) CapturePoints.DESERT_TEMPLE.getFight().end(null, true);
        if (CapturePoints.GRAVE_RUIN.getFight() != null) CapturePoints.GRAVE_RUIN.getFight().end(null, true);
        externalFightManager.onShutdown();
        temporaryWorldManager.onShutdown();
        guildManager.onShutdown();
        pvpChestManager.removeAll();
        //Stop global stat trackers
        Object lock = new Object();
        getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                GlobalStats.getContext().getStatTracker().stopAllTrackers(getExternalDatabase());
                GlobalStats.getTopContext().getStatTracker().stopAllTrackers(getExternalDatabase());
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        HibernateUtil.shutdown();
        schedulers.stop();
        regionManager.reset();
        temporaryBlockManager.rollbackAll();
        explosionHandler.onServerShutdown();

        for (World w : Bukkit.getWorlds()) {
            w.save();
        }

        userPlacedBlockManager.performSave();

        getLogger().info("ArterionPlugin disabled.");
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    public Database getExternalDatabase() {
        return database;
    }

    public Schedulers getSchedulers() {
        return schedulers;
    }

    public ChatSystem getChatSystem() {
        return chatSystem;
    }

    public SanctionManager getSanctionManager() {
        return sanctionManager;
    }

    public boolean isMaintenance() {
        return getArterionConfig().maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        if (maintenance) {
            for (Player p : ArterionPlugin.getOnlinePlayers()) {
                ArterionPlayer ep = ArterionPlayerUtil.get(p);
                if (ep == null || !Permission.getRank(p).isTeam())
                    p.kickPlayer("\247cThe server is now in maintenance mode! Please come back later!");
            }
        }
        this.getArterionConfig().maintenance = maintenance;
        this.saveConfig();
    }

    public MCTiming timing(String name) {
        return timingManager.of(name);
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public HotbarMessageUI getHotbarMessageUI() {
        return hotbarMessageUI;
    }

    public ArterionPluginConfig getArterionConfig() {
        return this.config;
    }

    public void saveConfig() {
        ConfigAPI.writeConfig(this.config);
    }

    public ExplosionHandler getExplosionHandler() {
        return explosionHandler;
    }

    public ArterionFormulaManager getFormulaManager() {
        return formulaManager;
    }

    public SpecialBlockManager getSpecialBlockManager() {
        return specialBlockManager;
    }

    public InvitationSystem getInvitationSystem() {
        return invitationSystem;
    }

    public GroupSystem getGroupSystem() {
        return groupSystem;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildFightManager getGuildFightManager() {
        return guildFightManager;
    }

    public DamageManager getDamageManager() {
        return damageManager;
    }

    public CombatLoggingHandler getCombatLoggingHandler() {
        return combatLoggingHandler;
    }

    public RecordingManager getRecordingManager() {
        return recordingManager;
    }

    public TemporaryBlockManager getTemporaryBlockManager() {
        return temporaryBlockManager;
    }

    public UserPlacedBlockManager getUserPlacedBlockManager() {
        return userPlacedBlockManager;
    }

    public TemporaryWorldManager getTemporaryWorldManager() {
        return temporaryWorldManager;
    }

    public ExternalFightManager getExternalFightManager() {
        return externalFightManager;
    }

    public PvPChestManager getPvpChestManager() {
        return pvpChestManager;
    }
}
