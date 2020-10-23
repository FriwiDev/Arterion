package me.friwi.arterion.plugin.combat.skill.impl.forestrunner;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.impl.barbar.StompSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.recordable.FallingBlockDropUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.LinkedList;

public class ThrowNetSkill extends RestrictedActiveSkill<SkillContainerData> {
    public ThrowNetSkill() {
        super(ClassEnum.FORESTRUNNER, SkillSlotEnum.ACTIVE3);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double throwForce = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_THROW_NET_FORCE.evaluateDouble(p);
        double throwAngle = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_THROW_NET_ANGLE.evaluateDouble(p);
        this.printCastMessage(p, null);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.SPIDER_IDLE, 0.75f, 1);

        FallingBlock[][] grid = new FallingBlock[5][];
        for (int x = 0; x < grid.length; x++) {
            grid[x] = new FallingBlock[grid.length];
            for (int y = 0; y < grid.length; y++) {
                FallingBlock block = p.getBukkitPlayer().getWorld().spawnFallingBlock(p.getBukkitPlayer().getEyeLocation(), Material.WEB, (byte) 0);
                block.setDropItem(false);
                block.setHurtEntities(false);
                Vector direction = p.getBukkitPlayer().getLocation().getDirection().clone();
                direction.multiply(throwForce);
                block.setVelocity(direction);
                grid[x][y] = block;
                Binding<FallingBlock> binding[] = new Binding[1];
                int finalX = x;
                int finalY = y;
                Runnable onHit = () -> {
                    if (finalX == 2 && finalY == 2) {
                        //Spawn throw net
                        Location loc = block.getLocation().clone();
                        if (loc.getBlock().getType().isSolid()) loc.add(0, 1, 0);
                        Block b = loc.getBlock();
                        //Attempt to fix block offset when thrown down wall
                        if (!b.getRelative(BlockFace.DOWN).getType().isSolid()) {
                            outer:
                            for (int x1 = -1; x1 <= 1; x1++) {
                                for (int z1 = -1; z1 <= 1; z1++) {
                                    Block rel = b.getLocation().clone().add(x1, 0, z1).getBlock();
                                    Block rela = rel.getRelative(BlockFace.UP);
                                    Block relb = rel.getRelative(BlockFace.DOWN);
                                    if (rela.getType().isSolid()) {
                                        b = rela.getRelative(BlockFace.UP);
                                        break outer;
                                    }
                                    if (rel.getType().isSolid()) {
                                        b = rela;
                                        break outer;
                                    }
                                    if (relb.getType().isSolid()) {
                                        b = rel;
                                        break outer;
                                    }
                                }
                            }
                        }
                        createThrowNet(b, p);
                        for (int x1 = 0; x1 < grid.length; x1++) {
                            for (int y1 = 0; y1 < grid.length; y1++) {
                                if (!grid[x1][y1].isDead()) grid[x1][y1].remove();
                            }
                        }
                    }
                };
                binding[0] = Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.subscribe(block, evt -> {
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                        @Override
                        public void run() {
                            Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.unsubscribe(binding[0]);
                        }
                    }, 1);
                    evt.setCancelled(true);
                    block.remove();
                    onHit.run();
                    return evt;
                });
                FallingBlockDropUtil.setOnDropItem(block, () -> {
                    Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.unsubscribe(binding[0]);
                    onHit.run();
                });
            }
        }

        int tickInterval = 1;
        int toleranceTicks = (int) (10 * 20);

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;
            double distance = 0;

            @Override
            public void run() {
                if (grid[2][2].isDead()) {
                    cancel();
                    return;
                }
                distance += throwAngle;
                if (distance >= 1) distance = 1;
                for (int x = 0; x < grid.length; x++) {
                    for (int y = 0; y < grid.length; y++) {
                        if (!grid[x][y].isDead()) {
                            if (x != 2 || y != 2) {
                                Vector ydiff;
                                if (grid[2][2].getVelocity().getX() != 0 || grid[2][2].getVelocity().getZ() != 0) {
                                    ydiff = grid[2][2].getVelocity().clone().crossProduct(new Vector(0, 1, 0)).normalize();
                                } else {
                                    ydiff = new Vector(1, 0, 0);
                                }
                                Vector xdiff = grid[2][2].getVelocity().clone().crossProduct(ydiff).normalize();
                                Location wantedLoc = grid[2][2].getLocation().clone();
                                wantedLoc.add(xdiff.clone().multiply((x - 2) * distance));
                                wantedLoc.add(ydiff.clone().multiply((y - 2) * distance));
                                Vector teleportVector = wantedLoc.toVector().subtract(grid[x][y].getLocation().toVector()).normalize();
                                Vector direction = grid[2][2].getVelocity().clone().normalize().add(teleportVector.multiply(0.2));
                                direction.multiply(grid[2][2].getVelocity().length());
                                grid[x][y].setVelocity(direction);
                            }
                        }
                    }
                }
                if (tick >= toleranceTicks) cancel();
                tick += tickInterval;
            }
        }, 0, tickInterval);
        return true;
    }

    private void createThrowNet(Block block, ArterionPlayer p) {
        int regenDelay = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_THROW_NET_REGEN_DELAY.evaluateInt(p) / 50;
        int regenSpeed = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_THROW_NET_REGEN_SPEED.evaluateInt(p) / 50;

        if (regenDelay < 1) regenDelay = 1;
        if (regenSpeed < 1) regenSpeed = 1;

        this.getSkillDataContainer(p).setActiveUntil(System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_THROW_NET_REGEN_DELAY.evaluateInt(p));
        this.updateToMod(p);

        LinkedList<Block> topMost = StompSkill.getTopmostBlocks(block.getLocation(), 5, 3, false);
        LinkedList<Block> qualifying = new LinkedList<>();
        ArterionChunk c = ArterionChunkUtil.getNonNull(block.getChunk());
        if (!c.isTemporaryBlock(block)) qualifying.add(block);
        for (Block b : topMost) {
            if (Math.abs(b.getX() - block.getX()) <= 2 && Math.abs(b.getZ() - block.getZ()) <= 2 && b.getY() + 1 < b.getWorld().getMaxHeight()) {
                Block rel = b.getRelative(BlockFace.UP);
                c = ArterionChunkUtil.getNonNull(rel.getChunk());
                if (c.isTemporaryBlock(rel)) continue;
                boolean cactus = false;
                for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                    if (rel.getRelative(face).getType() == Material.CACTUS) {
                        cactus = true;
                        break;
                    }
                }
                if (!cactus) qualifying.add(rel);
            }
        }
        ArterionPlugin.getInstance().getExplosionHandler().handleExplosion(block.getLocation(), qualifying, (b, type, data) -> {
            b.setType(Material.WEB);
            Hooks.BLOCK_BREAK_EVENT_HOOK.subscribe(b.getLocation(), evt -> {
                evt.setCancelled(true); //Cancel item drop
                if (b.getType() != Material.WEB) return evt;
                b.setType(Material.AIR, false); //Will be restored later
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        Hooks.BLOCK_BREAK_EVENT_HOOK.unsubscribeAll(b.getLocation());
                        Hooks.BLOCK_REMOVE_BY_EXPLOSION_HOOK.unsubscribeAll(b.getLocation());
                    }
                }, 1);
                return null; //We handled this event, do not touch it anymore!
            });
            Hooks.BLOCK_REMOVE_BY_EXPLOSION_HOOK.subscribe(b.getLocation(), bool -> false);
            return null;
        }, true, true, regenDelay, regenSpeed, true, restoreLocation -> {
            Hooks.BLOCK_BREAK_EVENT_HOOK.unsubscribeAll(restoreLocation);
            Hooks.BLOCK_REMOVE_BY_EXPLOSION_HOOK.unsubscribeAll(restoreLocation);
            restoreLocation.getWorld().playSound(restoreLocation, Sound.DIG_SNOW, 0.5f, 1f);
        });
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_THROW_NET_REGEN_DELAY.evaluateDouble(p) / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        setSkillDataContainer(p, new SkillContainerData());
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        setSkillDataContainer(p, null);
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return p.getRegion() != null && p.getRegion().isPvp();
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return 0;
    }
}
