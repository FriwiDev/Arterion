package me.friwi.arterion.plugin.listener;

import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

public class EntityExplodeListener implements Listener {
    private ArterionPlugin plugin;

    public EntityExplodeListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent evt) {
        //Handle entity explode event hook
        if (Hooks.ENTITY_EXPLODE_EVENT_HOOK.execute(evt.getEntity(), evt) == null) {
            return;
        }
        this.plugin.getExplosionHandler().handleExplosion(evt.getLocation(), Lists.newLinkedList(evt.blockList()), (b, type, data) -> {
            b.setType(Material.AIR, false);
            FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation().clone().add(evt.getLocation()).multiply(0.5).add(0, 0.2, 0), type, data);
            fb.setDropItem(false);
            fb.setHurtEntities(false);
            Vector vec = fb.getLocation().toVector().subtract(evt.getLocation().toVector()).multiply(0.1);
            vec.setY(0.6 + Math.random() * 0.6);
            fb.setVelocity(vec);
            return fb;
        }, true, true);
        evt.blockList().clear();
    }
}
