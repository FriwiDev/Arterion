package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import io.papermc.lib.PaperLib;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.permissions.TeleportPreconditions;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("spawn|s")
public class SpawnCommand extends BaseCommand {
    private CommandManager commandManager;

    public SpawnCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void spawn(Player sender, String args[]) {
        if (args.length != 0) {
            this.help(sender);
            return;
        }
        ArterionPlayer ep = ArterionPlayerUtil.get(sender);
        if (ep.isArtefactCarrier()) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.spawn.artefact"));
            return;
        }
        if (ep.getGuild() != null && ep.getGuild().isInLocalFight()
                && ep.getGuild().getLocalFight().getDefender().equals(ep.getGuild())
                && ep.getRegion() instanceof GuildRegion) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.spawn.defending"));
            return;
        }
        String reason = TeleportPreconditions.canTeleport(ep);
        if (reason == null) {
            PaperLib.teleportAsync(ep.getBukkitPlayer(), commandManager.getPlugin().getArterionConfig().spawn).thenRun(() -> ep.updateRegion(commandManager.getPlugin().getArterionConfig().spawn));
            commandManager.getPlugin().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                @Override
                public void run() {
                    //Teleport once more to prevent players getting stuck
                    PaperLib.teleportAsync(ep.getBukkitPlayer(), commandManager.getPlugin().getArterionConfig().spawn);
                }
            }, 3l);
            sender.sendMessage(LanguageAPI.translate(sender, "command.spawn.success"));
        } else {
            Combat.sendInCombatMessage(ep, reason);
        }

    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.spawn.help"));
    }
}
