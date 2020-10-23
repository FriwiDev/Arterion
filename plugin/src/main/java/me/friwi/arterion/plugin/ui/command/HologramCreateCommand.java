package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.hologram.HologramCreator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("hologramcreate|hc")
public class HologramCreateCommand extends BaseCommand {
    private CommandManager commandManager;

    public HologramCreateCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("<text>")
    public void hc(Player sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length < 1) {
            this.help(sender);
            return;
        }
        String text = args[0];
        for (int i = 1; i < args.length; i++) {
            text += " " + args[i];
        }
        text = ChatColor.translateAlternateColorCodes('&', text).replace("&&", "&").replace("&g", "\n");
        HologramCreator.createHologram(sender.getPlayer().getLocation(), text.split("\n"));
        sender.sendMessage(LanguageAPI.translate(sender, "command.hc.created"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.hc.help"));
    }
}
