package me.friwi.arterion.plugin.guild;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.block.nonbtblocks.GuildBlock;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class GuildManager {
    private ArterionPlugin plugin;
    private List<Guild> guilds = new ArrayList<>();

    public GuildManager(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Object lock = new Object();
        new DatabaseTask() {
            boolean first = true;

            @Override
            public boolean performTransaction(Database db) {
                if (!first) {
                    return true;
                }
                first = false;
                System.out.println("Collecting guilds...");
                db.findAll(DatabaseGuild.class).forEach(dbg -> {
                    Guild g = new Guild(dbg);
                    GlobalStats.getContext().getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), g, null);
                    GlobalStats.getTopContext().getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), g, null);
                    guilds.add(g);
                    if (g.getDeleted() == DatabaseGuild.NOT_DELETED && g.getHomeLocation() != null) {
                        int dist = g.getRegionDistance();
                        GuildRegion region = new GuildRegion(g, g.getHomeLocation().getWorld(), (g.getHomeLocation().getBlockX() >> 4) - dist, (g.getHomeLocation().getBlockX() >> 4) + dist, (g.getHomeLocation().getBlockZ() >> 4) - dist, (g.getHomeLocation().getBlockZ() >> 4) + dist);
                        plugin.getRegionManager().registerRegion(region);
                        plugin.getSpecialBlockManager().add(new GuildBlock(g.getHomeLocation(), g));
                        g.setRegion(region);
                    }
                });
                synchronized (lock) {
                    lock.notifyAll();
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                System.out.println("Done");
            }

            @Override
            public void onTransactionError() {
                System.out.println("Error while collecting guilds!");
            }
        }.execute();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Apply wither to all enemies on every guild region
        plugin.getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                    if (ap.isVanished()) continue;
                    if (ap.getRegion() instanceof GuildRegion && ((GuildRegion) ap.getRegion()).getGuild().getDeleted() == DatabaseGuild.NOT_DELETED) {
                        if (ap.getGuild() == null || !((GuildRegion) ap.getRegion()).getGuild().isAllowedOnLand(ap)) {
                            ap.sendTranslation("region.guild.wither");
                            ap.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 7 * 20, 4));
                            continue;
                        }
                    }
                }
            }
        }, 5 * 20, 5 * 20);
        //Tax every hour
        plugin.getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                List<Guild> toCheck = new LinkedList<>();
                for (Guild g : guilds) if (g.getDeleted() == DatabaseGuild.NOT_DELETED) toCheck.add(g);
                if (toCheck.size() > 0) {
                    Guild first = toCheck.remove(0);
                    first.calculateAndSubtractGuildTax(toCheck);
                }
            }
        }, 3600 * 20 - (System.currentTimeMillis() % (3600 * 1000)) / 50, 3600 * 20); //Every hour on full hour
    }

    public void onPlayerJoin(ArterionPlayer player) {
        for (Guild g : guilds) {
            if (g.getDeleted() == DatabaseGuild.NOT_DELETED) {
                if (g.isInGuild(player)) {
                    player.setGuild(g);
                    g.onPlayerJoin(player);
                    //Guild defense
                    if (g.isInLocalFight()) {
                        g.getLocalFight().signupDefender(player);
                    }
                    break;
                }
            }
        }
    }

    public void onPlayerLeave(ArterionPlayer player) {
        for (Guild g : guilds) {
            if (g.getDeleted() == DatabaseGuild.NOT_DELETED) {
                if (g.isInGuild(player)) {
                    g.onPlayerLeave(player);
                    break;
                }
            }
        }
    }

    public Guild getGuildByName(String name) {
        for (Guild g : guilds) {
            if (g.getName().equalsIgnoreCase(name) && g.getDeleted() == DatabaseGuild.NOT_DELETED) return g;
        }
        return null;
    }

    public Guild getGuildByTag(String tag) {
        for (Guild g : guilds) {
            if (g.getTag().equalsIgnoreCase(tag) && g.getDeleted() == DatabaseGuild.NOT_DELETED) return g;
        }
        return null;
    }

    public Guild getGuildByUUID(UUID uuid) {
        for (Guild g : guilds) {
            if (g.getUuid().equals(uuid)) return g;
        }
        return null;
    }

    public Guild getGuildByMemberUUID(UUID uuid) {
        for (Guild g : guilds) {
            if (g.getDeleted() == DatabaseGuild.NOT_DELETED) {
                if (g.getMember(uuid) != null) return g;
            }
        }
        return null;
    }

    public void onGuildCreate(Guild guild, ArterionPlayer leader) {
        leader.setGuild(guild);
        guild.onPlayerJoin(leader, true);
        this.guilds.add(guild);
        LanguageAPI.broadcastMessage("line");
        LanguageAPI.broadcastMessage("guild.created", guild, leader);
        LanguageAPI.broadcastMessage("line");
    }

    public Iterable<? extends Guild> getGuilds() {
        return guilds;
    }

    public void onShutdown() {
        Object lock = new Object();
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                for (Guild g : getGuilds()) {
                    if (g.getVault() != null) g.getVault().save();
                    GlobalStats.getContext().getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), g);
                    GlobalStats.getTopContext().getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), g);
                    g.getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
                }
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
    }
}
