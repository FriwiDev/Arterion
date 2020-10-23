package me.friwi.arterion.plugin.combat.gamemode.capturepoint;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.replay.EventReplay;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.replay.acl.ReplayACLEntryType;
import me.friwi.arterion.plugin.replay.acl.ReplayACLFile;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.context.StatContextType;
import me.friwi.arterion.plugin.stats.list.FightStats;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseCapturePointFight;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class CapturePointFight implements StatContext<CapturePointFight> {
    private DatabaseCapturePointFight persistenceHolder;
    private CapturePoint capturePoint;

    private StatContextTracker<CapturePointFight> statContextTracker;

    public CapturePointFight(CapturePoint capturePoint) {
        this.capturePoint = capturePoint;
        persistenceHolder = new DatabaseCapturePointFight(capturePoint.getRawName(),
                System.currentTimeMillis(),
                DatabaseCapturePointFight.FIGHT_UNFINISHED,
                new HashSet<>(),
                null,
                new HashSet<>(),
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
                if (!committed) System.err.println("Capture point fight not committed!");
            }

            @Override
            public void onTransactionError() {
                System.err.println("Error persisting capture point fight!");
            }
        }.execute();

        this.statContextTracker = new StatContextTracker<>(this);

        for (ArterionPlayer p : capturePoint.getOnGrounds()) {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
                @Override
                public void run() {
                    getStatTracker().beginTracking(p, p.getGuild() == null ? null : p.getGuild().getUUID());
                }
            });
        }
    }

    public void end(DatabaseGuild winner, boolean instant) {
        capturePoint.setFight(null);
        capturePoint.remove();
        EventReplay backup = capturePoint.getEventReplay();
        Object lock = new Object();
        capturePoint.endEventReplay((file, uuid) -> {
            if (file != null) {
                System.out.println("Finished event replay: " + file);
                Set<UUID> players = backup.getOccuringPlayers();
                Set<DatabaseGuild> participating = new HashSet<>();
                Set<DatabasePlayer> playersParticipating = new HashSet<>();
                Set<DatabasePlayer> playersWinning = new HashSet<>();
                for (UUID u : players) {
                    Guild guild = ArterionPlugin.getInstance().getGuildManager().getGuildByMemberUUID(u);
                    if (guild != null) {
                        DatabasePlayer p = guild.getMember(u);
                        participating.add(guild.getPersistenceHolder());
                        playersParticipating.add(p);
                        if (winner != null && winner.equals(guild.getPersistenceHolder())) {
                            playersWinning.add(p);
                        }
                    }
                }
                try {
                    ReplayACLFile acl = new ReplayACLFile(new File(ArterionPlugin.REPLAY_DIR + File.separator + "capture_point_fights",
                            uuid.toString().replace("-", "") + File.separator + "replayacl"));
                    acl.addEntry(ReplayACLEntryType.ALL, null, true);
                    acl.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.updateInDB(dbf -> {
                    dbf.setReplayLocation("capture_point_fights" + File.separator +
                            uuid.toString().replace("-", ""));
                    dbf.setTimeEnd(System.currentTimeMillis());
                    dbf.setParticipating(participating);
                    dbf.setWinner(winner);
                    dbf.setPlayersParticipating(playersParticipating);
                    dbf.setPlayersWinning(playersWinning);
                    //Calculate players guild assignments
                    Map<Guild, ArrayList<UUID>> guildMap = new HashMap<>();
                    for (DatabasePlayer player : playersParticipating) {
                        Guild g = ArterionPlugin.getInstance().getGuildManager().getGuildByMemberUUID(player.getUuid());
                        if (g != null) {
                            ArrayList<UUID> list = guildMap.get(g);
                            if (list == null) {
                                list = new ArrayList<>(10);
                                guildMap.put(g, list);
                            }
                            list.add(player.getUuid());
                        }
                    }
                    for (Map.Entry<Guild, ArrayList<UUID>> entry : guildMap.entrySet()) {
                        dbf.getGuilds().put(entry.getKey().getPersistenceHolder(), entry.getValue().toArray(new UUID[0]));
                    }
                }, succ -> {
                    if (!succ) {
                        System.err.println("Error while saving capture point fight! :(");
                    }
                    if (instant) {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
            } else {
                if (instant) {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
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
        capturePoint.remove();
        if (!instant) {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
                @Override
                public void run() {
                    getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
                }
            });
        }
    }

    public void enterRegion(ArterionPlayer p) {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), p, p.getGuild() == null ? null : p.getGuild().getUUID());
            }
        });
    }

    public void leaveRegion(ArterionPlayer p) {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), p);
            }
        });
    }

    public long getTimeBegin() {
        return persistenceHolder.getTimeBegin();
    }

    public long getTimeEnd() {
        return persistenceHolder.getTimeEnd();
    }

    public void setTimeEnd(long timeEnd, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setTimeEnd(timeEnd), successCallback);
    }

    public Set<DatabaseGuild> getParticipating() {
        return persistenceHolder.getParticipating();
    }

    public void setParticipating(Set<DatabaseGuild> participating, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setParticipating(participating), successCallback);
    }

    public DatabaseGuild getWinner() {
        return persistenceHolder.getWinner();
    }

    public void setWinner(DatabaseGuild winner, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setWinner(winner), successCallback);
    }

    public Set<DatabasePlayer> getPlayersParticipating() {
        return persistenceHolder.getPlayersParticipating();
    }

    public void setPlayersParticipating(Set<DatabasePlayer> playersParticipating, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setPlayersParticipating(playersParticipating), successCallback);
    }

    public Set<DatabasePlayer> getPlayersWinning() {
        return persistenceHolder.getPlayersWinning();
    }

    public void setPlayersWinning(Set<DatabasePlayer> playersWinning, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setPlayersWinning(playersWinning), successCallback);
    }

    public String getReplayLocation() {
        return persistenceHolder.getReplayLocation();
    }

    public void setReplayLocation(String replayLocation, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setReplayLocation(replayLocation), successCallback);
    }

    private void updateInDB(Consumer<DatabaseCapturePointFight> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabaseCapturePointFight>(DatabaseCapturePointFight.class, persistenceHolder.getUuid()) {
            DatabaseCapturePointFight updatedHolder;

            @Override
            public void updateObject(DatabaseCapturePointFight databaseArtefactFight) {
                updatedHolder = databaseArtefactFight;
                apply.accept(databaseArtefactFight);
            }

            @Override
            public void success() {
                CapturePointFight.this.persistenceHolder = updatedHolder;
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
        return StatContextType.CAPTURE_POINT_FIGHT;
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
    public StatContextTracker<CapturePointFight> getStatTracker() {
        return statContextTracker;
    }

    @Override
    public TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        return FightStats.getTrackedStatistics(objectType);
    }
}
