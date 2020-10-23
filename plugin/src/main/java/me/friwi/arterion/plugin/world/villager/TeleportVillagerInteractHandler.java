package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Location;

public class TeleportVillagerInteractHandler implements VillagerInteractHandler {
    private Location tp;

    public TeleportVillagerInteractHandler(Location tp) {
        this.tp = tp;
    }

    @Override
    public void handleInteract(ArterionPlayer ep) {
        ep.sendTranslation("villager.teleported");
        ep.getBukkitPlayer().teleport(tp);
    }
}
