package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.player.ArterionPlayer;

public class HotbarMessageCard extends HotbarCard {
    private String message;

    public HotbarMessageCard(long duration, ArterionPlayer player, String translation, Object... values) {
        this(duration, player.getTranslation(translation, values));
    }

    public HotbarMessageCard(long duration, String message) {
        super(duration);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
