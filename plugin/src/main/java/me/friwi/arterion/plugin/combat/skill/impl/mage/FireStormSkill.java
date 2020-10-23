package me.friwi.arterion.plugin.combat.skill.impl.mage;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Supplier;

public class FireStormSkill extends RestrictedActiveSkill<SkillContainerData> {

    private LinkedList<LivingEntity> blackListed = new LinkedList<>();

    public FireStormSkill() {
        super(ClassEnum.MAGE, SkillSlotEnum.ACTIVE5);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_RANGE.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_FIRE_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_AOE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_DAMAGE.evaluateInt(p)};
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
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_DURATION.evaluateInt(p);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int range = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_RANGE.evaluateInt(p);
        int ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_DURATION.evaluateInt(p) / 50;
        int fireTicks = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_FIRE_DURATION.evaluateInt(p) / 50;
        double aoeRange = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_AOE.evaluateDouble(p);
        int damage = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_DAMAGE.evaluateInt(p);
        double pull = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_FIRE_STORM_PULL.evaluateDouble(p);
        int damageEvery = 20;

        Block targetBlock = TargetCalculator.getTargetBlock(p.getBukkitPlayer(), range);
        if (targetBlock == null || targetBlock.getType() == Material.AIR || (targetBlock.getRelative(BlockFace.UP).getType().isSolid() && targetBlock.getRelative(BlockFace.UP, 2).getType().isSolid())) {
            p.scheduleHotbarCard(new SkillFailCard(p, "skill.noblock.hotbar", this));
            return false;
        }

        this.printCastMessage(p, null);

        targetBlock = targetBlock.getRelative(BlockFace.UP);
        if (targetBlock.getType().isSolid()) {
            targetBlock = targetBlock.getRelative(BlockFace.UP);
        }

        int spawnEvery = 2;
        int blockLifeDuration = 70;
        double maxSpeed = 2.5;

        Block finalTargetBlock = targetBlock;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;
            Map<FallingBlock, Supplier<Boolean>> doTick = new HashMap<>();

            @Override
            public void run() {
                if ((tick % spawnEvery == 0 || tick % 20 == 0) && tick < ticks) {
                    //Spawn a block
                    FallingBlock block = finalTargetBlock.getWorld().spawnFallingBlock(finalTargetBlock.getLocation().clone().add(0, 0.2, 0), Material.FIRE, (byte) 0);
                    block.setHurtEntities(false);
                    block.setDropItem(false);
                    Binding<FallingBlock>[] binding = new Binding[1];
                    binding[0] = Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.subscribe(block, evt -> {
                        evt.setCancelled(true);
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
                            @Override
                            public void run() {
                                Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.unsubscribe(binding[0]);
                            }
                        }, 1l);
                        return evt;
                    });
                    doTick.put(block, new Supplier<Boolean>() {
                        int remaining = blockLifeDuration;
                        int random = (int) (Math.random() * 20);

                        @Override
                        public Boolean get() {
                            if (remaining <= 0) {
                                Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.unsubscribe(binding[0]);
                                return false;
                            }
                            remaining--;
                            double angle = (random + remaining) / 20f * Math.PI * 2;
                            double speed = maxSpeed * ((blockLifeDuration - remaining + 0f) / (blockLifeDuration + 0f));
                            if (remaining % 12 == 0) {
                                ParticleEffect.LAVA.display(0.3f, 0.3f, 0.3f, 1, 1, block.getLocation(), PARTICLE_RANGE);
                            }
                            Vector mot = new Vector(Math.sin(angle) * speed, 0.35, Math.cos(angle) * speed);
                            if (block.isDead()) {
                                Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.unsubscribe(binding[0]);
                                return false;
                            } else {
                                block.setVelocity(mot);
                            }
                            return true;
                        }
                    });
                    //Damage enemies in aoe
                    List<Entity> nearby = TargetCalculator.getAOETargetsOfEntity(block, aoeRange);
                    for (Entity n : nearby) {
                        if (n instanceof LivingEntity && Combat.isEnemy(p, (LivingEntity) n)) {
                            if (tick % damageEvery == 0 && !blackListed.contains(n)) {
                                ArterionPlugin.getInstance().getDamageManager().damage((LivingEntity) n, p, damage, FireStormSkill.this);
                                if (n.getFireTicks() < fireTicks) n.setFireTicks(fireTicks);
                                blackListed.add((LivingEntity) n);
                                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                                    @Override
                                    public void run() {
                                        blackListed.remove(n);
                                    }
                                }, damageEvery - 1);
                            }
                            //Pull entities
                            Vector dir = block.getLocation().clone().subtract(n.getLocation()).toVector().setY(0).normalize().multiply(pull);
                            n.setVelocity(n.getVelocity().clone().add(dir));
                        }
                    }
                }
                //Tick all blocks
                Iterator<Map.Entry<FallingBlock, Supplier<Boolean>>> it = doTick.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<FallingBlock, Supplier<Boolean>> ent = it.next();
                    if (!ent.getValue().get()) {
                        ent.getKey().remove();
                        it.remove();
                    }
                }
                if (tick % 5 == 0) {
                    finalTargetBlock.getWorld().playSound(finalTargetBlock.getLocation(), Sound.FIRE, 1f, 1f);
                }
                if (tick >= ticks + blockLifeDuration) {
                    cancel();
                    return;
                }
                tick++;
            }
        }, 1, 1);

        return true;
    }
}
