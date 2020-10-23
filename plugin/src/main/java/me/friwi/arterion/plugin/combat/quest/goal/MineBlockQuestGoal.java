package me.friwi.arterion.plugin.combat.quest.goal;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.quest.QuestGoal;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.Crops;

import java.nio.ByteBuffer;

public class MineBlockQuestGoal extends QuestGoal {
    private int left;
    private Material[] validTypes;

    public MineBlockQuestGoal(int left, Material... validTypes) {
        this.left = left;
        this.validTypes = validTypes;
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
        String entityList = "";
        for (int i = 0; i < validTypes.length; i++) {
            entityList += "\247a" + lang.translateObject(validTypes[i]);
            if (i < validTypes.length - 2) {
                entityList += "\2477, ";
            } else if (i == validTypes.length - 2) {
                entityList += "\2477 " + lang.getTranslation("or").translate().getMessage() + " ";
            }
        }
        return lang.getTranslation("quest.goal.mineblocks").translate(left, entityList).getMessage();
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
        if (left <= 0) return;
        BlockState state = block.getState();
        if (state instanceof Crops) {
            if (!((Crops) state).getState().equals(CropState.RIPE)) return;
        } else {
            if (ArterionPlugin.getInstance().getUserPlacedBlockManager().isPlacedByUser(block)) return;
        }
        for (Material check : validTypes) {
            if (block.getType() == check) {
                left--;
                getQuest().save(p);
                return;
            }
        }
    }

    @Override
    public void onEarnXP(ArterionPlayer p, int xp) {

    }
}
