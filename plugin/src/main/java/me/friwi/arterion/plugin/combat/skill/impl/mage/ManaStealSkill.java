package me.friwi.arterion.plugin.combat.skill.impl.mage;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.PassiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.entity.Entity;

public class ManaStealSkill extends PassiveSkill<ManaStealSkill.ManaStealSkillContainerData> {

    public ManaStealSkill() {
        super(ClassEnum.MAGE);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_MANA_STEAL_PERCENT.evaluateDouble(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with damage hook
        Binding<Entity> binding = Hooks.ABSOLUTE_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), tuple -> {
            double percent = ArterionPlugin.getInstance().getFormulaManager().SKILL_MAGE_MANA_STEAL_PERCENT.evaluateDouble(p) / 100d;
            p.addMana((int) (tuple.getSecondValue() * percent));
            return tuple;
        });
        setSkillDataContainer(p, new ManaStealSkillContainerData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        ManaStealSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.ABSOLUTE_DAMAGE_DEALT_HOOK.unsubscribe(data.binding);
        }
    }

    protected class ManaStealSkillContainerData extends SkillContainerData {
        private Binding<Entity> binding;

        private ManaStealSkillContainerData(Binding<Entity> binding) {
            this.binding = binding;
        }
    }
}
