package me.friwi.arterion.plugin.world.markedblock.userplacedblocks;

import me.friwi.arterion.plugin.world.markedblock.MarkedBlockManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

public class UserPlacedBlockManager extends MarkedBlockManager implements Listener {

    public UserPlacedBlockManager(Plugin plugin) {
        super("userPlacedBlocks");
        startSaveAndGcThread();
    }

    public void startSaveAndGcThread() {
        new Thread() {
            @Override
            public void run() {
                setName("UPB_Thread");
                while (true) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //long begin = System.currentTimeMillis();
                    UserPlacedBlockManager.this.gc(60000 * 5);
                    //long end = System.currentTimeMillis();
                    //System.out.println("UPB GC Finished in " + (end - begin) + "ms");
                }
            }
        }.start();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPlaceEvent(BlockPlaceEvent evt) {
        if (!evt.isCancelled()) {
            this.setBlock(evt.getBlock(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPistonExtendEvent(BlockPistonExtendEvent evt) {
        if (!evt.isCancelled()) {
            for (Block b : evt.getBlocks()) {
                this.setBlock(b.getRelative(evt.getDirection()), true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPistonRetractEvent(BlockPistonRetractEvent evt) {
        if (!evt.isCancelled()) {
            for (Block b : evt.getBlocks()) {
                this.setBlock(b.getRelative(evt.getDirection()), true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockBreakEvent(BlockBreakEvent evt) {
        if (!evt.isCancelled()) {
            this.setBlock(evt.getBlock(), false);
        }
    }

    public boolean isPlacedByUser(Block b) {
        return super.isBlockMarked(b);
    }
}
