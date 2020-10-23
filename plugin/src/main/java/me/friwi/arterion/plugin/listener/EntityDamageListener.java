package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.Region;
import me.friwi.arterion.plugin.world.villager.CustomVillagerUtil;
import me.friwi.arterion.plugin.world.villager.VillagerType;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    private ArterionPlugin plugin;

    public EntityDamageListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent evt) {
        //World protection
        if (evt instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) evt).getDamager() instanceof Player
                && (evt.getEntity() instanceof ArmorStand || evt.getEntity() instanceof ItemFrame)) {
            Region region = ArterionChunkUtil.getNonNull(evt.getEntity().getLocation().getChunk()).getRegion();
            ArterionPlayer player = ArterionPlayerUtil.get((Player) ((EntityDamageByEntityEvent) evt).getDamager());
            if (!region.canPlayerBuild(player) && !region.isModifyEntities()) {
                evt.setCancelled(true);
                player.sendTranslation("region.nobuild");
                return;
            }
        }

        //Block damage against friendlies and in peaceful zones
        if (evt.getEntity() instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) evt.getEntity());
            if (p.getRegion() != null && !p.getRegion().isPvp()) {
                evt.setCancelled(true);
                return;
            }
            if (evt instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) evt).getDamager();
                if (damager instanceof Projectile) {
                    if (((Projectile) damager).getShooter() != null && ((Projectile) damager).getShooter() instanceof Entity) {
                        damager = (Entity) ((Projectile) damager).getShooter();
                    }
                }
                if (damager instanceof Player) {
                    ArterionPlayer other = ArterionPlayerUtil.get((Player) damager);
                    if (p.getPlayerRelation(other) != PlayerRelation.ENEMY) {
                        //Player can not damage neutrals and friendlies
                        evt.setCancelled(true);
                        return;
                    }
                }
            }
        }

        //Custom Villager
        if (evt instanceof EntityDamageByEntityEvent) {
            VillagerType villagerType = CustomVillagerUtil.getVillagerType(evt.getEntity());
            if (villagerType != null) {
                if (!(((EntityDamageByEntityEvent) evt).getDamager() instanceof Player) || ((Player) ((EntityDamageByEntityEvent) evt).getDamager()).getGameMode() != GameMode.CREATIVE) {
                    evt.setCancelled(true);
                    return;
                }
            }
        }

        //Else handle damage
        plugin.getDamageManager().handle(evt);
    }
}
