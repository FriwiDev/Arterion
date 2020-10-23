package me.friwi.arterion.plugin.world.block;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;

public class DefaultBlock extends CustomBlock {
    public DefaultBlock(Block block) {
        super(CustomBlockType.NONE, block);
    }

    public DefaultBlock() {
        super(CustomBlockType.NONE);
    }

    @Override
    protected void parseBlock() {

    }

    @Override
    public void applyToBlock(Block block) {

    }

    @Override
    public boolean onInteract(ArterionPlayer player, Action action) {
        return true;
    }

    @Override
    public boolean onBreak(ArterionPlayer player) {
        return true;
    }
}
