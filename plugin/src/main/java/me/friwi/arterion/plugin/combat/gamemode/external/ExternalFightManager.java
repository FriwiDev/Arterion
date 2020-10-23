package me.friwi.arterion.plugin.combat.gamemode.external;

import me.friwi.arterion.plugin.player.ArterionPlayer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ExternalFightManager {
    private List<ExternalFight> fights = new LinkedList<>();

    public void registerFight(ExternalFight fight) {
        fights.add(fight);
    }

    public void removeFight(ExternalFight fight) {
        fights.remove(fight);
    }

    public void onShutdown() {
        Iterator<ExternalFight> it = fights.iterator();
        while (it.hasNext()) {
            ExternalFight f = it.next();
            f.endFight(true, false);
            it.remove();
        }
    }

    public ExternalFight getFightByPlayer(ArterionPlayer player) {
        for (ExternalFight fight : fights) {
            if (fight.isParticipating(player)) return fight;
        }
        return null;
    }
}
