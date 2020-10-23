package me.friwi.arterion.plugin.combat.quest;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.nio.ByteBuffer;

public abstract class QuestGoal {
    private Quest quest;

    public abstract boolean isReached(ArterionPlayer p);

    public abstract void consume(ArterionPlayer p);

    public abstract String getDescription(Language lang);

    public abstract void readFrom(ByteBuffer buffer);

    public abstract void writeTo(ByteBuffer buffer);

    public abstract void onKillEntity(ArterionPlayer p, LivingEntity entity);

    public abstract void onMineBlock(ArterionPlayer p, Block block);

    public abstract void onEarnXP(ArterionPlayer p, int xp);

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }
}
