package me.friwi.arterion.plugin.combat.skill.impl.forestrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.MorgothDungeonRegion;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class JumpSkill extends RestrictedActiveSkill<SkillContainerData> {
    private double G_FORCE = 45;

    public JumpSkill() {
        super(ClassEnum.FORESTRUNNER, SkillSlotEnum.ACTIVE2);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_JUMP_DISTANCE.evaluateDouble(p);
        double forestRunnerHeight = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_JUMP_HEIGHT.evaluateDouble(p);
        this.printCastMessage(p, null);

        double forestRunnerYForce = Math.sqrt(2 * G_FORCE * forestRunnerHeight) / 20;
        double timeUntilImpact = 2 * G_FORCE * forestRunnerYForce;
        double forestRunnerXZForce = range / (timeUntilImpact);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.SHOOT_ARROW, 0.75f, 1);

        p.getBAC().disableMovementChecksFor(5000);

        Vector v = p.getBukkitPlayer().getVelocity().clone();
        v.setY(forestRunnerYForce);
        Vector direction = p.getBukkitPlayer().getLocation().getDirection().clone();
        direction.setY(0);
        direction.normalize();
        direction.multiply(forestRunnerXZForce * 10);
        v.setX(direction.getX());
        v.setZ(direction.getZ());
        if (!p.getBukkitPlayer().getLocation().getBlock().getRelative(BlockFace.UP, 2).getType().isSolid()) {
            p.getBukkitPlayer().teleport(p.getBukkitPlayer().getLocation().clone().add(0, 0.2, 0));
        }
        p.getBukkitPlayer().setVelocity(v);

        //System.out.println(range+" "+forestRunnerHeight+" "+forestRunnerYForce+" "+timeUntilImpact+" "+forestRunnerXZForce+" "+direction.getX()+" "+direction.getZ());


        int tickInterval = 1;
        int toleranceTicks = (int) (timeUntilImpact * 2);

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (p.getHealth() <= 0) {
                    cancel();
                    return;
                }

                p.getBukkitPlayer().setFallDistance(0);

                ParticleEffect.CLOUD.display(0, 0, 0, 0.05f, 3, p.getBukkitPlayer().getLocation(), PARTICLE_RANGE);

                if (tick >= 5) {
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
                        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ARROW_HIT, 1, 1);
                        cancel();
                    }
                }
                if (tick >= toleranceTicks) cancel();
                tick += tickInterval;
            }
        }, 0, tickInterval);
        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_JUMP_DISTANCE.evaluateDouble(p)};
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
        if (p.getRegion() instanceof MorgothDungeonRegion) return false;
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return 0;
    }
}
