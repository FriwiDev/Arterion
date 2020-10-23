package me.friwi.arterion.plugin.combat.skill.impl.barbar;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.PassiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.entity.Entity;

public class BerserkRageSkill extends PassiveSkill<BerserkRageSkill.BerserkRageSkillContainerData> {

    public BerserkRageSkill() {
        super(ClassEnum.BARBAR);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_BERSERK_RAGE_INCREASE.evaluateDouble(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with damage hook
        Binding<Entity> binding = Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), tuple -> {
            double perMissingPercent = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_BERSERK_RAGE_INCREASE.evaluateDouble(p) / 100d;
            double missingPercent = (1 - (p.getBukkitPlayer().getHealth() / p.getBukkitPlayer().getMaxHealth())) * 100d;
            tuple.setSecondValue(tuple.getSecondValue() * (1 + missingPercent * perMissingPercent));
            return tuple;
        });
        setSkillDataContainer(p, new BerserkRageSkillContainerData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        BerserkRageSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.unsubscribe(data.binding);
        }
    }

    protected class BerserkRageSkillContainerData extends SkillContainerData {
        private Binding<Entity> binding;

        private BerserkRageSkillContainerData(Binding<Entity> binding) {
            this.binding = binding;
        }
    }
}
