package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class DungeonMobSpawner {
    private static final int ACTIVATION_RANGE_SQUARE = 45 * 45;

    private Location loc;
    private int initial;
    private int everyNTicks;
    private EntityType mobType;
    private boolean spawned_initial;

    public DungeonMobSpawner(Location loc, int initial, int perMinute, EntityType mobType) {
        this.loc = loc;
        this.initial = initial;
        this.everyNTicks = (60 * 20) / perMinute;
        this.mobType = mobType;
    }

    public void tick(MorgothDungeonFight fight, long tick) {
        if (!hasNearbyPlayers(fight)) return;
        if (!spawned_initial) {
            spawned_initial = true;
            for (int i = 0; i < initial; i++) {
                loc.getWorld().spawn(loc, mobType.getEntityClass());
            }
        } else if (tick % everyNTicks == 0) {
            loc.getWorld().spawn(loc, mobType.getEntityClass());
        }
    }

    public boolean hasNearbyPlayers(MorgothDungeonFight fight) {
        for (ArterionPlayer p : fight.getTeam()) {
            if (p.getBukkitPlayer().getWorld().equals(loc.getWorld()) && p.getBukkitPlayer().getLocation().distanceSquared(loc) <= ACTIVATION_RANGE_SQUARE) {
                return true;
            }
        }
        return false;
    }
}
