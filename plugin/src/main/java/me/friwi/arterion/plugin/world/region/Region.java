package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class Region {
    private String name;
    private World world;
    private int x1, x2, z1, z2;

    private boolean pvp;
    private boolean noEnter;
    private boolean mobSpawn;
    private boolean animalSpawn;
    private boolean modifyEntities;
    private boolean stopDecay;

    public Region(String name, World world, int x1, int x2, int z1, int z2, boolean pvp, boolean noEnter, boolean mobSpawn, boolean animalSpawn, boolean modifyEntities, boolean stopDecay) {
        this.name = name;
        this.world = world;
        this.x1 = x1 < x2 ? x1 : x2;
        this.x2 = x2 < x1 ? x1 : x2;
        this.z1 = z1 < z2 ? z1 : z2;
        this.z2 = z2 < z1 ? z1 : z2;
        this.pvp = pvp;
        this.noEnter = noEnter;
        this.mobSpawn = mobSpawn;
        this.animalSpawn = animalSpawn;
        this.modifyEntities = modifyEntities;
        this.stopDecay = stopDecay;
    }

    public String getName(Language lang) {
        return lang.getTranslation(name).translate().getMessage();
    }

    public String getRawName() {
        return name;
    }

    public boolean isPvp() {
        return pvp;
    }

    public boolean isNoEnter(ArterionPlayer player) {
        return noEnter;
    }

    public boolean isMobSpawn() {
        return mobSpawn;
    }

    public boolean isAnimalSpawn() {
        return animalSpawn;
    }

    public boolean isModifyEntities() {
        return modifyEntities;
    }

    public boolean isStopDecay() {
        return stopDecay;
    }

    public abstract boolean canPlayerBuild(ArterionPlayer p);

    public boolean canPlayerDestroy(ArterionPlayer p, Block b) {
        return canPlayerBuild(p);
    }

    public boolean isInZone(World w, int x, int z) {
        if (!w.equals(this.world)) return false;
        return x1 <= x && x2 >= x && z1 <= z && z2 >= z;
    }

    public void forEachChunkAsync(Consumer<Chunk> consumer) {
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {

            @Override
            public void run() {
                for (int x = x1; x <= x2; x++) {
                    for (int z = z1; z <= z2; z++) {
                        consumer.accept(world.getChunkAt(x, z));
                        //PaperLib.getChunkAtAsync(world, x, z).thenAccept(consumer);
                    }
                }
            }
        });
    }

    public void forEachChunkParallel(Consumer<Chunk> consumer, int batchSize, Runnable finished) {
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int x = x1;
            int z = z1;
            int batchSizeInternal = batchSize;
            int completed = 0;
            int target = (x2 - x1 + 1) * (z2 - z1 + 1);

            @Override
            public void run() {
                for (int i = 0; i < batchSizeInternal; i++) {
                    /*PaperLib.getChunkAtAsync(world, x, z).thenAccept(c -> {
                        consumer.accept(c);
                        completed++;
                        if (completed == target) {
                            finished.run();
                        }
                    });
                    */
                    Chunk c = world.getChunkAt(x, z);
                    consumer.accept(c);
                    completed++;
                    if (completed == target) {
                        finished.run();
                    }

                    z++;
                    if (z > z2) {
                        z = z1;
                        x++;
                    }
                    if (x > x2) {
                        cancel();
                        batchSizeInternal = 0;
                        return;
                    }
                }
            }
        }, 0, 1);
    }

    public abstract void greetMsg(ArterionPlayer player);

    public abstract void denyMsg(ArterionPlayer player);

    public int getLowX() {
        return x1;
    }

    public int getHighX() {
        return x2;
    }

    public int getLowZ() {
        return z1;
    }

    public int getHighZ() {
        return z2;
    }

    public abstract boolean belongsToPlayer(ArterionPlayer player);

    public abstract boolean administeredByPlayer(ArterionPlayer player);

    public abstract void onEnter(ArterionPlayer player);

    public abstract void onLeave(ArterionPlayer player);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;
        Region region = (Region) o;
        return isPvp() == region.isPvp() &&
                noEnter == region.noEnter &&
                isMobSpawn() == region.isMobSpawn() &&
                isAnimalSpawn() == region.isAnimalSpawn() &&
                isModifyEntities() == region.isModifyEntities() &&
                name.equals(region.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isPvp(), noEnter, isMobSpawn(), isAnimalSpawn(), isModifyEntities());
    }
}
