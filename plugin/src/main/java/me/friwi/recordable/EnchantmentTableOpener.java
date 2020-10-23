package me.friwi.recordable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface EnchantmentTableOpener {
    void open(Player player, Location loc, boolean force, boolean enchantAsWeapon);
}
