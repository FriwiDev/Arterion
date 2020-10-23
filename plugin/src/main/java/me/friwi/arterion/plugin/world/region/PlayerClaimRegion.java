package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.GameMode;
import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;

public class PlayerClaimRegion extends Region {
    private String playerName;
    private UUID playerUUID;

    public PlayerClaimRegion(String playerName, UUID playerUUID, World world, int x, int z) {
        super("region.private", world, x, x, z, z, true, false, false, true, true, false);
        this.playerName = playerName;
        this.playerUUID = playerUUID;
    }

    @Override
    public boolean canPlayerBuild(ArterionPlayer p) {
        return p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE
                || p.getBukkitPlayer().getUniqueId().equals(playerUUID)
                || (p.getRoomMate() != null && p.getRoomMate().equals(playerUUID));
    }

    @Override
    public String getName(Language lang) {
        return lang.getTranslation(this.getRawName()).translate(playerName).getMessage();
    }

    @Override
    public void greetMsg(ArterionPlayer player) {
        TitleAPI.send(player, this.getName(player.getLanguage()), player.getTranslation(this.getRawName() + ".greet"));
    }

    @Override
    public void denyMsg(ArterionPlayer player) {
        //never heppens
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public boolean belongsToPlayer(ArterionPlayer player) {
        return player.getBukkitPlayer().getUniqueId().equals(playerUUID) || player.getRoomMate().equals(playerUUID);
    }

    @Override
    public boolean administeredByPlayer(ArterionPlayer player) {
        return player.getBukkitPlayer().getUniqueId().equals(playerUUID);
    }

    @Override
    public void onEnter(ArterionPlayer player) {

    }

    @Override
    public void onLeave(ArterionPlayer player) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerClaimRegion)) return false;
        PlayerClaimRegion region = (PlayerClaimRegion) o;
        return region.getPlayerUUID().equals(getPlayerUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPlayerUUID());
    }
}
