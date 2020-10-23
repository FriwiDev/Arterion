package me.friwi.arterion.plugin.world.block.nonbtblocks;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.PlayerClaimRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.concurrent.CopyOnWriteArrayList;

public class SpecialBlockManager {
    private CopyOnWriteArrayList<SpecialBlock> SPECIAL_BLOCKS = new CopyOnWriteArrayList<>();
    private ArterionPlugin plugin;

    public SpecialBlockManager(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        Object lock = new Object();
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                System.out.println("Collecting home blocks...");
                //Then players
                db.findAllNonNullByColumnStream(DatabasePlayer.class, "claimWorld").forEach(dbp -> {
                    /*if (dbp.getLastOnline()!=-1 && dbp.getLastOnline() < System.currentTimeMillis() - plugin.getFormulaManager().PLAYER_HOMEBLOCK_AUTOREMOVE.evaluateInt() * 1000 * 60 * 60 * 24) {
                        //Player is idle
                        dbp.setOwnsHomeBlock(false);
                        dbp.setClaimWorld(null);
                        dbp.setHomeX(0);
                        dbp.setHomeY(0);
                        dbp.setHomeZ(0);
                        dbp.setRoomMate(null);
                        db.save(dbp);
                        return;
                    }*/
                    if (!dbp.isOwnsHomeBlock()) return;
                    Location loc = new Location(Bukkit.getWorld(dbp.getClaimWorld()), dbp.getHomeX(), dbp.getHomeY(), dbp.getHomeZ());
                    SPECIAL_BLOCKS.add(new HomeBlock(loc, dbp.getUuid()));
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                        @Override
                        public void run() {
                            plugin.getRegionManager().registerRegion(new PlayerClaimRegion(dbp.getName(), dbp.getUuid(), loc.getWorld(), loc.getChunk().getX(), loc.getChunk().getZ()));
                        }
                    });
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
                System.out.println("Error while collecting home blocks!");
            }
        }.execute();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(SpecialBlock block) {
        SPECIAL_BLOCKS.add(block);
    }

    public boolean remove(SpecialBlock block) {
        return SPECIAL_BLOCKS.remove(block);
    }

    public SpecialBlock remove(Location loc) {
        SpecialBlock found = null;
        for (SpecialBlock b : SPECIAL_BLOCKS) {
            if (b.getLocation().equals(loc)) {
                found = b;
                break;
            }
        }
        if (found != null) SPECIAL_BLOCKS.remove(found);
        return found;
    }

    public SpecialBlock get(Block block) {
        for (SpecialBlock b : SPECIAL_BLOCKS) if (b.getLocation().equals(block.getLocation())) return b;
        return null;
    }

    public Iterable<? extends SpecialBlock> getAll() {
        return SPECIAL_BLOCKS;
    }
}
