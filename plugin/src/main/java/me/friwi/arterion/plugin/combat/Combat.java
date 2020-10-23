package me.friwi.arterion.plugin.combat;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.world.villager.CustomVillagerUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.*;

import java.util.List;

public class Combat {
    /**
     * Checks if player is in combat with reason
     *
     * @param p
     * @return null, fire, water, monster, player
     */
    public static String isPlayerInCombat(ArterionPlayer p) {
        return isPlayerInCombat(p, false, true, true);
    }

    /**
     * Checks if player is in combat with reason
     *
     * @param p
     * @return null, fire, water, monster, player
     */
    public static String isPlayerInCombat(ArterionPlayer p, boolean isBlockPlace, boolean withEnvironmental, boolean withMobCheck) {
        if (p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE || p.getBukkitPlayer().getGameMode() == GameMode.SPECTATOR || p.isVanished())
            return null;
        if (p.getRegion() == null) return null;
        if (!p.getRegion().isPvp()) return null;
        if (!isBlockPlace && p.getBukkitPlayer().getFireTicks() > 0) return "fire";
        if (withEnvironmental && p.getBukkitPlayer().getRemainingAir() < p.getBukkitPlayer().getMaximumAir())
            return "water";
        double mobdistance = ArterionPlugin.getInstance().getFormulaManager().PLAYER_COMBAT_MOBDISTANCE.evaluateDouble();
        double playerdistance = ArterionPlugin.getInstance().getFormulaManager().PLAYER_COMBAT_PLAYERDISTANCE.evaluateDouble();
        if (p.isArtefactCarrier())
            playerdistance = ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_CARRIER_COMBAT_DISTANCE.evaluateDouble();
        List<Entity> entities;
        if (withMobCheck) {
            entities = p.getBukkitPlayer().getNearbyEntities(mobdistance, 15, mobdistance);
            for (Entity entity : entities) {
                if (entity instanceof Monster) {
                    if (((Monster) entity).getTarget() != null && ((Monster) entity).getTarget().equals(p.getBukkitPlayer())) {
                        return "monster";
                    }
                }
            }
        }
        entities = p.getBukkitPlayer().getNearbyEntities(playerdistance, playerdistance, playerdistance);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                ArterionPlayer other = ArterionPlayerUtil.get((Player) entity);
                if (p.getPlayerRelation(other) == PlayerRelation.ENEMY) return "player";
            }
        }
        return null;
    }

    public static void sendInCombatMessage(ArterionPlayer player, String msg) {
        player.sendTranslation("combat.incombat." + msg);
    }

    public static boolean isEnemy(ArterionPlayer p, LivingEntity other) {
        if (other instanceof Player) {
            return p.getPlayerRelation(ArterionPlayerUtil.get((Player) other)) == PlayerRelation.ENEMY;
        } else if (other instanceof Villager) {
            //When villager type is not null, the villager is custom and thus not an enemy
            if (CustomVillagerUtil.getVillagerType(other) != null) return false;
            //Combat logging villagers
            Guild g = ArterionPlugin.getInstance().getCombatLoggingHandler().getGuild(other);
            if (g != null) {
                if (g.equals(p.getGuild())) {
                    return false;
                }
            }
            DatabasePlayer x = ArterionPlugin.getInstance().getCombatLoggingHandler().getDatabasePlayer(other);
            if (x != null && x.getRoomMate() != null) {
                if (x.getRoomMate().equals(p.getBukkitPlayer().getUniqueId())) {
                    return false;
                }
            }
            return true;
        } else if (other instanceof ArmorStand) {
            return ((ArmorStand) other).isVisible();
        } else {
            return true;
        }
    }
}
