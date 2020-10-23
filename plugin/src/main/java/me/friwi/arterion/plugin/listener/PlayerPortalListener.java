package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothManager;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import me.friwi.arterion.plugin.world.region.PvPRegion;
import me.friwi.arterion.plugin.world.region.Region;
import me.friwi.arterion.plugin.world.region.WildernessRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.security.SecureRandom;
import java.util.Random;

public class PlayerPortalListener implements Listener {
    private final Random RANDOM = new SecureRandom();
    private ArterionPlugin plugin;

    public PlayerPortalListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent evt) {
        ArterionPlayer ap = ArterionPlayerUtil.get(evt.getPlayer());
        if (ap.getLastPortalMessage() + 3000 > System.currentTimeMillis()) {
            evt.setCancelled(true);
            return;
        }
        ap.setLastPortalMessage(System.currentTimeMillis());

        //Handle portal logic for players
        if (ap.isArtefactCarrier()) {
            ap.sendTranslation("portal.noarte");
            evt.setCancelled(true);
            return;
        }
        if (ap.getHealth() < ap.getMaxHealth() && ap.getRegion() != null && ap.getRegion().isPvp()) {
            ap.sendTranslation("portal.nofullhealth");
            evt.setCancelled(true);
            return;
        }
        //Morgoth portal
        if (ap.getBukkitPlayer().getWorld().equals(ArterionPlugin.getInstance().getArterionConfig().morgoth_portal.getWorld())
                && ap.getBukkitPlayer().getLocation().distance(ArterionPlugin.getInstance().getArterionConfig().morgoth_portal) < 20) {
            if (!MorgothManager.isMorgothBlocked() && MorgothDungeonKeyItem.deductKey(ap)) {
                evt.setCancelled(true);
                MorgothManager.onPlayerPassPortal(ap);
                return;
            } else {
                ap.sendTranslation("portal.nokey");
                evt.setCancelled(true);
                return;
            }
        }
        //Wilderness portal
        if (ap.getBukkitPlayer().getWorld().equals(ArterionPlugin.getInstance().getArterionConfig().wilderness_portal.getWorld())
                && ap.getBukkitPlayer().getLocation().distance(ArterionPlugin.getInstance().getArterionConfig().wilderness_portal) < 20) {
            evt.setCancelled(true);
            ap.sendTranslation("portal.wilderness");
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double radius = 600;
            double x = Math.sin(angle) * radius;
            double z = Math.cos(angle) * radius;
            Block b = Bukkit.getWorlds().get(0).getHighestBlockAt((int) x, (int) z);
            while (!b.getType().isSolid()
                    || b.getType() == Material.LEAVES || b.getType() == Material.LEAVES_2
                    || b.getType() == Material.LOG || b.getType() == Material.LOG_2) {
                b = b.getRelative(BlockFace.DOWN);
            }
            while (b.getType().isSolid() || b.getRelative(BlockFace.UP).getType().isSolid()
                    || b.isLiquid() || b.getRelative(BlockFace.UP).isLiquid()) {
                b = b.getRelative(BlockFace.UP);
            }
            ap.getBukkitPlayer().teleport(b.getLocation().add(0.5, 0, 0.5));
            return;
        }
        //World protection
        if (!isPortalFromAllowed(evt.getFrom()) || !isPortalToAllowed(evt.getTo())) {
            evt.setCancelled(true);
            ap.sendTranslation("portal.invalidregion");
            return;
        }
        //Set end spawn
        if (evt.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            Location loc = evt.getTo().getWorld().getSpawnLocation();
            loc = loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()).getLocation().add(0.5, 1, 0.5);
            evt.setTo(loc);
        }
    }

    public boolean isPortalToAllowed(Location loc) {
        return ArterionChunkUtil.getNonNull(loc.getChunk()).getRegion() instanceof WildernessRegion;
    }

    public boolean isPortalFromAllowed(Location loc) {
        Region r = ArterionChunkUtil.getNonNull(loc.getChunk()).getRegion();
        return r instanceof WildernessRegion || r instanceof PvPRegion;
    }
}
