package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;

public class HotbarTitleMessageCard extends HotbarMessageCard {
    private boolean first = true;
    private ArterionPlayer player;

    public HotbarTitleMessageCard(long duration, ArterionPlayer player, String translation, Object... values) {
        super(duration, player, translation, values);
        this.player = player;
    }

    @Override
    public String getMessage() {
        if (first) {
            TitleAPI.send(player, "", super.getMessage(), 0, (int) (duration / 50), 0);
            first = false;
        }
        return super.getMessage();
    }
}
