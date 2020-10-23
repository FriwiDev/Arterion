package me.friwi.arterion.plugin.world.lock;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.material.EnderChest;

import java.util.UUID;

public class LockUtil {
    public static Lock getLock(Chest chest) {
        if (chest instanceof EnderChest) return null;
        if (chest.getInventory() instanceof DoubleChestInventory) {
            DoubleChest dc = ((DoubleChestInventory) chest.getInventory()).getHolder();
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            Lock l = getLockSingleChest(left);
            if (l == null) l = getLockSingleChest(right);
            return l;
        } else {
            return getLockSingleChest(chest);
        }
    }

    private static Lock getLockSingleChest(Chest chest) {
        NBTCompound nbt = NBTInjector.getNbtData(chest);
        if (!nbt.hasKey("art_lock")) return null;
        byte ordinal = nbt.getByte("art_lock");
        LockTypeEnum type = LockTypeEnum.values()[ordinal];
        Lock ret = null;
        switch (type) {
            case PRIVATE:
                ret = new PrivateLock(getUUID(nbt, "art_owner"));
                break;
            case GUILD:
                ret = new GuildLock(ArterionPlugin.getInstance().getGuildManager().getGuildByUUID(getUUID(nbt, "art_owner")));
                break;
            case OFFICER:
                ret = new OfficerLock(ArterionPlugin.getInstance().getGuildManager().getGuildByUUID(getUUID(nbt, "art_owner")));
                break;
        }
        if (!ret.isValid()) {
            removeLockSingleChest(chest, ret);
            return null;
        }
        return ret;
    }

    public static String addLock(ArterionPlayer player, Chest chest, Lock lock) {
        if (chest instanceof EnderChest) return null;
        if (chest.getInventory() instanceof DoubleChestInventory) {
            DoubleChest dc = ((DoubleChestInventory) chest.getInventory()).getHolder();
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            String ret = addLockSingleChest(player, left, lock);
            if (ret.equals("success")) {
                ret = addLockSingleChest(player, right, lock);
                if (ret.equals("success")) {
                    return ret;
                } else {
                    removeLockSingleChest(left, lock);
                    return ret;
                }
            } else {
                return ret;
            }
        } else {
            return addLockSingleChest(player, chest, lock);
        }
    }

    public static String addLockSingleChest(ArterionPlayer player, Chest chest, Lock lock) {
        if (chest instanceof EnderChest) return null;
        if (getLock(chest) != null) return "haslock";
        if (player != null && !ArterionChunkUtil.getNonNull(chest.getChunk()).getRegion().belongsToPlayer(player))
            return "nothere";
        NBTCompound nbt = NBTInjector.getNbtData(chest);
        nbt.setByte("art_lock", (byte) lock.getType().ordinal());
        lock.applyTo(nbt);
        return "success";
    }

    public static void removeLock(Chest chest) {
        removeLock(chest, getLock(chest));
    }

    public static void removeLock(Chest chest, Lock lock) {
        if (chest instanceof EnderChest) return;
        if (chest.getInventory() instanceof DoubleChestInventory) {
            DoubleChest dc = ((DoubleChestInventory) chest.getInventory()).getHolder();
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            removeLockSingleChest(left, lock);
            removeLockSingleChest(right, lock);
        } else {
            removeLockSingleChest(chest, lock);
        }
    }

    private static void removeLockSingleChest(Chest chest, Lock lock) {
        NBTCompound nbt = NBTInjector.getNbtData(chest);
        nbt.removeKey("art_lock");
        lock.removeFrom(nbt);
    }

    public static void sendLockPlaceMessage(ArterionPlayer p, String msg) {
        p.sendTranslation("lock.place." + msg);
    }

    public static UUID getUUID(NBTCompound nbt, String key) {
        return new UUID(nbt.getLong(key + "_msb"), nbt.getLong(key + "_lsb"));
    }

    public static void setUUID(NBTCompound nbt, String key, UUID value) {
        nbt.setLong(key + "_msb", value.getMostSignificantBits());
        nbt.setLong(key + "_lsb", value.getLeastSignificantBits());
    }

    public static void removeUUID(NBTCompound nbt, String key) {
        nbt.removeKey(key + "_msb");
        nbt.removeKey(key + "_lsb");
    }
}
