package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import io.papermc.lib.PaperLib;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.permissions.TeleportPreconditions;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("home")
public class HomeCommand extends BaseCommand {
    private CommandManager commandManager;

    public HomeCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void home(Player sender, String args[]) {
        if (args.length != 0) {
            this.help(sender);
            return;
        }
        ArterionPlayer ep = ArterionPlayerUtil.get(sender);
        String reason = TeleportPreconditions.canTeleport(ep);
        if (reason == null) {
            if (ep.getGuild() == null && ep.getHomeLocation() != null) {
                if (ep.getHomeLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid() || ep.getHomeLocation().getBlock().getRelative(BlockFace.UP, 2).getType().isSolid()) {
                    ep.sendTranslation("command.home.obstructed");
                    return;
                }
                if (ep.getGuild() != null && ep.getGuild().getRegion().isNoEnter(ep)) {
                    return;
                }
                PaperLib.teleportAsync(ep.getBukkitPlayer(), ep.getHomeLocation().add(0.5, 1.2, 0.5)).thenRun(() -> ep.updateRegion(ep.getHomeLocation().add(0.5, 1.2, 0.5)));
                commandManager.getPlugin().getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        //Teleport once more to prevent players getting stuck
                        PaperLib.teleportAsync(ep.getBukkitPlayer(), ep.getHomeLocation().add(0.5, 1.2, 0.5));
                    }
                }, 3l);
                ep.sendTranslation("command.home.success");
            } else if (ep.getGuild() == null && ep.getBukkitPlayer().getBedSpawnLocation() != null) {
                Location loc = ep.getBukkitPlayer().getBedSpawnLocation().clone();
                loc.setWorld(Bukkit.getWorlds().get(0));
                if (loc.getBlock().getType().isSolid() || loc.getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                    ep.sendTranslation("command.home.obstructed");
                    return;
                }
                PaperLib.teleportAsync(ep.getBukkitPlayer(), loc).thenRun(() -> ep.updateRegion(loc));
                commandManager.getPlugin().getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        //Teleport once more to prevent players getting stuck
                        PaperLib.teleportAsync(ep.getBukkitPlayer(), loc);
                    }
                }, 3l);
                ep.sendTranslation("command.home.success");
            } else if (ep.getGuild() != null && ep.getGuild().getHomeLocation() != null) {
                //Check for tp cooldown on defense
                if (ep.getGuild().getLocalFight() != null && ep.getGuild().getLocalFight().getDefender().equals(ep.getGuild())) {
                    Long cd = ep.getGuild().getLocalFight().getHomeCd().get(ep.getBukkitPlayer().getUniqueId());
                    if (cd != null && cd.longValue() > System.currentTimeMillis() - ArterionPlugin.getInstance().getFormulaManager().FIGHT_GUILD_HOME_CD.evaluateInt()) {
                        //Still on cooldown
                        int seconds = (int) ((ArterionPlugin.getInstance().getFormulaManager().FIGHT_GUILD_HOME_CD.evaluateInt() - System.currentTimeMillis() + cd.longValue()) / 1000);
                        ep.sendTranslation("command.home.cooldown", seconds);
                        return;
                    } else {
                        ep.getGuild().getLocalFight().getHomeCd().put(ep.getBukkitPlayer().getUniqueId(), System.currentTimeMillis());
                    }
                }
                PaperLib.teleportAsync(ep.getBukkitPlayer(), ep.getGuild().getHomeLocation().add(0.5, 2, 0.5)).thenRun(() -> ep.updateRegion(ep.getGuild().getHomeLocation().add(0.5, 2, 0.5)));
                commandManager.getPlugin().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        //Teleport once more to prevent players getting stuck
                        PaperLib.teleportAsync(ep.getBukkitPlayer(), ep.getGuild().getHomeLocation().add(0.5, 2, 0.5));
                    }
                }, 3l);
                ep.sendTranslation("command.home.success");
            } else {
                ep.sendTranslation("command.home.nohome");
            }
        } else {
            Combat.sendInCombatMessage(ep, reason);
        }

    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.home.help"));
    }
}
