package me.friwi.arterion.plugin.combat.skill.impl.barbar;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;

public class StompSkill extends RestrictedActiveSkill<SkillContainerData> {
    private double G_FORCE = 45;

    public StompSkill() {
        super(ClassEnum.BARBAR, SkillSlotEnum.ACTIVE4);
    }

    public static LinkedList<Block> getTopmostBlocks(Location loc, int maxrecursion, int jumps, boolean isStomp) {
        int miny = Math.max(loc.getBlockY() - 4, 1);
        while (loc.getBlockY() >= miny) {
            loc.add(0, -1, 0);
            Block b = loc.getBlock();
            if (b.getType().isSolid()) return getTopmostBlocks(b, 0, maxrecursion, jumps, isStomp);
        }
        return new LinkedList<>();
    }

    public static LinkedList<Block> getTopmostBlocks(Block b, int recursion, int maxrecursion, int jumps, boolean isStomp) {
        if (b.getY() <= 1 || (b.getType() == Material.BARRIER && isStomp))
            return new LinkedList<>();
        LinkedList<Block> build = new LinkedList<>();
        if (!build.contains(b)) build.add(b);
        if (recursion + 1 != maxrecursion) {
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH}) {
                Block[] rel = new Block[2 + jumps * 2];
                rel[0] = b.getRelative(face).getRelative(BlockFace.DOWN, jumps);
                for (int i = 1; i < rel.length; i++) {
                    rel[i] = rel[i - 1].getRelative(BlockFace.UP);
                }
                boolean found = false;
                for (int i = jumps + 1; i > 0; i--) {
                    //Above non-solid, self solid
                    if (!rel[i + 1].getType().isSolid() && rel[i].getType().isSolid()) {
                        for (Block b1 : getTopmostBlocks(rel[i], recursion + 1, maxrecursion, jumps, isStomp)) {
                            if (!build.contains(b1)) build.add(b1);
                        }
                        found = true;
                        break;
                    }
                    //Do not continue when found an obstacle
                    if (rel[i].getType().isSolid()) break;
                }
                if (!found && jumps >= 2) {
                    for (int i = jumps + 2; i < rel.length - 2; i++) {
                        //Above non-solid, self solid
                        if (!rel[i + 1].getType().isSolid() && rel[i].getType().isSolid()) {
                            for (Block b1 : getTopmostBlocks(rel[i], recursion + 1, maxrecursion, jumps, isStomp)) {
                                if (!build.contains(b1)) build.add(b1);
                            }
                            found = true;
                            break;
                        }
                        //Do not continue when found a non-solid block (do not warp the skill ;))
                        if (!rel[i].getType().isSolid()) break;
                    }
                }
            }
        }
        return build;
    }

    private void playEffect(Block b, double blockYForce) {
        if (b.getType() == Material.BARRIER || b.getType() == Material.MOB_SPAWNER) return;
        List<Block> bs = new LinkedList<>();
        bs.add(b);
        ArterionPlugin.getInstance().getExplosionHandler().handleExplosion(b.getLocation(), bs, (b1, type, data) -> {
            if (b1.getType().isSolid()) b1.setType(Material.BARRIER, false);
            else b1.setType(Material.AIR, false);
            ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(type, data), 0, 0, 0, 1, 20, b1.getLocation(), PARTICLE_RANGE);
            b1.getWorld().playSound(b1.getLocation(), Sound.DIG_STONE, 1f, 1f);
            FallingBlock fb = b1.getWorld().spawnFallingBlock(b1.getLocation().clone().add(0, 0.2, 0), type, data);
            fb.setDropItem(false);
            fb.setHurtEntities(false);
            fb.setVelocity(new Vector(0, blockYForce, 0));
            return fb;
        }, true, true, 1, 1 /*We do single blocks, so this is irrelevant*/, true);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_RANGE.evaluateDouble(p);
        double barbarHeight = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_SELF_LAUNCH.evaluateDouble(p);
        double otherHeight = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_ENEMY_LAUNCH.evaluateDouble(p);
        double damage = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_DAMAGE.evaluateDouble(p);
        this.printCastMessage(p, null);

        double barbarYForce = Math.sqrt(2 * G_FORCE * barbarHeight) / 20;
        double otherYForce = Math.sqrt(2 * G_FORCE * otherHeight) / 20;
        double blockYForce = Math.sqrt(2 * G_FORCE * 0.35) / 20;

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.DIG_STONE, 0.75f, 1);

        p.getBAC().disableMovementChecksFor(2000);

        Vector v = p.getBukkitPlayer().getVelocity();
        v.setY(barbarYForce);
        p.getBukkitPlayer().setVelocity(v);


        int tickInterval = 1;
        int toleranceTicks = 7 * 20;

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 5;
            int landed = -1;
            int work = 1;
            LinkedList<Block> affectedBlocks = null;

            @Override
            public void run() {
                if (p.getHealth() <= 0) {
                    cancel();
                    return;
                }

                if (landed == -1) {
                    boolean solid = false;
                    //Check all four player corners
                    for (int i = 0; i < 4; i++) {
                        Block b = p.getBukkitPlayer().getLocation().clone().add(i >= 2 ? PLAYER_MODEL_WIDTH_RADIUS : -PLAYER_MODEL_WIDTH_RADIUS, 0, i % 2 == 0 ? PLAYER_MODEL_WIDTH_RADIUS : -PLAYER_MODEL_WIDTH_RADIUS).getBlock();
                        if (b != null) {
                            b = b.getRelative(BlockFace.DOWN);
                            if (b != null) {
                                if (b.getType().isSolid()) {
                                    solid = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (solid) {
                        landed = tick;
                        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
                        for (Entity e : TargetCalculator.getAOETargets(p.getBukkitPlayer(), range)) {
                            if (e instanceof LivingEntity) {
                                if (Combat.isEnemy(p, (LivingEntity) e)) {
                                    if (e instanceof Player) {
                                        ArterionPlayer attackedPlayer = ArterionPlayerUtil.get((Player) e);
                                        attackedPlayer.getBAC().disableMovementChecksFor(2000);
                                    }
                                    ArterionPlugin.getInstance().getDamageManager().damage(((LivingEntity) e), p, damage, StompSkill.this);
                                    Block b1 = e.getLocation().getBlock().getRelative(BlockFace.DOWN);
                                    Block b2 = e.getLocation().getBlock().getRelative(BlockFace.DOWN, 2);
                                    if (b1 != null && b2 != null) {
                                        if (!b1.getType().isSolid() && !b2.getType().isSolid())
                                            continue; //No push up when in air already
                                    }
                                    Vector v = e.getVelocity();
                                    v.setY(otherYForce);
                                    e.setVelocity(v);
                                }
                            }
                        }
                        affectedBlocks = getTopmostBlocks(p.getBukkitPlayer().getLocation(), (int) range, 1, true);
                        affectedBlocks.sort((b1, b2) -> Integer.valueOf((int) b1.getLocation().distanceSquared(p.getBukkitPlayer().getLocation())).compareTo((int) b2.getLocation().distanceSquared(p.getBukkitPlayer().getLocation())));
                    }
                }

                if (landed != -1) {
                    int since = tick - landed;
                    if (since % 2 == 0) {
                        for (int i = 0; i < work && !affectedBlocks.isEmpty(); i++) {
                            Block b = affectedBlocks.removeFirst();
                            playEffect(b, blockYForce);
                        }

                        if (affectedBlocks.isEmpty()) {
                            cancel();
                            return;
                        }

                        //Next ring (will not always be real rings when some blocks are missing)
                        if (this.work == 1) work *= 4;
                        else work *= 2;
                    }
                }


                if (tick >= toleranceTicks) cancel();
                tick += tickInterval;
            }
        }, 5, tickInterval);
        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_SELF_LAUNCH.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_ENEMY_LAUNCH.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_STOMP_DAMAGE.evaluateDouble(p)};
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
