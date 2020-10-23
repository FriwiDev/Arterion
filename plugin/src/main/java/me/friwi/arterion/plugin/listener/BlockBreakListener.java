package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.jobs.JobActivityHandler;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.block.CustomBlock;
import me.friwi.arterion.plugin.world.block.CustomBlockUtil;
import me.friwi.arterion.plugin.world.block.nonbtblocks.SpecialBlock;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.LockUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.*;

public class BlockBreakListener implements Listener {
    private ArterionPlugin plugin;

    public BlockBreakListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public static int blockToStatData(Block b) {
        int id = b.getTypeId();
        int data = 0;
        BlockState state = b.getState();
        if (b.getType() == Material.STAINED_CLAY || b.getType() == Material.STAINED_GLASS || b.getType() == Material.STAINED_GLASS_PANE) {
            data = b.getData();
        } else if (b.getType() == Material.LEAVES || b.getType() == Material.LOG) {
            data = b.getData() & 0x03;
        } else if (b.getType() == Material.LEAVES_2 || b.getType() == Material.LOG_2) {
            data = b.getData() & 0x01;
        } else if (state instanceof Leaves || state instanceof Coal || state instanceof Sandstone || state instanceof SmoothBrick || state instanceof Wool) {
            data = state.getRawData();
        } else if (state instanceof Door) {
            ((Door) state).setOpen(false);
            ((Door) state).setTopHalf(false);
            ((Door) state).setFacingDirection(BlockFace.WEST);
            ((Door) state).setHinge(false);
            data = state.getRawData();
        } else if (state instanceof Gate) {
            ((Gate) state).setFacingDirection(BlockFace.SOUTH);
            ((Gate) state).setOpen(false);
            data = state.getRawData();
        } else if (state instanceof Step) {
            ((Step) state).setInverted(false);
            data = state.getRawData();
        } else if (state instanceof Tree) {
            ((Tree) state).setDirection(BlockFace.UP);
            data = state.getRawData();
        } else if (state instanceof WoodenStep) {
            ((WoodenStep) state).setInverted(false);
            data = state.getRawData();
        }

        if (b.getType() == Material.GLOWING_REDSTONE_ORE) {
            id = Material.REDSTONE_ORE.getId();
        } else if (b.getType() == Material.BURNING_FURNACE) {
            id = Material.FURNACE.getId();
        } else if (b.getType() == Material.DAYLIGHT_DETECTOR_INVERTED) {
            id = Material.DAYLIGHT_DETECTOR.getId();
        } else if (b.getType() == Material.WALL_SIGN) {
            id = Material.SIGN_POST.getId();
        } else if (b.getType() == Material.PISTON_EXTENSION) {
            id = Material.PISTON_BASE.getId();
        } else if (b.getType() == Material.PISTON_MOVING_PIECE) {
            id = Material.PISTON_BASE.getId();
        } else if (b.getType() == Material.REDSTONE_COMPARATOR_ON) {
            id = Material.REDSTONE_COMPARATOR_OFF.getId();
        } else if (b.getType() == Material.REDSTONE_LAMP_ON) {
            id = Material.REDSTONE_LAMP_OFF.getId();
        } else if (b.getType() == Material.DIODE_BLOCK_ON) {
            id = Material.DIODE_BLOCK_OFF.getId();
        } else if (b.getType() == Material.REDSTONE_TORCH_ON) {
            id = Material.REDSTONE_TORCH_OFF.getId();
        } else if (b.getType() == Material.WALL_BANNER) {
            id = Material.STANDING_BANNER.getId();
        } else if (b.getType() == Material.REDSTONE) {
            data = 0;
        }
        return id << 8 | data;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent evt) {
        Block b = evt.getBlock();
        BlockState bs = b.getState();

        //Hook
        evt = Hooks.BLOCK_BREAK_EVENT_HOOK.execute(b.getLocation(), evt);
        if (evt == null) return; //Event was priority handled by hook

        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());

        //Allow all creative mode interacts
        if (evt.getPlayer().getGameMode() == GameMode.CREATIVE) {
            //Stats
            ep.trackStatistic(StatType.DESTROYED_BLOCKS, blockToStatData(b), v -> v + 1);
            return;
        }

        ArterionChunk chunk = ArterionChunkUtil.getNonNull(evt.getBlock().getChunk());

        if (chunk.isTemporaryBlock(evt.getBlock())) {
            evt.setCancelled(true);
            return;
        }

        ArterionChunk ec = ArterionChunkUtil.getNonNull(b.getChunk());
        if (ec.getRegion() != null && ep != null) {
            if (ec.getRegion().canPlayerDestroy(ep, evt.getBlock())) {
                //Check further permissions
                //Blaze spawner not destroyable
                if (b.getType() == Material.MOB_SPAWNER && b.getWorld().getEnvironment() == World.Environment.NETHER) {
                    evt.setCancelled(true);
                    ep.sendTranslation("blazespawner.nodestroy");
                    return;
                }
                //Custom blocks
                CustomBlock block = CustomBlockUtil.getCustomBlock(evt.getBlock());
                if (!block.onBreak(ArterionPlayerUtil.get(evt.getPlayer()))) {
                    evt.setCancelled(true);
                    return;
                }
                if (evt.getBlock().getType() == Material.ENDER_PORTAL_FRAME) {
                    //Special blocks
                    SpecialBlock block1 = plugin.getSpecialBlockManager().get(evt.getBlock());
                    if (block1 != null && !block1.onBreak(ArterionPlayerUtil.get(evt.getPlayer()))) {
                        evt.setCancelled(true);
                        return;
                    }
                }
                //Locks
                if (bs instanceof Chest) {
                    Lock l = LockUtil.getLock((Chest) bs);
                    if (l != null) {
                        if (!l.canDestroy(ep, (Chest) bs)) {
                            l.sendDeny(ep);
                            evt.setCancelled(true);
                            return;
                        }
                    }
                }

                //Jobs
                JobActivityHandler.onBreakBlock(b, ep);

                //Stats
                ep.trackStatistic(StatType.DESTROYED_BLOCKS, blockToStatData(b), v -> v + 1);

                //Quests
                if (ep.getQuest() != null) {
                    ep.getQuest().onMineBlock(ep, evt.getBlock());
                }
                return;
            }
        }
        evt.setCancelled(true);
        if (ep == null) {
            evt.getPlayer().sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("region.nobuild").translate().getMessage());
        } else {
            ep.sendTranslation("region.nobuild");
        }
    }
}
