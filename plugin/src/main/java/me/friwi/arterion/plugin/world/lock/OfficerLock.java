package me.friwi.arterion.plugin.world.lock;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.InstantHotbarMessageCard;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.block.Chest;

public class OfficerLock implements Lock {
    private Guild owner;

    public OfficerLock(Guild owner) {
        this.owner = owner;
    }

    @Override
    public boolean canAccess(ArterionPlayer p) {
        if (owner.getDeleted() != DatabaseGuild.NOT_DELETED) return true;
        return owner.getOfficer(p.getBukkitPlayer().getUniqueId()) != null;
    }

    @Override
    public void sendDeny(ArterionPlayer p) {
        p.scheduleHotbarCard(new InstantHotbarMessageCard(2000, p, "lock.officer.deny", p.getLanguage().translateObject(owner)));
    }

    @Override
    public void sendAllow(ArterionPlayer p) {
        p.scheduleHotbarCard(new InstantHotbarMessageCard(2000, p, "lock.officer.allow", p.getLanguage().translateObject(owner)));
    }

    @Override
    public boolean canDestroy(ArterionPlayer p, Chest chest) {
        if (ArterionChunkUtil.getNonNull(chest.getChunk()).getRegion().administeredByPlayer(p)) return true;
        return canAccess(p);
    }

    @Override
    public void applyTo(NBTCompound nbt) {
        LockUtil.setUUID(nbt, "art_owner", owner.getUuid());
    }

    @Override
    public void removeFrom(NBTCompound nbt) {
        LockUtil.removeUUID(nbt, "art_owner");
    }

    @Override
    public LockTypeEnum getType() {
        return LockTypeEnum.OFFICER;
    }

    @Override
    public boolean isFriendlyLock(ArterionPlayer arterionPlayer) {
        return arterionPlayer.getGuild() != null && arterionPlayer.getGuild().equals(getOwner());
    }

    @Override
    public boolean isValid() {
        return owner != null;
    }

    public Guild getOwner() {
        return owner;
    }
}
