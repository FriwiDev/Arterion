package me.friwi.arterion.plugin.combat.quest;

import me.friwi.arterion.plugin.combat.quest.goal.DeliverItemQuestGoal;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.nio.ByteBuffer;
import java.util.List;

public class Quest {
    private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(1024);

    private int id;
    private String nameKey;
    private List<QuestGoal> goals;
    private List<QuestReward> rewards;

    public Quest(int id, String nameKey, List<QuestGoal> goals, List<QuestReward> rewards) {
        this.id = id;
        this.nameKey = nameKey;
        this.goals = goals;
        for (QuestGoal goal : goals) goal.setQuest(this);
        this.rewards = rewards;
    }

    public void checkFinished(ArterionPlayer p) {
        for (QuestGoal goal : goals) {
            if (!goal.isReached(p)) {
                p.sendTranslation("quest.nocomplete");
                String desc = goal.getDescription(p.getLanguage());
                if (desc != null) p.sendMessage(desc);
                return;
            }
        }
        for (QuestGoal goal : goals) {
            goal.consume(p);
        }
        p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
        p.sendTranslation("line");
        p.sendTranslation("quest.completed", p.getTranslation("quest.name." + nameKey));
        p.sendTranslation("quest.reward");
        for (QuestReward reward : rewards) {
            reward.give(p);
        }
        p.sendTranslation("line");
        p.setQuest(null, succ -> {
        });
    }

    public void print(ArterionPlayer p) {
        p.sendTranslation("line");
        p.sendTranslation("quest.contract", p.getTranslation("quest.name." + nameKey));
        int printed = 0;
        for (QuestGoal goal : goals) {
            String desc = goal.getDescription(p.getLanguage());
            if (desc != null) {
                p.sendMessage(desc);
                if (!(goal instanceof DeliverItemQuestGoal)) printed++;
            }
        }
        if (printed == 0) {
            p.sendTranslation("quest.finished");
        }
        p.sendTranslation("quest.reward");
        for (QuestReward reward : rewards) {
            p.sendMessage(reward.getDescription(p.getLanguage()));
        }
        p.sendTranslation("line");
    }

    public void readFrom(byte[] bytes) {
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        for (QuestGoal goal : goals) {
            goal.readFrom(buff);
        }
    }

    public byte[] write() {
        WRITE_BUFFER.position(0);
        for (QuestGoal goal : goals) {
            goal.writeTo(WRITE_BUFFER);
        }
        byte[] data = new byte[WRITE_BUFFER.position()];
        WRITE_BUFFER.position(0);
        WRITE_BUFFER.get(data);
        return data;
    }

    public int getId() {
        return id;
    }

    public void onKillEntity(ArterionPlayer p, LivingEntity entity) {
        for (QuestGoal goal : goals) {
            goal.onKillEntity(p, entity);
        }
    }

    public void onMineBlock(ArterionPlayer p, Block block) {
        for (QuestGoal goal : goals) {
            goal.onMineBlock(p, block);
        }
    }

    public void onEarnXP(ArterionPlayer p, int xp) {
        for (QuestGoal goal : goals) {
            goal.onEarnXP(p, xp);
        }
    }

    public void save(ArterionPlayer player) {
        player.setQuest(this, succ -> {
        });
    }
}
