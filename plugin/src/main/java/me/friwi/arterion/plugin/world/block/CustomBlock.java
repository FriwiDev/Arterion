package me.friwi.arterion.plugin.world.block;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;

public abstract class CustomBlock {
    protected Block block;
    private CustomBlockType type;

    public CustomBlock(CustomBlockType type, Block block) {
        this.type = type;
        this.block = block;
        this.parseBlock();
    }

    public CustomBlock(CustomBlockType type) {
        this.type = type;
    }

    public CustomBlockType getType() {
        return type;
    }

    protected abstract void parseBlock();

    public abstract void applyToBlock(Block block);

    public abstract boolean onInteract(ArterionPlayer player, Action action);

    public abstract boolean onBreak(ArterionPlayer player);
}
