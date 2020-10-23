package me.friwi.arterion.plugin.combat.gamemode.artefact;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.replay.EventReplay;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
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
import me.friwi.arterion.plugin.util.database.entity.DatabaseArtefactFight;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.ArtefactRegion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ArtefactFight implements StatContext<ArtefactFight> {
    private boolean ended = false;
    private DatabaseArtefactFight persistenceHolder;
    private List<ArterionPlayer> onGrounds = new LinkedList<>();
    private StatContextTracker<ArtefactFight> statContextTracker;

    public ArtefactFight() {
        persistenceHolder = new DatabaseArtefactFight(System.currentTimeMillis(),
                DatabaseArtefactFight.FIGHT_UNFINISHED,
                new HashSet<>(),
                null,
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
                if (!committed) System.err.println("Arte fight not committed!");
            }

            @Override
            public void onTransactionError() {
                System.err.println("Error persisting arte fight!");
            }
        }.execute();
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            if (ap.getRegion() instanceof ArtefactRegion) enterRegion(ap);
        }

        statContextTracker = new StatContextTracker<>(this);

        begin();
    }

    public boolean isEnded() {
        return ended;
    }

    public void onCristalRespawn() {
        Artefact.getBlock().setHealth(Artefact.getBlock().getMaxHealth());
        LanguageAPI.broadcastMessage("artefact.cristal.respawned", Artefact.countLivingCristals(), Artefact.getCristals().length);
        Artefact.appendTranslationToRecordingChat("artefact.replay.cristal.respawned", Artefact.countLivingCristals(), Artefact.getCristals().length);
    }

    public void begin() {

    }

    public void end(DatabaseGuild winner, DatabasePlayer capturer, boolean instant) {
        ended = true;
        //Reset cristals
        for (ArtefactCristal cristal : Artefact.getCristals()) {
            cristal.removeAll();
        }
        //Reset block
        Artefact.getBlock().remove();

        Artefact.setFight(null);
        updateObjectives();
        EventReplay backup = Artefact.getEventReplay();
        Object lock = new Object();
        Artefact.endEventReplay((file, uuid) -> {
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
                    ReplayACLFile acl = new ReplayACLFile(new File(ArterionPlugin.REPLAY_DIR + File.separator + "artefact_fights",
                            uuid.toString().replace("-", "") + File.separator + "replayacl"));
                    acl.addEntry(ReplayACLEntryType.ALL, null, true);
                    acl.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.updateInDB(dbf -> {
                    dbf.setReplayLocation("artefact_fights" + File.separator +
                            uuid.toString().replace("-", ""));
                    dbf.setTimeEnd(System.currentTimeMillis());
                    dbf.setParticipating(participating);
                    dbf.setWinner(winner);
                    dbf.setCapturer(capturer);
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
                        System.err.println("Error while saving arte fight! :(");
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
        for (ArtefactCristal cristal : Artefact.getCristals()) {
            cristal.removeAll();
        }
        Artefact.getBlock().remove();
        if (!instant) {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
                @Override
                public void run() {
                    getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
                }
            });
        }
    }

    public void enterRegion(ArterionPlayer player) {
        onGrounds.add(player);
        Artefact.doReplayEvent();
        updateObjective(player);
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), player, player.getGuild() == null ? null : player.getGuild().getUUID());
            }
        });
    }

    public void leaveRegion(ArterionPlayer player) {
        onGrounds.remove(player);
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), player);
            }
        });
    }

    public void updateObjectives() {
        for (ArterionPlayer player : onGrounds) {
            updateObjective(player);
        }
    }

    public void updateObjective(ArterionPlayer player) {
        Objective objective = null;
        if (Artefact.getFight() != null) {
            if (Artefact.getCarrier() == null && Artefact.getOwner() == null) {
                if (Artefact.areCristalsAlive()) {
                    objective = new Objective(new ItemStack(Material.ENDER_PORTAL_FRAME), "end_portal_frame", -1, "artefact.objective.cristals", Artefact.getCristals().length - Artefact.countLivingCristals(), Artefact.getCristals().length);
                } else if (player.getGuild() != null && Artefact.hasClaim(player.getGuild())) {
                    objective = new Objective(new ItemStack(Material.ENDER_PORTAL_FRAME), "end_portal_frame", -1, "artefact.objective.artefact");
                } else {
                    objective = new Objective(new ItemStack(Material.ENDER_PORTAL_FRAME), "end_portal_frame", -1, "artefact.objective.wait");
                }
            } else if (Artefact.getCarrier() != null) {
                if (player.equals(Artefact.getCarrier())) {
                    objective = new Objective(new ItemStack(Material.ENDER_PORTAL_FRAME), "end_portal_frame", -1, "artefact.objective.carry");
                } else if (player.getGuild() != null && Artefact.getCarrier().getGuild() != null && player.getGuild().equals(Artefact.getCarrier().getGuild())) {
                    objective = new Objective(new ItemStack(Material.ENDER_PORTAL_FRAME), "end_portal_frame", -1, "artefact.objective.escort", Artefact.getCarrier().getName());
                } else {
                    objective = new Objective(new ItemStack(Material.ENDER_PORTAL_FRAME), "end_portal_frame", -1, "artefact.objective.intercept", Artefact.getCarrier().getName());
                }
            }
        }
        player.getSkillSlots().setObjective(objective, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
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

    public DatabasePlayer getCapturer() {
        return persistenceHolder.getCapturer();
    }

    public void setCapturer(DatabasePlayer capturer, Consumer<Boolean> successCallback) {
        this.updateInDB(dbf -> dbf.setCapturer(capturer), successCallback);
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

    private void updateInDB(Consumer<DatabaseArtefactFight> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabaseArtefactFight>(DatabaseArtefactFight.class, persistenceHolder.getUuid()) {
            DatabaseArtefactFight updatedHolder;

            @Override
            public void updateObject(DatabaseArtefactFight databaseArtefactFight) {
                updatedHolder = databaseArtefactFight;
                apply.accept(databaseArtefactFight);
            }

            @Override
            public void success() {
                ArtefactFight.this.persistenceHolder = updatedHolder;
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
        return StatContextType.ARTE_FIGHT;
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
    public StatContextTracker<ArtefactFight> getStatTracker() {
        return statContextTracker;
    }

    @Override
    public TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        return FightStats.getTrackedStatistics(objectType);
    }
}
