package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerRespawnListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        //Update health info
        ArterionPlayer ap = ArterionPlayerUtil.get(evt.getPlayer());
        if (ap != null) {
            //Reset player mana
            ap.setMana(0);

            //Reset skills
            ap.getSkillSlots().resetSkillSlots();

            //Respawn at spawn
            Location respawn = ArterionPlugin.getInstance().getArterionConfig().spawn.clone();

            //External fights
            ExternalFight fight = ap.getRespawnFight();
            if (fight != null && fight.getWorld() != null) {
                Location suggested = fight.onRespawn(ap);
                if (suggested != null) respawn = suggested;
            }

            //Apply respawn location
            evt.setRespawnLocation(respawn);
            ap.updateRegion(respawn);

            //Update player health after 1 tick to prevent it from glitching - set to one heart
            plugin.getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    evt.getPlayer().setHealth(1 / 20d * evt.getPlayer().getMaxHealth());
                    ap.getPlayerScoreboard().updateHealth();
                }
            }, 1);

            //Update badlion timers
            plugin.getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    ap.getSkillSlots().reapplyBadlionTimers();
                    ap.getPlayerScoreboard().syncModValues();
                }
            }, 1);
        }
    }
}
