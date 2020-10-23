package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.player.ArterionPlayer;

public class InstantHotbarMessageCard extends HotbarMessageCard implements PriorityHotbarCard {
    public InstantHotbarMessageCard(long duration, ArterionPlayer player, String translation, Object... values) {
        super(duration, player, translation, values);
    }

    public InstantHotbarMessageCard(long duration, String message) {
        super(duration, message);
    }
}
