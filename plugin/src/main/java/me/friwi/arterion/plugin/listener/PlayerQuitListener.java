package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerQuitListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        evt.setQuitMessage("");

        //Process quit
        Player bukkitPlayer = evt.getPlayer();
        ArterionPlayer ep = ArterionPlayerUtil.get(bukkitPlayer);
        if (ep != null) {
            //Abort guild fight actions
            plugin.getGuildFightManager().onDeathOrLeave(ep);
            //Process player itself
            ep.onQuit();
        }

        //Guild
        plugin.getGuildManager().onPlayerLeave(ep);

        //Group
        plugin.getGroupSystem().onPlayerQuit(ep);

        //Arterion player list
        ArterionPlugin.getOnlinePlayers().remove(bukkitPlayer);

        //Update header and footer player count
        plugin.getTablistManager().updateTablistHeaderFooter();

        //External fights
        boolean enableVillagerSpawn = true;
        ExternalFight fight = ArterionPlugin.getInstance().getExternalFightManager().getFightByPlayer(ep);
        if (fight != null) {
            enableVillagerSpawn = fight.onQuit(ep, true);
        }

        //Combat logging
        if (enableVillagerSpawn) plugin.getCombatLoggingHandler().handleLogout(ep);
    }
}
