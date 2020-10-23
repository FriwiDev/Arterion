package me.friwi.arterion.plugin.combat.skill.impl.shadowrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.card.SkillFailCard;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.PotionTrackerEntry;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShadowCloneSkill extends RestrictedActiveSkill<ShadowCloneSkill.ShadowCloneSkillContainerData> {

    public ShadowCloneSkill() {
        super(ClassEnum.SHADOWRUNNER, SkillSlotEnum.ACTIVE3);
    }

    @Override
    public void attemptCast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return;
        ShadowCloneSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            if (data.expires > System.currentTimeMillis() && data.backup != null) {
                data.expires = 0;
                this.getSkillDataContainer(p).setActiveUntil(0);
                this.updateToMod(p);
                this.printCastMessage(p, null);
                if (data.potionTrackerEntry != null) {
                    p.getPotionTracker().removePotionTrackerEntry(data.potionTrackerEntry);
                    data.potionTrackerEntry = null;
                }
                double ps = PLAYER_MODEL_WIDTH_RADIUS;
                for (int i = 0; i < 8; i++) {
                    Vector vec = new Vector(i % 2 >= 1 ? -ps : ps, i >= 4 ? PLAYER_MODEL_HEIGHT : 0, i % 4 >= 2 ? -ps : ps);
                    Location check = data.backup.clone().add(vec);
                    if (check.getBlockY() <= 0 || check.getBlockY() >= check.getWorld().getMaxHeight()) continue;
                    Block b = check.getBlock();
                    if (b.getType().isSolid()) {
                        //We have a problem
                        p.scheduleHotbarCard(new SkillFailCard(p, "skill.nospace.hotbar", this));
                        data.backup = null;
                        return;
                    }
                }
                p.getBukkitPlayer().teleport(data.backup);
                p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ENDERMAN_TELEPORT, 0.7f, 1);
                data.backup.getWorld().playSound(data.backup, Sound.ENDERMAN_TELEPORT, 0.7f, 1);
                data.backup = null;
                return;
            }
        }
        super.attemptCast(p);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int duration = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_SHADOW_CLONE_DURATION.evaluateInt(p);
        int ticks = duration / 50;
        this.printCastMessage(p, null);


        p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.CLICK, 0.8f, 1);

        ShadowCloneSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            data.potionTrackerEntry = p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 0));

            data.expires = System.currentTimeMillis() + duration;
            data.backup = p.getBukkitPlayer().getLocation();

            p.scheduleHotbarCard(new ShadowCloneHotbarCard(duration, data));
        }

        int tickInterval = 2;
        int amountPerSpawn = 3;

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                ShadowCloneSkillContainerData data = getSkillDataContainer(p);
                if (p.getHealth() <= 0) {
                    if (data != null) data.expires = 0;
                    cancel();
                    return;
                }

                if (data != null && data.expires < System.currentTimeMillis()) {
                    cancel();
                    return;
                }

                if (data != null && data.backup != null) {
                    Location loc = data.backup.clone();

                    for (int i = 0; i < 3; i++)
                        ParticleEffect.SMOKE_LARGE.display(0, 0.5f, 0, 0.05f, amountPerSpawn, loc.add(0, 0.4, 0), PARTICLE_RANGE);
                }

                if (tick >= ticks) cancel();
                tick += tickInterval;
            }
        }, 0, tickInterval);
        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_SHADOW_CLONE_DURATION.evaluateInt(p) / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        setSkillDataContainer(p, new ShadowCloneSkillContainerData());
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
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_SHADOW_CLONE_DURATION.evaluateInt(p);
    }

    protected class ShadowCloneSkillContainerData extends SkillContainerData {
        long expires;
        Location backup;
        PotionTrackerEntry potionTrackerEntry;

        private ShadowCloneSkillContainerData() {

        }
    }
}
