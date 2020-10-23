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

public class MightyHitSkill extends RestrictedActiveSkill<MightyHitSkill.MightyHitSkillContainerData> {

    public MightyHitSkill() {
        super(ClassEnum.BARBAR, SkillSlotEnum.ACTIVE2);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int duration = ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_MIGHTY_HIT_DURATION.evaluateInt(p);
        this.printCastMessage(p, null);
        p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.CLICK, 0.8f, 1);

        MightyHitSkill.MightyHitSkillContainerData data = getSkillDataContainer(p);
        data.expires = System.currentTimeMillis() + duration;

        p.scheduleHotbarCard(new MightyHitHotbarCard(duration, data));

        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_MIGHTY_HIT_INCREASE.evaluateDouble(p), ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_MIGHTY_HIT_DURATION.evaluateInt(p) / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with attack damage hook
        Binding<Entity> binding = Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), tuple -> {
            double damage = tuple.getSecondValue();
            MightyHitSkill.MightyHitSkillContainerData data = getSkillDataContainer(p);
            if (data.expires > System.currentTimeMillis()) {
                damage *= ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_MIGHTY_HIT_INCREASE.evaluateDouble(p);
                data.expires = 0;
                data.setActiveUntil(0);
                updateToMod(p);
                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.ANVIL_LAND, 0.8f, 1);
            }
            tuple.setSecondValue(damage);
            return tuple;
        });
        setSkillDataContainer(p, new MightyHitSkill.MightyHitSkillContainerData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        MightyHitSkill.MightyHitSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.unsubscribe(data.binding);
            data.expires = 0;
        }
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_BARBAR_MIGHTY_HIT_DURATION.evaluateInt(p);
    }

    protected class MightyHitSkillContainerData extends SkillContainerData {
        long expires;
        private Binding<Entity> binding;

        private MightyHitSkillContainerData(Binding<Entity> binding) {
            this.binding = binding;
        }
    }
}
