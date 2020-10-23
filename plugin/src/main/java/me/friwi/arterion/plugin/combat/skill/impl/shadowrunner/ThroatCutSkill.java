package me.friwi.arterion.plugin.combat.skill.impl.shadowrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.ParticleEffectUtil;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetActiveSkill;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ThroatCutSkill extends TargetActiveSkill<SkillContainerData, LivingEntity> {

    public ThroatCutSkill() {
        super(ClassEnum.SHADOWRUNNER, SkillSlotEnum.ACTIVE2, LivingEntity.class);
    }

    private static boolean isInRange(double x, int low, int high) {
        return x > low && x < high;
    }

    public static Location findSafeLocation(Location target, Location behindTarget, boolean allowInTarget, double maxDistance) {
        //Fix location and handle block stuck
        Vector[] possibleDirections = new Vector[3];
        double ps = PLAYER_MODEL_WIDTH_RADIUS;
        outer:
        for (int i = 0; i < 8; i++) {
            Vector vec = new Vector(i % 2 >= 1 ? -ps : ps, i >= 4 ? PLAYER_MODEL_HEIGHT : 0, i % 4 >= 2 ? -ps : ps);
            Location check = behindTarget.clone().add(vec);
            if (check.getBlockY() <= 0 || check.getBlockY() >= check.getWorld().getMaxHeight()) continue;
            Block b = check.getBlock();
            if (b.getType().isSolid()) {
                //We have a problem
                if (i < 4) {
                    //Lower end
                    for (int y = 1; y >= 0; y--) {
                        for (int x = 0; x < 3; x++) {
                            if (x == 2) x = -1;
                            for (int z = 0; z < 3; z++) {
                                if (z == 2) z = -1;
                                Block c = b.getLocation().clone().add(x, y, z).getBlock();
                                Block ca = c.getRelative(BlockFace.UP);
                                if (!c.getType().isSolid() && !ca.getType().isSolid()) {
                                    //Use this block to stand on
                                    Location bt = behindTarget.clone();
                                    if (x > 0)
                                        bt.setX(c.getX() + (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    else
                                        bt.setX(c.getX() + 1 - (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    if (z > 0)
                                        bt.setZ(c.getZ() + (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    else
                                        bt.setZ(c.getZ() + 1 - (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    bt.setY(c.getY());
                                    if (bt.distance(behindTarget) < maxDistance) behindTarget = bt;
                                    continue outer;
                                }
                                if (z == -1) z = 3;
                            }
                            if (x == -1) x = 3;
                        }
                    }
                } else {
                    //Higher end
                    for (int y = 0; y >= -1; y--) {
                        for (int x = 0; x < 3; x++) {
                            if (x == 2) x = -1;
                            for (int z = 0; z < 3; z++) {
                                if (z == 2) z = -1;
                                Block c = b.getLocation().clone().add(x, y, z).getBlock();
                                Block cb = c.getRelative(BlockFace.DOWN);
                                if (!c.getType().isSolid() && !cb.getType().isSolid()) {
                                    //Use this block to stand on
                                    Location bt = behindTarget.clone();
                                    if (x > 0)
                                        bt.setX(c.getX() + (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    else
                                        bt.setX(c.getX() + 1 - (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    if (z > 0)
                                        bt.setZ(c.getZ() + (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    else
                                        bt.setZ(c.getZ() + 1 - (PLAYER_MODEL_WIDTH_RADIUS_SAFE));
                                    bt.setY(cb.getY());
                                    if (bt.distance(behindTarget) < maxDistance) behindTarget = bt;
                                    continue outer;
                                }
                                if (z == -1) z = 3;
                            }
                            if (x == -1) x = 3;
                        }
                    }
                }
            }
        }

        if (behindTarget.distance(target) >= maxDistance) {
            return allowInTarget ? target.clone() : null;
        }

        boolean inTarget = false;
        for (int i = 0; i < 8; i++) {
            Vector vec = new Vector(i % 2 >= 1 ? -ps : ps, i >= 4 ? PLAYER_MODEL_HEIGHT : 0, i % 4 >= 2 ? -ps : ps);
            Location check = behindTarget.clone().add(vec);
            if (check.getBlockY() <= 0 || check.getBlockY() >= check.getWorld().getMaxHeight()) continue;
            Block b = check.getBlock();
            if (b.getType().isSolid()) {
                //We have a problem
                if (allowInTarget) {
                    inTarget = true;
                    break;
                } else {
                    return null;
                }
            }
        }

        if (inTarget) {
            //Check if position in target is safe
            for (int i = 0; i < 8; i++) {
                Vector vec = new Vector(i % 2 >= 1 ? -ps : ps, i >= 4 ? PLAYER_MODEL_HEIGHT : 0, i % 4 >= 2 ? -ps : ps);
                Location check = target.clone().add(vec);
                if (check.getBlockY() <= 0 || check.getBlockY() >= check.getWorld().getMaxHeight()) continue;
                Block b = check.getBlock();
                if (b.getType().isSolid()) {
                    //We have a problem
                    return null;
                }
            }
            return target.clone();
        }

        return behindTarget;
    }

    @Override
    public double getRange(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_THROAT_CUT_RANGE.evaluateDouble(p);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_THROAT_CUT_DAMAGE.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_THROAT_CUT_RANGE.evaluateDouble(p)};
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

    @Override
    public boolean castWithTarget(ArterionPlayer p, LivingEntity target) {
        int dmg = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_THROAT_CUT_DAMAGE.evaluateInt(p);

        if (target == null || !Combat.isEnemy(p, target)) {
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.notarget.hotbar", this));
            return true;
        }

        if (target.getHealth() <= 0) return false;
        this.printCastMessage(p, target);

        ArterionPlugin.getInstance().getDamageManager().damage(target, p, dmg, this);

        int divider = 6;
        double radius = 0.23;
        double angle = Math.PI * 2 / divider;

        ParticleEffect.ParticleColor color = new ParticleEffect.OrdinaryColor(155, 0, 0);
        Location loc = target.getEyeLocation().clone().add(0, -0.25, 0);

        for (int i = 0; i < divider; i++) {
            double x = Math.sin(angle * i) * radius;
            double z = Math.cos(angle * i) * radius;
            ParticleEffect.REDSTONE.display(color, loc.add(x, 0, z), PARTICLE_RANGE);
            loc.add(-x, 0, -z); //Reset location
            ParticleEffect.REDSTONE.display(color, loc.add(x / 2, 0, z / 2), PARTICLE_RANGE);
            loc.add(-x / 2, 0, -z / 2); //Reset location
        }

        ParticleEffect.CRIT_MAGIC.display(0.5f, 0.5f, 0.5f, 0, 7, target.getEyeLocation().clone().add(0, -0.25, 0), PARTICLE_RANGE);
        target.getWorld().playSound(target.getLocation(), Sound.CREEPER_HISS, 1, 2f);

        Location behindTarget = target.getLocation().clone().add(target.getLocation().getDirection().clone().multiply(-1).normalize());
        behindTarget.setY(target.getLocation().getY());

        //Fix location and handle block stuck
        behindTarget = findSafeLocation(target.getLocation(), behindTarget, true, 1.2); //Do not set maxdistance higher, player could come out in front of enemy!
        if (behindTarget == null) {
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.nospacebehind.hotbar", this));
            return true;
        }

        //Target the enemy (when target was inserted by findSafeLocation, the direction already matches)
        if (!target.getLocation().equals(behindTarget)) {
            behindTarget.setDirection(target.getEyeLocation().clone().subtract(behindTarget.clone().add(0, p.getBukkitPlayer().getEyeHeight(), 0)).toVector());
        }
        if (behindTarget.getPitch() <= -89.5) behindTarget.setDirection(target.getLocation().getDirection());

        Location backup = p.getBukkitPlayer().getLocation().clone();

        //Perform tp
        p.getBukkitPlayer().teleport(behindTarget);

        if (!target.equals(behindTarget) && !p.getBukkitPlayer().hasLineOfSight(target)) {
            //Revert, we cannot see the other entity!
            p.getBukkitPlayer().teleport(backup);
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.nospacebehind.hotbar", this));
            return true;
        }

        p.getBukkitPlayer().setSprinting(true);

        //Draw line
        Location from = backup.add(0, 1, 0);
        Location to = target.getLocation().clone().add(0, 1, 0);
        ParticleEffectUtil.drawLine(from, to, 3, locc -> ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(200, 0, 200), locc, PARTICLE_RANGE));


        return true;
    }
}
