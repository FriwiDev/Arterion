package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public abstract class TargetActiveSkill<T extends SkillContainerData, S extends LivingEntity> extends RestrictedActiveSkill<T> {
    private Class<S> possibleTargets;

    public TargetActiveSkill(ClassEnum boundClass, SkillSlotEnum skillSlot, Class<S> possibleTargets) {
        super(boundClass, skillSlot);
        this.possibleTargets = possibleTargets;
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        Entity target = TargetCalculator.getTargetEntity(p.getBukkitPlayer(), getRange(p));
        if (target != null && possibleTargets.isAssignableFrom(target.getClass())) {
            return castWithTarget(p, (S) target);
        } else {
            return castWithTarget(p, null);
        }
    }

    public abstract boolean castWithTarget(ArterionPlayer p, S target);

    public abstract double getRange(ArterionPlayer p);
}
