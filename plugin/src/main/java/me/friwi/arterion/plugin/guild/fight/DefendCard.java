package me.friwi.arterion.plugin.guild.fight;

import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;
import me.friwi.arterion.plugin.ui.title.TitleAPI;

public class DefendCard extends HotbarCard implements PriorityHotbarCard {
    private Guild other;
    private ArterionPlayer player;
    private boolean first = true;

    public DefendCard(ArterionPlayer player, Guild other) {
        super(4000);
        this.other = other;
        this.player = player;
    }

    @Override
    public String getMessage() {
        if (first) {
            TitleAPI.send(player, "", player.getTranslation("guild.defend.title", player.getLanguage().translateObject(other)), 0, (int) (duration / 50), 0);
            first = false;
        }
        return player.getTranslation("guild.defend.hotbar", player.getLanguage().translateObject(other));
    }
}
