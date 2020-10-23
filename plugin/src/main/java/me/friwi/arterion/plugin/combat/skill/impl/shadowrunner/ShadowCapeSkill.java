package me.friwi.arterion.plugin.combat.skill.impl.shadowrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.recordable.InvisibleEntityUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShadowCapeSkill extends RestrictedActiveSkill<ShadowCapeSkill.ShadowCapeSkillContainerData> {

    public ShadowCapeSkill() {
        super(ClassEnum.SHADOWRUNNER, SkillSlotEnum.ACTIVE4);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int duration = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_SHADOW_CAPE_DURATION.evaluateInt(p);
        int ticks = duration / 50;
        this.printCastMessage(p, null);
        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1);
        InvisibleEntityUtil.setInvisible(p.getBukkitPlayer(), true);
        p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 0));
        p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 2));

        ShadowCapeSkillContainerData data = getSkillDataContainer(p);
        data.expires = System.currentTimeMillis() + duration;

        p.scheduleHotbarCard(new ShadowCapeHotbarCard(duration, data));

        Location loc = p.getBukkitPlayer().getLocation().clone();
        for (int i = 0; i < 6; i++)
            ParticleEffect.SMOKE_LARGE.display(0, 0.5f, 0, 0.05f, 5, loc.add(0, 0.2, 0), PARTICLE_RANGE);

        final Bat[] bats = new Bat[3];
        //Spawn bats
        for (int i = 0; i < bats.length; i++) {
            CreatureSpawnListener.isSpawningWithCommand = true;
            Bat bat = p.getBukkitPlayer().getWorld().spawn(p.getBukkitPlayer().getLocation().clone().add(0, 1, 0), Bat.class);
            bat.setMaximumNoDamageTicks(40);
            bat.setNoDamageTicks(40);
            bats[i] = bat;
            CreatureSpawnListener.isSpawningWithCommand = false;
        }

        int tickInterval = 2;

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;
            boolean removed = false;

            @Override
            public void run() {
                ShadowCapeSkill.ShadowCapeSkillContainerData data = getSkillDataContainer(p);
                if (p.getHealth() <= 0 || !InvisibleEntityUtil.getInvisible(p.getBukkitPlayer())) {
                    if (data != null) data.expires = 0;
                    removeBats();
                    p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1);
                    if (InvisibleEntityUtil.getInvisible(p.getBukkitPlayer()))
                        InvisibleEntityUtil.setInvisible(p.getBukkitPlayer(), false);
                    cancel();
                    return;
                }

                if (data != null && data.expires < System.currentTimeMillis()) {
                    removeBats();
                    p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1);
                    InvisibleEntityUtil.setInvisible(p.getBukkitPlayer(), false);
                    cancel();
                    return;
                }

                if (tick >= 20) {
                    removeBats();
                }

                if (tick >= ticks) {
                    removeBats();
                    p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1);
                    InvisibleEntityUtil.setInvisible(p.getBukkitPlayer(), false);
                    cancel();
                }
                tick += tickInterval;
            }

            private void removeBats() {
                if (!removed) {
                    for (Bat b : bats) {
                        if (b != null && !b.isDead()) b.remove();
                    }
                    removed = true;
                }
            }
        }, 0, tickInterval);

        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_SHADOW_CAPE_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_CHAIN_REVEAL_DURATION.evaluateInt() / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with attack damage hook
        Binding<ArterionPlayer> binding1 = Hooks.PLAYER_POST_SKILL_CAST_HOOK.subscribe(p, skill -> {
            ShadowCapeSkillContainerData data = getSkillDataContainer(p);
            if (data.expires > System.currentTimeMillis()) {
                data.expires = 0;
                this.getSkillDataContainer(p).setActiveUntil(0);
                this.updateToMod(p);
                p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1);
                InvisibleEntityUtil.setInvisible(p.getBukkitPlayer(), false);
            }
            return skill;
        });
        Binding<Entity> binding2 = Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), damage -> {
            ShadowCapeSkillContainerData data = getSkillDataContainer(p);
            if (data.expires > System.currentTimeMillis()) {
                data.expires = 0;
                this.getSkillDataContainer(p).setActiveUntil(0);
                this.updateToMod(p);
                p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1);
                InvisibleEntityUtil.setInvisible(p.getBukkitPlayer(), false);
            }
            return damage;
        });
        setSkillDataContainer(p, new ShadowCapeSkillContainerData(binding1, binding2));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        ShadowCapeSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PLAYER_POST_SKILL_CAST_HOOK.unsubscribe(data.binding1);
            Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.unsubscribe(data.binding2);
            data.expires = 0;
        }
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_SHADOW_CAPE_DURATION.evaluateInt(p);
    }

    protected class ShadowCapeSkillContainerData extends SkillContainerData {
        long expires;
        private Binding<ArterionPlayer> binding1;
        private Binding<Entity> binding2;

        private ShadowCapeSkillContainerData(Binding<ArterionPlayer> binding1, Binding<Entity> binding2) {
            this.binding1 = binding1;
            this.binding2 = binding2;
        }
    }
}
