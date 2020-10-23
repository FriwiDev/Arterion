package me.friwi.arterion.plugin.combat.quest;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;

public abstract class QuestReward {
    public abstract void give(ArterionPlayer p);

    public abstract String getDescription(Language lang);
}
