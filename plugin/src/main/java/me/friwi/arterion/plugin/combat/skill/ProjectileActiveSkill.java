package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

public abstract class ProjectileActiveSkill<T extends SkillContainerData> extends RestrictedActiveSkill<T> {
    public ProjectileActiveSkill(ClassEnum boundClass, SkillSlotEnum skillSlot) {
        super(boundClass, skillSlot);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        Projectile pro = createProjectileOnCast(p);
        if (pro == null) return false;
        Binding[] binding = new Binding[1];
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
        return true;
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return p.getRegion() != null && p.getRegion().isPvp();
    }

    public abstract Projectile createProjectileOnCast(ArterionPlayer p);

    public abstract void onHitEntity(ArterionPlayer p, Projectile projectile, Entity hit);

    public abstract void onHitBlock(ArterionPlayer p, Projectile projectile, Block hit);
}
