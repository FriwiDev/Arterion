package me.friwi.arterion.plugin.world.temporaryblock;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemporaryBlockCompound {
    private Map<Block, BlockState> backupStates = new HashMap<>();

    protected TemporaryBlockCompound() {

    }

    public boolean backupAndMark(Block b) {
        ArterionChunk ac = ArterionChunkUtil.getNonNull(b.getChunk());
        if (!ac.isTemporaryBlock(b)) {
            backupStates.put(b, b.getState());
            ac.setTemporaryBlock(b, true);
            return true;
        }
        return false;
    }

    public void rollback(Block b) {
        BlockState state = backupStates.remove(b);
        if (state != null) {
            state.update(true, false);
            if (b.getRelative(BlockFace.UP).getType() == Material.SNOW) {
                b.getRelative(BlockFace.UP).setType(Material.AIR, false);
            }
            ArterionChunk ac = ArterionChunkUtil.getNonNull(b.getChunk());
            ac.setTemporaryBlock(b, false);
        }
        if (isEmpty()) {
            ArterionPlugin.getInstance().getTemporaryBlockManager().removeCompound(this);
        }
    }

    public void rollbackAll() {
        for (Map.Entry<Block, BlockState> entry : backupStates.entrySet()) {
            ArterionChunk ac = ArterionChunkUtil.getNonNull(entry.getKey().getChunk());
            entry.getValue().update(true, false);
            ac.setTemporaryBlock(entry.getKey(), false);
            if (entry.getKey().getRelative(BlockFace.UP).getType() == Material.SNOW) {
                entry.getKey().getRelative(BlockFace.UP).setType(Material.AIR, false);
            }
        }
        backupStates.clear();
    }

    public boolean isEmpty() {
        return backupStates.isEmpty();
    }

    public void rollbackOne() {
        Map.Entry<Block, BlockState> first = null;
        for (Map.Entry<Block, BlockState> entry : backupStates.entrySet()) {
            first = entry;
            break;
        }
        if (first != null) {
            rollback(first.getKey());
        }
    }

    public Set<Block> getAllBlocks() {
        return backupStates.keySet();
    }
}
