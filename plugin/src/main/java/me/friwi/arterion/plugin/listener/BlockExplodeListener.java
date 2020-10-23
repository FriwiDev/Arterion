package me.friwi.arterion.plugin.listener;

import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.util.Vector;

public class BlockExplodeListener implements Listener {
    private ArterionPlugin plugin;

    public BlockExplodeListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent evt) {
        this.plugin.getExplosionHandler().handleExplosion(evt.getBlock().getLocation(), Lists.newLinkedList(evt.blockList()), (b, type, data) -> {
            b.setType(Material.AIR, false);
            FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation().clone().add(evt.getBlock().getLocation()).multiply(0.5).add(0, 0.2, 0), type, data);
            fb.setDropItem(false);
            fb.setHurtEntities(false);
            Vector vec = fb.getLocation().toVector().subtract(evt.getBlock().getLocation().toVector()).multiply(0.1);
            vec.setY(0.6 + Math.random() * 0.6);
            fb.setVelocity(vec);
            return fb;
        }, true, true);
        evt.blockList().clear();
    }
}
