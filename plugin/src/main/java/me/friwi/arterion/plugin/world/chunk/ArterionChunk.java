package me.friwi.arterion.plugin.world.chunk;

import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ArterionChunk {
    private Region region;
    private Chunk chunk;
    private int temporaryBlocks;
    private int[] temporaryBlockFlags;
    private Map[] listeners = new Map[3];

    public ArterionChunk(Chunk chunk) {
        this.chunk = chunk;
        this.temporaryBlockFlags = new int[chunk.getWorld().getMaxHeight() * 8];
        for (int i = 0; i < listeners.length; i++) {
            listeners[i] = new HashMap();
        }
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public boolean isTemporaryBlock(Block block) {
        return isTemporaryBlock(block.getX(), block.getY(), block.getZ());
    }

    public boolean isTemporaryBlock(int x, int y, int z) {
        x &= 0xF;
        z &= 0xF;
        int index = y * 8 + x / 2;
        if (index >= temporaryBlockFlags.length || index < 0) return false;
        int bit = (x % 2) * 16 + z;
        return ((temporaryBlockFlags[index] >> bit) & 0x00000001) == 0x00000001;
    }

    public void setTemporaryBlock(Block block, boolean value) {
        setTemporaryBlock(block.getX(), block.getY(), block.getZ(), value);
    }

    public void setTemporaryBlock(int x, int y, int z, boolean value) {
        x &= 0xF;
        z &= 0xF;
        int index = y * 8 + x / 2;
        if (index >= temporaryBlockFlags.length || index < 0) return;
        if (isTemporaryBlock(x, y, z) == value) return;
        if (value) temporaryBlocks++;
        else temporaryBlocks--;
        int bit = (x % 2) * 16 + z;
        int val = 0x00000001 << bit;
        if (value) {
            temporaryBlockFlags[index] |= val;
        } else {
            val = ~val;
            temporaryBlockFlags[index] &= val;
        }
    }

    public boolean hasTemporaryBlocks() {
        return temporaryBlocks > 0;
    }

    public Map getListeners(int index) {
        return listeners[index];
    }
}
