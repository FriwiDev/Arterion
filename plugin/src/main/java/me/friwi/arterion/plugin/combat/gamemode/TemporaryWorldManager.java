package me.friwi.arterion.plugin.combat.gamemode;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class TemporaryWorldManager {
    private Map<String, TemporaryWorld> worlds = new HashMap<>();

    public TemporaryWorld createWorld(File templateDir, Consumer<Boolean> successCallback) {
        TemporaryWorld n = new TemporaryWorld(templateDir);
        n.loadWorldAsync(succ -> {
            if (!succ) {
                worlds.remove(n.getName());
            }
            successCallback.accept(succ);
        });
        worlds.put(n.getName(), n);
        return n;
    }

    public void unloadWorld(TemporaryWorld temporaryWorld, boolean instant) {
        worlds.remove(temporaryWorld.getName());
        temporaryWorld.unloadWorld(instant);
    }

    public TemporaryWorld getWorld(String name) {
        return worlds.get(name);
    }

    public ChunkPreferenceEnum isWantedChunk(Chunk chunk) {
        return isWantedChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public ChunkPreferenceEnum isWantedChunk(World world, int x, int z) {
        TemporaryWorld tw = getWorld(world.getName());
        if (tw == null) return ChunkPreferenceEnum.UNMANAGED;
        else if (tw.isWantedChunk(x, z)) {
            return ChunkPreferenceEnum.WANTED;
        } else {
            return ChunkPreferenceEnum.UNWANTED;
        }
    }

    public void onShutdown() {
        Iterator<Map.Entry<String, TemporaryWorld>> it = worlds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, TemporaryWorld> ent = it.next();
            ent.getValue().unloadWorld(true);
            it.remove();
        }
    }

    public Collection<TemporaryWorld> all() {
        return worlds.values();
    }

    public boolean isTemporaryWorld(World world) {
        return getWorld(world.getName()) != null;
    }

    public enum ChunkPreferenceEnum {
        UNMANAGED,
        WANTED,
        UNWANTED
    }
}
