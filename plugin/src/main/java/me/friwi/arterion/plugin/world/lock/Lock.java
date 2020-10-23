package me.friwi.arterion.plugin.world.lock;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.block.Chest;

public interface Lock {
    boolean canAccess(ArterionPlayer p);

    void sendDeny(ArterionPlayer p);

    void sendAllow(ArterionPlayer p);

    boolean canDestroy(ArterionPlayer p, Chest chest);

    void applyTo(NBTCompound nbt);

    void removeFrom(NBTCompound nbt);

    LockTypeEnum getType();

    boolean isFriendlyLock(ArterionPlayer arterionPlayer);

    boolean isValid();
}
