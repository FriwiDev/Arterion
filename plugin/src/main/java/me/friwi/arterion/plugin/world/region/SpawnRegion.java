package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import org.bukkit.GameMode;
import org.bukkit.World;

public class SpawnRegion extends Region {

    public SpawnRegion(World world, int x1, int x2, int z1, int z2) {
        super("region.spawn", world, x1, x2, z1, z2, false, true, false, false, false, true);
    }

    @Override
    public boolean canPlayerBuild(ArterionPlayer p) {
        return p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public void greetMsg(ArterionPlayer player) {
        //never happens
    }

    @Override
    public void denyMsg(ArterionPlayer player) {
        TitleAPI.send(player, "", player.getTranslation(this.getRawName() + ".deny"));
    }

    @Override
    public boolean belongsToPlayer(ArterionPlayer player) {
        return false;
    }

    @Override
    public boolean administeredByPlayer(ArterionPlayer player) {
        return false;
    }

    @Override
    public void onEnter(ArterionPlayer player) {
        //Reset artefact if player somehow gets in
        if (player.isArtefactCarrier()) Artefact.reset();
    }

    @Override
    public void onLeave(ArterionPlayer player) {

    }
}
