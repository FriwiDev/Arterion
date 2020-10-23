package me.friwi.arterion.plugin.world.chunk;

import me.friwi.arterion.plugin.util.patch.SpigotPatcher;
import org.bukkit.Chunk;

public class ArterionChunkUtil {
    public static ArterionChunk getNonNull(Chunk c) {
        ArterionChunk ec = get(c);
        if (ec == null) {
            ec = new ArterionChunk(c);
            set(c, ec);
        }
        return ec;
    }

    private static ArterionChunk get(Chunk c) {
        try {
            return (ArterionChunk) SpigotPatcher.chunkAccess.get(c);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void set(Chunk c, ArterionChunk ec) {
        try {
            SpigotPatcher.chunkAccess.set(c, ec);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
