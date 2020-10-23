package me.friwi.arterion.plugin.combat.gamemode.capturepoint;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.world.region.CapturePointRegion;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CapturePoints {
    public static CapturePoint GRAVE_RUIN, DESERT_TEMPLE;

    public static void init() {
        Long graveRuinUntil = ArterionPlugin.getInstance().getArterionConfig().graveruin_owner_until;
        Guild graveRuinOwner;
        if (graveRuinUntil == null || graveRuinUntil.longValue() == -1) {
            graveRuinOwner = null;
            graveRuinUntil = Long.valueOf(-1);
        } else {
            graveRuinOwner = ArterionPlugin.getInstance().getGuildManager().getGuildByUUID(UUID.fromString(ArterionPlugin.getInstance().getArterionConfig().graveruin_owner));
        }

        Long desertTempleUntil = ArterionPlugin.getInstance().getArterionConfig().deserttemple_owner_until;
        Guild desertTempleOwner;
        if (desertTempleUntil == null || desertTempleUntil.longValue() == -1) {
            desertTempleOwner = null;
            desertTempleUntil = Long.valueOf(-1);
        } else {
            desertTempleOwner = ArterionPlugin.getInstance().getGuildManager().getGuildByUUID(UUID.fromString(ArterionPlugin.getInstance().getArterionConfig().deserttemple_owner));
        }

        GRAVE_RUIN = new CapturePoint("graveruin",
                ArterionPlugin.getInstance().getArterionConfig().graveruin_glass,
                ArterionPlugin.getInstance().getArterionConfig().graveruin_center,
                0, graveRuinOwner, graveRuinUntil.longValue(), (g, l) -> {
            ArterionPlugin.getInstance().getArterionConfig().graveruin_owner = g.getUUID().toString();
            ArterionPlugin.getInstance().getArterionConfig().graveruin_owner_until = l;
            ArterionPlugin.getInstance().saveConfig();
        });
        DESERT_TEMPLE = new CapturePoint("deserttemple",
                ArterionPlugin.getInstance().getArterionConfig().deserttemple_glass,
                ArterionPlugin.getInstance().getArterionConfig().deserttemple_center,
                1, desertTempleOwner, desertTempleUntil.longValue(), (g, l) -> {
            ArterionPlugin.getInstance().getArterionConfig().deserttemple_owner = g.getUUID().toString();
            ArterionPlugin.getInstance().getArterionConfig().deserttemple_owner_until = l;
            ArterionPlugin.getInstance().saveConfig();
        });
        //Create regions
        ArterionPlugin.getInstance().getRegionManager().registerRegion(new CapturePointRegion(GRAVE_RUIN, Bukkit.getWorlds().get(0), -24, -16, -9, -18));
        ArterionPlugin.getInstance().getRegionManager().registerRegion(new CapturePointRegion(DESERT_TEMPLE, Bukkit.getWorlds().get(0), 3, 18, 14, 28));
    }
}
