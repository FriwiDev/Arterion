package me.friwi.arterion.plugin.combat.damage.handlers;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.hook.TriTuple;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

public class ProjectileDamageHandler implements DamageHandler {

    private Random random = new Random();

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        if (damager == null) return;
        if (damager instanceof Projectile) {
            damager.remove();
            if (((Projectile) damager).getShooter() == null || !(((Projectile) damager).getShooter() instanceof Entity)) {
                //Default damage
                evt.setDamage(evt.getDamage() * 0.2);
                DamageManager.applyDefaultDamage(evt);
                return;
            }
            if (damager instanceof Arrow) {
                //The damage on the arrow is precalculated
                NBTCompound ent = NBTInjector.getNbtData(damager);
                if (ent == null) {
                    //Default damage
                    DamageManager.applyDefaultDamage(evt);
                } else {
                    double damage = ent.hasKey("art_dmg") ? ent.getDouble("art_dmg") : evt.getDamage();
                    if (((Arrow) damager).getShooter() instanceof Player) {
                        //Damage dealer hook
                        damage = Hooks.PLAYER_ARROW_ATTACK_DAMAGE_DEALT_HOOK
                                .execute((Player) ((Arrow) damager).getShooter(),
                                        new TriTuple<>((Arrow) damager, (LivingEntity) damaged, damage))
                                .getThirdValue();
                        //Stats
                        ArterionPlayer p = ArterionPlayerUtil.get((Player) ((Arrow) damager).getShooter());
                        if (p != null && p.getSelectedClass() == ClassEnum.FORESTRUNNER) {
                            p.trackStatistic(StatType.HITS, Material.BOW.getId(), v -> v + 1);
                        }
                    }
                    DamageManager.applyDamage(evt, damage, false);
                }
            } else {
                ArterionFormula entityDamage = ArterionPlugin.getInstance().getFormulaManager().DMG_ENTITY_ATTACK.get(((Entity) ((Projectile) damager).getShooter()).getType().toString());
                double damage = 0;
                if (entityDamage != null && entityDamage.isDeclared()) {
                    damage = entityDamage.evaluateDouble(random.nextDouble());
                }
                DamageManager.applyDamage(evt, damage, false);
            }
        }
    }
}
