package net.badlion.timers.listeners;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import net.badlion.timers.BadlionTimers;
import net.badlion.timers.impl.NmsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BadlionTimerListener implements Listener {

    private final BadlionTimers plugin;

    public BadlionTimerListener(BadlionTimers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        NmsManager.sendPluginMessage(event.getPlayer(), BadlionTimers.CHANNEL_NAME, "REGISTER|{}".getBytes(BadlionTimers.UTF_8_CHARSET));
        NmsManager.sendPluginMessage(event.getPlayer(), BadlionTimers.CHANNEL_NAME, "CHANGE_WORLD|{}".getBytes(BadlionTimers.UTF_8_CHARSET));
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        this.plugin.getTimerApi().clearTimers(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            NmsManager.sendPluginMessage(event.getPlayer(), BadlionTimers.CHANNEL_NAME, "CHANGE_WORLD|{}".getBytes(BadlionTimers.UTF_8_CHARSET));

            //Arterion stuff
            ArterionPlayer ap = ArterionPlayerUtil.get(event.getPlayer());
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    ap.getSkillSlots().reapplyBadlionTimers();
                    ap.getPlayerScoreboard().syncModValues();
                }
            }, 15l);
            ExternalFight fight = ArterionPlugin.getInstance().getExternalFightManager().getFightByPlayer(ap);
            if (fight != null && fight.getWorld() != null && event.getFrom().getWorld().equals(fight.getWorld())) {
                fight.onQuit(ap, false);
            }
        }
    }
}
