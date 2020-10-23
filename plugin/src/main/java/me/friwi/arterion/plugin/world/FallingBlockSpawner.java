package me.friwi.arterion.plugin.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

public interface FallingBlockSpawner {
    FallingBlock createFallingBlock(Block b, Material type, byte data);
}
