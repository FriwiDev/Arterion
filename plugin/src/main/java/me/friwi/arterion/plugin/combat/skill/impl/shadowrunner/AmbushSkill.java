package me.friwi.arterion.plugin.combat.skill.impl.shadowrunner;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.PassiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class AmbushSkill extends PassiveSkill<AmbushSkill.AmbushSkillContainerData> {
    public static final double TRESHOLD = 0.9;

    public AmbushSkill() {
        super(ClassEnum.SHADOWRUNNER);
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_AMBUSH_INCREASE.evaluateDouble(p)};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        //Register with damage hook
        Binding<Entity> binding = Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.subscribe(p.getBukkitPlayer(), tuple -> {
            if (tuple.getFirstValue() instanceof LivingEntity) {
                Vector dir1 = p.getBukkitPlayer().getLocation().getDirection();
                Vector dir2 = tuple.getFirstValue().getLocation().getDirection();
                dir1.setY(0).normalize();
                dir2.setY(0).normalize();
                if (dir1.dot(dir2) > 0) {
                    if (dir1.crossProduct(dir2).length() < TRESHOLD) { //Watch out! crossProduct modifies vector!
                        double boost = ArterionPlugin.getInstance().getFormulaManager().SKILL_SHADOWRUNNER_AMBUSH_INCREASE.evaluateDouble(p);

                        tuple.setSecondValue(tuple.getSecondValue() * (1 + boost / 100d));

                        ParticleEffect.ParticleColor color = new ParticleEffect.OrdinaryColor(155, 0, 0);
                        Location loc = tuple.getFirstValue().getLocation().clone().add(0, ((LivingEntity) tuple.getFirstValue()).getEyeHeight(), 0);

                        int divider = 8;
                        double radius = 0.25;
                        double angle = Math.PI * 2 / divider;

                        for (int i = 0; i < divider; i++) {
                            double x = Math.sin(angle * i) * radius;
                            double z = Math.cos(angle * i) * radius;
                            ParticleEffect.REDSTONE.display(color, loc.add(x, 0, z), PARTICLE_RANGE);
                            loc.add(-x, 0, -z); //Reset location
                            ParticleEffect.REDSTONE.display(color, loc.add(x / 2, 0, z / 2), PARTICLE_RANGE);
                            loc.add(-x / 2, 0, -z / 2); //Reset location
                        }
                    }
                }
            }
            return tuple;
        });
        setSkillDataContainer(p, new AmbushSkillContainerData(binding));
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        AmbushSkillContainerData data = getSkillDataContainer(p);
        if (data != null) {
            Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.unsubscribe(data.binding);
        }
    }

    protected class AmbushSkillContainerData extends SkillContainerData {
        private Binding<Entity> binding;

        private AmbushSkillContainerData(Binding<Entity> binding) {
            this.binding = binding;
        }
    }
}
