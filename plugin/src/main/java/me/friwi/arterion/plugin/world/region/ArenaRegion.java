package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.combat.gamemode.arena.ArenaMaps;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.GameMode;
import org.bukkit.World;

public class ArenaRegion extends Region {

    public ArenaRegion(ArenaMaps map, World world, int x1, int x2, int z1, int z2) {
        super(map.getTranslationKey(), world, x1, x2, z1, z2, true, false, false, false, false, true);
    }

    @Override
    public boolean canPlayerBuild(ArterionPlayer p) {
        return p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public void greetMsg(ArterionPlayer player) {

    }

    @Override
    public void denyMsg(ArterionPlayer player) {

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

    }

    @Override
    public void onLeave(ArterionPlayer player) {

    }
}
