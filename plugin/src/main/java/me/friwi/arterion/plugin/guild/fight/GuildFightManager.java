package me.friwi.arterion.plugin.guild.fight;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuildFight;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Instrument;
import org.bukkit.Note;

import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class GuildFightManager {
    private ArterionPlugin plugin;
    private List<GuildFight> activeFights = new LinkedList<>();

    public GuildFightManager(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void attack(ArterionPlayer initiator, Guild defender) {
        Guild attacker = initiator.getGuild();
        if (attacker == null) {
            initiator.sendTranslation("guild.notinguild");
            return;
        }
        if (attacker.getMembers().contains(initiator.getPersistenceHolder())) {
            initiator.sendTranslation("fight.guild.noofficer");
            return;
        }
        if (defender == null) {
            initiator.sendTranslation("fight.guild.nosuchguild");
            return;
        }
        if (attacker.equals(defender)) {
            initiator.sendTranslation("fight.guild.self");
            return;
        }
        if (attacker.isInLocalFight()) {
            initiator.sendTranslation("fight.guild.youinfight");
            return;
        }
        if (defender.isInLocalFight()) {
            initiator.sendTranslation("fight.guild.otherinfight");
            return;
        }
        if (defender.getHomeLocation() == null) {
            initiator.sendTranslation("fight.guild.noregion");
            return;
        }
        if (defender.getOnlineMembers().size() == 0) {
            initiator.sendTranslation("fight.guild.notonline");
            return;
        }
        if (defender.isProtected() && !defender.hasArtefact()) {
            initiator.sendTranslation("fight.guild.protected");
            return;
        }
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE);
        int hour = localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE).getHour();
        if (hour >= 23 || hour < 9) {
            initiator.sendTranslation("fight.guild.daytime");
            return;
        }

        int limit = defender.getRegionDistance() + 2;
        if (Math.abs(defender.getHomeLocation().getChunk().getX() - initiator.getBukkitPlayer().getLocation().getChunk().getX()) > limit
                || Math.abs(defender.getHomeLocation().getChunk().getZ() - initiator.getBukkitPlayer().getLocation().getChunk().getZ()) > limit
                || !defender.getHomeLocation().getWorld().equals(initiator.getBukkitPlayer().getWorld())) {
            initiator.sendTranslation("fight.guild.notinregion");
            return;
        }
        long cd = ArterionPlugin.getInstance().getFormulaManager().FIGHT_REPEAT_COOLDOWN.evaluateInt();
        new DatabaseTask() {
            List<DatabaseGuildFight> fights;

            @Override
            public boolean performTransaction(Database db) {
                fights = db.findAllByCriteria(DatabaseGuildFight.class, (query, builder) -> {
                            Root root = query.from(DatabaseGuildFight.class);
                            query
                                    .select(root)
                                    .where(builder.and(
                                            builder.equal(root.get("attacker"), attacker.getPersistenceHolder()),
                                            builder.equal(root.get("defender"), defender.getPersistenceHolder()),
                                            builder.gt(root.get("timeBegin"), System.currentTimeMillis() - cd))
                                    );
                        }
                );
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (committed) {
                    if (!fights.isEmpty() && !defender.hasArtefact()) {
                        initiator.sendTranslation("fight.guild.recent");
                        return;
                    }
                    int fee = attacker.getLeader().getMaxLevel();
                    for (DatabasePlayer p : attacker.getOfficers()) {
                        fee += p.getMaxLevel();
                    }
                    for (DatabasePlayer p : attacker.getMembers()) {
                        fee += p.getMaxLevel();
                    }
                    fee *= ArterionPlugin.getInstance().getFormulaManager().FIGHT_FEE_MULTIPLIER.evaluateInt();
                    int finalFee = fee;
                    attacker.getMoneyBearer().addMoney(-fee, success -> {
                        if (!success) {
                            initiator.sendTranslation("fight.guild.nomoney", finalFee / 100f);
                            return;
                        }
                        new DatabaseTask() {
                            DatabaseGuildFight fight;

                            @Override
                            public boolean performTransaction(Database db) {
                                fight = new DatabaseGuildFight(System.currentTimeMillis(), DatabaseGuildFight.FIGHT_UNFINISHED, attacker.getPersistenceHolder(), defender.getPersistenceHolder(), null, new HashSet<>(), new HashSet<>(), null);
                                db.save(fight);
                                return true;
                            }

                            @Override
                            public void onTransactionCommitOrRollback(boolean committed) {
                                if (committed) {
                                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                        @Override
                                        public void run() {
                                            GuildFight gfight = new GuildFight(attacker, defender, fight);
                                            activeFights.add(gfight);
                                            attacker.setLocalFight(gfight);
                                            defender.setLocalFight(gfight);
                                            defender.forceAllGuisClose();
                                            defender.sendTranslation("line");
                                            LanguageAPI.broadcastMessage("fight.guild.begin", attacker, defender);
                                            defender.sendTranslation("line");
                                            defender.playNote(Instrument.BASS_GUITAR, Note.flat(0, Note.Tone.A));
                                            for (ArterionPlayer p : defender.getOnlineMembers()) {
                                                p.scheduleHotbarCard(new DefendCard(p, attacker));
                                            }
                                            gfight.startTimer();
                                        }
                                    });
                                } else {
                                    initiator.sendTranslation("fight.guild.dberror");
                                }
                            }

                            @Override
                            public void onTransactionError() {
                                initiator.sendTranslation("fight.guild.dberror");
                            }
                        }.execute();
                    });
                } else {
                    initiator.sendTranslation("fight.guild.dberror");
                }
            }

            @Override
            public void onTransactionError() {
                initiator.sendTranslation("fight.guild.dberror");
            }
        }.execute();


    }

    public void steal(ArterionPlayer initiator) {
        if (initiator.getGuild() == null) {
            initiator.sendTranslation("guild.notinguild");
            return;
        }
        if (!initiator.getGuild().isInLocalFight()) {
            initiator.sendTranslation("fight.guild.notinfight");
            return;
        }
        initiator.getGuild().getLocalFight().updateSteal(initiator, true);
    }

    public void onDeathOrLeave(ArterionPlayer player) {
        if (player.getGuild() != null && player.getGuild().getLocalFight() != null) {
            player.getGuild().getLocalFight().getStealingPlayers().remove(player);
            if (!player.getGuild().getLocalFight().isPrefight()) {
                player.getGuild().getLocalFight().getHomeCd().put(player.getBukkitPlayer().getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    public List<GuildFight> getActiveFights() {
        return activeFights;
    }

    public void onDisable() {
        Object lock = new Object();
        for (GuildFight fight : activeFights) {
            fight.endFight(file -> {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }, false, true);
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
