package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import org.bukkit.GameMode;
import org.bukkit.World;

public class ArtefactRegion extends Region {

    public ArtefactRegion(World world, int x1, int x2, int z1, int z2) {
        super("region.artefact", world, x1, x2, z1, z2, true, false, false, false, false, true);
    }

    @Override
    public boolean canPlayerBuild(ArterionPlayer p) {
        return p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public void greetMsg(ArterionPlayer player) {
        TitleAPI.send(player, this.getName(player.getLanguage()), player.getTranslation(this.getRawName() + ".greet"));
    }

    @Override
    public void denyMsg(ArterionPlayer player) {
        //never heppens
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
        if (Artefact.getFight() != null) Artefact.getFight().enterRegion(player);
    }

    @Override
    public void onLeave(ArterionPlayer player) {
        if (Artefact.getFight() != null) Artefact.getFight().leaveRegion(player);
    }
}
