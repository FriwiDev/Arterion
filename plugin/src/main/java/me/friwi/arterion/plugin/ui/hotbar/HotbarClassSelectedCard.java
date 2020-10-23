package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import org.bukkit.Sound;

public class HotbarClassSelectedCard extends HotbarCard {
    private ArterionPlayer player;
    private ClassEnum clasz;
    private boolean first = true;

    public HotbarClassSelectedCard(ArterionPlayer player, ClassEnum clasz) {
        super(4000);
        this.player = player;
        this.clasz = clasz;
    }

    @Override
    public String getMessage() {
        if (first) {
            first = false;
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
            TitleAPI.send(player, "", player.getTranslation("hotbar.classselect.title", player.getTranslation("class." + clasz.name().toLowerCase())), 0, (int) duration / 50, 0);
        }
        return player.getTranslation("hotbar.classselect", player.getTranslation("class." + clasz.name().toLowerCase()));
    }
}
