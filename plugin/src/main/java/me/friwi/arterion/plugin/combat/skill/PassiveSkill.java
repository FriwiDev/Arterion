package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;

public abstract class PassiveSkill<T extends SkillContainerData> extends Skill<T> {
    public PassiveSkill(ClassEnum boundClass) {
        super(boundClass);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public T getSkillDataContainer(ArterionPlayer p) {
        return (T) p.getSkillSlots().getPassiveSkillData();
    }

    @Override
    public void setSkillDataContainer(ArterionPlayer p, T container) {
        p.getSkillSlots().setPassiveSkillData(container);
    }
}
