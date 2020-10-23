package me.friwi.arterion.plugin.world.lock;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.InstantHotbarMessageCard;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;

import java.util.UUID;

public class PrivateLock implements Lock {
    private UUID owner;

    public PrivateLock(UUID owner) {
        this.owner = owner;
    }

    @Override
    public boolean canAccess(ArterionPlayer p) {
        return p.getBukkitPlayer().getUniqueId().equals(owner);
    }

    @Override
    public void sendDeny(ArterionPlayer p) {
        p.scheduleHotbarCard(new InstantHotbarMessageCard(2000, p, "lock.private.deny", Bukkit.getOfflinePlayer(owner).getName()));
    }

    @Override
    public void sendAllow(ArterionPlayer p) {
        p.scheduleHotbarCard(new InstantHotbarMessageCard(2000, p, "lock.private.allow", Bukkit.getOfflinePlayer(owner).getName()));
    }

    @Override
    public boolean canDestroy(ArterionPlayer p, Chest chest) {
        if (ArterionChunkUtil.getNonNull(chest.getChunk()).getRegion().administeredByPlayer(p)) return true;
        return canAccess(p);
    }

    @Override
    public void applyTo(NBTCompound nbt) {
        LockUtil.setUUID(nbt, "art_owner", owner);
    }

    @Override
    public void removeFrom(NBTCompound nbt) {
        LockUtil.removeUUID(nbt, "art_owner");
    }

    @Override
    public LockTypeEnum getType() {
        return LockTypeEnum.PRIVATE;
    }

    @Override
    public boolean isFriendlyLock(ArterionPlayer arterionPlayer) {
        return arterionPlayer.getBukkitPlayer().getUniqueId().equals(owner) //Self
                || (arterionPlayer.getRoomMate() != null && arterionPlayer.getRoomMate().equals(owner)) //Roommate
                || (arterionPlayer.getGuild() != null && arterionPlayer.getGuild().getMember(owner) != null //Same guild
                && !arterionPlayer.getGuild().getLeader().getUuid().equals(arterionPlayer.getBukkitPlayer().getUniqueId())); //Leader is allowed to target lock!
    }

    @Override
    public boolean isValid() {
        return owner != null;
    }

    public UUID getOwner() {
        return owner;
    }
}
