package me.friwi.arterion.plugin.world.markedblock;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MarkedBlockSection {
    public static final int regionSize = 512;
    public static final int regionBitShift = 9;
    public static final int regionBitMask = 0x1FF;

    private byte[] blockMatrix;
    private World world;
    private File dataFile;
    private boolean dirty = false;
    private long lastChanged;
    private long lastAccessed;

    public MarkedBlockSection(World world, int rx, int rz, String folderName) {
        this.world = world;
        this.blockMatrix = new byte[regionSize * regionSize * world.getMaxHeight() / 8];
        this.dataFile = new File(ArterionPlugin.getInstance().getDataFolder(), folderName + File.separator + world.getName() + File.separator + rx + "_" + rz + ".dat");
    }

    public void load() {
        if (!dataFile.exists()) return;
        try (FileInputStream fis = new FileInputStream(dataFile)) {
            long length = dataFile.length();
            int r = 0;
            while (r < length) {
                r += fis.read(blockMatrix, r, blockMatrix.length - r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (!dirty) return;
        dirty = false;
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            fos.write(blockMatrix);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getBlock(Block block) {
        return getBlock(block.getX(), block.getY(), block.getZ());
    }

    public boolean getBlock(int x, int y, int z) {
        if (y < 0 || y >= world.getMaxHeight()) return false;
        lastAccessed = System.currentTimeMillis();
        x &= regionBitMask;
        z &= regionBitMask;
        int index = y * (regionSize * regionSize / 8) + x * (regionSize / 8) + z / 8;
        int bit = 0x1 << (z % 8);
        return (blockMatrix[index] & bit) == bit;
    }

    public void setBlock(Block block, boolean marked) {
        setBlock(block.getX(), block.getY(), block.getZ(), marked);
    }

    public void setBlock(int x, int y, int z, boolean marked) {
        if (y < 0 || y >= world.getMaxHeight()) return;
        lastAccessed = System.currentTimeMillis();
        x &= regionBitMask;
        z &= regionBitMask;
        int index = y * (regionSize * regionSize / 8) + x * (regionSize / 8) + z / 8;
        int bit = 0x1 << (z % 8);
        if (marked) {
            blockMatrix[index] |= bit;
        } else {
            blockMatrix[index] &= ~bit;
        }
        lastChanged = System.currentTimeMillis();
        dirty = true;
    }

    public long getLastChanged() {
        return lastChanged;
    }

    public boolean isDirty() {
        return dirty;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }
}
