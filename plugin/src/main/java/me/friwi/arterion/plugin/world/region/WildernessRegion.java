package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;

public class WildernessRegion extends Region {
    public WildernessRegion() {
        super("region.wilderness", null, -100000, 100000, -100000, 100000, true, false, true, true, true, false);
    }

    @Override
    public boolean canPlayerBuild(ArterionPlayer p) {
        return true;
    }

    @Override
    public void greetMsg(ArterionPlayer player) {
        TitleAPI.send(player, this.getName(player.getLanguage()), player.getTranslation(this.getRawName() + ".greet"));
    }

    @Override
    public void denyMsg(ArterionPlayer player) {
        //never happens
    }

    @Override
    public boolean belongsToPlayer(ArterionPlayer player) {
        return true;
    }

    @Override
    public boolean administeredByPlayer(ArterionPlayer player) {
        return true;
    }

    @Override
    public void onEnter(ArterionPlayer player) {

    }

    @Override
    public void onLeave(ArterionPlayer player) {

    }
}
