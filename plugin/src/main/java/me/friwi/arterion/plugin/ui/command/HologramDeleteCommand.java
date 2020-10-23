package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CommandAlias("hologramdelete|hd")
public class HologramDeleteCommand extends BaseCommand {
    private CommandManager commandManager;

    public HologramDeleteCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void hd(Player sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        for (Entity e : sender.getNearbyEntities(1, 1, 1)) {
            if (e instanceof ArmorStand) {
                e.remove();
            }
        }

        sender.sendMessage(LanguageAPI.translate(sender, "command.hd.deleted"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.hd.help"));
    }
}
