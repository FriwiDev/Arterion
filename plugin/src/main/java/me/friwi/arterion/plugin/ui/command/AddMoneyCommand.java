package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("addmoney|addgold")
public class AddMoneyCommand extends BaseCommand {
    private CommandManager commandManager;

    public AddMoneyCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> <amount>")
    public void addMoney(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length != 2) {
            this.help(sender);
            return;
        }

        String player = args[0];
        int amount = 0;
        try {
            double i = Math.round(Double.parseDouble(args[1].replace(",", ".")) * 100);
            if (i > Integer.MAX_VALUE || i < Integer.MIN_VALUE) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.addmoney.nonumber"));
                return;
            }
            amount = (int) i; //Translate to cents
        } catch (NumberFormatException e) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.addmoney.nonumber"));
            return;
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            int finalAmount = amount;
            onlinePlayer.getBagMoneyBearer().addMoney(amount, success -> {
                if (success) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.addmoney.sent", onlinePlayer, finalAmount / 100f));
                } else {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.addmoney.error"));
                }
            });
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.addmoney.notonline"));
            return;
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.addmoney.help"));
    }
}
