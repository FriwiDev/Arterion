package me.friwi.arterion.plugin.combat.skill.impl.forestrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.PassiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.entity.Player;

public class HeadshotSkill extends PassiveSkill<HeadshotSkill.HeadshotSkillContainerData> {

    public HeadshotSkill() {
        super(ClassEnum.FORESTRUNNER);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_HEADSHOT_INCREASE.evaluateDouble(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with damage hook
        Binding<Player> binding = Hooks.PLAYER_ARROW_ATTACK_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), tuple -> {
            double diff = tuple.getFirstValue().getLocation().getY() - tuple.getSecondValue().getLocation().getY() - tuple.getSecondValue().getEyeHeight();
            if (diff > -0.25) {
                //Was a headshot
                int maxhealth = (int) tuple.getSecondValue().getMaxHealth();
                if (tuple.getSecondValue() instanceof Player) {
                    ArterionPlayer hit = ArterionPlayerUtil.get((Player) tuple.getSecondValue());
                    maxhealth = hit.getMaxHealth();
                }
                double headshotBonus = ArterionPlugin.getInstance().getFormulaManager().SKILL_FORESTRUNNER_HEADSHOT_INCREASE.evaluateDouble(p) / 100d;
                double bonus = (headshotBonus * maxhealth);
                if (bonus > 130) bonus = 130;
                tuple.setThirdValue(tuple.getThirdValue() + bonus);

                ParticleEffect.CRIT.display(0.5f, 0.5f, 0.5f, 0.05f, 12, tuple.getSecondValue().getEyeLocation(), PARTICLE_RANGE);
            }
            return tuple;
        });
        setSkillDataContainer(p, new HeadshotSkillContainerData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        HeadshotSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PLAYER_ARROW_ATTACK_DAMAGE_DEALT_HOOK.unsubscribe(data.binding);
        }
    }

    protected class HeadshotSkillContainerData extends SkillContainerData {
        private Binding<Player> binding;

        private HeadshotSkillContainerData(Binding<Player> binding) {
            this.binding = binding;
        }
    }
}
