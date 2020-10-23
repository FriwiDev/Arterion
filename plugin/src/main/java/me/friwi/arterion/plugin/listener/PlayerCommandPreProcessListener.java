package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreProcessListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerCommandPreProcessListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent evt) {
        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());
        if (ep == null) {
            evt.getPlayer().sendMessage("\247cError while processing your command.");
            evt.setCancelled(true);
            return;
        }
        String command = evt.getMessage().split(" ")[0].toLowerCase().substring(1);
        Command cmd = plugin.getCommandManager().getCommand(command);
        if (cmd == null || !cmd.testPermissionSilent(evt.getPlayer())) {
            ep.sendTranslation("command.notavailable");
            evt.setCancelled(true);
            return;
        }
    }
}
