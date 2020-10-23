package me.friwi.arterion.plugin.anticheat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class UtilBlock {
    public static boolean evaluateOnlyLiquidSurroundingIgnoreAir(Block block, boolean diagonals) {
        if (diagonals) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if ((x != 0) || (y != 0) || (z != 0)) {
                            if (!block.getRelative(x, y, z).isLiquid()) {
                                return false;
                            }
                        }
                    }
                }
            }
        } else {
            if (!block.getRelative(BlockFace.UP).isLiquid()) {
                return false;
            }
            if (!block.getRelative(BlockFace.DOWN).isLiquid()) {
                return false;
            }
            if (!block.getRelative(BlockFace.NORTH).isLiquid()) {
                return false;
            }
            if (!block.getRelative(BlockFace.SOUTH).isLiquid()) {
                return false;
            }
            if (!block.getRelative(BlockFace.EAST).isLiquid()) {
                return false;
            }
            if (!block.getRelative(BlockFace.WEST).isLiquid()) {
                return false;
            }
        }
        return true;
    }

    public static boolean evaluateOnlyAirSurrounding(Block block, boolean diagonals) {
        if (diagonals) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if ((x != 0) || (y != 0) || (z != 0)) {
                            if (block.getRelative(x, y, z).getType() != Material.AIR) {
                                return false;
                            }
                        }
                    }
                }
            }
        } else {
            if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
                return false;
            }
            if (block.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                return false;
            }
            if (block.getRelative(BlockFace.NORTH).getType() != Material.AIR) {
                return false;
            }
            if (block.getRelative(BlockFace.SOUTH).getType() != Material.AIR) {
                return false;
            }
            if (block.getRelative(BlockFace.EAST).getType() != Material.AIR) {
                return false;
            }
            if (block.getRelative(BlockFace.WEST).getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    public static Block getBlockAbove(Player p) {
        return p.getLocation().getBlock().getRelative(BlockFace.UP, 2);
    }

    public static boolean onStairs(Player p) {
        Material m = p.getLocation().getBlock().getType();
        Material mu = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        if (m == Material.ACACIA_STAIRS || m == Material.WOOD_STAIRS || m == Material.COBBLESTONE_STAIRS || m == Material.BRICK_STAIRS || m == Material.SMOOTH_STAIRS
                || m == Material.NETHER_BRICK_STAIRS || m == Material.SANDSTONE_STAIRS || m == Material.SPRUCE_WOOD_STAIRS || m == Material.BIRCH_WOOD_STAIRS
                || m == Material.JUNGLE_WOOD_STAIRS || m == Material.QUARTZ_STAIRS || m == Material.DARK_OAK_STAIRS || m == Material.RED_SANDSTONE_STAIRS
                || mu == Material.ACACIA_STAIRS || mu == Material.WOOD_STAIRS || mu == Material.COBBLESTONE_STAIRS || mu == Material.BRICK_STAIRS || mu == Material.SMOOTH_STAIRS
                || mu == Material.NETHER_BRICK_STAIRS || mu == Material.SANDSTONE_STAIRS || mu == Material.SPRUCE_WOOD_STAIRS || mu == Material.BIRCH_WOOD_STAIRS
                || mu == Material.JUNGLE_WOOD_STAIRS || mu == Material.QUARTZ_STAIRS || mu == Material.DARK_OAK_STAIRS || mu == Material.RED_SANDSTONE_STAIRS) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean onBlock(Location loc) {
        double xMod = loc.getX() % 1.0D;
        if (loc.getX() < 0.0D) {
            xMod += 1.0D;
        }
        double zMod = loc.getZ() % 1.0D;
        if (loc.getZ() < 0.0D) {
            zMod += 1.0D;
        }
        int xMin = 0;
        int xMax = 0;
        int zMin = 0;
        int zMax = 0;

        if (xMod < 0.3D)
            xMin = -1;
        if (xMod > 0.7D) {
            xMax = 1;
        }
        if (zMod < 0.3D)
            zMin = -1;
        if (zMod > 0.7D) {
            zMax = 1;
        }

        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                if (loc.add(x, 0, z).getBlock().getType() == Material.WATER_LILY) {
                    return true;
                }
                if ((loc.add(x, -0.5D, z).getBlock().getType() != Material.AIR)
                        && (!loc.add(x, -0.5D, z).getBlock().isLiquid())) {
                    return true;
                }

                Material beneath = loc.add(x, -1.5D, z).getBlock().getType();
                if ((loc.getY() % 0.5D == 0.0D) && (beneath == Material.FENCE
                        || beneath == Material.FENCE_GATE
                        || beneath == Material.NETHER_FENCE
                        || beneath == Material.SPRUCE_FENCE_GATE
                        || beneath == Material.BIRCH_FENCE_GATE
                        || beneath == Material.JUNGLE_FENCE_GATE
                        || beneath == Material.DARK_OAK_FENCE_GATE
                        || beneath == Material.ACACIA_FENCE_GATE
                        || beneath == Material.SPRUCE_FENCE
                        || beneath == Material.BIRCH_FENCE
                        || beneath == Material.JUNGLE_FENCE
                        || beneath == Material.DARK_OAK_FENCE
                        || beneath == Material.ACACIA_FENCE
                        || beneath == Material.COBBLE_WALL)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean onBlock(Player arg0) {
        return onBlock(arg0.getLocation());
    }
}
