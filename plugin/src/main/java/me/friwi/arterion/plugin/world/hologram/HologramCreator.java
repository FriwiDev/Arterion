package me.friwi.arterion.plugin.world.hologram;

import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class HologramCreator {
    private static final double LINE_HEIGHT = 0.25;
    private static final double INITIAL_OFFSET = -0.8;

    public static ArmorStand createHologram(Location loc, String text) {
        CreatureSpawnListener.isSpawningWithCommand = true;
        ArmorStand s = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        CreatureSpawnListener.isSpawningWithCommand = false;
        s.setSmall(true);
        s.setVisible(false);
        s.setRemoveWhenFarAway(false);
        s.setCustomName(text);
        s.setCustomNameVisible(true);
        s.setNoDamageTicks(Short.MAX_VALUE);
        s.setGravity(false);
        return s;
    }

    public static ArmorStand[] createHologram(Location loc, String[] text) {
        loc = loc.clone().add(0, (text.length - 1) * LINE_HEIGHT + INITIAL_OFFSET, 0);
        ArmorStand[] s = new ArmorStand[text.length];
        for (int i = 0; i < s.length; i++) {
            s[i] = createHologram(loc, text[i]);
            loc.add(0, -LINE_HEIGHT, 0);
        }
        return s;
    }
}
