package me.friwi.arterion.plugin.combat.skill;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class ParticleEffectUtil {
    public static void drawLine(Location from, Location to, int perBlock, Consumer<Location> spawnParticle) {
        from = from.clone();
        Vector diff = to.clone().subtract(from).toVector();
        int particles = (int) (diff.length() * perBlock);
        diff.multiply(1 / (particles + 0f));
        for (int i = 0; i < particles; i++) {
            from.add(diff);
            spawnParticle.accept(from);
        }
    }
}
