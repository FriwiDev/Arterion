package me.friwi.arterion.plugin.listener;

import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {
    public static boolean isSpawningWithCommand = false;
    public static boolean forceSpawn = false;
    private ArterionPlugin plugin;

    public CreatureSpawnListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent evt) {
        //Only handle living entities that are not players
        if (evt.getEntity() instanceof Player) {
            return;
        }

        if (evt.getEntityType() == EntityType.IRON_GOLEM) {
            evt.setCancelled(true);
            return;
        }

        //Block nether ceiling exploit
        if (evt.getEntity().getWorld().getEnvironment() == World.Environment.NETHER && evt.getEntity().getLocation().getY() >= 126) {
            evt.setCancelled(true);
            return;
        }

        //Do not intercept forced entities
        if (forceSpawn) return;

        //Prevent mob spawn in protected regions
        if (evt.getEntity() instanceof Monster) {
            if (!ArterionChunkUtil.getNonNull(evt.getLocation().getChunk()).getRegion().isMobSpawn()) {
                evt.setCancelled(true);
                return;
            }
        }

        //Prevent animal spawn in protected regions
        if (evt.getEntity() instanceof Animals || evt.getEntity() instanceof WaterMob) {
            if (!ArterionChunkUtil.getNonNull(evt.getLocation().getChunk()).getRegion().isAnimalSpawn()) {
                evt.setCancelled(true);
            }
        }

        //Don't patch or alter custom entities
        if (isSpawningWithCommand) return;

        if (evt.getEntity().getType() == EntityType.BAT) {
            if (Math.random() > 0.3) {
                evt.setCancelled(true);
                return;
            }
        }

        if (evt.getEntity() instanceof Wither || evt.getEntity() instanceof Snowman || evt.getEntity() instanceof IronGolem) {
            plugin.getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    apply(evt.getEntity(), evt.getSpawnReason());
                }
            }, 1);
        } else {
            apply(evt.getEntity(), evt.getSpawnReason());
        }
    }

    private void apply(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        //Patch entity
        Entity patched = NBTInjector.patchEntity(entity);

        //Apply max health if living entity
        if (patched instanceof LivingEntity) {
            plugin.getDamageManager().applyMaxHealth((LivingEntity) patched, reason);
        }
    }
}
