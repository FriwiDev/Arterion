package me.friwi.arterion.plugin.combat.skill.impl.paladin;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.ParticleEffectUtil;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.combat.skill.TargetActiveSkill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HelpingHandSkill extends TargetActiveSkill<SkillContainerData, Player> {

    public HelpingHandSkill() {
        super(ClassEnum.PALADIN, SkillSlotEnum.ACTIVE2, Player.class);
    }

    @Override
    public boolean castWithTarget(ArterionPlayer p, Player target) {
        int heal = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_HELPING_HAND_HEAL.evaluateInt(p);

        ArterionPlayer targetPlayer = target == null ? null : ArterionPlayerUtil.get(target);
        ArterionPlayer applyPlayer = p;
        //Heal target when it has lower hp
        if (targetPlayer != null) {
            if (targetPlayer.getHealth() < p.getHealth() && p.getPlayerRelation(targetPlayer) == PlayerRelation.FRIENDLY) {
                applyPlayer = targetPlayer;
                heal = ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_HELPING_HAND_OTHER_HEAL.evaluateInt(p);
            }
        }

        if (applyPlayer.getHealth() <= 0) return false;
        applyPlayer.heal(heal);
        applyPlayer.getBukkitPlayer().setFoodLevel(20);
        applyPlayer.getBukkitPlayer().setSaturation(10);

        int finalHeal = heal;
        p.trackStatistic(StatType.HEAL, 0, v -> v + finalHeal);

        this.printCastMessage(p, applyPlayer.equals(p) ? null : applyPlayer);

        ParticleEffect.HEART.display(0.5f, 0.5f, 0.5f, 0, 7, applyPlayer.getBukkitPlayer().getLocation().clone().add(0, 1, 0), PARTICLE_RANGE);
        applyPlayer.getBukkitPlayer().getWorld().playSound(applyPlayer.getBukkitPlayer().getLocation(), Sound.CAT_PURREOW, 1, 1);
        if (!applyPlayer.equals(p)) {
            //Draw line
            Location from = p.getBukkitPlayer().getLocation().clone().add(0, 1, 0);
            Location to = applyPlayer.getBukkitPlayer().getLocation().clone().add(0, 1, 0);
            ParticleEffectUtil.drawLine(from, to, 3, loc -> ParticleEffect.VILLAGER_HAPPY.display(0f, 0f, 0f, 0, 1, loc, PARTICLE_RANGE));
        }
        return true;
    }

    @Override
    public double getRange(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_HELPING_HAND_RANGE.evaluateDouble(p);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_HELPING_HAND_HEAL.evaluateInt(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_HELPING_HAND_RANGE.evaluateDouble(p),
                ArterionPlugin.getInstance().getFormulaManager().SKILL_PALADIN_HELPING_HAND_OTHER_HEAL.evaluateInt(p),};
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
