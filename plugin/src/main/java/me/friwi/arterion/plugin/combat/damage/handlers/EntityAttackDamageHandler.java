package me.friwi.arterion.plugin.combat.damage.handlers;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.damage.DamageHandler;
import me.friwi.arterion.plugin.combat.damage.DamageManager;
import me.friwi.arterion.plugin.combat.damage.PotionDamageBoostCalculator;
import me.friwi.arterion.plugin.combat.damage.WeaponDamageCalculator;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.hook.Tuple;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class EntityAttackDamageHandler implements DamageHandler {

    private Random random = new Random();

    @Override
    public void handleDamage(Entity damaged, Entity damager, EntityDamageEvent evt) {
        if (damager == null) return;
        double damageboost = 1;
        if (damager instanceof LivingEntity) {
            damageboost = 1 + PotionDamageBoostCalculator.calculateDamageBoost((LivingEntity) damager) / 100d;
        }
        double damage = 0;
        if (damager instanceof Player) {
            boolean isCritical =
                    damager.getFallDistance() > 0
                            && !damager.isOnGround()
                            && damager.getLocation().getBlock().getType() != Material.LADDER
                            && damager.getLocation().getBlock().getType() != Material.VINE
                            && !damager.getLocation().getBlock().isLiquid()
                            && !((Player) damager).hasPotionEffect(PotionEffectType.BLINDNESS)
                            && damager.getVehicle() == null
                            && !((Player) damager).isSprinting();
            ArterionPlayer p = ArterionPlayerUtil.get((Player) damager);
            ItemStack used = ((Player) damager).getItemInHand();
            if (used != null) {
                if (p == null) return;
                if (p.getSelectedClass() == null) {
                    used = null;
                    p.sendTranslation("wrongweapon.none");
                } else if (!p.getSelectedClass().isWeaponAllowed(used.getType())) {
                    boolean isWeapon = false;
                    for (ClassEnum cl : ClassEnum.values()) {
                        if (cl.isWeaponAllowed(used.getType())) {
                            isWeapon = true;
                            break;
                        }
                    }
                    if (isWeapon) p.sendTranslation("wrongweapon." + p.getSelectedClass().name().toLowerCase());
                    used = null;
                }
            }
            damage = WeaponDamageCalculator.calculateDamage(used, damaged, isCritical);
            //Apply prestige point
            damage *= ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_ATTACK.evaluateDouble(p.getPointsAttack());
            //Stats
            if (p.getSelectedClass() != null && p.getSelectedClass() != ClassEnum.NONE && p.getBukkitPlayer().getItemInHand() != null) {
                if (p.getSelectedClass().isWeaponAllowed(p.getBukkitPlayer().getItemInHand().getType()) && p.getBukkitPlayer().getItemInHand().getType() != Material.BOW) {
                    p.trackStatistic(StatType.HITS, p.getBukkitPlayer().getItemInHand().getType().getId(), v -> v + 1);
                }
            }
        } else {
            ArterionFormula entityDamage = ArterionPlugin.getInstance().getFormulaManager().DMG_ENTITY_ATTACK.get(DamageManager.entityTypeToString(damager));
            if (entityDamage != null && entityDamage.isDeclared()) {
                damage = entityDamage.evaluateDouble(random.nextDouble());
            }
            if (damager instanceof HumanEntity && !(damager instanceof WitherSkull)) { //Weapon in hand damage does not count for wither skull
                if (((HumanEntity) damager).getItemInHand() != null) {
                    damage = WeaponDamageCalculator.calculateDamage(((HumanEntity) damager).getItemInHand(), damaged);
                }
            }
        }

        //Apply potion damage boost
        damage *= damageboost;

        //Damage dealer hook
        damage = Hooks.PRIMARY_ATTACK_DAMAGE_DEALT_HOOK.execute(damager, new Tuple<>(damaged, damage)).getSecondValue();

        //Damage target hook
        damage = Hooks.PRIMARY_ATTACK_DAMAGE_RECEIVE_HOOK.execute(damaged, new Tuple<>(damager, damage)).getSecondValue();

        DamageManager.applyDamage(evt, damage, false);
    }
}
