package me.friwi.arterion.plugin.combat.skill.impl.paladin;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.PassiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.List;

public class ResistanceSkill extends PassiveSkill<ResistanceSkill.ResistanceSkillContainerData> {

    public ResistanceSkill() {
        super(ClassEnum.PALADIN);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_RESISTANCE_PERENEMY.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_RESISTANCE_RANGE.evaluateDouble(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with damage hook
        Binding<Entity> binding = Hooks.FINAL_DAMAGE_RECEIVED_HOOK.subscribe(p.getBukkitPlayer(), damage -> {
            double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_RESISTANCE_RANGE.evaluateDouble(p);
            double damageReduce = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_RESISTANCE_PERENEMY.evaluateDouble(p) / 100d;
            List<Entity> enemies = TargetCalculator.getAOETargetsWithRelation(p, range, PlayerRelation.ENEMY);
            int enemyCount = 0;
            for (Entity check : enemies) {
                if ((check instanceof Monster) || (check instanceof Player)) {
                    //Entity is real threat, count it!
                    enemyCount++;
                }
            }
            return damage * (1 - damageReduce * enemyCount);
        });
        setSkillDataContainer(p, new ResistanceSkillContainerData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        ResistanceSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.FINAL_DAMAGE_RECEIVED_HOOK.unsubscribe(data.binding);
        }
    }

    protected class ResistanceSkillContainerData extends SkillContainerData {
        private Binding<Entity> binding;

        private ResistanceSkillContainerData(Binding<Entity> binding) {
            this.binding = binding;
        }
    }
}
