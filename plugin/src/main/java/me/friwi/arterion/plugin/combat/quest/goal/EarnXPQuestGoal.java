package me.friwi.arterion.plugin.combat.quest.goal;

import me.friwi.arterion.plugin.combat.quest.QuestGoal;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.nio.ByteBuffer;

public class EarnXPQuestGoal extends QuestGoal {
    private int left;

    public EarnXPQuestGoal(int left) {
        this.left = left;
    }

    @Override
    public boolean isReached(ArterionPlayer p) {
        return left <= 0;
    }

    @Override
    public void consume(ArterionPlayer p) {

    }

    @Override
    public String getDescription(Language lang) {
        if (left <= 0) return null;
        return lang.getTranslation("quest.goal.earnxp").translate(left).getMessage();
    }

    @Override
    public void readFrom(ByteBuffer buffer) {
        left = buffer.getInt();
    }

    @Override
    public void writeTo(ByteBuffer buffer) {
        buffer.putInt(left);
    }

    @Override
    public void onKillEntity(ArterionPlayer p, LivingEntity entity) {

    }

    @Override
    public void onMineBlock(ArterionPlayer p, Block block) {

    }

    @Override
    public void onEarnXP(ArterionPlayer p, int xp) {
        if (left <= 0) return;
        left -= xp;
        if (left < 0) left = 0;
        getQuest().save(p);
    }
}
