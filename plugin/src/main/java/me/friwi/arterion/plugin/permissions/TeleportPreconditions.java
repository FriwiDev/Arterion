package me.friwi.arterion.plugin.permissions;

import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.GameMode;

public class TeleportPreconditions {
    public static String canTeleport(ArterionPlayer player) {
        return canTeleport(player, true);
    }

    public static String canTeleport(ArterionPlayer player, boolean withEnvironmental) {
        if (player.getBukkitPlayer().getGameMode() == GameMode.CREATIVE || player.getBukkitPlayer().getGameMode() == GameMode.SPECTATOR || player.isVanished())
            return null;
        if (!player.getRegion().isPvp()) return null;
        if (withEnvironmental && player.getBukkitPlayer().getFallDistance() > 0 && !player.getBukkitPlayer().getLocation().getBlock().isLiquid())
            return "flight";
        return Combat.isPlayerInCombat(player, false, withEnvironmental, true);
    }
}
