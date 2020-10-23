package me.friwi.arterion.plugin.combat.gamemode.arena;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.replay.acl.ReplayACLEntryType;
import me.friwi.arterion.plugin.replay.acl.ReplayACLFile;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.context.StatContextType;
import me.friwi.arterion.plugin.stats.list.FightStats;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseArenaFight;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.ArenaRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class ArenaFight extends ExternalFight<ArenaFightConfig> implements StatContext<ArenaFight> {
    private List<ArterionPlayer> players1, players2;
    private Set<DatabasePlayer> playersTeamOne, playersTeamTwo;
    private Team team1, team2, winner;
    private int remaining1, remaining2;
    private DatabaseArenaFight persistenceHolder;

    private int state = 0;
    private long fightDeadline = 0;
    private ArenaMaps map;
    private ArenaRegion region;
    private Consumer<Boolean> startSuccess;

    private StatContextTracker<ArenaFight> statContextTracker;

    private List<UUID> deadPlayers = new LinkedList<>();

    public ArenaFight(List<ArterionPlayer> team1, List<ArterionPlayer> team2, ArenaMaps map, Consumer<Boolean> startSuccess) {
        super(new File(ArterionPlugin.getInstance().getDataFolder(), "templates" + File.separator + "arena" + File.separator + map.getTemplateDir()),
                new ArenaFightConfig(),
                "arena_fights",
                r -> {
                    //Print intro and set tab list header
                    LocalDateTime localTime = LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE);
                    String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
                    r.setTablistHeaderFooter(LanguageAPI.translate("arena.replay.title"), LanguageAPI.translate("arena.replay.subtitle", time));
                    r.setServerName(LanguageAPI.translate("arena.replay.servername", time).replace("&", "\247"));
                    r.addChat(LanguageAPI.translate("arena.replay.intro1"));
                    r.addChat(LanguageAPI.translate("arena.replay.intro2"));
                    r.addChat(LanguageAPI.translate("arena.replay.intro3", time));
                },
                r -> {
                    r.addChat(LanguageAPI.translate("arena.replay.outro1"));
                });
        this.startSuccess = startSuccess;
        this.players1 = team1;
        this.players2 = team2;
        this.remaining1 = team1.size();
        this.remaining2 = team2.size();
        this.map = map;
        playersTeamOne = new HashSet<>();
        for (ArterionPlayer player : players1) playersTeamOne.add(player.getPersistenceHolder());
        playersTeamTwo = new HashSet<>();
        for (ArterionPlayer player : players2) playersTeamTwo.add(player.getPersistenceHolder());
        this.statContextTracker = new StatContextTracker<>(this);
    }

    @Override
    public void onWorldLoaded() {
        this.region = new ArenaRegion(map, getWorld(),
                getWorldConfig().min_chunk_x, getWorldConfig().max_chunk_x,
                getWorldConfig().min_chunk_z, getWorldConfig().max_chunk_z);
        ArterionPlugin.getInstance().getRegionManager().registerRegion(region);
        getGameModeConfig().respawn.setWorld(getWorld());
        team1 = addTeam(ChatColor.RED);
        team2 = addTeam(ChatColor.AQUA);
        for (ArterionPlayer p : players1) {
            team1.addMember(p);
            p.getBukkitPlayer().setGameMode(GameMode.ADVENTURE);
            statContextTracker.beginTracking(p, new UUID(0, 0));
        }
        for (ArterionPlayer p : players2) {
            team2.addMember(p);
            p.getBukkitPlayer().setGameMode(GameMode.ADVENTURE);
            statContextTracker.beginTracking(p, new UUID(0, 1));
        }
        beginReplay();
        persistenceHolder = new DatabaseArenaFight(System.currentTimeMillis(),
                DatabaseArenaFight.FIGHT_UNFINISHED,
                playersTeamOne,
                playersTeamTwo,
                -1, -1, -1, null);
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                db.save(persistenceHolder);
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (!committed) System.err.println("Arena fight not committed!");
            }

            @Override
            public void onTransactionError() {
                System.err.println("Error persisting arena fight!");
            }
        }.execute();
        //Build cages
        Location[] spawnLocations1 = getGameModeConfig().getTeamSpawns(1);
        Location[] spawnLocations2 = getGameModeConfig().getTeamSpawns(2);
        for (Location loc : spawnLocations1) {
            loc.setWorld(getWorld());
            buildCage(loc, Material.BARRIER);
        }
        for (Location loc : spawnLocations2) {
            loc.setWorld(getWorld());
            buildCage(loc, Material.BARRIER);
        }
        //Teleport players
        int i = 0;
        for (ArterionPlayer p : team1.getMembers()) {
            if (p.getBukkitPlayer().isOnline()) {
                p.getBukkitPlayer().teleport(spawnLocations1[i]);
                TitleAPI.send(p, map.getName(p.getLanguage()), map.getBy(p.getLanguage()));
            } else {
                onQuit(p, false);
            }
            heal(p);
            i++;
        }
        i = 0;
        for (ArterionPlayer p : team2.getMembers()) {
            if (p.getBukkitPlayer().isOnline()) {
                p.getBukkitPlayer().teleport(spawnLocations2[i]);
                TitleAPI.send(p, map.getName(p.getLanguage()), map.getBy(p.getLanguage()));
            } else {
                onQuit(p, false);
            }
            heal(p);
            i++;
        }
        //Objective
        updateObjectives();
        //Start main tick
        int interval = 20;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int ticksRemaining = (getGameModeConfig().pregame_seconds + getGameModeConfig().game_seconds) * 20;

            @Override
            public void run() {
                if (winner != null) {
                    cancel();
                    return;
                }
                ticksRemaining -= interval;
                if (ticksRemaining > getGameModeConfig().game_seconds * 20) {
                    int remaining = ticksRemaining / 20 - getGameModeConfig().game_seconds;
                    if (remaining <= 5) {
                        playSound(Sound.CLICK, 1f, 1f);
                        team1.sendTranslation("arena.pregame.countdown", remaining);
                        team2.sendTranslation("arena.pregame.countdown", remaining);
                        appendTranslationToRecordingChat("arena.replay.pregame.countdown", remaining);
                    }
                } else if (ticksRemaining == getGameModeConfig().game_seconds * 20) {
                    team1.sendTranslation("arena.game.begin");
                    team2.sendTranslation("arena.game.begin");
                    appendTranslationToRecordingChat("arena.replay.game.begin");

                    for (ArterionPlayer player : team1) {
                        player.getPlayerScoreboard().updateAllPlayerRelations();
                    }
                    for (ArterionPlayer player : team2) {
                        player.getPlayerScoreboard().updateAllPlayerRelations();
                    }

                    state = 1;
                    fightDeadline = System.currentTimeMillis() + getGameModeConfig().game_seconds * 1000;
                    updateObjectives();
                    playSound(Sound.NOTE_PIANO, 1f, 2f);
                    for (Location loc : spawnLocations1) {
                        buildCage(loc, Material.AIR);
                    }
                    for (Location loc : spawnLocations2) {
                        buildCage(loc, Material.AIR);
                    }
                } else if (ticksRemaining == 0) {
                    cancel();
                    team1.sendTranslation("arena.game.end");
                    team2.sendTranslation("arena.game.end");
                    appendTranslationToRecordingChat("arena.replay.game.end");
                    state = 2;
                    updateObjectives();
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                        @Override
                        public void run() {
                            endFight();
                        }
                    }, 60);
                } else {
                    int remaining = ticksRemaining / 20;
                    if (remaining % 60 == 0 && remaining > 60) {
                        team1.sendTranslation("arena.game.remainingm", remaining / 60);
                        team2.sendTranslation("arena.game.remainingm", remaining / 60);
                        appendTranslationToRecordingChat("arena.replay.game.remainingm", remaining / 60);
                    } else if (remaining % 10 == 0 && (remaining == 60 || remaining <= 30)) {
                        team1.sendTranslation("arena.game.remainings", remaining);
                        team2.sendTranslation("arena.game.remainings", remaining);
                        appendTranslationToRecordingChat("arena.replay.game.remainings", remaining);
                    } else if (remaining <= 5) {
                        team1.sendTranslation("arena.game.countdown", remaining);
                        team2.sendTranslation("arena.game.countdown", remaining);
                        appendTranslationToRecordingChat("arena.replay.game.countdown", remaining);
                    }
                }
            }
        }, interval, interval);
        //Report success
        startSuccess.accept(true);

        //Check if players left
        if (team1.getMembers().size() <= 0) {
            win(team2);
            return;
        }
        if (team2.getMembers().size() <= 0) {
            win(team1);
            return;
        }
    }

    private void heal(ArterionPlayer p) {
        p.getBukkitPlayer().setFoodLevel(20);
        p.getBukkitPlayer().setSaturation(20);
        p.getSkillSlots().setAllOffCooldown();
        p.setMana(p.getMaxMana());
        p.getPlayerScoreboard().updateHealth();
    }

    private void buildCage(Location loc, Material mat) {
        Block b = loc.getBlock();
        for (int i = 0; i <= 2; i++) {
            b.getRelative(BlockFace.NORTH).setType(mat);
            b.getRelative(BlockFace.EAST).setType(mat);
            b.getRelative(BlockFace.WEST).setType(mat);
            b.getRelative(BlockFace.SOUTH).setType(mat);
            b = b.getRelative(BlockFace.UP);
        }
        b.setType(mat);
    }

    @Override
    public void onFailWorldLoad() {
        for (ArterionPlayer p : players1) {
            p.sendTranslation("arena.initfail");
        }
        for (ArterionPlayer p : players2) {
            p.sendTranslation("arena.initfail");
        }
        //Report fail
        startSuccess.accept(false);
    }

    @Override
    public void onWorldUnload(boolean instant) {
        ArterionPlugin.getInstance().getRegionManager().all().remove(region);
        for (Team team : getTeams()) {
            for (ArterionPlayer player : team.getMembers()) {
                player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
                player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
            }
            team.disband();
        }
    }

    @Override
    public void onReplaySaved(File file, boolean instant) {
        try {
            ReplayACLFile acl = new ReplayACLFile(new File(ArterionPlugin.REPLAY_DIR + File.separator + "arena_fights",
                    getReplayUUID().toString().replace("-", "") + File.separator + "replayacl"));
            for (DatabasePlayer p : persistenceHolder.getPlayersTeamOne()) {
                acl.addEntry(ReplayACLEntryType.PLAYER, p.getUuid(), true);
            }
            for (DatabasePlayer p : persistenceHolder.getPlayersTeamTwo()) {
                acl.addEntry(ReplayACLEntryType.PLAYER, p.getUuid(), true);
            }
            acl.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object lock = new Object();
        this.updateInDB(dbf -> {
            dbf.setReplayLocation("arena_fights" + File.separator +
                    getReplayUUID().toString().replace("-", ""));
            dbf.setTimeEnd(System.currentTimeMillis());
            dbf.setWinner(winner == null ? -1 : (winner.equals(team1) ? 1 : 2));
            dbf.setRemaining1(remaining1);
            dbf.setRemaining2(remaining2);
        }, succ -> {
            if (!succ) {
                System.err.println("Error while saving arena fight! :(");
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
        if (state == 2) return false;
        if (!deadPlayers.contains(player.getUUID())) {
            deadPlayers.add(player.getUUID());
            if (team1.hasMember(player)) {
                remaining1--;
                if (remaining1 <= 0) win(team2);
            } else if (team2.hasMember(player)) {
                remaining2--;
                if (remaining2 <= 0) win(team1);
            }
        }
        updateObjectives();
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircleLater(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), player);
            }
        }, 20l);
        return false;
    }

    private void win(Team team) {
        team1.sendTranslation("arena.game.end");
        team2.sendTranslation("arena.game.end");
        appendTranslationToRecordingChat("arena.replay.game.end");
        if (team1.equals(team)) {
            team1.sendTranslation("arena.win");
            team2.sendTranslation("arena.loose");
        } else {
            team2.sendTranslation("arena.win");
            team1.sendTranslation("arena.loose");
        }
        playSound(Sound.NOTE_PIANO, 1f, 2f);
        this.winner = team;
        state = 2;
        updateObjectives();
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                endFight();
            }
        }, 60);
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircleLater(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
            }
        }, 20);
    }

    @Override
    public boolean onQuit(ArterionPlayer player, boolean defineTeleport) {
        player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        if (state == 2) return false;
        onDeath(player);
        team1.removeMember(player);
        team2.removeMember(player);
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        if (defineTeleport) {
            player.getBukkitPlayer().teleport(ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
            player.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
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
            Objective obj = new Objective(new ItemStack(Material.IRON_SWORD),
                    "iron_sword",
                    System.currentTimeMillis() + getGameModeConfig().pregame_seconds * 1000, "arena.pregame");
            team1.setObjective(obj);
            team2.setObjective(obj);
        } else if (state == 1) {
            //Ingame
            team1.setObjective(new Objective(new ItemStack(Material.IRON_SWORD),
                    "iron_sword",
                    fightDeadline, "arena.ingame", remaining1, remaining2));
            team2.setObjective(new Objective(new ItemStack(Material.IRON_SWORD),
                    "iron_sword",
                    fightDeadline, "arena.ingame", remaining2, remaining1));
        } else if (state == 2) {
            //Post-game
            Objective obj = new Objective(new ItemStack(Material.IRON_SWORD),
                    "iron_sword",
                    -1, "arena.postgame");
            team1.setObjective(obj);
            team2.setObjective(obj);
        }
    }

    private void updateInDB(Consumer<DatabaseArenaFight> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabaseArenaFight>(DatabaseArenaFight.class, persistenceHolder.getUuid()) {
            DatabaseArenaFight updatedHolder;

            @Override
            public void updateObject(DatabaseArenaFight databaseArenaFight) {
                updatedHolder = databaseArenaFight;
                apply.accept(databaseArenaFight);
            }

            @Override
            public void success() {
                ArenaFight.this.persistenceHolder = updatedHolder;
                successCallBack.accept(true);
            }

            @Override
            public void fail() {
                successCallBack.accept(false);
            }
        }.execute();
    }

    @Override
    public StatContextType getType() {
        return StatContextType.ARENA_FIGHT;
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
    public StatContextTracker<ArenaFight> getStatTracker() {
        return statContextTracker;
    }

    @Override
    public TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        return FightStats.getTrackedStatistics(objectType);
    }
}
