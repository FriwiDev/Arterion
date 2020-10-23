package me.friwi.arterion.plugin.world.block.nonbtblocks;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;

public abstract class SpecialBlock {
    private Location location;

    public SpecialBlock(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void applyToBlock(Block block) {
        block.setType(Material.ENDER_PORTAL_FRAME);
    }

    public abstract boolean onInteract(ArterionPlayer player, Action action);

    public abstract boolean onBreak(ArterionPlayer player);
}
