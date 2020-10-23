package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.util.patch.SpigotPatcher;
import org.bukkit.entity.Player;

public class ArterionPlayerUtil {
    public static ArterionPlayer get(Player p) {
        try {
            return (ArterionPlayer) SpigotPatcher.playerAccess.get(p);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void set(Player p, ArterionPlayer ep) {
        try {
            SpigotPatcher.playerAccess.set(p, ep);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
