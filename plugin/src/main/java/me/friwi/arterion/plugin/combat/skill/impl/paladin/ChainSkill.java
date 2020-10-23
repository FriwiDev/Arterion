package me.friwi.arterion.plugin.combat.skill.impl.paladin;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.*;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.combat.skill.impl.shadowrunner.ShadowCapeSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.PlayerPotionTracker;
import me.friwi.arterion.plugin.player.PotionTrackerEntry;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.recordable.InvisibleEntityUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class ChainSkill extends RestrictedActiveSkill<SkillContainerData> {

    public ChainSkill() {
        super(ClassEnum.PALADIN, SkillSlotEnum.ACTIVE4);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int slownessTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_SLOWNESS_DURATION.evaluateInt(p) / 50;
        int weaknessTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_WEAKNESS_DURATION.evaluateInt(p) / 50;
        double rangeInner = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_INNER_RANGE.evaluateDouble(p);
        double rangeOuter = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_OUTER_RANGE.evaluateDouble(p);
        int revealTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_REVEAL_DURATION.evaluateInt() / 50;
        this.printCastMessage(p, null);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ITEM_BREAK, 1f, 1);

        List<LivingEntity> chained = new LinkedList<>();
        Map<ArterionPlayer, PotionTrackerEntry[]> potions = new HashMap<>();
        for (Entity e : TargetCalculator.getAOETargets(p.getBukkitPlayer(), rangeInner)) {
            if (e instanceof LivingEntity) {
                if (Combat.isEnemy(p, (LivingEntity) e)) {
                    chained.add((LivingEntity) e);

                    if (e instanceof PlayerPotionTracker) {
                        ArterionPlayer ep = ArterionPlayerUtil.get((Player) e);
                        PotionTrackerEntry[] entries = new PotionTrackerEntry[2];
                        entries[0] = ep.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slownessTicks, 1));
                        entries[1] = ep.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessTicks, 1));
                        potions.put(ep, entries);
                    } else {
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slownessTicks, 1));
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessTicks, 1));
                    }
                }
            }
        }

        int tickInterval = 2;
        int ticks = Math.max(slownessTicks, weaknessTicks);

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (p.getHealth() <= 0) {
                    for (LivingEntity e : chained) {
                        exclude(potions, e);
                    }
                    cancel();
                    return;
                }

                Iterator<LivingEntity> it = chained.iterator();
                while (it.hasNext()) {
                    LivingEntity e = it.next();
                    if (e.isDead() || (e.getLocation().getWorld().equals(p.getBukkitPlayer().getLocation()) && e.getLocation().distance(p.getBukkitPlayer().getLocation()) > rangeOuter) || !p.getBukkitPlayer().hasLineOfSight(e)) {
                        it.remove();
                        exclude(potions, e);
                        if (!e.isDead())
                            p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ITEM_BREAK, 1f, 1);
                    } else {
                        //Draw line
                        Location from = p.getBukkitPlayer().getLocation().clone().add(0, 1, 0);
                        Location to = e.getLocation().clone().add(0, 1, 0);
                        ParticleEffectUtil.drawLine(from, to, 3, loc -> ParticleEffect.CRIT.display(0f, 0f, 0f, 0, 3, loc, PARTICLE_RANGE));

                        if (tick % 40 == 0) {
                            if (e instanceof Player) {
                                ArterionPlayer attackedPlayer = ArterionPlayerUtil.get((Player) e);
                                attackedPlayer.getBAC().disableMovementChecksFor(2000);
                            }
                            //Knockback
                            Vector mot = e.getLocation().toVector().subtract(p.getBukkitPlayer().getLocation().toVector()).normalize();
                            mot.multiply(-ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_KNOCKBACK_XZ.evaluateDouble(p));
                            mot.setY(ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_KNOCKBACK_Y.evaluateDouble(p));
                            e.setVelocity(mot);
                        }

                        //Invisible shadowrunners
                        if (tick >= revealTicks && InvisibleEntityUtil.getInvisible(e)) {
                            InvisibleEntityUtil.setInvisible(e, false);
                            if (e instanceof Player) {
                                ArterionPlayer ep = ArterionPlayerUtil.get((Player) e);
                                ep.scheduleHotbarCard(new SkillFailCard(ep, "skill.returnvisiblebychain.hotbar", ChainSkill.this));
                                for (Skill s : ep.getSkillSlots().getSkillSlotsActive()) {
                                    if (s instanceof ShadowCapeSkill) {
                                        s.getSkillDataContainer(ep).setActiveUntil(0);
                                        ((ActiveSkill) s).updateToMod(ep);
                                    }
                                }
                                ((Player) e).playSound(e.getLocation(), Sound.FIZZ, 0.8f, 1f);
                            }
                        }
                    }
                }

                if (tick % 40 == 0) {
                    p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.PISTON_RETRACT, 0.7f, 1);
                }


                tick += tickInterval;
                if (tick >= ticks) cancel();
            }
        }, 0, tickInterval);
        return true;
    }

    private void exclude(Map<ArterionPlayer, PotionTrackerEntry[]> potions, LivingEntity entity) {
        if (entity instanceof Player) {
            ((Player) entity).playSound(entity.getLocation(), Sound.NOTE_PLING, 0.8f, 1);
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) entity);
            PotionTrackerEntry[] pot = potions.remove(ep);
            if (pot != null) {
                for (PotionTrackerEntry pote : pot) {
                    ep.getPotionTracker().removePotionTrackerEntry(pote);
                }
            }
        } else {
            if (entity.hasPotionEffect(PotionEffectType.WEAKNESS)) entity.removePotionEffect(PotionEffectType.WEAKNESS);
            if (entity.hasPotionEffect(PotionEffectType.BLINDNESS))
                entity.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_SLOWNESS_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_WEAKNESS_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_INNER_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_OUTER_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_REVEAL_DURATION.evaluateInt() / 1000d};
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
        return Math.max(ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_SLOWNESS_DURATION.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_WEAKNESS_DURATION.evaluateInt(p));
    }
}
