package me.friwi.recordable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface AnvilOpener {
    void open(Player player, Location loc, boolean force, boolean enchantAsWeapon, Consumer<Integer> priceAnnounce);
}
