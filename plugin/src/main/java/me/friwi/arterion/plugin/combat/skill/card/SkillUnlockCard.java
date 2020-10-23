package me.friwi.arterion.plugin.combat.skill.card;

import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import org.bukkit.Sound;

public class SkillUnlockCard extends HotbarCard {
    private Skill skill;
    private ArterionPlayer player;
    private boolean first = true;

    public SkillUnlockCard(ArterionPlayer player, Skill skill) {
        super(4000);
        this.skill = skill;
        this.player = player;
    }

    @Override
    public String getMessage() {
        if (first) {
            this.player.getBukkitPlayer().playSound(this.player.getBukkitPlayer().getLocation(), Sound.BLAZE_HIT, 1, 1);
            TitleAPI.send(player, "", player.getTranslation("skill.unlocked.title", skill.getName(player)), 0, (int) (duration / 50), 0);
            first = false;
        }
        return player.getTranslation("skill.unlocked.hotbar", skill.getName(player));
    }
}
