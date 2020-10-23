package me.friwi.arterion.plugin.combat.skill.impl.paladin;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetCalculator;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlessingOfTheGodsSkill extends RestrictedActiveSkill<SkillContainerData> {

    public BlessingOfTheGodsSkill() {
        super(ClassEnum.PALADIN, SkillSlotEnum.ACTIVE5);
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int ticksSelf = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_BLESSING_OF_THE_GODS_DURATION.evaluateInt(p) / 50;
        int ticksOther = ticksSelf;
        double range = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_BLESSING_OF_THE_GODS_RANGE.evaluateDouble(p);
        this.printCastMessage(p, null);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.BLAZE_DEATH, 1f, 1);

        p.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, ticksSelf, 1));

        for (Entity e : TargetCalculator.getAOETargets(p.getBukkitPlayer(), range)) {
            if (e instanceof Player) {
                ArterionPlayer other = ArterionPlayerUtil.get((Player) e);
                if (p.getPlayerRelation(other) == PlayerRelation.FRIENDLY) {
                    other.getPotionTracker().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, ticksOther, 0));
                }
            }
        }
        return true;
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_BLESSING_OF_THE_GODS_DURATION.evaluateInt(p) / 1000d,
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_BLESSING_OF_THE_GODS_RANGE.evaluateDouble(p)};
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
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_BLESSING_OF_THE_GODS_DURATION.evaluateInt(p);
    }
}
