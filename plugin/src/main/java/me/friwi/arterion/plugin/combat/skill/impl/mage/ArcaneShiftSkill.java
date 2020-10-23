package me.friwi.arterion.plugin.combat.skill.impl.mage;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.*;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.combat.skill.impl.shadowrunner.ThroatCutSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.MorgothDungeonRegion;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ArcaneShiftSkill extends RestrictedActiveSkill<SkillContainerData> {

    public ArcaneShiftSkill() {
        super(ClassEnum.MAGE, SkillSlotEnum.ACTIVE3);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_ARCAN_SHIFT_RANGE.evaluateInt(p)};
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

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;
        this.printCastMessage(p, null);

        int range = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_ARCAN_SHIFT_RANGE.evaluateInt(p);

        Block targetBlock = TargetCalculator.getTargetBlock(p.getBukkitPlayer(), range);
        Location target = null;
        if (targetBlock != null && targetBlock.getType() != Material.AIR) {
            if (p.getBukkitPlayer().getLocation().getY() + p.getBukkitPlayer().getEyeHeight() < targetBlock.getY()) {
                //Can only see block from below!
                boolean surrounded = true;
                for (BlockFace f : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH_WEST, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST}) {
                    if (!targetBlock.getRelative(f).getType().isSolid()) {
                        surrounded = false;
                        break;
                    }
                }
                if (surrounded) {
                    //Prevent glitching through ceilings, set target block below original target
                    targetBlock = targetBlock.getRelative(BlockFace.DOWN, 3);
                }
            }
            if (!targetBlock.getRelative(BlockFace.UP).getType().isSolid() && !targetBlock.getRelative(BlockFace.UP, 2).getType().isSolid()) {
                target = targetBlock.getLocation().add(0.5, 1, 0.5);
            } else {
                Location shortened = targetBlock.getLocation().add(p.getBukkitPlayer().getLocation().getDirection().clone().normalize().multiply(-1));
                target = ThroatCutSkill.findSafeLocation(shortened, shortened, true, 0.4);
            }
        }
        if (target == null) {
            double r = range;
            if (targetBlock != null) r = targetBlock.getLocation().distance(p.getBukkitPlayer().getLocation()) - 1;
            targetBlock = p.getBukkitPlayer().getEyeLocation().clone().add(p.getBukkitPlayer().getLocation().getDirection().clone().normalize().multiply(r)).getBlock();
            if (!targetBlock.getRelative(BlockFace.UP).getType().isSolid()) {
                target = targetBlock.getLocation().add(0.5, 0, 0.5);
            } else if (!targetBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                target = targetBlock.getLocation().add(0.5, -1, 0.5);
            }
            //Prevent glitching in blocks
            if (target != null) target = ThroatCutSkill.findSafeLocation(target, target, false, 0.4);
        }
        if (target == null) {
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.nospace.hotbar", this));
            return false;
        }

        //Draw line
        Location from = p.getBukkitPlayer().getLocation().clone().add(0, 1, 0);
        Location to = target.clone().add(0, 1, 0);
        ParticleEffectUtil.drawLine(from, to, 3, locc -> ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(200, 0, 200), locc, PARTICLE_RANGE));

        //Draw ender effects
        p.getBukkitPlayer().getWorld().playEffect(p.getBukkitPlayer().getLocation(), Effect.ENDER_SIGNAL, 0);
        target.getWorld().playEffect(target, Effect.ENDER_SIGNAL, 0);

        //Draw witch magic
        ParticleEffect.SPELL_WITCH.display(0.5f, 0.5f, 0.5f, 0, 6, from, PARTICLE_RANGE);
        ParticleEffect.SPELL_WITCH.display(0.5f, 0.5f, 0.5f, 0, 6, to, PARTICLE_RANGE);

        //Play sounds
        from.getWorld().playSound(from, Sound.ENDERMAN_TELEPORT, 1f, 1f);
        to.getWorld().playSound(to, Sound.ENDERMAN_TELEPORT, 1f, 1f);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
            }
        }, 1);

        target.setDirection(p.getBukkitPlayer().getLocation().getDirection());
        p.getBukkitPlayer().teleport(target);
        p.getBukkitPlayer().setVelocity(p.getBukkitPlayer().getVelocity().setY(0.15));
        p.getBukkitPlayer().setFallDistance(0);
        p.getBukkitPlayer().setSprinting(true);

        return true;
    }
}
