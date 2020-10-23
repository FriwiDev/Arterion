package me.friwi.arterion.plugin.combat.skill.impl.none;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.stats.StatType;
import org.bukkit.Effect;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class HealSkill extends RestrictedActiveSkill<SkillContainerData> {

    public HealSkill() {
        super(ClassEnum.NONE, SkillSlotEnum.ACTIVE1);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;
        int heal = ArterionPlugin.getInstance().getFormulaManager().SKILL_NONE_HEAL.evaluateInt(p);
        p.heal(heal);
        p.trackStatistic(StatType.HEAL, 0, v -> v + heal);
        this.printCastMessage(p, null);
        p.getBukkitPlayer().getWorld().playEffect(p.getBukkitPlayer().getLocation(), Effect.POTION_BREAK, new Potion(PotionType.INSTANT_HEAL, 1));
        ParticleEffect.HEART.display(0.5f, 0.5f, 0.5f, 0, 7, p.getBukkitPlayer().getLocation().clone().add(0, 1, 0), PARTICLE_RANGE);
        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_NONE_HEAL.evaluateInt(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        setSkillDataContainer(p, new SkillContainerData());
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        setSkillDataContainer(p, null);
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return 0;
    }
}
