package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import org.bukkit.GameMode;
import org.bukkit.World;

public class BankRegion extends Region {

    public BankRegion(World world, int x1, int x2, int z1, int z2) {
        super("region.bank", world, x1, x2, z1, z2, false, true, false, false, false, true);
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
        //Hopefully never happens
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
        return;
    }

    @Override
    public void onLeave(ArterionPlayer player) {
        return;
    }
}
