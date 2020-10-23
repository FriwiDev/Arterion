package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.recordable.ProjectileHitTargetEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ProjectileHitTargetListener implements Listener {
    private ArterionPlugin plugin;

    public ProjectileHitTargetListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHitTarget(ProjectileHitTargetEvent evt) {
        Hooks.PROJECTILE_HIT_TARGET_EVENT_HOOK.execute(evt.getEntity(), evt);
        //Remove projectiles in spawn regions
        Location hit = null;
        if (evt.getHitEntity() != null) hit = evt.getHitEntity().getLocation();
        if (evt.getHitBlock() != null) hit = evt.getHitBlock().getLocation();
        if (evt.getEntity() != null && evt.getEntity().getLocation() != null) hit = evt.getEntity().getLocation();
        if (hit != null) {
            ArterionChunk chunk = ArterionChunkUtil.getNonNull(hit.getChunk());
            if (!chunk.getRegion().isPvp()) {
                if (evt.getEntity() != null) evt.getEntity().remove();
            }
        }
    }
}
