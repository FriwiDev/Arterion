package me.friwi.arterion.plugin.combat.skill.impl.barbar;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RageSkill extends RestrictedActiveSkill<SkillContainerData> {

    public RageSkill() {
        super(ClassEnum.BARBAR, SkillSlotEnum.ACTIVE5);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_RAGE_DURATION.evaluateInt(p) / 50;
        this.printCastMessage(p, null);


        p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 2));
        p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, ticks, 0));
        p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, ticks, 3));
        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1);
        p.getBukkitPlayer().setSprinting(true);

        int tickInterval = 3;

        float radius = 0.5f;
        int amountPerSpawn = 12;

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (p.getHealth() <= 0) {
                    cancel();
                    return;
                }

                Location loc = p.getBukkitPlayer().getLocation().clone().add(0, 1, 0);

                ParticleEffect.FLAME.display(radius, radius, radius, 0, amountPerSpawn, loc, PARTICLE_RANGE);

                if (tick >= ticks) cancel();
                tick += tickInterval;
            }
        }, 0, tickInterval);
        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_RAGE_DURATION.evaluateInt(p) / 1000d};
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
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_RAGE_DURATION.evaluateInt(p);
    }
}
