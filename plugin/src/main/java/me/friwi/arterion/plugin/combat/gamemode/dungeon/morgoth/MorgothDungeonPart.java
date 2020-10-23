package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public abstract class MorgothDungeonPart {
    private long tick = 0;
    private MorgothDungeonFight fight;

    public MorgothDungeonPart(MorgothDungeonFight fight) {
        this.fight = fight;
    }

    public void onTick(MorgothDungeonFight fight) {
        if (isGoalFulfilled(fight.team)) {
            fight.setPart(next());
            return;
        }
        tick(fight, tick);
        tick++;
    }

    public boolean isAnyPlayerBetween(Team team, Location loc1, Location loc2) {
        double xmin = Math.min(loc1.getX(), loc2.getX());
        double ymin = Math.min(loc1.getY(), loc2.getY());
        double zmin = Math.min(loc1.getZ(), loc2.getZ());
        double xmax = Math.max(loc1.getX(), loc2.getX());
        double ymax = Math.max(loc1.getY(), loc2.getY());
        double zmax = Math.max(loc1.getZ(), loc2.getZ());
        for (ArterionPlayer p : team) {
            if (p.getBukkitPlayer().getLocation().getWorld().equals(loc1.getWorld())) {
                Location c = p.getBukkitPlayer().getLocation();
                if (c.getX() >= xmin && c.getX() <= xmax
                        && c.getY() >= ymin && c.getY() <= ymax
                        && c.getZ() >= zmin && c.getZ() <= zmax) {
                    return true;
                }
            }
        }
        return false;
    }

    public MorgothDungeonFight getFight() {
        return fight;
    }

    public Location at(double x, double y, double z) {
        return new Location(getFight().getWorld(), x, y, z);
    }

    public Location at(double x, double y, double z, float yaw, float pitch) {
        return new Location(getFight().getWorld(), x, y, z, yaw, pitch);
    }

    public Location at(double x, double y, double z, float yaw) {
        return new Location(getFight().getWorld(), x, y, z, yaw, 0);
    }

    public Location at(double x, double y, double z, BlockFace blockFace) {
        float yaw = 0;
        switch (blockFace) {
            case NORTH:
                yaw = 180;
                break;
            case EAST:
                yaw = 270;
                break;
            case WEST:
                yaw = 90;
                break;
        }
        return new Location(getFight().getWorld(), x, y, z, yaw, 0);
    }

    public abstract void tick(MorgothDungeonFight fight, long tick);

    public abstract MorgothDungeonPart next();

    public abstract boolean isGoalFulfilled(Team team);
}
