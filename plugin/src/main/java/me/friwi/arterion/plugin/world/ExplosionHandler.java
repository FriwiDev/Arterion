package me.friwi.arterion.plugin.world;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.LockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.function.Consumer;

public class ExplosionHandler {
    private ArterionPlugin plugin;
    private List<FallingBlock> fallingBlocks = new LinkedList<>();
    private List<BlockState> toBeRestored = new LinkedList<>();
    private Map<Block, ItemStack[]> inventories = new HashMap<>();
    private Map<Block, String[]> signTexts = new HashMap<>();
    private Map<Block, Lock> lockMap = new HashMap<>();

    public ExplosionHandler(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleExplosion(Location loc, List<Block> blockList, FallingBlockSpawner blockSpawner, boolean restoreBlocks, boolean restoreAttachments) {
        this.handleExplosion(loc, blockList, blockSpawner, restoreBlocks, restoreAttachments, plugin.getFormulaManager().EXPLOSION_REGEN_DELAY.evaluateInt(), plugin.getFormulaManager().EXPLOSION_REGEN_SPEED.evaluateInt(), false);
    }

    public void handleExplosion(Location loc, List<Block> blockList, FallingBlockSpawner blockSpawner, boolean restoreBlocks, boolean restoreAttachments, int delay, int speed, boolean updateTwice) {
        this.handleExplosion(loc, blockList, blockSpawner, restoreBlocks, restoreAttachments, delay, speed, updateTwice, block -> {
            block.getWorld().playSound(block, Sound.DIG_GRASS, 0.5f, 1f);
        });
    }

    public void handleExplosion(Location loc, List<Block> blockList, FallingBlockSpawner blockSpawner, boolean restoreBlocks, boolean restoreAttachments, int delay, int speed, boolean updateTwice, Consumer<Location> restoreCallback) {
        //Remove temporary blocks from blockList
        Iterator<Block> it = blockList.iterator();
        while (it.hasNext()) {
            Block b = it.next();
            ArterionChunk chunk = ArterionChunkUtil.getNonNull(b.getChunk());

            if (chunk.isTemporaryBlock(b) || b.getType() == Material.IRON_TRAPDOOR || b.getType() == Material.IRON_DOOR_BLOCK) {
                it.remove();
            } else {
                boolean allow = Hooks.BLOCK_REMOVE_BY_EXPLOSION_HOOK.execute(b.getLocation(), true);
                if (!allow) it.remove();
            }
        }
        //Build an attachment list from our block list
        LinkedList<Block> attachmentsInValidOrder = new LinkedList<>();
        for (Block b : blockList) {
            fetchAttachments(b, attachmentsInValidOrder, 0, blockList);
        }
        //Randomize blockList for cooler effect
        Collections.shuffle(blockList);
        //Sort from bottom up, to get the block states into correct order
        blockList.sort(Comparator.comparingInt(Block::getY));
        //Backup block states
        LinkedList<BlockState> blockStates = new LinkedList<BlockState>();
        if (restoreBlocks) {
            for (Block b : blockList) {
                BlockState old = b.getState();
                if (old instanceof InventoryHolder) {
                    InventoryHolder holder = (InventoryHolder) old;
                    if (!inventories.containsKey(b)) {
                        if (holder.getInventory() instanceof DoubleChestInventory) {
                            //This chest will need to be split
                            DoubleChest dc = ((DoubleChestInventory) holder.getInventory()).getHolder();
                            Chest side = (Chest) dc.getLeftSide();
                            if (!side.getLocation().equals(b.getLocation())) side = (Chest) dc.getRightSide();
                            inventories.put(b, Arrays.copyOf(side.getBlockInventory().getContents(), side.getBlockInventory().getContents().length));
                            side.getBlockInventory().clear();
                        } else {
                            inventories.put(b, Arrays.copyOf(holder.getInventory().getContents(), holder.getInventory().getContents().length));
                            holder.getInventory().clear();
                        }
                        old.update();
                    }
                }
                if (old instanceof Sign) {
                    signTexts.put(b, ((Sign) old).getLines());
                }
                if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
                    Lock l = LockUtil.getLock((Chest) b.getState());
                    if (l != null) {
                        lockMap.put(b, l);
                    }
                }
                blockStates.add(old);
                toBeRestored.add(old);
            }
        }
        //Backup attachments and add them to the end in valid order (is also restore order)
        if (restoreAttachments) {
            for (Block b : attachmentsInValidOrder) {
                BlockState old = b.getState();
                if (old instanceof InventoryHolder) {
                    InventoryHolder holder = (InventoryHolder) old;
                    if (!inventories.containsKey(b)) {
                        inventories.put(b, Arrays.copyOf(holder.getInventory().getContents(), holder.getInventory().getContents().length));
                        holder.getInventory().clear();
                        old.update();
                    }
                }
                if (old instanceof Sign) {
                    signTexts.put(b, ((Sign) old).getLines());
                }
                if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
                    Lock l = LockUtil.getLock((Chest) b.getState());
                    if (l != null) {
                        lockMap.put(b, l);
                    }
                }
                blockStates.add(old);
                toBeRestored.add(old);
            }
        }
        List<Binding<Location>> denyFlow = new LinkedList<>();
        //Remove attachments first in reverse order
        it = attachmentsInValidOrder.descendingIterator();
        while (it.hasNext()) {
            Block b = it.next();
            b.setType(Material.AIR, false);
            denyFlow.add(Hooks.BLOCK_FLOW_TO_HOOK.subscribe(b.getLocation(), evt -> {
                evt.setCancelled(true);
                return null;
            }));
        }
        //Remove top layer first
        blockList.sort(Comparator.comparingInt(Block::getY).reversed());
        List<FallingBlock> createdFB = new LinkedList<>();
        for (Block b : blockList) {
            Material type = b.getType();
            byte data = b.getData();
            FallingBlock fb = blockSpawner.createFallingBlock(b, type, data);
            if (fb != null) {
                fallingBlocks.add(fb);
                createdFB.add(fb);
            }
            denyFlow.add(Hooks.BLOCK_FLOW_TO_HOOK.subscribe(b.getLocation(), evt -> {
                evt.setCancelled(true);
                return null;
            }));
        }
        //Remove dead falling blocks
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                for (FallingBlock fb : createdFB) if (!fb.isDead()) fb.remove();
                fallingBlocks.removeAll(createdFB);
            }
        }, 20 * 20);
        //Rebuild from bottom layer
        if (!blockStates.isEmpty()) {
            if (updateTwice) {
                LinkedList<BlockState> copy = ((LinkedList<BlockState>) blockStates.clone());
                rebuild(blockStates, delay, speed, false, restoreCallback, denyFlow);
                rebuild(copy, delay + 10, speed, true, block -> {
                }, denyFlow);
            } else {
                rebuild(blockStates, delay, speed, false, restoreCallback, denyFlow);
            }
        }
    }

    private void rebuild(Queue<BlockState> blockStates, int delay, int speed, boolean checkMaterial, Consumer<Location> restoreCallback, List<Binding<Location>> denyFlow) {
        plugin.getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                if (!blockStates.isEmpty()) {
                    BlockState bs = blockStates.peek();
                    ArterionChunk c = ArterionChunkUtil.getNonNull(bs.getChunk());
                    if (c.isTemporaryBlock(bs.getX(), bs.getY(), bs.getZ())) {
                        return;
                    }
                    blockStates.poll();
                    toBeRestored.remove(bs);
                    if (checkMaterial) {
                        bs.getLocation().getBlock().getState().update(true, false);
                    } else {
                        bs.update(true, false);
                        refill(bs);
                    }
                    restoreCallback.accept(bs.getLocation());
                } else {
                    cancel();
                    for (Binding<Location> binding : denyFlow) {
                        Hooks.BLOCK_FLOW_TO_HOOK.unsubscribe(binding);
                    }
                    denyFlow.clear();
                }
            }
        }, delay, speed);
    }

    private void refill(BlockState bs) {
        if (bs instanceof InventoryHolder) {
            ItemStack[] stacks = inventories.remove(bs.getBlock());
            BlockState ns = bs.getWorld().getBlockAt(bs.getLocation()).getState();
            if (stacks != null) {
                if (((InventoryHolder) ns).getInventory() instanceof DoubleChestInventory) {
                    //This chest will need to be split
                    DoubleChest dc = ((DoubleChestInventory) ((InventoryHolder) ns).getInventory()).getHolder();
                    Chest side = (Chest) dc.getLeftSide();
                    if (!side.getLocation().equals(bs.getLocation())) side = (Chest) dc.getRightSide();
                    side.getBlockInventory().setContents(stacks);
                    side.update(true, false);
                } else {
                    ((InventoryHolder) ns).getInventory().setContents(stacks);
                    ns.update(true, false);
                }
            }
        }
        if (bs instanceof Sign) {
            Sign ns = (Sign) bs.getWorld().getBlockAt(bs.getLocation()).getState();
            String[] lines = signTexts.remove(bs.getBlock());
            if (lines != null) {
                for (int i = 0; i < lines.length; i++) {
                    ns.setLine(i, lines[i]);
                }
                ns.update(true, false);
            }
        }
        Lock l = lockMap.remove(bs.getWorld().getBlockAt(bs.getLocation()));
        if (l != null) {
            Block b = bs.getWorld().getBlockAt(bs.getLocation());
            if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
                Lock l1 = LockUtil.getLock((Chest) b.getState());
                if (l1 == null) {
                    LockUtil.addLock(null, (Chest) b.getState(), l); //Multiple locks can not be added anyways
                }
            }
        }
    }

    private void fetchAttachments(Block b, List<Block> attachmentsInValidOrder, int depth, List<Block> ignore) {
        if (depth > 5) return;
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN}) {
            Block rel = b.getRelative(face);
            if (rel != null) {
                MaterialData state = rel.getState().getData();
                boolean allow = Hooks.BLOCK_REMOVE_BY_EXPLOSION_HOOK.execute(rel.getLocation(), true);
                if (!allow) continue;
                if (state instanceof Attachable && rel.getType() != Material.IRON_TRAPDOOR && rel.getType() != Material.IRON_DOOR_BLOCK) {
                    if (ArterionChunkUtil.getNonNull(rel.getChunk()).isTemporaryBlock(rel) || ignore.contains(rel))
                        continue;
                    if (((Attachable) state).getAttachedFace().getOppositeFace() == face) {
                        //Rel is attached to b
                        attachmentsInValidOrder.add(rel);
                        //Recursive next attachment
                        fetchAttachments(rel, attachmentsInValidOrder, depth + 1, ignore);
                    }
                } else if (face == BlockFace.UP && rel.getType() != Material.AIR
                        && (!rel.getType().isSolid() || rel.getType().hasGravity())
                        && rel.getType() != Material.IRON_DOOR_BLOCK
                        && rel.getType() != Material.IRON_TRAPDOOR) {
                    if (ArterionChunkUtil.getNonNull(rel.getChunk()).isTemporaryBlock(rel) || ignore.contains(rel))
                        continue;
                    //Rel is attached to b
                    attachmentsInValidOrder.add(rel);
                    //Recursive next attachment
                    fetchAttachments(rel, attachmentsInValidOrder, depth + 1, ignore);
                }
            }
        }
    }

    public boolean handleFallingBlockImpact(FallingBlock block) {
        if (fallingBlocks.contains(block)) {
            block.remove();
            ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(block.getMaterial(), block.getBlockData()), 0, 0, 0, 1, 20, block.getLocation(), 30);
            block.getWorld().playSound(block.getLocation(), Sound.DIG_GRASS, 1f, 1f);
            return true;
        } else {
            return false;
        }
    }

    public void onServerShutdown() {
        //Regenerate stuff and despawn falling blocks
        for (FallingBlock b : fallingBlocks) b.remove();
        for (BlockState bs : toBeRestored) {
            bs.update(true, false);
            if (bs instanceof InventoryHolder) {
                ItemStack[] stacks = inventories.remove(bs.getBlock());
                BlockState ns = bs.getWorld().getBlockAt(bs.getLocation()).getState();
                refill(ns);
            }
        }
    }

    public List<FallingBlock> getFallingBlocks() {
        return fallingBlocks;
    }
}
