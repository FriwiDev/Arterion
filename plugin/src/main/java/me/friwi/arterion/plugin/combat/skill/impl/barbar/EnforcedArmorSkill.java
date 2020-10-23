package me.friwi.arterion.plugin.combat.skill.impl.barbar;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;

public class EnforcedArmorSkill extends RestrictedActiveSkill<EnforcedArmorSkill.EnforcedArmorSkillContainterData> {

    public EnforcedArmorSkill() {
        super(ClassEnum.BARBAR, SkillSlotEnum.ACTIVE3);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int duration = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_ENFORCED_ARMOR_DURATION.evaluateInt(p);
        this.printCastMessage(p, null);
        p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.ANVIL_USE, 0.8f, 1);

        EnforcedArmorSkillContainterData data = getSkillDataContainer(p);
        data.expires = System.currentTimeMillis() + duration;

        p.scheduleHotbarCard(new EnforcedArmorHotbarCard(duration, data));

        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_ENFORCED_ARMOR_INCREASE.evaluateDouble(p), ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_ENFORCED_ARMOR_DURATION.evaluateInt(p) / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with attack damage hook
        Binding<Entity> binding = Hooks.ARMOR_RESISTANCE_HOOK.subscribe(p.getBukkitPlayer(), resistance -> {
            EnforcedArmorSkillContainterData data = getSkillDataContainer(p);
            if (data.expires > System.currentTimeMillis()) {
                resistance *= 1 + (ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_ENFORCED_ARMOR_INCREASE.evaluateDouble(p) / 100d);
            }
            return resistance;
        });
        setSkillDataContainer(p, new EnforcedArmorSkillContainterData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        EnforcedArmorSkillContainterData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.ARMOR_RESISTANCE_HOOK.unsubscribe(data.binding);
            data.expires = 0;
        }
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_ENFORCED_ARMOR_DURATION.evaluateInt(p);
    }

    protected class EnforcedArmorSkillContainterData extends SkillContainerData {
        long expires;
        private Binding<Entity> binding;

        private EnforcedArmorSkillContainterData(Binding<Entity> binding) {
            this.binding = binding;
        }
    }
}
