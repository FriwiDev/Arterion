package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarCard;
import me.friwi.arterion.plugin.ui.hotbar.NotForModUser;
import me.friwi.arterion.plugin.ui.hotbar.PriorityHotbarCard;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

public abstract class ArrowActiveSkill<T extends ArrowActiveSkill.ArrowActiveSkillContainerData> extends RestrictedActiveSkill<T> {
    public ArrowActiveSkill(ClassEnum boundClass, SkillSlotEnum skillSlot) {
        super(boundClass, skillSlot);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        Hooks.ARROW_ACTIVE_SKILL_ACTIVATE_HOOK.execute(p, null);
        ArrowActiveSkillContainerData data = getSkillDataContainer(p);
        long delay = getAllowedShootDelay(p);
        data.expires = System.currentTimeMillis() + delay;
        p.scheduleHotbarCard(new ArrowActiveSkillHotbarCard(delay, data));
        p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.CLICK, 0.8f, 1);
        return true;
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return p.getRegion() != null && p.getRegion().isPvp();
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        T data = createContainerData(p);
        data.shootBinding = Hooks.PLAYER_SHOOT_BOW_HOOK.subscribe(p, tuple -> {
            if (data.expires > System.currentTimeMillis()) {
                data.expires = 0;
                this.getSkillDataContainer(p).setActiveUntil(0);
                this.updateToMod(p);
                tuple.setSecondValue(this.applyToProjectile(p, tuple.getFirstValue(), tuple.getSecondValue()));
                Projectile pro = tuple.getFirstValue();
                Binding[] binding = new Binding[1];
                this.printCastMessage(p, null);
                binding[0] = Hooks.PROJECTILE_HIT_TARGET_EVENT_HOOK.subscribe(pro, evt -> {
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                        @Override
                        public void run() {
                            Hooks.PROJECTILE_HIT_TARGET_EVENT_HOOK.unsubscribe(binding[0]);
                        }
                    }, 1);
                    if (evt.getHitEntity() != null) {
                        onHitEntity(p, pro, evt.getHitEntity());
                    } else if (evt.getHitBlock() != null) {
                        onHitBlock(p, pro, evt.getHitBlock());
                    }
                    return evt;
                });
            }
            return tuple;
        });
        data.activateArrowSkillBinding = Hooks.ARROW_ACTIVE_SKILL_ACTIVATE_HOOK.subscribe(p, v -> {
            //Cancel this skill when player activates another ArrowActiveSkill
            data.expires = 0;
            this.getSkillDataContainer(p).setActiveUntil(0);
            this.updateToMod(p);
            return null;
        });
        this.setSkillDataContainer(p, data);
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        T data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PLAYER_SHOOT_BOW_HOOK.unsubscribe(data.shootBinding);
            Hooks.ARROW_ACTIVE_SKILL_ACTIVATE_HOOK.unsubscribe(data.activateArrowSkillBinding);
        }
    }

    public abstract long getAllowedShootDelay(ArterionPlayer p);

    public abstract T createContainerData(ArterionPlayer p);

    public abstract double applyToProjectile(ArterionPlayer p, Entity projectile, double damage);

    public abstract void onHitEntity(ArterionPlayer p, Projectile projectile, Entity hit);

    public abstract void onHitBlock(ArterionPlayer p, Projectile projectile, Block hit);

    public class ArrowActiveSkillContainerData extends SkillContainerData {
        long expires = 0;
        Binding<ArterionPlayer> shootBinding, activateArrowSkillBinding;
    }

    protected class ArrowActiveSkillHotbarCard extends HotbarCard implements PriorityHotbarCard, NotForModUser {
        ArrowActiveSkillContainerData data;

        public ArrowActiveSkillHotbarCard(long duration, ArrowActiveSkillContainerData data) {
            super(duration);
            this.data = data;
        }

        @Override
        public String getMessage() {
            long remaining = getExpires() - System.currentTimeMillis();
            float percentage = (remaining + 0f) / (duration + 0f);
            if (data.expires < System.currentTimeMillis()) {
                this.setExpires(0);
                percentage = 0;
            }
            return ProgressBar.generate("\2474", percentage, 40);
        }
    }
}
