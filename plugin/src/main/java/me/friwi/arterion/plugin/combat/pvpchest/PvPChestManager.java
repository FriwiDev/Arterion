package me.friwi.arterion.plugin.combat.pvpchest;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PvPChestManager {
    private List<PvPChest> spawnedChests = new LinkedList<>();

    public void init() {
        spawnAll(null);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                tick++;
                for (PvPChest c : spawnedChests) {
                    c.playEffect(tick % 5 == 0);
                }
            }
        }, 20, 20);
    }

    public void spawnAll(Location blackListed) {
        int wanted = ArterionPlugin.getInstance().getFormulaManager().PVPCHEST_AMOUNT.evaluateInt();
        while (spawnedChests.size() < wanted) {
            Location spawn = PvPChestLocations.getRandomLocation(blackListed, spawnedChests);
            if (spawn != null) {
                PvPChest c = new PvPChest(spawn.getBlock(), (byte) spawn.getYaw());
                spawnedChests.add(c);
                c.spawnChest();
            } else {
                return;
            }
        }
    }

    public void removeAll() {
        for (PvPChest c : spawnedChests) {
            c.removeChestWithoutDrops();
        }
    }

    public void onChestClose(Block location) {
        Iterator<PvPChest> it = spawnedChests.iterator();
        while (it.hasNext()) {
            PvPChest c = it.next();
            if (location.getLocation().equals(c.getLoc().getLocation())) {
                c.removeChest();
                it.remove();
            }
        }
        spawnAll(location.getLocation());
    }
}
