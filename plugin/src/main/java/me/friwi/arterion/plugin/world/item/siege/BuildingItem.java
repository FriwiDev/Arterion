package me.friwi.arterion.plugin.world.item.siege;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.temporaryblock.TemporaryBlockCompound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public interface BuildingItem {
    default void beginDestroy(TemporaryBlockCompound comp, int remove_delay, int remove_speed, Location pushAway, boolean noX, boolean noZ, AtomicBoolean begin, Binding<Location> destroyBinding) {
        LinkedList<Block> blocks = new LinkedList<>();
        blocks.addAll(comp.getAllBlocks());
        Collections.shuffle(blocks);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int ticks = 0;
            boolean unregistered = false;

            @Override
            public void run() {
                if (ticks < remove_delay && !begin.get()) {
                    ticks += remove_speed;
                    return;
                } else if (!unregistered) {
                    if (destroyBinding != null) Hooks.BLOCK_BREAK_EVENT_HOOK.unsubscribe(destroyBinding);
                    unregistered = true;
                }
                if (blocks.isEmpty()) {
                    cancel();
                    return;
                }
                Block rb = blocks.removeFirst();
                Material type = rb.getType();
                comp.rollback(rb);

                if (ArterionPlugin.getInstance().shutdownTime - 30000 > System.currentTimeMillis()) {
                    FallingBlock fb = rb.getWorld().spawnFallingBlock(rb.getLocation().add(0.5, 0, 0.5), type.getId(), (byte) 0);
                    fb.setDropItem(false);
                    fb.setHurtEntities(false);
                    Vector vec = fb.getLocation().toVector().subtract(pushAway.toVector()).multiply(0.1);
                    vec.setY(0.25);
                    if (noX) vec.setX(0);
                    if (noZ) vec.setZ(0);
                    fb.setVelocity(vec);
                    Hooks.FALLING_BLOCK_HIT_GROUND_HOOK.subscribe(fb, evt1 -> {
                        fb.remove();
                        ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(fb.getMaterial(), fb.getBlockData()), 0, 0, 0, 1, 20, fb.getLocation(), 30);
                        fb.getWorld().playSound(fb.getLocation(), Sound.DIG_GRASS, 1f, 1f);
                        evt1.setCancelled(true);
                        return evt1;
                    });
                }
            }
        }, 1, remove_speed);
    }
}
