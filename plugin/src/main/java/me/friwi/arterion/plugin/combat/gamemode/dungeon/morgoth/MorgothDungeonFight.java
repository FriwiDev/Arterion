package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room.MorgothFirstRoom;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.XPEarnHandler;
import me.friwi.arterion.plugin.replay.acl.ReplayACLEntryType;
import me.friwi.arterion.plugin.replay.acl.ReplayACLFile;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.context.StatContextType;
import me.friwi.arterion.plugin.stats.list.FightStats;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseArenaFight;
import me.friwi.arterion.plugin.util.database.entity.DatabaseMorgothDungeonFight;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import me.friwi.arterion.plugin.world.region.MorgothDungeonRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class MorgothDungeonFight extends ExternalFight<MorgothDungeonFightConfig> implements StatContext<MorgothDungeonFight> {
    public static final int STARTING_PLAYERS = 4;

    Team team;
    private Set<DatabasePlayer> playersWinning;
    private DatabaseMorgothDungeonFight persistenceHolder;

    private int state = 0;
    private long fightDeadline = 0;
    private long pregameDeadline = 0;
    private MorgothDungeonRegion region;
    private boolean morgothDefeated = false;
    private MorgothBoss boss = null;
    private MorgothDungeonPart part;
    private Runnable onFinishInit;

    private StatContextTracker<MorgothDungeonFight> statContextTracker;

    public MorgothDungeonFight(Runnable onFinishInit) {
        super(new File(ArterionPlugin.getInstance().getDataFolder(), "templates" + File.separator + "morgoth_dungeon"),
                new MorgothDungeonFightConfig(),
                "morgoth_dungeon_fights",
                r -> {
                    //Print intro and set tab list header
                    LocalDateTime localTime = LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE);
                    String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
                    r.setTablistHeaderFooter(LanguageAPI.translate("dungeon.replay.morgoth.title"), LanguageAPI.translate("dungeon.replay.morgoth.subtitle", time));
                    r.setServerName(LanguageAPI.translate("dungeon.replay.morgoth.servername", time).replace("&", "\247"));
                    r.addChat(LanguageAPI.translate("dungeon.replay.morgoth.intro1"));
                    r.addChat(LanguageAPI.translate("dungeon.replay.morgoth.intro2"));
                    r.addChat(LanguageAPI.translate("dungeon.replay.morgoth.intro3", time));
                },
                r -> {
                    r.addChat(LanguageAPI.translate("dungeon.replay.morgoth.outro1"));
                });
        this.team = this.addTeam(ChatColor.AQUA);
        this.playersWinning = new HashSet<>();
        this.onFinishInit = onFinishInit;
        this.statContextTracker = new StatContextTracker<>(this);
    }

    public void joinPlayer(ArterionPlayer player) {
        if (state != 0) return;
        player.getBukkitPlayer().teleport(getGameModeConfig().respawn);
        team.addMember(player);
        updateObjectives();
    }

    @Override
    public void onWorldLoaded() {
        this.region = new MorgothDungeonRegion(getWorld(),
                getWorldConfig().min_chunk_x, getWorldConfig().max_chunk_x,
                getWorldConfig().min_chunk_z, getWorldConfig().max_chunk_z);
        ArterionPlugin.getInstance().getRegionManager().registerRegion(region);
        getGameModeConfig().respawn.setWorld(getWorld());
        getGameModeConfig().boss_spawn.setWorld(getWorld());
        getWorld().setGameRuleValue("doMobLoot", "false");
        getWorld().setGameRuleValue("doMobSpawning", "false");
        getWorld().setGameRuleValue("mobGriefing", "false");
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int ticks = 0;

            @Override
            public void run() {
                if (pregameDeadline == 0 && team.getMembers().size() >= STARTING_PLAYERS) {
                    pregameDeadline = System.currentTimeMillis() + 60000;
                    ticks = 60 * 20;
                    updateObjectives();
                }
                if (pregameDeadline != 0 && team.getMembers().size() < STARTING_PLAYERS) {
                    pregameDeadline = 0;
                    ticks = 60 * 20;
                    sendTranslation("dungeon.morgoth.countdown_stopped");
                    updateObjectives();
                }
                if (ticks == 60 * 20 || ticks == 30 * 20 || ticks == 20 * 20 || ticks == 10 * 20) {
                    sendTranslation("dungeon.morgoth.countdown_remain", ticks / 20);
                }
                if (ticks == 5 * 20 || ticks == 4 * 20 || ticks == 3 * 20 || ticks == 2 * 20 || ticks == 1 * 20) {
                    sendTranslation("dungeon.morgoth.countdown_final", ticks / 20);
                    playSound(Sound.CLICK, 1f, 1f);
                }
                if (ticks > 0) {
                    ticks -= 20;
                } else if (pregameDeadline != 0) {
                    sendTranslation("dungeon.morgoth.game_begin");
                    cancel();
                    beginDungeon();
                }
            }
        }, 20, 20);
        this.onFinishInit.run();
    }

    public void beginDungeon() {
        state = 1;
        MorgothManager.onBeginFight();
        Set<DatabasePlayer> pp = new HashSet<>();
        for (ArterionPlayer p : team.getMembers()) {
            pp.add(p.getPersistenceHolder());
            getStatTracker().beginTracking(p, null);
        }
        persistenceHolder = new DatabaseMorgothDungeonFight(System.currentTimeMillis(),
                DatabaseArenaFight.FIGHT_UNFINISHED,
                pp,
                new HashSet<>(),
                null);
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                db.save(persistenceHolder);
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (!committed) System.err.println("Morgoth dungeon fight not committed!");
            }

            @Override
            public void onTransactionError() {
                System.err.println("Error persisting Morgoth dungeon fight!");
            }
        }.execute();
        beginReplay();
        fightDeadline = System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_MAX_FIGHT_DURATION.evaluateInt();
        setPart(new MorgothFirstRoom(this));
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int ticks = ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_MAX_FIGHT_DURATION.evaluateInt() / 50;

            @Override
            public void run() {
                if (morgothDefeated) {
                    cancel();
                    return;
                }
                ticks--;
                if (ticks % (5 * 60 * 20) == 0 || ticks == (3 * 60 * 20) || ticks == (2 * 60 * 20)) {
                    sendTranslation("dungeon.morgoth.timer.minutes", ticks / 60 / 20);
                }
                if (ticks == 60 * 20 || ticks == 30 * 20 || ticks == 20 * 20 || ticks == 10 * 20) {
                    sendTranslation("dungeon.morgoth.timer.seconds", ticks / 20);
                    playSound(Sound.CLICK, 1f, 1f);
                }
                if (ticks == 5 * 20 || ticks == 4 * 20 || ticks == 3 * 20 || ticks == 2 * 20 || ticks == 1 * 20) {
                    sendTranslation("dungeon.morgoth.timer.countdown", ticks / 20);
                    playSound(Sound.CLICK, 1f, 1f);
                }
                if (ticks <= 0) {
                    cancel();
                    gameEnd();
                    return;
                }
                if (getPart() != null) {
                    getPart().onTick(MorgothDungeonFight.this);
                }
                if (boss != null && ticks % 10 == 0) {
                    updateObjectives();
                }
            }
        }, 1, 1);
        updateObjectives();
    }

    @Override
    public void onFailWorldLoad() {
        //Report fail
        MorgothManager.setMorgothBlocked(true);
    }

    @Override
    public void onWorldUnload(boolean instant) {
        ArterionPlugin.getInstance().getRegionManager().all().remove(region);
        for (Team team : getTeams()) {
            for (ArterionPlayer player : team.getMembers()) {
                player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    @Override
    public void onReplaySaved(File file, boolean instant) {
        try {
            ReplayACLFile acl = new ReplayACLFile(new File(ArterionPlugin.REPLAY_DIR + File.separator + "morgoth_dungeon_fights",
                    getReplayUUID().toString().replace("-", "") + File.separator + "replayacl"));
            for (DatabasePlayer p : persistenceHolder.getPlayersParticipating()) {
                acl.addEntry(ReplayACLEntryType.PLAYER, p.getUuid(), true);
            }
            acl.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object lock = new Object();
        this.updateInDB(dbf -> {
            dbf.setReplayLocation("morgoth_dungeon_fights" + File.separator +
                    getReplayUUID().toString().replace("-", ""));
            dbf.setTimeEnd(System.currentTimeMillis());
            dbf.setPlayersWinning(playersWinning);
        }, succ -> {
            if (!succ) {
                System.err.println("Error while saving morgoth dungeon fight! :(");
            }
            if (instant) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
        if (instant) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onDeath(ArterionPlayer player) {
        if (state == 2 || state == 0) return false;
        if (team.hasMember(player)) {
            team.removeMember(player);
            if (team.getMembers().size() <= 0) {
                gameEnd();
            }
        }
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircleLater(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), player);
            }
        }, 20);
        updateObjectives();
        return false;
    }

    private void gameEnd() {
        if (morgothDefeated) {
            int xp = ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_REWARD_XP.evaluateInt();
            int gold = ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_REWARD_GOLD.evaluateInt();
            double damagesum = 0;
            for (Map.Entry<ArterionPlayer, Double> damage : boss.getDamagePerPlayer().entrySet()) {
                if (team.hasMember(damage.getKey())) damagesum += damage.getValue();
            }
            for (ArterionPlayer player : team.getMembers()) {
                Double damage = boss.getDamagePerPlayer().get(player);
                if (damage != null) {
                    double part = damage / damagesum;
                    int pxp = (int) (xp * part);
                    long pgold = (long) (gold * part);
                    XPEarnHandler.earnXP(player, pxp, false);
                    player.getBagMoneyBearer().addMoney(pgold, s -> {
                    });
                    player.sendTranslation("dungeon.morgoth.gain", pxp, pgold / 100d);
                }
                playersWinning.add(player.getPersistenceHolder());
            }
        } else {
            for (Player player : getWorld().getPlayers()) {
                ArterionPlayerUtil.get(player).sendTranslation("dungeon.morgoth.fail");
            }
        }
        playSound(Sound.NOTE_PIANO, 1f, 2f);
        state = 2;
        updateObjectives();
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                endFight();
            }
        }, 200);
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircleLater(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
            }
        }, 20);
        MorgothManager.onEndFight();
    }

    @Override
    public boolean onQuit(ArterionPlayer player, boolean defineTeleport) {
        player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        if (state == 2) return false;
        onDeath(player);
        if (team.hasMember(player)) team.removeMember(player);
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        if (defineTeleport) {
            player.getBukkitPlayer().teleport(ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
            player.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
        }
        if (state == 0 && !defineTeleport) { //Only restore once
            MorgothDungeonKeyItem.restoreKey(player);
            updateObjectives();
        }
        return false;
    }

    @Override
    public Location onRespawn(ArterionPlayer player) {
        player.getBukkitPlayer().setGameMode(GameMode.SPECTATOR);
        return getGameModeConfig().respawn.clone();
    }

    private void updateObjectives() {
        if (state == 0) {
            //Pregame
            if (pregameDeadline != 0) {
                Objective obj = new Objective(new ItemStack(Material.IRON_SWORD),
                        "iron_sword",
                        pregameDeadline, "dungeon.morgoth.pregame");
                team.setObjective(obj);
            } else {
                Objective obj = new Objective(new ItemStack(Material.IRON_SWORD),
                        "iron_sword",
                        -1, "dungeon.morgoth.wait", STARTING_PLAYERS - team.getMembers().size());
                team.setObjective(obj);
            }
        } else if (state == 1) {
            //Ingame
            if (boss != null && !boss.isDead()) {
                String color = "\247c";
                double perc = boss.getHealth() / boss.getMaxHealth();
                if (perc > 0.25) color = "\2476";
                if (perc > 0.5) color = "\247a";
                team.setObjective(new Objective(new ItemStack(Material.IRON_SWORD),
                        "iron_sword",
                        fightDeadline, "dungeon.morgoth.ingame_boss", team.getMembers().size(), Math.round(boss.getHealth()), Math.round(boss.getMaxHealth()), ProgressBar.generate(color, (float) perc, 25)));
            } else {
                team.setObjective(new Objective(new ItemStack(Material.IRON_SWORD),
                        "iron_sword",
                        fightDeadline, "dungeon.morgoth.ingame", team.getMembers().size()));
            }
        } else if (state == 2) {
            //Post-game
            if (playersWinning.size() <= 0) {
                Objective obj = new Objective(new ItemStack(Material.IRON_SWORD),
                        "iron_sword",
                        -1, "dungeon.morgoth.loose");
                team.setObjective(obj);
            } else {
                Objective obj = new Objective(new ItemStack(Material.IRON_SWORD),
                        "iron_sword",
                        -1, "dungeon.morgoth.win");
                team.setObjective(obj);
            }

        }
    }

    private void updateInDB(Consumer<DatabaseMorgothDungeonFight> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabaseMorgothDungeonFight>(DatabaseMorgothDungeonFight.class, persistenceHolder.getUuid()) {
            DatabaseMorgothDungeonFight updatedHolder;

            @Override
            public void updateObject(DatabaseMorgothDungeonFight databaseMorgothDungeonFight) {
                updatedHolder = databaseMorgothDungeonFight;
                apply.accept(databaseMorgothDungeonFight);
            }

            @Override
            public void success() {
                MorgothDungeonFight.this.persistenceHolder = updatedHolder;
                successCallBack.accept(true);
            }

            @Override
            public void fail() {
                successCallBack.accept(false);
            }
        }.execute();
    }

    public void spawnBoss() {
        sendTranslation("dungeon.morgoth.appeared");
        this.boss = new MorgothBoss(getGameModeConfig().boss_spawn.clone(), () -> {
            morgothDefeated = true;
            sendTranslation("dungeon.morgoth.defeated");
            playSound(Sound.ENDERDRAGON_DEATH, 0.8f, 1f);
            gameEnd();
        });
    }

    public MorgothDungeonPart getPart() {
        return part;
    }

    public void setPart(MorgothDungeonPart part) {
        this.part = part;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public StatContextType getType() {
        return StatContextType.DUNGEON_MORGOTH_FIGHT;
    }

    @Override
    public UUID getUUID() {
        return persistenceHolder.getUuid();
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis() - persistenceHolder.getTimeBegin();
    }

    @Override
    public StatContextTracker<MorgothDungeonFight> getStatTracker() {
        return statContextTracker;
    }

    @Override
    public TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        return FightStats.getTrackedStatistics(objectType);
    }
}
